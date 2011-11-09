package org.openxdata.oc.transport.impl

import groovy.util.logging.Log

import java.util.Collection

import org.openxdata.oc.Transform
import org.openxdata.oc.exception.UnAvailableException
import org.openxdata.oc.model.ConvertedOpenclinicaStudy
import org.openxdata.oc.transport.OpenClinicaSoapClient
import org.openxdata.oc.transport.factory.ConnectionFactory
import org.openxdata.oc.transport.proxy.ImportWebServiceProxy
import org.openxdata.oc.transport.proxy.ListAllByStudyWebServiceProxy
import org.openxdata.oc.transport.proxy.ListAllWebServiceProxy
import org.openxdata.oc.transport.proxy.StudyMetaDataWebServiceProxy


@Log
public class OpenClinicaSoapClientImpl implements OpenClinicaSoapClient {

	def username
	def password
	
	def header
	def dataPath = "/ws/data/v1"
	def studyPath = "/ws/study/v1"
	def subjectPath = "/ws/studySubject/v1"
	
	private def connectionFactory

	/**
	 * Constructs a OpenClinicaSoapClientImpl that connects to openclinica web services.
	 * 
	 * @param userName the user name
	 * @param password the users password
	 */
	OpenClinicaSoapClientImpl(def userName, def password){
		log.info("Initialized Openclinica Soap Client.")
		
		this.username = userName
		this.password = password
		buildHeader(userName, password)
	}

	/**
	 * Builds the header for openclinica web services.
	 * 
	 * @param user username for user allowed to access openclinica web services.
	 * @param password Password for user.
	 * 
	 * @return Valid SOAP header with authentication details.
	 */
	private def buildHeader(def userName, def password){
		header = """<soapenv:Header>
					  <wsse:Security soapenv:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
					        <wsse:UsernameToken wsu:Id="UsernameToken-27777511" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
					            <wsse:Username>""" + userName + """</wsse:Username>
					            <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">"""+password+"""</wsse:Password>
					        </wsse:UsernameToken>
					  </wsse:Security></soapenv:Header>"""
	}

	/**
	 * Builds a given an endpoint path and a body.
	 * 
	 * @param path endpoint to connect to.
	 * @param body Body of the envelope encapsulating the request to make.
	 * 
	 * @return Valid envelope that can initiate requests against an openlinica service.
	 */
	private String buildEnvelope(String path, String body) {
		def envelope = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://openclinica.org""" + path + """" xmlns:bean="http://openclinica.org/ws/beans">""" + header + body +
				"""</soapenv:Envelope>"""
		return envelope
	}

	/**
	 * Sends request to the openclinica web service.
	 * 
	 * @param envelope SOAP envelope to send.
	 * @param conn HTTPURLConnection to connect to.
	 * 
	 * @return Response from openclinica web service.
	 */
	private Node sendRequest(String envelope, HttpURLConnection conn) {
		log.info("Sending request to: " + conn)
		
		def outs = envelope.getBytes()

		conn.setRequestMethod("POST")
		conn.setDoOutput(true)

		conn.setRequestProperty("Content-Length", outs.length.toString())
		conn.setRequestProperty("Content-Type", "text/xml")

		def os
		def is
		
		try{
			os = conn.getOutputStream()
			os.write(outs)
			is = conn.getInputStream()
		}catch (Exception ex){
			log.info('Error Processing connection to:' + conn)
			throw new UnAvailableException('Connection Failed', ex)
		}
		
		def xml = buildResponse(is)
		return xml
	}

	/**
	 * Builds a response by reading from the HttpConnection input stream.
	 * 
	 * @param is Input stream to read from.
	 * @return Response in XML format.
	 */
	private buildResponse(InputStream is) {
		def builder = new StringBuilder()
		for (String s :is.readLines()) {
			builder.append(s)
		}
		def xml = parseXML(builder.toString())
		return xml
	}
	
	/**
	 * Parses an XML removing invalid characters that are occasionally appended to the responses from openclinica web services when getting study subjects.
	 * @param response Response to parse.
	 * @return A valid XML string.
	 */
	def parseXML(String response){
		
		log.info("Parsing returned XML to remove characters not allowed in prolong.")
		def validXML
		if(response.startsWith("--") && response.endsWith("--")){
			
			def beginIndex = response.indexOf("""<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">""")
			def endIndex = response.indexOf("</SOAP-ENV:Envelope>")
			validXML = response.substring(beginIndex, endIndex)
			
			// Add enclosing envelope tag
			def builder = new StringBuilder()
			builder.append(validXML)
			builder.append("</SOAP-ENV:Envelope>")
			
			validXML = builder.toString()
			
		}
		else{
			validXML = response
		}
		
		
		def xml = new XmlParser().parseText(validXML)
		
		return xml
	} 

	public List<ConvertedOpenclinicaStudy> listAll(){
		
		def listAllProxy = new ListAllWebServiceProxy(username:username, hashedPassword:password, connectionFactory:connectionFactory)
		return listAllProxy.listAll()
	}
	
	public String getMetadata(String identifier) {
		def getMetaDataProxy = new StudyMetaDataWebServiceProxy(username:username, hashedPassword:password, connectionFactory:connectionFactory)
		return getMetaDataProxy.getMetaData(identifier)
	}
	
	public Collection<String> getSubjectKeys(String studyIdentifier){
		def listAllByStudyProxy = new ListAllByStudyWebServiceProxy(username:username, hashedPassword:password, connectionFactory:connectionFactory)
		return listAllByStudyProxy.listAllByStudy(studyIdentifier)
	}
	
	public def getOpenxdataForm(String studyOID) {
		
		log.info("Fetching Form for Openclinica study with ID: " + studyOID)
		
		def odmMetaData = getMetadata(studyOID)
		def convertedStudy = transformMetaData(odmMetaData)
		
		log.info("<< ODM To OpenXData Transformation Complete. Returning... >>")
		
		return convertedStudy.convertedXformXml
	}

	private transformMetaData(String odmMetaData) {
		
		def convertedStudy = Transform.getTransformer().ConvertODMToXform(odmMetaData)

		convertedStudy.appendSubjectKeyNode([:])
		convertedStudy.parseMeasurementUnits()
		convertedStudy.serializeXformNode()
		
		return convertedStudy
	}
		
	public def importData(Collection<String> instanceData){
		def importProxy = new ImportWebServiceProxy(username:username, hashedPassword:password, connectionFactory:connectionFactory)
		return importProxy.importData(instanceData);
	}
	
	public void setConnectionFactory(ConnectionFactory factory){
		this.connectionFactory = factory
	}
}

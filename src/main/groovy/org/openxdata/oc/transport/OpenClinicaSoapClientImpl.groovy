package org.openxdata.oc.transport

import groovy.xml.Namespace

import java.util.Collection

import org.openxdata.oc.ODMBuilder
import org.openxdata.oc.Transform
import org.openxdata.oc.exception.ImportException
import org.openxdata.oc.model.OpenclinicaStudy
import org.openxdata.oc.transport.factory.ConnectionURLFactory


public class OpenClinicaSoapClientImpl implements OpenClinicaSoapClient {

	def header
	def dataPath = "/ws/data/v1"
	def studyPath = "/ws/study/v1"
	def subjectPath = "/ws/studySubject/v1"
	
	private def factory

	/**
	 * Constructs a OpenClinicaSoapClientImpl that connects to openclinica web services.
	 * 
	 * @param userName the user name
	 * @param password the users password
	 */
	OpenClinicaSoapClientImpl(def userName, def password){
		buildHeader(userName, password)
	}

	private def buildHeader(def user, def password){
		header = """<soapenv:Header>
					  <wsse:Security soapenv:mustUnderstand="1" xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd">
					        <wsse:UsernameToken wsu:Id="UsernameToken-27777511" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd">
					            <wsse:Username>""" + user + """</wsse:Username>
					            <wsse:Password Type="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordText">"""+password+"""</wsse:Password>
					        </wsse:UsernameToken>
					  </wsse:Security></soapenv:Header>"""
	}

	private String buildEnvelope(String path, String body) {
		def envelope = """<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:v1="http://openclinica.org""" + path + """" xmlns:bean="http://openclinica.org/ws/beans">""" + header + body +
				"""</soapenv:Envelope>"""
		return envelope
	}

	Node sendRequest(String envelope, HttpURLConnection conn) {
		def outs = envelope.getBytes()

		conn.setRequestMethod("POST")
		conn.setDoOutput(true)

		conn.setRequestProperty("Content-Length", outs.length.toString())
		conn.setRequestProperty("Content-Type", "text/xml")

		def os = conn.getOutputStream()
		os.write(outs)
		def is = conn.getInputStream()
		def builder = new StringBuilder()
		for (String s :is.readLines()) {
			builder.append(s)
		}
		def xml = parseXML(builder.toString())
		return xml
	}
	
	def parseXML(String response){
		
		def validXML
		if(response.startsWith("--") && response.endsWith("--")){
			
			def beginIndex = response.indexOf("""<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">""")
			def endIndex = response.indexOf("</SOAP-ENV:Envelope>")
			validXML = response.substring(beginIndex, endIndex)
			
			// Add enclosing envlope tag
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

	public String getMetadata(String studyOID) {
		def body = """<soapenv:Body>
					      <v1:getMetadataRequest>
					         <v1:studyMetadata>
					            <bean:studyRef>
					               <bean:identifier>"""+ studyOID +"""</bean:identifier>
					            </bean:studyRef>
					         </v1:studyMetadata>
					      </v1:getMetadataRequest>
					   </soapenv:Body>"""

		def envelope = buildEnvelope(studyPath, body)
		def xml = sendRequest(envelope, factory.getStudyConnection())
		return """<ODM xmlns="http://www.cdisc.org/ns/odm/v1.3">""" + xml.depthFirst().odm[0].children()[0] +"</ODM>"
	}

	public String getOpenxdataForm(String studyOID, Collection<String> subjectKeys) {
		def ODM = getMetadata(studyOID)
		def transformer = Transform.getTransformer()
		
		return transformer.transformODM(ODM, subjectKeys)
	}

	public List<OpenclinicaStudy> listAll(){
		def body = """<soapenv:Body><v1:listAllRequest>?</v1:listAllRequest></soapenv:Body>"""

		def envelope = buildEnvelope(studyPath, body)
		def response = sendRequest(envelope, factory.getStudyConnection())

		Collection<OpenclinicaStudy> studies = extractStudies(response)
		return studies;
	}

	private def extractStudies(def response){

		List<OpenclinicaStudy> studies  = new ArrayList<OpenclinicaStudy>()
		response.depthFirst().study.each {
			def study = new OpenclinicaStudy()
			study.OID = it.oid.text()
			study.name = it.name.text()
			study.identifier = it.identifier.text()
			studies.add(study)
		}

		return studies
	}

	public def importData(Collection<String> instanceData){

		def importODM = new ODMBuilder().buildODM(instanceData)
		def body = """<soapenv:Body>
					  	<v1:importRequest>""" + importODM + """</v1:importRequest>
					  </soapenv:Body>"""

		def envelope = buildEnvelope(dataPath, body)
		def reply = sendRequest(envelope, factory.getStudyConnection())

		def result = reply.depthFirst().result[0].text()
		if(result != "Success")
			throw new ImportException(reply.depthFirst().error[0].text())

		return result;
	}
	
	public Collection<String> getSubjectKeys(String studyOID){
		def body = """<soapenv:Body>
				      <v1:listAllByStudyRequest>
				         <bean:studyRef>
				            <bean:identifier>""" + studyOID + """</bean:identifier>
				         </bean:studyRef>
				      </v1:listAllByStudyRequest>
				   </soapenv:Body>"""
		
		def envelope = buildEnvelope(subjectPath, body)
		def response = sendRequest(envelope, factory.getStudySubjectConnectionURL())
		Collection<String> subjectKeys = extractSubjectKeys(response)

		return subjectKeys
	}
	
	def extractSubjectKeys(def response){
		
		List<String> subjectKeys = new ArrayList<String>()
		def ns = new Namespace("http://openclinica.org/ws/beans", "ns2")
		def subjects = response.depthFirst()[ns.subject].each {
			subjectKeys.add(it[ns.uniqueIdentifier].text())
		}
 
		return subjectKeys
	}
	
	public void setConnectionFactory(ConnectionURLFactory factory){
		this.factory = factory
	}
}

package org.openxdata.oc.transport.impl

import static org.hamcrest.Matchers.*

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.openxdata.oc.data.TestData
import org.openxdata.oc.exception.ImportException
import org.openxdata.oc.exception.TransformationException
import org.openxdata.oc.transport.factory.ConnectionFactory
import org.openxdata.oc.util.PropertiesUtil
import org.openxdata.server.admin.model.FormData


@WithGMock
class OpenClinicaSoapClientTest extends GroovyTestCase {
	
	def props
	def latestCRFVersions
	
	@Before public void setUp(){
		
		props = new PropertiesUtil().loadProperties('META-INF/openclinica.properties')
		latestCRFVersions = TestData.getCRFWebServiceResponse()
	}
	
	@Test void testGetOpenxdataFormReturnsValidXmlWithCorrectStudyName() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		
		play {
			
			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()
			
			assertEquals  "Default Study", convertedStudyXml.@name.text()
						
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithStudyRootElement() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)

			def convertedStudyXml = client.getOpenxdataForm("oid")

			assertEquals 'study', convertedStudyXml.name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithStudyStudyKey() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)

			def convertedStudyXml = client.getOpenxdataForm("oid")

			assertEquals 'S_DEFAULTS1', convertedStudyXml.@studyKey.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithCorrectNumberOfForms() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()

			assertEquals 21, forms.size()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithCorrectFormName() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()


			assertEquals  "CCA: Child Clinical Assessment - 2", forms[0].@name.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithVersionElement() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()[0]

			assertEquals  "version", version.name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithVersionName() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()[0]

			assertEquals  "CCA: Child Clinical Assessment - 2-v1", version.@name.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithVersionDescription() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()[0]

			assertEquals  "Converted from ODM using the oc-conversion-tools", version.@description.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsCorrectXformWithXformElement() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {


			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()

			assertEquals  "xform", forms.xform[0].name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsCorrectXformWithXformsElement() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def xforms = convertedStudyXml.form.version.xform.xforms[0]

			assertEquals  "xforms", xforms.name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsCorrectXformWithXformsIsSerialized() {
		def connectionFactory = setUpConnectionFactoryMock(latestCRFVersions)
		play {

			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def xformsContent = convertedStudyXml.form.version.xform[0].text()

			assertTrue xformsContent instanceof String
		}
	}
	
	@Test void testThatImportDataDoesNotReturnNull(){
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPSuccessResponse)
		play{
			
			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def reponse = client.importData(TestData.getInstanceData())
			
			assertNotNull reponse
		}
	}
	
	@Test void testThatSuccessfulImportReturnsCorrectMessage() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPSuccessResponse)
		play{
			
			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def reponse = client.importData(TestData.getInstanceData())
			
			assertEquals 'Success', reponse
		}
	}
	
	@Test void testThatImportDataReturnsErrorOnIncorrectODM(){
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPErrorResponse)
		play{

			shouldFail(ImportException){
				
				def client = new OpenClinicaSoapClientImpl(props)
				client.setConnectionFactory(connectionFactory)
				
				def reponse = client.importData([])
			}
		}
	}
	
	@Test void testThatInvalidXmlThrowsRaisesTransformationException(){
		def connectionFactory = setUpConnectionFactoryMock('''<////ODM>''')
		play{
			shouldFail(TransformationException){
				
				def client = new OpenClinicaSoapClientImpl(props)
				client.setConnectionFactory(connectionFactory)
				
				def xml = client.getOpenxdataForm("001")
			}
		}
	}
	
	@Test void testFindAllCRFSDoesNotReturnNull() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play{
			
			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def response = client.findAllCRFS("oid")
			
			assertNotNull response
		}
	}
	
	@Test void testFindEventsByStudyOIDDoesNotReturnNull() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.eventProxyResponse)
		play{
			
			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def studyEvents = client.findEventsByStudyOID("oid")
			
			assertNotNull "Should never return null", studyEvents
		}
	}
	
	@Test void testFindEventsByStudyOIDReturnsCorrectNumberOfEvents() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.eventProxyResponse)
		play{
			
			def client = new OpenClinicaSoapClientImpl(props)
			client.setConnectionFactory(connectionFactory)
			
			def studyEvents = client.findEventsByStudyOID("oid")
			
			assertEquals "The events should be 64", 64, studyEvents.children().size()
		}
	}
	
	private def setUpConnectionFactoryMock(returnXml) {
		
		def connection = mock(HttpURLConnection.class)
		connection.setRequestMethod("POST").atMostOnce()
		connection.setRequestProperty("Content-Type", "text/xml").atMostOnce()
		connection.setRequestProperty("Content-Length", is(instanceOf(String.class))).atMostOnce()
		connection.setDoOutput(true).atMostOnce()
		connection.getURL().returns("mock url").atMostOnce()

		def outputStream = new ByteArrayOutputStream()
		connection.getOutputStream().returns(outputStream).atMostOnce()
		connection.getInputStream().returns(new ByteArrayInputStream(returnXml.getBytes())).atMostOnce()
		
		def connectionFactory = mock(ConnectionFactory.class)
		
		connectionFactory.getCRFConnection().returns(connection).atMostOnce()
		connectionFactory.getStudyConnection().returns(connection).atMostOnce()
		connectionFactory.getEventConnection().returns(connection).atMostOnce()
		
		return connectionFactory
	}
}

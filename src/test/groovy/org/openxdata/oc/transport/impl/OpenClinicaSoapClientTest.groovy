package org.openxdata.oc.transport.impl

import static org.hamcrest.Matchers.*

import org.gmock.WithGMock
import org.junit.Before
import org.junit.Test
import org.openxdata.oc.data.TestData
import org.openxdata.oc.exception.ImportException
import org.openxdata.oc.transport.factory.ConnectionFactory
import org.openxdata.oc.util.PropertiesUtil
import org.openxdata.server.admin.model.User


@WithGMock
class OpenClinicaSoapClientTest extends GroovyTestCase {
	
	def client

	private def createUser() {

		def user = new User("foo")
		user.setSecretAnswer("openclinica-user-hash")

		return user
	}

	@Before public void setUp(){
		
		def props = new PropertiesUtil().loadProperties('META-INF/openclinica.properties')
		
		client = new OpenClinicaSoapClientImpl(props)
		
	}
	
	@Test void testGetOpenxdataFormReturnsValidXmlWithCorrectStudyName() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		
		play {
			
			client.setConnectionFactory(connectionFactory)
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()
			
			assertEquals  "Default Study", convertedStudyXml.@name.text()
						
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithStudyRootElement() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)

			def convertedStudyXml = client.getOpenxdataForm("oid")

			assertEquals 'study', convertedStudyXml.name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithStudyStudyKey() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)

			def convertedStudyXml = client.getOpenxdataForm("oid")

			assertEquals 'S_DEFAULTS1', convertedStudyXml.@studyKey.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithCorrectNumberOfForms() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()

			assertEquals 21, forms.size()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithCorrectFormName() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()


			assertEquals  "CCA: Child Clinical Assessment - 2", forms[0].@name.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithVersionElement() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()[0]

			assertEquals  "version", version.name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithVersionName() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()[0]

			assertEquals  "CCA: Child Clinical Assessment - 2-v1", version.@name.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsValidXformWithVersionDescription() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()[0]

			assertEquals  "Converted from ODM using the oc-conversion-tools", version.@description.text()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsCorrectXformWithXformElement() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {


			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def forms = convertedStudyXml.children()
			def version = forms.children()

			assertEquals  "xform", forms.xform[0].name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsCorrectXformWithXformsElement() {
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def xforms = convertedStudyXml.form.version.xform.xforms[0]

			assertEquals  "xforms", xforms.name()
		}
	}
	
	@Test void testGetOpenxdataFormReturnsCorrectXformWithXformsElementSerializedAsAString() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play {

			client.setConnectionFactory(connectionFactory)
			
			def convertedStudyXml = client.getOpenxdataForm("oid")

			def xformsContent = convertedStudyXml.form.version.xform[0].text()

			assertTrue xformsContent instanceof String
		}
	}
	
	@Test void testThatImportDataDoesNotReturnNull(){
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPSuccessResponse)
		play{
			
			client.setConnectionFactory(connectionFactory)
			
			def reponse = client.importData(createUser(), TestData.getInstanceData())
			
			assertNotNull reponse
		}
	}
	
	@Test void testThatSuccessfulImportReturnsCorrectMessageOnSuccessfulImport() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPSuccessResponse)
		play{
			
			client.setConnectionFactory(connectionFactory)
			
			def importMessages = client.importData(createUser(), TestData.getInstanceData())
			
			assertEquals 'Success', importMessages.get("F_MSA2_1")
		}
	}
	
	@Test void testThatSuccessfulImportReturnsCorrectMessageOnErraticImport() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPErrorResponse)
		play{
			
			client.setConnectionFactory(connectionFactory)
			
			def importMessages = client.importData(createUser(), TestData.getInstanceData())
			
			assertEquals 'Fail: Subject key not found', importMessages.get("F_MSA2_1")
		}
	}
	
	@Test void testThatImportDataReturnsErrorOnIncorrectODM(){
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPErrorResponse)

		shouldFail(ImportException){

			client.setConnectionFactory(connectionFactory)

			def reponse = client.importData(createUser(), [])
		}
	}

	@Test void testThatImportProceedsOnNullUser() {

		def connectionFactory = setUpConnectionFactoryMock(TestData.importSOAPSuccessResponse)
		play{
	
			client.setConnectionFactory(connectionFactory)

			def importMessages = client.importData(null, TestData.getInstanceData())

			assertEquals 'Success', importMessages.get("F_MSA2_1")
		}
	}

	@Test void testThatInvalidXmlThrowsRaisesTransformationException(){
		
		def connectionFactory = setUpConnectionFactoryMock('''<////ODM>''')
		
		shouldFail(ImportException){

			client.setConnectionFactory(connectionFactory)

			def xml = client.getOpenxdataForm("001")
		}
	}
	
	@Test void testFindAllCRFSDoesNotReturnNull() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.getCRFWebServiceResponse())
		play{
			
			client.setConnectionFactory(connectionFactory)
			
			def response = client.findAllCRFS("oid")
			
			assertNotNull response
		}
	}
	
	@Test void testFindStudySubjectEventsByStudyOIDRequestDoesNotReturnNull() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.getStudySubjectEventWebServiceResponse())
		play{

			client.setConnectionFactory(connectionFactory)

			def subjects = client.findStudySubjectEventsByStudyOID("oid")

			assertNotNull "findStudySubjectEventByStudyOID should never return null on valid studyOID", subjects
		}
	}
	
	@Test void testFindStudySubjectEventsByStudyOIDRequestReturnCorrectNumberOfSubjects() {

		def connectionFactory = setUpConnectionFactoryMock(TestData.getStudySubjectEventWebServiceResponse())
		play{

			client.setConnectionFactory(connectionFactory)

			def subjects = client.findStudySubjectEventsByStudyOID("oid")

			assertEquals 10, subjects.size()
		}
	}

	@Test void testFindStudySubjectEventsByStudyOIDRequestReturnsValidSubjectsWithEvents(){

		def connectionFactory = setUpConnectionFactoryMock(TestData.getStudySubjectEventWebServiceResponse())
		play{

			client.setConnectionFactory(connectionFactory)

			def subjects = client.findStudySubjectEventsByStudyOID("oid")

			subjects.each {
				assertTrue it.getEvents().size() > 0
			}
		}
	}
	
	@Test void testFindStudySubjectEventsByStudyOIDRequestReturnsValidSubjectsWithEventsHavingStartDate(){

		def connectionFactory = setUpConnectionFactoryMock(TestData.getStudySubjectEventWebServiceResponse())
		play{

			client.setConnectionFactory(connectionFactory)

			def subjects = client.findStudySubjectEventsByStudyOID("oid")

			subjects.each {
				
				it.getEvents().each { event ->
					assertNotNull "Event startDate should not be null", event.startDate
				}
			}
		}
	}
	
	@Test void testFindStudySubjectEventsByStudyOIDRequestReturnsValidSubjectsWithEventsHavingEndDate(){

		def connectionFactory = setUpConnectionFactoryMock(TestData.getStudySubjectEventWebServiceResponse())
		play{

			client.setConnectionFactory(connectionFactory)

			def subjects = client.findStudySubjectEventsByStudyOID("oid")

			subjects.each {

				it.getEvents().each { event ->
					assertNotNull "Event endDate should not be null", event.endDate
				}
			}
		}
	}
	
	@Test void testGetUserDetailsDoesNotReturnNullOnValidUsername() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.userDetailsResponse)
		play{

			client.setConnectionFactory(connectionFactory)
			
			def user = client.getUserDetails("username")
			
			assertNotNull "Should not return null user on valid username", user
			
		}
	}
	
	@Test void testGetUserDetailsReturnsUserWithCorrectUsername(){
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.userDetailsResponse)
		play{

			client.setConnectionFactory(connectionFactory)
			
			def user = client.getUserDetails("username")
			
			assertEquals "Username should be foo", "foo", user.username
		}
	}
	
	@Test void testGetUserDetailsReturnsUserWithCorrectHashedPassword() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.userDetailsResponse)
		play{

			client.setConnectionFactory(connectionFactory)
			
			def user = client.getUserDetails("username")
			
			assertEquals "Hashed Password should be hash LoL", "hash LoL", user.hashedPassword
		}
	}
	
	@Test void testGetUserDetailsReturnsUserWithCorrectWebServicePermissions() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.userDetailsResponse)
		play{

			client.setConnectionFactory(connectionFactory)
			
			def user = client.getUserDetails("username")
			
			assertFalse "User not authorized to use web services", user.canUseWebServices
		}
	}
	
	@Test void testGetUserDetailsReturnsUserAssignedToCorrectNumberOfStudies() {
		
		def connectionFactory = setUpConnectionFactoryMock(TestData.userDetailsResponse)
		play{

			client.setConnectionFactory(connectionFactory)
			
			def user = client.getUserDetails("username")
			
			assertEquals "User has access to only 2 studies", 2, user.getAllowedStudies().size()
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

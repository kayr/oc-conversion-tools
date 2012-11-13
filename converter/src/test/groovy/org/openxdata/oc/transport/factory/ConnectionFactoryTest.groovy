package org.openxdata.oc.transport.factory

import org.junit.Before
import org.junit.Test

class ConnectionFactoryTest extends GroovyTestCase {
	
	
	def factory
	
	@Before void setUp() {
		def host = "http://10.10.3.217:8080/OpenClinica-ws-SNAPSHOT"
		factory = new ConnectionFactory(host:host)
	}
	
	@Test void testHostNameEqualsPropertiesFileHostName() {
		
		def host = factory.getStudyConnection().getURL().getHost()
		assertEquals '10.10.3.217', host
	}
	
	@Test void testGetStudyConnectionShouldReturnCorrectURL(){
		
		def url = factory.getStudyConnection().getURL().toString()
		assertEquals 'http://10.10.3.217:8080/OpenClinica-ws-SNAPSHOT/ws/study/v1', url
	}
	
	@Test void testGetCRFConnectionURLDoesNotReturnNull() {
		def url = factory.getCRFConnection().getURL().toString()
		assertNotNull url
	}
	
	@Test void testGetCRFConnectionURLReturnsCorrectURL() {
		def url = factory.getCRFConnection().getURL().toString()
		assertEquals 'http://10.10.3.217:8080/OpenClinica-ws-SNAPSHOT/ws/crf/v1', url
	}
	
	@Test void testGetEventConnectionURLReturnsCorrectURL() {
		def url = factory.getEventConnection().getURL().toString()
		assertEquals "The events service should equal the actual one.", 'http://10.10.3.217:8080/OpenClinica-ws-SNAPSHOT/ws/event/v1', url
	}
}

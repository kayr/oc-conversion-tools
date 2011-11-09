package org.openxdata.oc.transport.soap

import org.junit.Before
import org.junit.Test
import org.openxdata.oc.transport.proxy.ListAllWebServiceProxy
import org.openxdata.oc.transport.proxy.StudyMetaDataWebServiceProxy


class SoapRequestPropertiesBuilderTest extends GroovyTestCase {
	
	def listAllProxy 
	def getMetaDataProxy
	def parser = new XmlSlurper() 
	
	@Before public void setUp(){
		listAllProxy = new ListAllWebServiceProxy(username:"uname", hashedPassword:"pass", connectionFactory:null)
		getMetaDataProxy = new StudyMetaDataWebServiceProxy(identifier:'OID', username:"uname", hashedPassword:"pass", connectionFactory:null)
	}
	
	@Test void testGetHeaderShouldReturnValidHeader(){
		
		def header = listAllProxy.getHeader()
		def headerXml = parser.parseText(header)
		
		assertEquals 'Header', headerXml.name()
		assertEquals 'Security', headerXml.children()[0].name()
		assertEquals 'uname', headerXml.depthFirst().find{it.name().equals('Username')}.text()
		assertEquals 'pass', headerXml.depthFirst().find{it.name().equals('Password')}.text()
	}
	
	@Test void testGetListAllSoapRequestEnvelopeShouldReturnValidEnvelope(){
		
		def envelope = listAllProxy.getSoapEnvelope()
		def envelopeXML = parser.parseText(envelope)
		
		assertEquals 'Envelope', envelopeXML.name()
		assertEquals 'Header', envelopeXML.children()[0].name()
	}
	
	@Test void testGetMetaDataSoapRequestShouldReturnValidEnvelope(){
		
		def envelope = getMetaDataProxy.getSoapEnvelope()
		def envelopeXML = parser.parseText(envelope)
		
		assertEquals 'Envelope', envelopeXML.name()
	}
	
	@Test void testGetMetaDataSoapRequestEnvelopeShouldReturnValidEnvelopeWithHeaderElement(){
		
		def envelope = getMetaDataProxy.getSoapEnvelope()
		def envelopeXML = parser.parseText(envelope)
		
		assertEquals 'Envelope', envelopeXML.name()
	}
}

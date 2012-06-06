package org.openxdata.oc

import org.junit.Before
import org.junit.Test
import org.openxdata.oc.InstanceDataHandler;
import org.openxdata.oc.data.TestData
import org.openxdata.oc.exception.ImportException


class InstanceDataHandlerTest extends GroovyTestCase {

	List<String> exportedInstanceData
	
	@Before public void setUp(){
		
		def odmDef = new InstanceDataHandler()
		
		exportedInstanceData = odmDef.processInstanceData(TestData.getInstanceData())
	}
	
	@Test void testAppendInstanceDataShouldConvertedOpenXDataInstanceDataUsingCorrectProtocol(){
		
		def xml = new XmlParser().parseText(exportedInstanceData[0])
		
		assertNotNull xml
	}
	
	@Test void testAppendInstacenDataReturnsXmlWithODMAsRootElement() {
		
		def xml = new XmlParser().parseText(exportedInstanceData[0])
		
		assertEquals "Root should be ODM", "ODM", xml.name()
	}
	
	@Test void testAppendInstacenDataReturnsXmlWithClinicalDataElement() {
		
		def xml = new XmlParser().parseText(exportedInstanceData[0])
		
		assertEquals "Second Node should be ClinicalData", "ClinicalData", xml.children()[0].name()
	}
	
	@Test void testInstanceDataHasMetaDataVersionOID() {
		
		def xml = new XmlParser().parseText(exportedInstanceData[0])
		assertEquals "v1.0.0", xml.ClinicalData.@MetaDataVersion[0]
	}
	
	@Test void testAppendInstanceReturnsCorrectNumberOfItemDatas(){
		
		
		def xml = new XmlParser().parseText(exportedInstanceData[0])
		
		assertEquals "ItemData Nodes should equal number of child elements in the oxd instance data xml (including child elements of repeats)", 30, xml.depthFirst().ItemData.size()
	}
	
	@Test void testAppendInstanceDataShouldThrowExceptionOnNullInstanceData(){
		
		def emptyInstanceData = new ArrayList<String>()
		shouldFail(ImportException.class){
			new InstanceDataHandler().processInstanceData(emptyInstanceData)
		}
	}
	
	@Test void testThatInstanceXmlWithHeaderQuestionsIsCleaned() {
		
		def xmlWithHeaders = ''''''
		
		def cleanXml = protocol.cleanXml(xmlWithHeaders)
		
		assertFalse "Xml Has no Headers", hasHeaders(cleanXml)
	}
}

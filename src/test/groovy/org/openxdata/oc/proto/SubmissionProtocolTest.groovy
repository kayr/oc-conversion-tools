package org.openxdata.oc.proto

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.openxdata.oc.data.TestData

class SubmissionProtocolTest {

	def xml
	def instanceData

	@Before void setUp() {

		def proto = new SubmissionProtocol()
		instanceData = proto.createOpenClinicaInstanceData(TestData.getOpenXdataInstanceData())

		xml = new XmlParser().parseText(instanceData)
	}

	@Test void testCreateInstanceDataDoesNotReturnNullOnValidOXDInstanceData() {

		assertNotNull 'Should not EVER return null', instanceData
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithODMAsRoot() {

		def name = xml.name()
		assertEquals 'ODM', name
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithODMAsRootWithFormKeyAttribute() {

		def formKey = xml.@formKey
		assertEquals 'SE_SC2', formKey
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithODMAsRootWithNameAttribute() {

		def name = xml.@name
		assertEquals 'SC2', name
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithODMAsRootWithDescriptionAttribute() {

		def description = xml.@Description
		assertEquals 'This Xform was converted from an ODM file using the oc-conversion-tools', description
	}

	@Test void testCreateInstanceDataReturnXmlWithClinicalDataNode() {

		def clinicalDataNode = xml.ClinicalData
		assertEquals 'ClinicalData', clinicalDataNode[0].name()
	}

	@Test void testCreateInstanceDataReturnXmlWithClinicalDataNodeWithStudyOIDAttribute() {

		def clinicalDataNode = xml.ClinicalData
		assertEquals 'S_12175', clinicalDataNode[0].@StudyOID
	}

	@Test void testCreateInstanceDataReturnXmlWithClinicalDataNodeHavingMetaVersionOIDAttribute() {

		def metaVersionOIDAttribute = xml.ClinicalData
		assertEquals 'v1.0.0', metaVersionOIDAttribute[0].@MetaDataVersionOID
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithSubjectDataNode() {

		def subjectDataNode = xml.ClinicalData.SubjectData
		assertEquals 'SubjectData', subjectDataNode[0].name()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithSubjectDataNodeHavingSubjectKeyAttribute() {
		def subjectDataNode = xml.ClinicalData.SubjectData
		def subjectKeyAttribute = subjectDataNode[0].attributes().get('SubjectKey')

		assertNotNull subjectKeyAttribute 
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithSubjectDataNodeHavingSubjectKeyAttributeWithCorrectValue() {
		def subjectDataNode = xml.ClinicalData.SubjectData

		assertEquals 'Foo_Key', subjectDataNode.'@SubjectKey'.text()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithStudyEventDataNode() {
		def studyEventDataNode = xml.ClinicalData.SubjectData.StudyEventData
		assertEquals 'StudyEventData', studyEventDataNode[0].name()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithStudyEventDataNodeHavingStudyEventOIDAttribute() {
		def studyEventDataNode = xml.ClinicalData.SubjectData.StudyEventData
		def studyEventOIDAttribute = studyEventDataNode[0].attributes().get('StudyEventOID')

		assertNotNull studyEventOIDAttribute
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithStudyEventDataNodeHavingStudyEventOIDAttributeWithCorrectValue() {
		def studyEventDataNode = xml.ClinicalData.SubjectData.StudyEventData

		assertEquals "SE_SC2", studyEventDataNode[0].@StudyEventOID
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWith2FormDataNodes() {
		def formDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData
		
		assertEquals 3, formDataNodes.size()
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithFormDataElementHavingFormOIDAttribute() {
		def formDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData
		
		assertEquals "F_MSA2_1_2", formDataNodes[0].@FormOID
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithFormDataElementHavingFormOIDAttribute1() {
		def formDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData
		
		assertEquals "F_MSA2_1", formDataNodes[1].@FormOID
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithFormDataElementHavingFormOIDAttribute2() {
		def formDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData
		
		assertEquals "F_MSA2_2", formDataNodes[2].@FormOID
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWith3ItemGroupDataNodes() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 3, itemGroupDataNodes.size()
	}
	
	@Test void testCreateInsanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemGroupOIDAttribute() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 'IG_MSA2_UNGROUPED_2', itemGroupDataNodes[0].@ItemGroupOID.toString()
	}
	
	@Test void testCreateInsanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemGroupOIDAttribute1() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 'IG_MSA2_UNGROUPED', itemGroupDataNodes[1].@ItemGroupOID.toString()
	}
	
	@Test void testCreateInsanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemGroupOIDAttribute2() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 'IG_MSA2_MSA2_POARTPRECG', itemGroupDataNodes[2].@ItemGroupOID.toString()
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemDataNodes() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		itemGroupDataNodes.each {
			assertTrue "Must have ItemData Nodes", it.children().size() > 0
		}
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithItemDataNodesHavingItemOIDAttribute() {
		def itemDataNodes = getItemDataNodes()
		itemDataNodes.each {
			
			def itemOID = it.@ItemOID
			
			assertNotNull "Should have ItemOID Attribute", itemOID
			
		}
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithFirstItemGroupDataHavingOneItemDataNode() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 1, itemGroupDataNodes[0].children().size()
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithSecondItemGroupDataHavingTwentyTwoItemDataNodes() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 22, itemGroupDataNodes[1].children().size()
	}
	
	@Test void testCreateInstanceDataReturnsValidXmlWithThirdItemGroupDataHavingThreeItemDataNodes() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		
		assertEquals 3, itemGroupDataNodes[2].children().size()
	}
	
	def getItemDataNodes() {
		def itemDataNodes = []
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData
		itemGroupDataNodes.each {
			itemDataNodes.add(it.children())
		}
		
		return itemDataNodes
	}
}

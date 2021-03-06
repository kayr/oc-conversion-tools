package org.openxdata.oc.proto

import static org.junit.Assert.*
import groovy.xml.XmlUtil

import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.openxdata.oc.data.TestData
import org.openxdata.oc.util.TransformUtil
import org.springframework.util.StringUtils

class DefaultSubmissionProtocolTest {

	def xml
	def protocol
	def instanceData
	def transformUtil

	@Before void setUp() {

		transformUtil = new TransformUtil()
		protocol = new DefaultSubmissionProtocol()

		// Because we bypass the instance handler, we have to clean the xml manually
		def cleanXml = transformUtil.isolateRepeats(TestData.getOpenXdataInstanceData())

		instanceData = protocol.createODMInstanceData(cleanXml)

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
		assertEquals 'F_MSA2_1', formKey
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

		def clinicalDataNode = xml.ClinicalData
		assertEquals 'v1.0.0', clinicalDataNode[0].@MetaDataVersion
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

		assertEquals 1, formDataNodes.size()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithFormDataElementHavingFormOIDAttribute() {

		def formDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData

		assertEquals "F_MSA2_1", formDataNodes[0].@FormOID
	}

	@Test void testCreateInstanceDataReturnsValidXmlWith3ItemGroupDataNodes() {

		def itemGroupDataNodes = xml.depthFirst().findAll {
			it.name().equals("ItemGroupData")
		}

		assertEquals 6, itemGroupDataNodes.size()
	}

	@Test void testCreateInsanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemGroupOIDAttribute() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		assertEquals 'IG_MSA2_UNGROUPED', itemGroupDataNodes[0].@ItemGroupOID.toString()
	}

	@Test void testCreateInsanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemGroupOIDAttribute1() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		assertEquals 'IG_MSA2_UNGROUPED_2', itemGroupDataNodes[1].@ItemGroupOID.toString()
	}

	@Test void testCreateInsanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemGroupOIDAttribute2() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		assertEquals 'IG_MSA2_MSA2_POARTPRECG2', itemGroupDataNodes[2].@ItemGroupOID.toString()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithItemGroupDataNodesHavingItemDataNodes() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		itemGroupDataNodes.each {
			assertTrue "Must have ItemData Nodes", it.children().size() > 0
		}
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithItemDataNodesHavingItemOIDAttribute() {
		def itemDataNodes = getItemDataNodes(xml)
		itemDataNodes.each {

			def itemOID = it.@ItemOID

			assertNotNull "Should have ItemOID Attribute", itemOID
		}
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithFirstItemGroupDataHaving7ItemDataNodes() {

		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		assertEquals 22, itemGroupDataNodes[0].children().size()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithSecondItemGroupDataHavingTwentyTwoItemDataNodes() {
		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		assertEquals 1, itemGroupDataNodes[1].children().size()
	}

	@Test void testCreateInstanceDataReturnsValidXmlWithThirdItemGroupDataHaving1ItemDataNodes() {

		def itemGroupDataNodes = xml.ClinicalData.SubjectData.StudyEventData.FormData.ItemGroupData

		assertEquals 3, itemGroupDataNodes[3].children().size()
	}

	@Test void testCreateInstanceDataReturnsXmlWithFormDataOIDEqualingFormKey() {

		def formOID = xml.depthFirst().FormData[0]

		assertEquals formOID.@FormOID, xml.@formKey
	}

	@Test void testThatCreateInstanceDataReturnsValidXmlWithCorrectNumberOfItemGroupDataNodes() {

		def itemGroupDatas = xml.depthFirst().FormData.ItemGroupData

		assertEquals 6, itemGroupDatas.size()
	}

	@Test void testCreateInstanceDataReturnsXmlWithItemGroupDataElementsHavingTransactionTyeAttribute() {

		def itemGroupDatas = xml.depthFirst().FormData.ItemGroupData
		itemGroupDatas.each {
			assertNotNull "TransactionType Attribute should not be null", it.@TransactionType
		}
	}

	@Test void testCreateInstanceReturnsXmlWithItemGroupDataElementsHavingTransactionTypeAttributeEqualsToInsert() {

		def itemGroupDatas = xml.depthFirst().FormData.ItemGroupData
		itemGroupDatas.each {
			assertEquals "TransactionType Attribute should be Insert", "Insert", it.@TransactionType
		}
	}

	@Test void testCreateInstanceDataReturnsXmlWithItemGroupDataElementsHavingItemGroupRepeatKeyAttribute() {

		def itemGroupDatas = xml.depthFirst().FormData.ItemGroupData
		itemGroupDatas.each {

			if(transformUtil.isRepeat(xml, it.@ItemGroupOID))
				assertNotNull "ItemGroupRepeatKey Attribute should not be null", it.@ItemGroupRepeatKey
		}
	}

	@Test void testCreateInstanceDataReturnsXmlWithRepeatItemGroupDataHavingCorrectItemGroupRepeatKey() {

		def itemGroupDatas = xml.depthFirst().FormData.ItemGroupData
		itemGroupDatas.each {

			if(transformUtil.isRepeat(xml, it.@ItemGroupOID) == true)
				assertEquals "ItemGroupRepeatKey Attribute should be 2", 2, Integer.valueOf(it.@ItemGroupRepeatKey)
		}
	}

	@Test void testIsRepeatReturnsTrueWhenNodeIsRepeat() {

		def xml = """<test><repeat></repeat></test>"""
		assertTrue "Node is Repeat", transformUtil.isRepeat("", new XmlSlurper().parseText(xml))
	}

	@Test void testIsRepeatReturnsTrueWhenNodeWithTwoChildren() {

		def xml = """<test><repeat><child></child></repeat></test>"""
		assertTrue "Node is Repeat", transformUtil.isRepeat("", new XmlSlurper().parseText(xml))
	}

	@Test void testIsRepeatReturnsTrueWhenNodeHasTwoChildren() {

		def xml = """<test><repeat><child></child></repeat></test>"""
		def node = new XmlSlurper().parseText(xml)

		assertTrue "Node is Repeat", transformUtil.isRepeat("", node.repeat)
	}

	@Test void testIsRepeatReturnsFalseWhenNodeIsNotRepeat() {

		def xml = """<test></test>"""
		assertFalse "Node is not Repeat", transformUtil.isRepeat("", new XmlSlurper().parseText(xml))
	}

	@Test void testIsRepeatReturnsFalseWhenNodeHasTwoChildren() {

		def xml = """<test><repeat><child></child></repeat></test>"""
		def node = new XmlSlurper().parseText(xml)

		assertFalse "Node is not Repeat", transformUtil.isRepeat("", node.repeat.child)
	}

	@Test void testProcessDataAddsCommasToMultipleSelectAnswers() {

		def xml = protocol.processMultipleSelectValues("1 2 3 4")

		assertThat xml, Matchers.containsString(',')
	}

	@Test void testProcessDataAddsCorrectNumberCommasToMultipleSelectAnswers() {

		def xml = protocol.processMultipleSelectValues("1 2 3 4")

		assertEquals 3, StringUtils.countOccurrencesOf(xml, ",")
	}

	@Test void testProcessDataAddsCommasToMultipleSelectAnswersWithMultipleDigits() {

		def xml = protocol.processMultipleSelectValues("11 22 33 44")

		assertThat xml, Matchers.containsString(',')
	}

	@Test void testProcessDataAddsCorrectNumberCommasToMultipleSelectAnswers2() {

		def xml = protocol.processMultipleSelectValues("11 22 33 44 55")

		assertEquals 4, StringUtils.countOccurrencesOf(xml, ",")
	}

	@Test void testProcessDataAdds3CommasWhenTheValuesAre4() {

		def xml = protocol.processMultipleSelectValues("1 2 3 4")
		int commaCount = xml.replaceAll("[^,]", "").length()

		assertEquals "The commas should be equal to number of values - 1", 3, commaCount
	}

	@Test void testThatProcessDoesNotAlterMultipleAnswersWhichAreNotNumbers() {

		def xml = protocol.processMultipleSelectValues("we all know remi is a kool dumb ass")

		assertFalse "Should not alter non-multiple select questions", xml.contains(",")
	}

	def getItemDataNodes(processedData) {

		return processedData.depthFirst().findAll {
			it.name().equals('ItemData')
		}
	}
}

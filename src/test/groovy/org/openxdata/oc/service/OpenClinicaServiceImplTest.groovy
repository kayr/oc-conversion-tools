package org.openxdata.oc.service

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import java.util.List

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.openxdata.oc.data.TestData
import org.openxdata.oc.service.impl.OpenclinicaServiceImpl
import org.openxdata.oc.transport.OpenClinicaSoapClient
import org.openxdata.server.admin.model.Editable
import org.openxdata.server.admin.model.FormData
import org.openxdata.server.admin.model.FormDef
import org.openxdata.server.admin.model.StudyDef
import org.openxdata.server.export.ExportConstants
import org.openxdata.server.service.DataExportService
import org.openxdata.server.service.FormService
import org.openxdata.server.service.StudyManagerService


@RunWith(MockitoJUnitRunner.class)
public class OpenClinicaServiceImplTest extends GroovyTestCase {

	@Mock private FormService formService
	@Mock private OpenClinicaSoapClient client
	@Mock private StudyManagerService studyService
	@Mock private DataExportService dataExportService
	
	@InjectMocks private def openClinicaService = new OpenclinicaServiceImpl()

	def studies = []
	def subjects = []
	def formDataList = []
	def openClinicaConvertedStudies = []

	@Before public void setUp() throws Exception {

		initSubjects()
		initFormDataList()
		initStudyDefinitions()

		StudyDef study = createStudy()

		Mockito.when(studyService.getStudies()).thenReturn(studies)
		Mockito.when(studyService.getStudyKey(Mockito.anyInt())).thenReturn("key")
		Mockito.when(studyService.getStudyByKey(Mockito.anyString())).thenReturn(study)
		Mockito.when(studyService.hasEditableData(Mockito.any(Editable.class))).thenReturn(Boolean.TRUE)

		Mockito.when(client.getSubjectKeys(Mockito.anyString())).thenReturn(subjects)

		Mockito.when(client.importData(Mockito.anyCollection())).thenReturn("Success")
		
		Mockito.when(client.findEventsByStudyOID(Mockito.anyString())).thenReturn(getEventNode())
		
		Mockito.when(dataExportService.getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)).thenReturn(formDataList)

	}
	
	private void initFormDataList() {

		FormData formData = new FormData()
		formData.setId(1)
		FormData formData2 = new FormData()
		formData2.setId(2)

		formDataList.add(formData)
		formDataList.add(formData2)
	}

	private StudyDef createStudy() {

		StudyDef study = new StudyDef()

		FormDef form = new FormDef()

		study.addForm(form)

		return study
	}

	private void initSubjects() {
		subjects.add("Jorn")
		subjects.add("Janne")
		subjects.add("Morten")
		subjects.add("Jonny")
	}

	private void initStudyDefinitions() {
		// Study definitions
		StudyDef study = new StudyDef()
		study.setName("study")
		study.setStudyKey("oid")

		studies.add(study)

	}
	
	private def getEventNode() {
		def eventNode = new XmlSlurper().parseText(TestData.eventNode)
	}

	@Test public void testHasStudyData(){

		String studyKey = studyService.getStudyKey(1)
		assertTrue(openClinicaService.hasStudyData(studyKey))

		Mockito.when(studyService.hasEditableData(Mockito.any(Editable.class))).thenReturn(Boolean.FALSE)
		String studyKey2 = studyService.getStudyKey(2)
		assertFalse(openClinicaService.hasStudyData(studyKey2))
	}

	@Test public void testGetSubjectsShouldReturnCorrectNumberOfSubjects(){

		List<String> studySubjects = openClinicaService.getStudySubjects("studyOID")

		assertEquals(4, studySubjects.size())
	}

	@Test public void testGetSubjectsShouldRetursValidSubjectKeys(){

		List<String> studySubjects = openClinicaService.getStudySubjects("studyOID")

		assertEquals("Jorn", studySubjects.get(0))
		assertEquals("Janne", studySubjects.get(1))
		assertEquals("Morten", studySubjects.get(2))
		assertEquals("Jonny", studySubjects.get(3))
	}

	@Test public void testExportDataShouldReturnSuccessMessage() {

		String message = openClinicaService.exportOpenClinicaStudyData()
		assertEquals("Success", message)
	}

	@Test public void testExportDataShouldShouldFailOnEmptyInstanceDataWithMessage() {

		Mockito.when(client.importData(Mockito.anyCollection())).thenReturn("Fail")
		String message = openClinicaService.exportOpenClinicaStudyData()
		assertEquals("Fail", message)
	}
	
	@Test public void testExportDataShouldSetFormDataWithOpenclinicaExportBitFlag() {
		
		openClinicaService.exportOpenClinicaStudyData()
		
		formDataList.each {
			
			assertTrue "Should be marked as Exported with openclinica export bit flag", it.isExported(ExportConstants.EXPORT_BIT_OPENCLINICA)
		}
	}
	
	@Test public void testGetEventsDoesNotReturnNull() {
		def returnedEvents = openClinicaService.getEvents("OID")
		assertNotNull returnedEvents
	}
	
	@Test public void testGetEventsReturnsCorrectNumberOfEvents() {
		def returnedEvents = openClinicaService.getEvents("OID")
		assertEquals 71, returnedEvents.size()
	}
	
	@Test void testGetEventsReturnsEventsWithSubjectKeys() {
		def returnedEvents = openClinicaService.getEvents("OID")
		returnedEvents.each {
			assertTrue "Each event should have at least one subject attached", it.getSubjectKeys().size() >= 1
		}
	}
}

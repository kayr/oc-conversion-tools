package org.openxdata.oc.service

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.openxdata.oc.data.TestData
import org.openxdata.oc.model.OpenClinicaUser
import org.openxdata.oc.model.StudySubject
import org.openxdata.oc.service.impl.OpenClinicaServiceImpl
import org.openxdata.oc.transport.OpenClinicaSoapClient
import org.openxdata.server.admin.model.Editable
import org.openxdata.server.admin.model.FormData
import org.openxdata.server.admin.model.FormDef
import org.openxdata.server.admin.model.StudyDef
import org.openxdata.server.admin.model.User;
import org.openxdata.server.export.ExportConstants
import org.openxdata.server.service.DataExportService
import org.openxdata.server.service.FormService
import org.openxdata.server.service.StudyManagerService


@RunWith(MockitoJUnitRunner.class)
public class OpenClinicaServiceTest extends GroovyTestCase {

	def formDataList = []

	@Mock private FormService formService
	@Mock private OpenClinicaSoapClient client
	@Mock private StudyManagerService studyService
	@Mock private DataExportService dataExportService

	@InjectMocks private def openClinicaService = new OpenClinicaServiceImpl()

	@Before public void setUp() throws Exception {

		createFormDataList()

		Mockito.when(client.getOpenxdataForm(Mockito.anyString())).thenReturn(TestData.getConvertedXform())
		Mockito.when(client.importData(Mockito.any(User.class), Mockito.anyCollection())).thenReturn(TestData.createImportMessages())
		Mockito.when(client.findStudySubjectEventsByStudyOID(Mockito.anyString())).thenReturn(createStudySubjectEvents())
		Mockito.when(client.getUserDetails(Mockito.anyString())).thenReturn(new OpenClinicaUser(TestData.findUserResponse))

		Mockito.when(studyService.getStudies()).thenReturn(createStudyList())
		Mockito.when(studyService.getStudyKey(Mockito.anyInt())).thenReturn("key")
		Mockito.when(studyService.getStudyByKey(Mockito.anyString())).thenReturn(createStudy())
		Mockito.when(studyService.hasEditableData(Mockito.any(Editable.class))).thenReturn(Boolean.TRUE)

		Mockito.when(dataExportService.getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)).thenReturn(formDataList)
	}

	private User createUser() {

		User user = new User("foo");
		user.setPassword("password");
		return user;
	}

	private StudyDef createStudy() {

		StudyDef study = new StudyDef()
		FormDef form = new FormDef()
		study.addForm(form)

		return study
	}

	private void createFormDataList() {

		FormData formData = new FormData()
		formData.setId(1)
		formData.setData("""<ODM formKey="F_MSA2_1"/>""")

		FormData formData2 = new FormData()
		formData2.setId(2)
		formData2.setData("""<ODM formKey="F_MSA2_2"/>""")

		formDataList.add(formData)
		formDataList.add(formData2)
	}

	private List<StudyDef> createStudyList() {

		def studies = []

		StudyDef study = new StudyDef()
		study.setName("study")
		study.setStudyKey("oid")

		studies.add(study)

		return studies
	}

	private List<StudySubject> createStudySubjectEvents(){

		def subjects = []

		def studySubjectEventNode = TestData.getStudySubjects()

		studySubjectEventNode.studySubject.each {
			def subject = new StudySubject(it)
			subjects.add(subject)
		}

		return subjects
	}

	@Test public void testHasStudyData(){

		String studyKey = studyService.getStudyKey(1)
		assertTrue(openClinicaService.hasStudyData(studyKey))

		Mockito.when(studyService.hasEditableData(Mockito.any(Editable.class))).thenReturn(Boolean.FALSE)

		String studyKey2 = studyService.getStudyKey(2)

		assertFalse(openClinicaService.hasStudyData(studyKey2))

		Mockito.verify(studyService, Mockito.atLeastOnce()).hasEditableData(Mockito.any(Editable.class))
	}

	@Test public void testExportDataShouldReturnsCorrectNumberOfMessages() {

		def messages = openClinicaService.exportOpenClinicaStudyData()

		assertEquals 4, messages.size()

		Mockito.verify(client, Mockito.atLeast(0)).importData(Mockito.any(User.class), Mockito.anyList())
		Mockito.verify(dataExportService, Mockito.atLeastOnce()).getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)
		Mockito.verify(dataExportService, Mockito.atLeast(2)).setFormDataExported(Mockito.any(FormData.class), Mockito.anyInt())
	}

	@Test public void testExportDataShouldReturnMessageOnEmptyInstanceData() {

		Mockito.when(dataExportService.getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)).thenReturn([])

		def messages = openClinicaService.exportOpenClinicaStudyData()

		assertEquals("No data items found to export.", messages.get(""))

		Mockito.verify(client, Mockito.atLeast(0)).importData(Mockito.any(User.class), Mockito.anyList())
		Mockito.verify(dataExportService, Mockito.atLeastOnce()).getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)
		Mockito.verify(dataExportService, Mockito.atLeast(0)).setFormDataExported(Mockito.any(FormData.class), Mockito.anyInt())
	}

	@Test public void testExportDataShouldReturnFailureMessageOnErraticExport() {

		def messages = openClinicaService.exportOpenClinicaStudyData()
		assertEquals("Fail: Incorrect FormData OID", messages.get("key1"))

		Mockito.verify(client).importData(Mockito.any(User.class), Mockito.anyList())
		Mockito.verify(dataExportService, Mockito.atLeastOnce()).getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)
		Mockito.verify(dataExportService, Mockito.atLeast(0)).setFormDataExported(Mockito.any(FormData.class), Mockito.anyInt())
	}

	@Test public void testExportDataShouldSetFormDataWithOpenclinicaExportBitFlag() {

		openClinicaService.exportOpenClinicaStudyData()

		formDataList.each {

			assertTrue "Should be marked as Exported with openclinica export bit flag", it.isExported(ExportConstants.EXPORT_BIT_OPENCLINICA)
		}

		Mockito.verify(client).importData(Mockito.any(User.class), Mockito.anyList())
		Mockito.verify(dataExportService, Mockito.atLeastOnce()).getFormDataToExport(ExportConstants.EXPORT_BIT_OPENCLINICA)
		Mockito.verify(dataExportService, Mockito.atMost(2)).setFormDataExported(Mockito.any(FormData.class), Mockito.anyInt())
	}

	@Test public void testExportFormDataReturnsSuccessOnSuccessfulExport() {

		createResponse("Success")

		def message = openClinicaService.exportFormData(createUser(), createFormData())

		assertEquals "Export should succeed", "Success", message

		Mockito.verify(client, Mockito.atLeastOnce()).importData(Mockito.any(User.class), Mockito.anyList())
	}

	@Test public void testGetStudySubjectEventsDoesNotReturnNull() {

		def studySubjectEvents = openClinicaService.getStudySubjectEvents()

		assertNotNull "Should never return null on valid studyOID", studySubjectEvents
	}

	@Test public void testGetStudysubjectEventReturnsCorrectNumberOfStudySubjectEvents() {

		def studySubjectEvents = openClinicaService.getStudySubjectEvents()

		assertEquals 10, studySubjectEvents.size()
	}

	@Test public void testGetStudysubjectEventReturnsStudySubjectEventsWithEvents() {

		def studySubjectEvents = openClinicaService.getStudySubjectEvents()

		studySubjectEvents.each {

			assertTrue "StudySubjectEvent should have at least one event definition", it.getEvents().size() > 0
		}
	}

	@Test public void testGetStudysubjectEventReturnsStudySubjectEventsWithEventsHavingFormOIDs() {

		def studySubjectEvents = openClinicaService.getStudySubjectEvents()

		studySubjectEvents.each {

			it.getEvents().each { event ->

				assertTrue "StudySubject event definition should have at least one formOID", event.getFormOIDs().size() > 0
			}
		}
	}

	@Test public void testGetStudysubjectEventReturnsStudySubjectEventsWithEventsHavingStartDate() {

		def studySubjectEvents = openClinicaService.getStudySubjectEvents()

		studySubjectEvents.each {

			it.getEvents().each { event ->

				assertNotNull "StudySubject event definition should have at least one formOID", event.startDate
			}
		}
	}

	@Test public void testGetStudysubjectEventReturnsStudySubjectEventsWithEventsHavingEndDate() {

		def studySubjectEvents = openClinicaService.getStudySubjectEvents()

		studySubjectEvents.each {

			it.getEvents().each { event ->

				assertNotNull "StudySubject event definition should have at least one formOID", event.endDate
			}
		}
	}

	@Test public void testImportOpenClinicaStudyDoesNotReturnNull() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertNotNull "Should never return null on valid studyOID", study
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithCorrectName() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals "Test Study", study.getName()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithCorrectStudyKey() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals "Test-OID", study.getStudyKey()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithCorrectNumberOfForms() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals 4, study.getForms().size()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithFormHavingCorrectName() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals "MSA1: Mother Screening Assessment 1 - 3", study.getForms()[0].getName()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithFormHavingCorrectName1() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals "MSA1: Mother Screening Assessment 1 - 2", study.getForms()[1].getName()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithFormHavingCorrectName2() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals "MSA2: Mother Screening Assessment 2 - 2", study.getForms()[2].getName()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithFormHavingCorrectName3() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		assertEquals "MSA2: Mother Screening Assessment 2 - 1", study.getForms()[3].getName()
	}

	@Test public void testImportOpenClinicaStudyReturnsStudyWithFormHavingFormVersions() {

		def study = openClinicaService.importOpenClinicaStudy("oid")

		study.getForms().each {

			assertTrue "Forms must have versions", it.getVersions().size() > 0
		}
	}

	@Test public void testThatExportFormDataExportsAGivenFormDataWithCorrectSuccessMessage() {

		createResponse("Success")
		def formData = createFormData()

		def response = openClinicaService.exportFormData(createUser(), formData)

		assertEquals "Success", response
	}

	@Test public void testThatExportFormDataExportsAGivenFormDataWithCorrectFailureMessage() {

		createResponse("Some Failure Message")
		def formData = createFormData()

		def response = openClinicaService.exportFormData(createUser(), formData)

		assertEquals "Some Failure Message", response
	}

	@Test public void testThatExportFormDataResetsExportFlagOnSuccessfulExportToOpenClinica() {

		createResponse("Success")
		def formData = createFormData()

		openClinicaService.exportFormData(createUser(), formData)

		assertTrue "Should reset export flag", formData.isExported(ExportConstants.EXPORT_BIT_OPENCLINICA)
	}

	@Test public void testThatExportFormDataDoesNotResetExportFlagOnFailedExportToOpenClinica() {

		createResponse("Some Failure Message")
		def formData = createFormData()

		openClinicaService.exportFormData(createUser(), formData)

		assertFalse "Should not reset export flag on failed OpenClinica Export", formData.isExported(ExportConstants.EXPORT_BIT_OPENCLINICA)
	}

	@Test public void testExtractKeyExtractsCorrectKey() {

		def xml = """<ODM formKey="Foo_Key"/>"""

		assertEquals "Foo_Key", openClinicaService.extractKey(xml)
	}

	@Test public void testExtractKeyExtractsCorrectKey2() {

		def xml = """<ODM formKey="Foo_Key"/>"""

		assertNotSame "Foo_Key", openClinicaService.extractKey(xml)
	}

	@Test public void testGetUserDetailsRetursnCreatedOpenClinicaUserWithCorrectUsername() {
		
		def user = openClinicaService.getUserDetails("username")
		
		assertEquals "Username should be foo", "foo", user.username
	}
	
	@Test public void testGetUserDetailsReturnsCreatedOpenCLinicaUserWithCorrectHashedPassword() {
		
		def user = openClinicaService.getUserDetails("username")
		
		assertEquals "Hashed Password should be hash LoL", "hash LoL", user.hashedPassword
	}
	
	@Test public void testGetUserDetailsReturnsCreatedOpenClinicaNotAuthorizedToUseWebservices() {
		 
		def user = openClinicaService.getUserDetails("username")
		
		assertFalse "User is not authorized to user web services", user.canUseWebServices
	}
	
	@Test public void testGetUserDetailsReturnsCreatedOpenClinicaUserWithCorrectStudyPermissions() {
		
		def user = openClinicaService.getUserDetails("username")
		
		assertEquals "User is authorized to access only two studies", 2, user.getAllowedStudies().size()
	}
	
	private createResponse(message) {

		def responses = [:]
		responses.put("Foo_Key", message)

		Mockito.when(client.importData(Mockito.any(User.class), Mockito.anyCollection())).thenReturn(responses)
	}

	private FormData createFormData() {

		def formData = new FormData()
		formData.setId(1)
		formData.setData("""<ODM formKey="Foo_Key"/>""")
		return formData
	}
}

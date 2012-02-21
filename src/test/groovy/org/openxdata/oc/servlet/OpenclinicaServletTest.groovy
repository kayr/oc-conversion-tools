package org.openxdata.oc.servlet

import static org.junit.Assert.*

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.runners.MockitoJUnitRunner
import org.openxdata.oc.data.TestData;
import org.openxdata.oc.service.OpenclinicaService
import org.openxdata.oc.transport.OpenClinicaSoapClient
import org.openxdata.server.admin.model.FormDef
import org.openxdata.server.admin.model.FormDefVersion
import org.openxdata.server.admin.model.StudyDef
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse


@RunWith(MockitoJUnitRunner.class)
class OpenclinicaServletTest extends GroovyTestCase {

	def study
	def request
	def response

	@Mock OpenclinicaService service
	@Mock OpenClinicaSoapClient client
	@InjectMocks def servlet = new OpenclinicaServlet(openclinicaService:service)

	@Before void setUp() {

		study = createStudy()

		Mockito.when(client.getOpenxdataForm('oid')).thenReturn(TestData.getCRFWebServiceResponse())
		Mockito.when(service.importOpenClinicaStudy('oid')).thenReturn(study)

		request = new MockHttpServletRequest()
		response = new MockHttpServletResponse()

		request.setParameter('oid', 'oid')
		request.setParameter('action', 'downloadAndConvert')
	}

	private def createStudy() {
		
		study = new StudyDef()
		study.setName('Test Study')

		def form = new FormDef()
		def version = new FormDefVersion()

		form.addVersion(version)

		study.addForm(form)

		return study
	}

	@Test public void testDownloadStudyDoesNotReturnNull() {

		servlet.doGet(request, response)

		def study = request.getSession().getAttribute('study')
		assertNotNull study
	}

	@Test public void testDownloadStudyReturnsValidStudyWithCorrectName() {

		servlet.doGet(request, response)

		def convertedStudy = request.getSession().getAttribute('study')
		assertEquals 'Test Study', convertedStudy.getName()
	}

	@Test public void testDownloadStudyReturnsValidStudyWithForms() {

		servlet.doGet(request, response)

		def convertedStudy = request.getSession().getAttribute('study')
		assertEquals 1, convertedStudy.getForms().size()
	}

	@Test public void testDownloadStudyReturnsValidStudyWithFormVersion() {

		servlet.doGet(request, response)

		def convertedStudy = request.getSession().getAttribute('study')
		assertEquals 1, convertedStudy.getForm(0).getVersions().size()
	}

}

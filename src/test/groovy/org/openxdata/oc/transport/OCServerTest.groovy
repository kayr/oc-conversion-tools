package org.openxdata.oc.transport;

import java.util.List

import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.openxdata.oc.model.ConvertedOpenclinicaStudy
import org.openxdata.oc.transport.factory.ConnectionFactory
import org.openxdata.oc.transport.impl.OpenClinicaSoapClientImpl


@Ignore("Not intended to be run during standard build because it is dependent on existing openclinica installation.")
public class OCServerTest extends GroovyTestCase {

	def client

	@Before public void setUp(){
		def factory = new ConnectionFactory("http://158.37.6.164/OpenClinica-ws")
		client = new OpenClinicaSoapClientImpl("MarkG", "b9a60a9d91a96ee522d0c942e5b88dfba25b0a12", factory)
	}

	@Test public void testListAllDoesNotReturnNull() {
		List<ConvertedOpenclinicaStudy> studies = client.listAll()

		assertNotNull studies
	}

	@Test public void testListAllReturns1Study() {
		List<ConvertedOpenclinicaStudy> studies = client.listAll()

		assertEquals 1, studies.size()
	}

	@Test public void testGetSubjectsDoesNotReturnNull() {
		def subjects = client.getSubjectKeys("default-study")

		assertNotNull subjects
	}

	@Test public void testGetSubjectsReturnsCorrectNumberOfSubjects() {
		def subjects = client.getSubjectKeys("default-study")

		assertEquals 82, subjects.size()
	}
	
	@Test public void testGetMetaDataDoesNotReturnNull() {
		def metadata = client.getMetadata("default-study")

		assertNotNull metadata
	}

	@Test public void testGetMetaDataReturnsValidODM() {
		def metadata = client.getMetadata("default-study")

		def metadataXml = new XmlSlurper().parseText(metadata)
		assertEquals 'ODM', metadataXml.name()
	}

	@Test public void testGetMetaDataReturnsValidODMWithStudy() {
		def metadata = client.getMetadata("default-study")

		def metadataXml = new XmlSlurper().parseText(metadata)

		def study = metadataXml.ODM.Study[0]

		assertEquals 'Study', study.name()
	}

	@Test public void testGetOpenXdataFormDoesNotReturnNull() {

		String convertedXform = client.getOpenxdataForm("default-study")

		assertNotNull convertedXform
	}

	@Test public void testGetOpenXdataFormReturnsValidStudyRoot() {

		String convertedXform = client.getOpenxdataForm("default-study")

		def xml = new XmlSlurper().parseText(convertedXform)
		assertEquals 'study', xml.name()

	}

	@Test public void testGetOpenXdataFormReturnsValidStudyWithFormElement() {

		String convertedXform = client.getOpenxdataForm("default-study")

		def form = convertedXform.form[0]

		assertEquals 'form', form.name()

	}

	@Test public void testGetOpenXdataFormReturnsValidStudyWithVersionElement() {

		String convertedXform = client.getOpenxdataForm("default-study")

		def version = convertedXform.form.version[0]

		assertEquals 'version', version.name()

	}

	@Test public void testGetOpenXdataFormReturnsValidStudyWithXformElement() {

		String convertedXform = client.getOpenxdataForm("default-study")

		def xform = convertedXform.form.version.xform[0]

		assertEquals 'xform', xform.name()

	}

	@Test public void testGetOpenXdataFormReturnsValidStudyWithXformsElement() {

		String convertedXform = client.getOpenxdataForm("default-study")

		def xforms = convertedXform.form.version.xform.xforms[0]

		assertEquals 'xforms', xforms.name()

	}
}

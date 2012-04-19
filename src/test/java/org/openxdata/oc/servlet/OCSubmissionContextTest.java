/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openxdata.oc.servlet;

import org.mockito.MockitoAnnotations;
import java.util.ArrayList;
import java.util.Collections;
import org.openxdata.oc.model.StudySubject;
import java.util.List;
import java.util.Properties;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.openxdata.oc.Fixtures;
import org.openxdata.oc.service.OpenClinicaService;
import org.openxdata.server.admin.model.StudyDef;
import org.openxdata.server.service.StudyManagerService;
import static org.junit.Assert.*;
import org.openxdata.oc.data.TestData;
import org.openxdata.xform.StudyImporter;
import static org.mockito.Mockito.*;

/**
 *
 * @author kay
 */
public class OCSubmissionContextTest {

	@Mock
	private OpenClinicaService ocService;
	@Mock
	private StudyManagerService studyManagerService;
	private static List<StudySubject> studySubjectsObjects = TestData.getStudySubjectsObjects();
	private static StudyDef oXDStudy = Fixtures.getOXDStudy();
	private OCSubmissionContext instance;
	private Properties props;

	public OCSubmissionContextTest() {
	}

	@BeforeClass
	public static void initTestData() {
		studySubjectsObjects = TestData.getStudySubjectsObjects();
		StudyImporter importer = new StudyImporter(TestData.getConvertedXform());
		oXDStudy = (StudyDef) importer.extractStudy();
	}

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		props = new Properties();
		props.setProperty("ocStudy", "Test Study");
		instance = new OCSubmissionContext(null, null, (byte) 1, null, null, null, studyManagerService, ocService,
				props);

	}

	/**
	 * Test of availableWorkitems method, of class OCSubmissionContext.
	 */
	@Test
	public void testAvailableWorkitems() {
		when(studyManagerService.getStudyByName(getStudyName())).thenReturn(new ArrayList<StudyDef>() {

			private static final long serialVersionUID = 1L;

			{
				add(oXDStudy);
			}
		});

		when(ocService.getStudySubjectEvents("S_DEFAULTS1")).thenReturn(studySubjectsObjects);

		List<?> result = instance.availableWorkitems();

		assertFalse("A list of workitems were expected but none were returned", result.isEmpty());

	}

	@Test
	public void testAvailableWorkitemReturnEmptListOfWorkitemsIfNoOcStudyAvailable() {
		when(ocService.getStudySubjectEvents(getStudyName())).thenReturn(studySubjectsObjects);
		when(studyManagerService.getStudyByName(getStudyName())).thenReturn(Collections.<StudyDef> emptyList());
		List<Object[]> availableWorkitems = instance.availableWorkitems();

		assertTrue("Workitems are expected to be empty", availableWorkitems.isEmpty());

		when(studyManagerService.getStudyByName(getStudyName()))
				.thenThrow(new RuntimeException("Deliberate Exception"));
		List<Object[]> availableWorkitems1 = instance.availableWorkitems();
		assertTrue("Workitems are expected to be empty", availableWorkitems1.isEmpty());

	}

	private String getStudyName() {
		return props.getProperty("ocStudy");
	}

}

package org.openxdata.oc.servlet;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.openxdata.oc.authentication.AuthenticationProvider;
import org.openxdata.oc.exception.ExportException;
import org.openxdata.oc.model.Event;
import org.openxdata.oc.model.StudySubject;
import org.openxdata.oc.service.OpenClinicaService;
import org.openxdata.oc.service.impl.OpenClinicaServiceImpl;
import org.openxdata.oc.util.PropertiesUtil;
import org.openxdata.proto.WFSubmissionContext;
import org.openxdata.proto.model.OxdWorkitem;
import org.openxdata.proto.model.ParameterQuestionMap;
import org.openxdata.proto.model.WorkitemFormRef;
import org.openxdata.server.admin.model.FormData;
import org.openxdata.server.admin.model.FormDef;
import org.openxdata.server.admin.model.StudyDef;
import org.openxdata.server.admin.model.User;
import org.openxdata.server.service.AuthenticationService;
import org.openxdata.server.service.DataExportService;
import org.openxdata.server.service.FormDownloadService;
import org.openxdata.server.service.FormService;
import org.openxdata.server.service.RoleService;
import org.openxdata.server.service.StudyManagerService;
import org.openxdata.server.service.UserService;
import org.openxdata.server.servlet.DefaultSubmissionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.ServletContextAware;

public class OCSubmissionContext extends DefaultSubmissionContext implements WFSubmissionContext, ServletContextAware,
		InitializingBean {

	private static Logger log = LoggerFactory.getLogger(OCSubmissionContext.class);

	private Properties props;
	private OpenClinicaService openclinicaService;

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private FormDownloadService formDownloadService;
	@Autowired
	private FormService formService;
	@Autowired
	private DataExportService dataExportService;
	@Autowired
	private StudyManagerService studyManagerService;

	@Autowired
	private AuthenticationService authenticationService;
	@Autowired
	private ServletContext servletContext;

	private boolean initaliazed = false;
	private List<Event> orphanedEvents = new ArrayList<Event>();
	private AuthenticationProvider authProvider;

	public OCSubmissionContext() {
		super();
	}

	public void init() {

		if (initaliazed)
			return;
		this.openclinicaService = new OpenClinicaServiceImpl(props);
		openclinicaService.setStudyService(studyManagerService);
		openclinicaService.setFormService(formService);
		openclinicaService.setDataExportService(dataExportService);

		// Initialize authentication provider for this session.
		authProvider = new AuthenticationProvider();

		authProvider.setUserService(userService);
		authProvider.setRoleService(roleService);
		authProvider.setStudyService(studyManagerService);
		authProvider.setOpenclinicaService(openclinicaService);
		authProvider.setAuthenticationService(authenticationService);
		props = new PropertiesUtil().loadOpenClinicaProperties(servletContext);
		initaliazed = true;
	}

	public Map<String, String> getOutParamsQuestionMapping(int formId, String caseId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public OxdWorkitem getWorkitem(String caseId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void submitWorkitem(String caseId, String paramXML) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public List<OxdWorkitem> availableWorkitems() {

		clearOphanedEvents();

		List<StudySubject> sbjEvents = openclinicaService.getStudySubjectEvents();
		List<OxdWorkitem> workitems = new ArrayList<OxdWorkitem>();
		StudyDef ocStudy = loadConvertedOpenClinicaStudy();
		if (ocStudy == null) {
			return workitems;
		}

		for (StudySubject studySubject : sbjEvents) {
			List<OxdWorkitem> studySubjWorkitems = studySubjectToWorkItems(studySubject, ocStudy);
			workitems.addAll(studySubjWorkitems);
		}

		return workitems;
	}

	public void clearOphanedEvents() {
		orphanedEvents.clear();
	}

	public List<Event> getOrphanedEvents() {
		return orphanedEvents;
	}

	List<OxdWorkitem> studySubjectToWorkItems(StudySubject studySubject, StudyDef ocStudy) {

		List<OxdWorkitem> workitems = new ArrayList<OxdWorkitem>();
		List<Event> allEvents = studySubject.getEvents();
		Hashtable<String, List<Event>> eventsGroupedByName = groupEventByName(allEvents);
		Set<Entry<String, List<Event>>> eventGroupEntry = eventsGroupedByName.entrySet();

		for (Entry<String, List<Event>> entry : eventGroupEntry) {
			List<Event> eventList = entry.getValue();

			String workitemName = (studySubject.getSubjectOID() + "-" + entry.getKey()).replaceFirst("SS_", "");
			String workitemId = generateWorkitemID(studySubject, entry.getKey());

			OxdWorkitem workitem = new OxdWorkitem(workitemId, workitemName);

			List<WorkitemFormRef> formReferences = new ArrayList<WorkitemFormRef>();

			for (Event ocEvent : eventList) {
				List<WorkitemFormRef> formRefs = extractFormReferencesFromEvent(ocEvent, ocStudy, studySubject);
				formReferences.addAll(formRefs);
			}
			if (!formReferences.isEmpty()) {
				workitem.getWorkitemForms().addAll(formReferences);
				workitems.add(workitem);
			}

		}
		return workitems;
	}

	private List<WorkitemFormRef> extractFormReferencesFromEvent(Event ocEvent, StudyDef studyDef,
			StudySubject studySubject) {
		List<WorkitemFormRef> formReferences = new ArrayList<WorkitemFormRef>();
		List<String> formOIDs = ocEvent.getFormOIDs();
		for (String formOID : formOIDs) {
			WorkitemFormRef formRef = formDefToFormReferece(formOID, studyDef, ocEvent, studySubject);
			if (formRef != null) {
				formReferences.add(formRef);
			}
		}
		return formReferences;
	}

	private WorkitemFormRef formDefToFormReferece(String formOID, StudyDef oCStudyID, Event event,
			StudySubject studySubject) {
		FormDef formDef = getFormByDescription(oCStudyID, formOID);

		if (formDef == null) {
			orphanedEvents.add(event);
			return null;
		}

		WorkitemFormRef formRef = new WorkitemFormRef(oCStudyID.getId(), formDef.getDefaultVersion().getId());
		formRef.addParameterQuetionMap(new ParameterQuestionMap("SubjectKey_", "subjectkey", studySubject
				.getSubjectOID() + "", false));
		return formRef;
	}

	public List<OxdWorkitem> getWorkItems(String... caseIds) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	private String generateWorkitemID(StudySubject studySubject, String eventOID) {
		String key = studySubject.getSubjectOID().toString();
		key = key + "&" + eventOID;
		return key;
	}

	// Loads the study that was converted.
	private StudyDef loadConvertedOpenClinicaStudy() {
		StudyDef study = null;
		try {
			String property = props.getProperty("studyOID");
			log.debug("Reading study from properties file: " + property);
			if (property == null || property.isEmpty()) {
				log.error("The studyOID Property has not been set");
				return null;
			}
			study = studyManagerService.getStudyByKey(property);
			if (!isMappedToStudy(study)) {
				log.info("**Access not allowed to OC Converted study hence no workitems will be created");
				study = null;
			}
		} catch (Exception e) {
			log.error("Failed to get openclinica study" + e.getMessage());
			log.trace("Failed to get openclinica study", e);
		}

		return study;

	}

	private boolean isMappedToStudy(StudyDef study) {
		Map<Integer, String> studyNames = studyManagerService.getStudyNamesForCurrentUser();
		if (study == null)
			return false;
		return studyNames != null && studyNames.containsKey(study.getId());
	}

	private FormDef getFormByDescription(StudyDef def, String description) {
		List<FormDef> forms = def.getForms();

		for (FormDef formDef1 : forms) {
			String frmDefDescr = formDef1.getDescription();
			if (frmDefDescr != null && frmDefDescr.equalsIgnoreCase(description)) {
				return formDef1;
			}
		}
		return null;
	}

	private Hashtable<String, List<Event>> groupEventByName(List<Event> events) {
		Hashtable<String, List<Event>> eventGroups = new Hashtable<String, List<Event>>();
		for (Event event : events) {
			String evntOID = (String) event.getEventDefinitionOID();

			List<Event> grpEvnts = eventGroups.get(evntOID);
			if (grpEvnts == null) {
				grpEvnts = new ArrayList<Event>();
				eventGroups.put(evntOID, grpEvnts);
			}

			grpEvnts.add(event);
		}

		return eventGroups;
	}

	public void setProps(Properties props) {
		this.props = props;
	}

	public void setStudyManagerService(StudyManagerService studyManagerService) {
		this.studyManagerService = studyManagerService;
	}

	@Override
	public String setUploadResult(String formInstance) {

		User user = userService.getLoggedInUser();

		FormData formData = formDownloadService.saveFormData(formInstance, user, new Date());
		String exportResponse = openclinicaService.exportFormData(user, formData);
		if ("Success".equals(exportResponse))
			return formData.getId() + "";
		else
			throw new ExportException("Upload Failed: " + exportResponse);
	}

	@Override
	public boolean authenticate(String username, String password) {
		User authenticatedUser = authProvider.authenticate(username, password);

		return authenticatedUser != null;
	}

	public void setFormService(FormDownloadService formService) {
		this.formDownloadService = formService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void disableWorkitem(String workitemId) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setAuthenticationProvider(AuthenticationProvider authProvider) {
		this.authProvider = authProvider;
	}

	void setOpenClinicaService(OpenClinicaService openClinicaService) {
		this.openclinicaService = openClinicaService;
	}

	public void afterPropertiesSet() throws Exception {
		init();
	}

	public void setServletContext(ServletContext servletContext) {
		this.servletContext = servletContext;
	}

}

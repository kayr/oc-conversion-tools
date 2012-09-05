package org.openxdata.proto;

import java.util.List;
import java.util.Map;

public interface WFSubmissionContext extends SubmissionContext {

	public Map<String, String> getOutParamsQuestionMapping(int formId, String caseId);

	/**
	 * TaskName = objects[0] CaseID = objects[1] StudyId = objects[2] FormId = objects[3] Prefills = objects[4] Prefills
	 * = List<String[Parameter,Question,Value,Output]>
	 */
	public Map<String, Object> getWorkitem(String caseId);

	public void submitWorkitem(String caseId, String paramXML);

	public List<Map<String, Object>> availableWorkitems();

	public List<Map<String, Object>> getWorkItems(String... caseIds);
}

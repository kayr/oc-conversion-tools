package org.openxdata.oc.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.openxdata.oc.service.OpenclinicaService;
import org.openxdata.oc.service.impl.OpenclinicaServiceImpl;
import org.openxdata.server.admin.model.StudyDef;
import org.openxdata.server.dao.EditableDAO;
import org.openxdata.server.dao.FormDataDAO;
import org.openxdata.server.service.StudyManagerService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class OpenclinicaServlet extends HttpServlet {

	private static final long serialVersionUID = 6577932874016086164L;
		
	private static final String DOWNLOAD_AND_CONVERT = "downloadAndConvert";
	
	private FormDataDAO formDataDAO;
	private EditableDAO editableDAO;
	
	private StudyManagerService studyService;
	private OpenclinicaService openclinicaService;
	
	
    @Override
	public void init() {
    	
		ServletContext sctx = this.getServletContext();
		WebApplicationContext ctx = WebApplicationContextUtils.getRequiredWebApplicationContext(sctx);
		
		 formDataDAO = (FormDataDAO) ctx.getBean("formDataDAO");
		 editableDAO = (EditableDAO) ctx.getBean("studyManagerDAO");
		 studyService = (StudyManagerService) ctx.getBean("studyManagerService");
		 
		 openclinicaService = new OpenclinicaServiceImpl();
		 
		 openclinicaService.setFormDataDAO(formDataDAO);
		 openclinicaService.setEditableDAO(editableDAO);
		 openclinicaService.setStudyService(studyService);
		 
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    	
    	StudyDef study = null;
    	String oid = request.getParameter("oid");
    	String action = request.getParameter("action");
    	
    	if(DOWNLOAD_AND_CONVERT.equals(action)) {
    		study = downloadAndConvertOpenclinicaStudy(oid);
    	}
    	
    	request.getSession().setAttribute("study", study);
    }
    
	private StudyDef downloadAndConvertOpenclinicaStudy(String oid) {
		return openclinicaService.importOpenClinicaStudy(oid);
	}
}

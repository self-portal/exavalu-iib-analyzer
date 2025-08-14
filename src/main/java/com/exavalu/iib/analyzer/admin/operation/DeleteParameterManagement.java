package com.exavalu.iib.analyzer.admin.operation;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class DeleteParameterManagement {
	private static final Logger log = LoggerFactory.getLogger(GetParameterManagement.class);
	
	@Autowired
	private EntityManager entityManager;

	@DeleteMapping("/update-parameter")
	@Transactional
	public String deleteParameter(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> params, final HttpServletResponse httpServletResponse) throws Exception {
		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/update-parameter endpoint invoked.");
		}
		
		String finalResponse = "";
		String requestedJobName = params.get("job_name");
		int deleteForMasterWeight = 0;
		int deleteForEstimationWeight = 0;
		
//		checking if any job name is requested or not
		if(requestedJobName != null) {
			deleteForMasterWeight = deleteFromRefMasterWeight(requestedJobName);
			deleteForEstimationWeight = deleteFromRefEstimationWeight(requestedJobName);
		}else {
			finalResponse = buildResponse("400", "Bad Request", "Please select at least one job for delete",
					"/update-parameter", "").toString();
		}
		if(deleteForMasterWeight == 1 || deleteForEstimationWeight == 1) {
			finalResponse = buildResponse("200", "Ok", "Parameter for "+  requestedJobName +" Deleted Successfully",
					"/update-parameter", "").toString();
		}
		
		return finalResponse;
	}
	
//	method for delete operation in ref_master_weight table
	public int deleteFromRefMasterWeight(String requestedJobName) {
		int rowNotAffected = 0;
		String queryForDelete = "DELETE FROM ref_master_weight WHERE job_name = ?";
		var query = entityManager.createNativeQuery(queryForDelete);
		query.setParameter(1,
				requestedJobName);
		rowNotAffected = query.executeUpdate();
		return rowNotAffected;
	}
	
//	method for delete operation in res_effor_estimation_weight table
	public int deleteFromRefEstimationWeight(String requestedJobName) {
		int rowNotAffected = 0;
		String queryForDelete = "DELETE FROM ref_effort_estimation_weight WHERE job_name = ?";
		var query = entityManager.createNativeQuery(queryForDelete);
		query.setParameter(1,
				requestedJobName);
		rowNotAffected = query.executeUpdate();
		return rowNotAffected;
	}
	
	/* Build response structure */
	public JsonObject buildResponse(String statusCode, String statusReasonPhrase, String statusMessage,
			String statusPath, String statusTrace) throws Exception {
		JsonObject finalResponseJsonObject = new JsonObject();
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		genericStatusResponse.setStatusCode(statusCode);
		genericStatusResponse.setStatusReasonPhrase(statusReasonPhrase);
		genericStatusResponse.setStatusMessage(statusMessage);
		genericStatusResponse.setStatusPath(statusPath);
		genericStatusResponse.setStatusTrace(statusTrace);

		finalResponseJsonObject = genericStatusResponse.getGenericStatusResponse();
		return finalResponseJsonObject;
	}
}

package com.exavalu.iib.analyzer.admin.operation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class GetParameterManagement {
	private static final Logger log = LoggerFactory.getLogger(GetParameterManagement.class);

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@GetMapping("/update-parameter")
	@Transactional
	public String getParameter(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> params, final HttpServletResponse httpServletResponse) {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/update-parameter endpoint invoked.");
		}
		
		String requestedJobName = params.get("job_name");
		String finalResponse = "";
		JsonArray finalResponseArray = new JsonArray();
		JsonObject masterWeight = new JsonObject();

//		checking if any job name is requested or not
		if (requestedJobName != null) {
			String getMasterWeight = "SELECT * FROM ref_master_weight WHERE job_name = ?";
			Query queryForMasterWeight = entityManager.createNativeQuery(getMasterWeight).setParameter(1,
					requestedJobName);
			List<Object[]> resultSetOfMasterWeight = queryForMasterWeight.getResultList();

			if (!resultSetOfMasterWeight.isEmpty()) {
				masterWeight = buildMasterWeight(resultSetOfMasterWeight.get(0));
				JsonArray arrayOfEstimationWeight = new JsonArray();
				String jobName = masterWeight.get("jobName").getAsString();
				String getEstimationWeight = "SELECT * FROM ref_effort_estimation_weight WHERE job_name = ?";
				Query query = entityManager.createNativeQuery(getEstimationWeight).setParameter(1, jobName);
				List<Object[]> resultSetOfEstimationWeight = query.getResultList();

				if (!resultSetOfEstimationWeight.isEmpty()) {
					for (int estimationWeightCount = 0; estimationWeightCount < resultSetOfEstimationWeight
							.size(); estimationWeightCount++) {
						arrayOfEstimationWeight
								.add(buildEstimationWeight(resultSetOfEstimationWeight.get(estimationWeightCount)));
					}
				}
				masterWeight.add("refEstmitationEffort", arrayOfEstimationWeight);
			} else {
				finalResponse = "JOB not found";
				httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
				genericStatusResponse.setProjectStatusCode("400");
				genericStatusResponse.setProjectStatusReasonPhrase("Bad Request");
				genericStatusResponse.setProjectStatusMessage("JOB not found in the Parameter List");
				masterWeight = genericStatusResponse.getProjectStatusResponse();
			}
			finalResponse = masterWeight.toString();
		} else {
			String getMasterWeight = "SELECT * FROM ref_master_weight";
			Query queryForMasterWeight = entityManager.createNativeQuery(getMasterWeight);
			List<Object[]> resultSetOfMasterWeight = queryForMasterWeight.getResultList();

			if (!resultSetOfMasterWeight.isEmpty()) {
				for (int masterWeightCount = 0; masterWeightCount < resultSetOfMasterWeight
						.size(); masterWeightCount++) {
					masterWeight = buildMasterWeight(resultSetOfMasterWeight.get(masterWeightCount));
					JsonArray arrayOfEstimationWeight = new JsonArray();
					String jobName = masterWeight.get("jobName").getAsString();
					String getEstimationWeight = "SELECT * FROM ref_effort_estimation_weight WHERE job_name = ?";
					Query query = entityManager.createNativeQuery(getEstimationWeight).setParameter(1, jobName);
					List<Object[]> resultSetOfEstimationWeight = query.getResultList();

					if (!resultSetOfEstimationWeight.isEmpty()) {
						for (int estimationWeightCount = 0; estimationWeightCount < resultSetOfEstimationWeight
								.size(); estimationWeightCount++) {
							arrayOfEstimationWeight
									.add(buildEstimationWeight(resultSetOfEstimationWeight.get(estimationWeightCount)));
						}
					}
					masterWeight.add("refEstmitationEffort", arrayOfEstimationWeight);
					finalResponseArray.add(masterWeight);
				}
			}
			finalResponse = finalResponseArray.toString();
		}
		return finalResponse;
	}

	public JsonObject buildEstimationWeight(Object[] estimationWeightInfo) {
		JsonObject estimationWeight = new JsonObject();
		estimationWeight.addProperty("id", (Number) estimationWeightInfo[0]);
		estimationWeight.addProperty("scoreMin", (Number) estimationWeightInfo[2]);
		estimationWeight.addProperty("scoreMax", (Number) estimationWeightInfo[3]);
		estimationWeight.addProperty("complexityLevel", estimationWeightInfo[4].toString());
		estimationWeight.addProperty("estimatedHours", (Number) estimationWeightInfo[5]);
		return estimationWeight;
	}

	public JsonObject buildMasterWeight(Object[] masterWeightInfo) {
		JsonObject masterWeight = new JsonObject();
		Gson gson = new Gson();
		JsonObject reUsableJsonObject = new JsonObject();

		masterWeight.addProperty("jobName", masterWeightInfo[1].toString());

		reUsableJsonObject = gson.fromJson(masterWeightInfo[2].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refListener", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[3].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refMethod", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[4].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refNode", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[5].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refConnector", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[6].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refNodeProto", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[7].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refTransformNode", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[8].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refTransformLoc", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[9].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refTransformLoop", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[10].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refRoutePath", reUsableJsonObject);

		reUsableJsonObject = new JsonObject();
		reUsableJsonObject = gson.fromJson(masterWeightInfo[11].toString(), reUsableJsonObject.getClass());
		masterWeight.add("refSchema", reUsableJsonObject);

		return masterWeight;
	}

}
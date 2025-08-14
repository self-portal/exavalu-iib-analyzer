package com.exavalu.iib.analyzer.admin.operation;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class ParameterManagement {
	private static final Logger log = LoggerFactory.getLogger(ParameterManagement.class);

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@PostMapping("/update-parameter")
	@Transactional
	public String updateParameter(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> params,
			@RequestBody ParameterManagementContainer requestBody, final HttpServletResponse httpServletResponse) {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/update-parameter endpoint invoked.");
		}

		String finalResponse = "";
		int insertedEstimationWeight = 0;
		int estimationWeightUpdated = 0;
		int dataInserted = 0;
		int dataUpdated = 0;
		String userName = params.get("user_name");
		int jobRole = 0;
		String jobName = requestBody.getJobName();
		String refListner = JSONparser(requestBody.getRefListner());
		String refMethod = JSONparser(requestBody.getRefMethod());
		String refNode = JSONparser(requestBody.getRefNode());
		String refConnector = JSONparser(requestBody.getRefConnector());
		String refNodeProto = JSONparser(requestBody.getRefNodeProto());
		String refTransformNode = JSONparser(requestBody.getRefTransformNode());
		String refTransformLoc = JSONparser(requestBody.getRefTransformLoc());
		String refTransformLoop = JSONparser(requestBody.getRefTransformLoop());
		String refRoutePath = JSONparser(requestBody.getRefRoutePath());
		String refSchema = JSONparser(requestBody.getRefSchema());

		if (!refListner.contentEquals("False") && !refSchema.contentEquals("False") && !refNode.contentEquals("False")
				&& !refConnector.contentEquals("False") && !refNodeProto.contentEquals("False")
				&& !refTransformNode.contentEquals("False") && !refTransformLoc.contentEquals("False")
				&& !refTransformLoop.contentEquals("False") && !refRoutePath.contentEquals("False")) {
			String getRole = "SELECT role_id FROM users WHERE user_name = ?";
			Query queryToFindJobRole = entityManager.createNativeQuery(getRole).setParameter(1, userName);

			List<Integer> resultSetOfJobRole = queryToFindJobRole.getResultList();

			if (!resultSetOfJobRole.isEmpty()) {

				jobRole = (int) resultSetOfJobRole.get(0);

			}
			// validating if the user is admin or not
			if (jobRole == 1) {
				// inserting or updating the data in ref_estimation_weight
				for (int estimationWeight = 0; estimationWeight < requestBody.getRefEstmitationEffort()
						.size(); estimationWeight++) {
					int id = getEstimationWeight(jobName,
							requestBody.getRefEstmitationEffort().get(estimationWeight).getComplexityLevel());
					// checking if the job_name and complexity_leve is present or not
					if (id == 0) {
						insertedEstimationWeight = insertEstimationWeight(jobName,
								requestBody.getRefEstmitationEffort().get(estimationWeight), userName);
					} else {
						estimationWeightUpdated = updateEstimationWeight(jobName,
								requestBody.getRefEstmitationEffort().get(estimationWeight), userName);
					}
				}

				// inserting or updating data in ref_master_weight
				int refId = getRefDetails(jobName);
				// checking it job_name is present or not in the table
				if (refId == 0) {
					dataInserted = insertDataRefMasterWeight(jobName, refListner, refMethod, refNode, refConnector,
							refNodeProto, refTransformNode, refTransformLoc, refTransformLoop, refRoutePath, refSchema,
							userName);
				} else {
					dataUpdated = updateRefMasterWeight(jobName, refListner, refMethod, refNode, refConnector,
							refNodeProto, refTransformNode, refTransformLoc, refTransformLoop, refRoutePath, refSchema,
							userName);
				}
			}
			if ((insertedEstimationWeight == 1 || estimationWeightUpdated == 1)
					&& (dataInserted == 1 || dataUpdated == 1))
				finalResponse = buildFinalResponse("200", "OK", "Successfully updated").toString();
			else {
				httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				finalResponse = buildFinalResponse("500", "OK", "Internal Servor Error").toString();
			}
		} else {
			httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			finalResponse = buildFinalResponse("400", "Bad Request", "Not updated").toString();
		}

		return finalResponse;
	}

	// Parsing the JSON request
	public String JSONparser(GeneralStructure request) {

		JsonObject requestInJson = new JsonObject();
		JsonArray weightInfo = new JsonArray();

		int min = request.getMinLimit();
		int max = request.getMaxLimit();
		int currMin = 0;
		int currMax = 0;
		int prevMax = 0;

		requestInJson.addProperty("weightType", request.getWeightType());

		if (min < max) {
			requestInJson.addProperty("minLimit", min);
			requestInJson.addProperty("maxLimit", max);
		} else
			return "False";

		weightInfo = new Gson().toJsonTree(request.getWeightInfo()).getAsJsonArray();
		for (int weightInfoLoop = 0; weightInfoLoop < weightInfo.size(); weightInfoLoop++) {
			JsonObject tempObject = weightInfo.get(weightInfoLoop).getAsJsonObject();
			currMin = tempObject.get("min").getAsInt(); // setting current min of weightInfo
			currMax = tempObject.get("max").getAsInt(); // setting current max of weightInfo
			// checking if the current min of weight info is smaller than the current max
			if (currMin > currMax)
				return "False";
			else {
				if (weightInfoLoop != 0) {
					// setting the value of previous max if current min is greater than previous Max
					// for the every object of weightInfo
					if (prevMax >= currMin) {
						return "False";
					} else {
						prevMax = currMax;
					}
				} else {
					// setting the value of previous max if current min is less than or equal to
					// overall min for the first object of weightInfo
					if (currMin < min)
						return "False";
					prevMax = currMax;
				}
			}
		}
		if (currMax > max)
			return "False"; // checking if max of last object of weightInfo is not greater than overall max
		requestInJson.add("weightInfo", weightInfo);
		return requestInJson.toString();
	}

	public String JSONparser(GeneralStructure_Listener_Method request) {

		JsonObject requestInJson = new JsonObject();
		JsonArray weightInfo = new JsonArray();

		requestInJson.addProperty("weightType", request.getWeightType());
		weightInfo = new Gson().toJsonTree(request.getWeightInfo()).getAsJsonArray();
		requestInJson.add("weightInfo", weightInfo);
		return requestInJson.toString();
	}

	// Method for updating data in ref_master_weight
	public int updateRefMasterWeight(String jobName, String refListner, String refMethod, String refNode,
			String refConnector, String refNodeProto, String refTransformNode, String refTransformLoc,
			String refTransformLoop, String refRoutePath, String refSchema, String userName) {
		int rowAffected = 0;

		String update = "UPDATE ref_master_weight SET ref_listener = ?, ref_method = ?, ref_node = ?, ref_connector = ?, ref_node_proto = ?, ref_transform_node = ?, ref_transform_loc = ?, ref_transform_loop = ?, ref_route_path = ?, ref_schema = ?, updated_by = ? WHERE job_name = ?";

		Query query = entityManager.createNativeQuery(update).setParameter(1, refListner).setParameter(2, refMethod)
				.setParameter(3, refNode).setParameter(4, refConnector).setParameter(5, refNodeProto)
				.setParameter(6, refTransformNode).setParameter(7, refTransformLoc).setParameter(8, refTransformLoop)
				.setParameter(9, refRoutePath).setParameter(10, refSchema).setParameter(11, userName)
				.setParameter(12, jobName);
		rowAffected = query.executeUpdate();

		return rowAffected;
	}

	// method for inserting data in ref_master_weight
	public int insertDataRefMasterWeight(String jobName, String refListner, String refMethod, String refNode,
			String refConnector, String refNodeProto, String refTransformNode, String refTransformLoc,
			String refTransformLoop, String refRoutePath, String refSchema, String userName) {
		int rowAffected = 0;
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String update = """
                INSERT INTO ref_master_weight (job_name, ref_listener, ref_method, ref_node, ref_connector, ref_node_proto, ref_transform_node, ref_transform_loc, ref_transform_loop, ref_route_path, ref_schema, created_by, created_date_timestamp, updated_by)\
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)\
                """;
		Query query = entityManager.createNativeQuery(update).setParameter(1, jobName).setParameter(2, refListner)
				.setParameter(3, refMethod).setParameter(4, refNode).setParameter(5, refConnector)
				.setParameter(6, refNodeProto).setParameter(7, refTransformNode).setParameter(8, refTransformLoc)
				.setParameter(9, refTransformLoop).setParameter(10, refRoutePath).setParameter(11, refSchema)
				.setParameter(12, userName).setParameter(13, timestamp).setParameter(14, userName);

		rowAffected = query.executeUpdate();

		return rowAffected;
	}

	// method for checking if job_name is present in ref_master_weight
	public int getRefDetails(String jobName) {
		int refId = 0;
		String getRefID = "SELECT ref_id FROM ref_master_weight WHERE job_name = ?";
		Query queryToFindRefID = entityManager.createNativeQuery(getRefID).setParameter(1, jobName);
		@SuppressWarnings("unchecked")
		List<Integer> resultSetOfRefId = queryToFindRefID.getResultList();

		if (!resultSetOfRefId.isEmpty()) {

			refId = (int) resultSetOfRefId.get(0);

		}
		return refId;
	}

	// method for checking if job_name and complexity_level is present in
	// res_effort_estimation_weight table
	public int getEstimationWeight(String jobName, String complexityLevel) {
		int refId = 0;
		String getID = "SELECT id FROM ref_effort_estimation_weight WHERE job_name = ? and complexity_level = ?";
		Query queryToFindID = entityManager.createNativeQuery(getID).setParameter(1, jobName).setParameter(2,
				complexityLevel);
		@SuppressWarnings("unchecked")
		List<Integer> resultSetOfGetID = queryToFindID.getResultList();

		if (!resultSetOfGetID.isEmpty()) {

			refId = (int) resultSetOfGetID.get(0);

		}
		return refId;
	}

	// method for inserting data in res_effort_estimation_weight table
	public int insertEstimationWeight(String jobName, RefEstmitationEffort request, String userName) {

		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		int rowAffected = 0;

		String update = """
                INSERT INTO ref_effort_estimation_weight (job_name, score_min, score_max, complexity_level, estimated_hours, created_by, created_date_timestamp, updated_by)\
                VALUES (?,?,?,?,?,?,?,?)\
                """;
		Query query = entityManager.createNativeQuery(update).setParameter(1, jobName)
				.setParameter(2, request.getScoreMin()).setParameter(3, request.getScoreMax())
				.setParameter(4, request.getComplexityLevel()).setParameter(5, request.getEstimatedHours())
				.setParameter(6, userName).setParameter(7, timestamp).setParameter(8, userName);
		rowAffected = query.executeUpdate();

		return rowAffected;
	}

	// method for updating res_effort_estimation_weight table
	public int updateEstimationWeight(String jobName, RefEstmitationEffort request, String userName) {
		int rowAffected = 0;

		String update = "UPDATE ref_effort_estimation_weight SET score_min = ?, score_max = ?, estimated_hours = ?, updated_by = ? WHERE job_name = ? and complexity_level = ?";
		Query query = entityManager.createNativeQuery(update).setParameter(1, request.getScoreMin())
				.setParameter(2, request.getScoreMax()).setParameter(3, request.getEstimatedHours())
				.setParameter(4, userName).setParameter(5, jobName).setParameter(6, request.getComplexityLevel());

		rowAffected = query.executeUpdate();

		return rowAffected;
	}

	public JsonObject buildFinalResponse(String statusCode, String reasonPhrase, String message) {
		JsonObject finalResponse = new JsonObject();
		GenericStatusResponse getGenericStatusResponse = new GenericStatusResponse();
		getGenericStatusResponse.setStatusCode(statusCode);
		getGenericStatusResponse.setStatusReasonPhrase(reasonPhrase);
		getGenericStatusResponse.setStatusMessage(message);
		getGenericStatusResponse.setStatusPath("/update-parameter");

		finalResponse = getGenericStatusResponse.getGenericStatusResponse();

		return finalResponse;
	}
}

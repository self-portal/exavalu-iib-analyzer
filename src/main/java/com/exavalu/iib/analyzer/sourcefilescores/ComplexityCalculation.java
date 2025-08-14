package com.exavalu.iib.analyzer.sourcefilescores;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

public class ComplexityCalculation {

	/* Call GetRefDetailsToCalculateComplexity Store Procedure */
	public StoredProcedureQuery callGetRefDetailsToCalculateComplexityStoreProcedure(String jobName,
			EntityManager entityManager) {
		var query = entityManager.createStoredProcedureQuery("GetRefDetailsToCalculateComplexity");
		query.registerStoredProcedureParameter("jobName", String.class, ParameterMode.IN);
		query.setParameter("jobName", jobName);
		return query;
	}

	/* Call GetSourceFilesOrchestrationDetails Store Procedure */
	public StoredProcedureQuery callGetSourceFilesOrchestrationDetailsStoreProcedure(String masterIdLike,
			EntityManager entityManager) {
		var query = entityManager.createStoredProcedureQuery("GetSourceFilesOrchestrationDetails");
		query.registerStoredProcedureParameter("masterId", String.class, ParameterMode.IN);
		query.setParameter("masterId", masterIdLike);
		return query;
	}

	/*
	 * Calculate nodes, connectors, transform nodes, loc, loop, message routes,
	 * schemas weight
	 */
	public int calculateWeight(String jsonString, int propertyCount) throws Exception {
		int weightScore = 0;
		Gson gson = new Gson();
		JsonObject refWeight = new JsonObject();
		refWeight = gson.fromJson(jsonString, refWeight.getClass());
		if (propertyCount > 0) {
			if (propertyCount <= refWeight.get("maxLimit").getAsInt()) {
				return weightScore = mesureWeight(refWeight, propertyCount);
			} else {
				while (propertyCount > refWeight.get("maxLimit").getAsInt()) {
					propertyCount = propertyCount - refWeight.get("maxLimit").getAsInt();
					weightScore = weightScore
							+ calculateWeight(refWeight.toString(), refWeight.get("maxLimit").getAsInt());
				}
				weightScore = weightScore + calculateWeight(refWeight.toString(), propertyCount);
			}
		}

		return weightScore;
	}

	/* calculating weight based on reference values */
	public int mesureWeight(JsonObject refWeight, int propertyCount) throws Exception {
		int weightScore = 0;

		for (int counter = 0; counter < refWeight.getAsJsonArray("weightInfo").size(); counter++) {
			if (propertyCount >= refWeight.getAsJsonArray("weightInfo").get(counter).getAsJsonObject().get("min")
					.getAsInt()
					&& propertyCount <= refWeight.getAsJsonArray("weightInfo").get(counter).getAsJsonObject().get("max")
							.getAsInt()) {
				return weightScore = refWeight.getAsJsonArray("weightInfo").get(counter).getAsJsonObject().get("weight")
						.getAsInt();
			}
		}
		return weightScore;
	}

	/* Calculate Method Weight */
	public int calculateWeight(String jsonString, String propertyValue) throws Exception {
		int weightScore = 0;
		Gson gson = new Gson();
		JsonObject refWeight = new JsonObject();
		refWeight = gson.fromJson(jsonString, refWeight.getClass());

		for (int counter = 0; counter < refWeight.getAsJsonArray("weightInfo").size(); counter++) {
			if (refWeight.getAsJsonArray("weightInfo").get(counter).getAsJsonObject().get("type").getAsString()
					.equals(propertyValue))
				return weightScore = refWeight.getAsJsonArray("weightInfo").get(counter).getAsJsonObject().get("weight")
						.getAsInt();
		}
		return weightScore;
	}

	/* Calculate total weight score */
	public int calculateTotalScore(JsonObject objectOfEnpoint) throws Exception {
		int totalScore = 0;
		totalScore = objectOfEnpoint.get("endpointScore").getAsInt() + objectOfEnpoint.get("methodScore").getAsInt() + objectOfEnpoint.get("nodeCountScore").getAsInt()
				+ objectOfEnpoint.get("connectorsCountScore").getAsInt()
				+ objectOfEnpoint.get("transformNodeCountScore").getAsInt()
				+ objectOfEnpoint.get("transformLoCCountScore").getAsInt()
				+ objectOfEnpoint.get("transformLoopCountScore").getAsInt()
				+ objectOfEnpoint.get("messageRoutingPathCountScore").getAsInt()
				+ objectOfEnpoint.get("schemaCountScore").getAsInt();
		return totalScore;
	}

	/* Calculate Estimated effort time in hours */
	public int calculateEffortTime(List<Object[]> resultSetOfEffortEstimation, int totalScore) throws Exception {
		Object[] effortEstimationInfo = null;
		for (int rowCount = 0; rowCount < resultSetOfEffortEstimation.size(); rowCount++) {
			effortEstimationInfo = resultSetOfEffortEstimation.get(rowCount);
			if (totalScore >= (int) effortEstimationInfo[0] && totalScore <= (int) effortEstimationInfo[1]) {
				return (int) effortEstimationInfo[3];
			}
		}
		return (int) effortEstimationInfo[3]
				+ calculateEffortTime(resultSetOfEffortEstimation, (totalScore - (int) effortEstimationInfo[1]));
	}

	/* Calculate Complexity Level */
	public String findComplexityLevel(List<Object[]> resultSetOfEffortEstimation, int totalScore) throws Exception {
		Object[] effortEstimationInfo = null;
		for (int rowCount = 0; rowCount < resultSetOfEffortEstimation.size(); rowCount++) {
			effortEstimationInfo = resultSetOfEffortEstimation.get(rowCount);
			if (totalScore >= (int) effortEstimationInfo[0] && totalScore <= (int) effortEstimationInfo[1])
				return (String) effortEstimationInfo[2];
		}
		return (String) effortEstimationInfo[2];
	}

	/* Calculation at Application level */
	public int calculateScoreForApp(JsonArray arrayOfEndpoints, String fieldName) throws Exception {
		int totalScore = 0;
		for (int endpointIndex = 0; endpointIndex < arrayOfEndpoints.size(); endpointIndex++) {
			totalScore = totalScore + arrayOfEndpoints.get(endpointIndex).getAsJsonObject().get(fieldName).getAsInt();
		}
		return totalScore;
	}

}

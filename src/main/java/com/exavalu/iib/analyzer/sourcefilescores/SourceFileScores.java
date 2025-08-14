package com.exavalu.iib.analyzer.sourcefilescores;

import java.util.List;
import java.util.Map;

import org.hibernate.internal.build.AllowSysOut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class SourceFileScores {
	private static final Logger log = LoggerFactory.getLogger(SourceFileScores.class);
	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@PostMapping("/source-file-scores")
	public String fetchSourceFileDetails(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> requestParams,
			@RequestBody SourceFileScoresContainer requestBody) throws Exception {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/source-file-scores endpoint invoked.");
		}

		String finalResponseObjectAsString = "";
		ComplexityCalculation complexityCalculation = new ComplexityCalculation();

		List<Object[]> resultsetOfRefWeight = null;
		List<Object[]> resultSetOfEffortEstimation = null;
		var query = complexityCalculation.callGetRefDetailsToCalculateComplexityStoreProcedure(requestBody.getJobName(),
				entityManager);
		query.execute();
		var resultSetIndex = 0;
		/* Assign result set to list */
		do {
			var resultSet = query.getResultStream();
			switch (resultSetIndex) {
			case 0 -> resultsetOfRefWeight = resultSet.toList();
			case 1 -> resultSetOfEffortEstimation = resultSet.toList();
			}
			resultSetIndex++;
		} while (query.hasMoreResults());

		/* Start build the success response */

		finalResponseObjectAsString = buildSuccessResponse(complexityCalculation, requestBody, resultsetOfRefWeight,
				resultSetOfEffortEstimation).toString();

		return finalResponseObjectAsString;
	}

	/* Build final response which contains scores of every components */
	@SuppressWarnings("unchecked")
	public JsonObject buildSuccessResponse(ComplexityCalculation complexityCalculation,
			SourceFileScoresContainer requestBody, List<Object[]> resultsetOfRefWeight,
			List<Object[]> resultSetOfEffortEstimation) throws Exception {
		
		JsonObject finalResponseJsonObject = new JsonObject();
		finalResponseJsonObject.addProperty("jobId", requestBody.getJobId());
		finalResponseJsonObject.addProperty("jobName", requestBody.getJobName());
		JsonArray arrayOfSubJobs = new JsonArray();
		for (int subJob = 0; subJob < requestBody.getSubJobs().size(); subJob++) {
			JsonObject objectOfSubJob = new JsonObject();
			objectOfSubJob.addProperty("subJobName", requestBody.getSubJobs().get(subJob).getSubJobName());
			String masterIdLike = requestBody.getJobId() + "_" + requestBody.getSubJobs().get(subJob).getSubJobId();
			var query = complexityCalculation.callGetSourceFilesOrchestrationDetailsStoreProcedure(masterIdLike,
					entityManager);
			query.execute();
			var resultSetIndex = 0;
			List<Object[]> resultSetOfSourceFilesDetails = null;
			List<Object[]> resultSetOfSourceFilesOrchestrationDetails = null;
			/* Assign result set to list */
			do {
				var resultSet = query.getResultStream();
				switch (resultSetIndex) {
				case 0 -> resultSetOfSourceFilesDetails = resultSet.toList();
				case 1 -> resultSetOfSourceFilesOrchestrationDetails = resultSet.toList();
				}
				resultSetIndex++;
			} while (query.hasMoreResults());
			JsonArray arrayOfSourceFiles = new JsonArray();
			if (!resultSetOfSourceFilesDetails.isEmpty() && !resultSetOfSourceFilesOrchestrationDetails.isEmpty()) {

				for (int sourceFileCount = 0; sourceFileCount < resultSetOfSourceFilesDetails
						.size(); sourceFileCount++) {

					var sourceFileinfo = resultSetOfSourceFilesDetails.get(sourceFileCount);
					for (int requestSourceFiles = 0; requestSourceFiles < requestBody.getSubJobs().get(subJob)
							.getSourceFiles().size(); requestSourceFiles++) {
						String sourceFileMaterId = requestBody.getJobId() + "_"
								+ requestBody.getSubJobs().get(subJob).getSubJobId() + "_" + requestBody.getSubJobs()
										.get(subJob).getSourceFiles().get(requestSourceFiles).getSourceFileId();
						if (sourceFileMaterId.equals((String) sourceFileinfo[0])) {
							JsonObject objectOfSourceFile = new JsonObject();

							objectOfSourceFile.addProperty("sourceFileName", sourceFileinfo[3].toString());
							objectOfSourceFile.addProperty("applicationMasterId", sourceFileinfo[0].toString());
							objectOfSourceFile.addProperty("applicationName", sourceFileinfo[1].toString());
							objectOfSourceFile.addProperty("applicationType",
									sourceFileinfo[2].toString().replaceAll("[\\[\\]{}]", ""));
							JsonArray arrayOfEndpoints = new JsonArray();
							for (int endpointCount = 0; endpointCount < resultSetOfSourceFilesOrchestrationDetails
									.size(); endpointCount++) {
								var endpointDetails = resultSetOfSourceFilesOrchestrationDetails.get(endpointCount);
								if (sourceFileinfo[0].equals(endpointDetails[0])) {

									// Skip endpoint type for common nodes
									if (endpointDetails[1].equals("common nodes")) {
										arrayOfEndpoints
												.add(buildEndpointInformation(complexityCalculation, endpointDetails,
														resultsetOfRefWeight, resultSetOfEffortEstimation));

										objectOfSourceFile.add("epComplexityDetails", arrayOfEndpoints);
									} else {
										arrayOfEndpoints.add(buildEndpointInformation(complexityCalculation,
												endpointDetails, resultsetOfRefWeight, resultSetOfEffortEstimation));
										objectOfSourceFile.add("epComplexityDetails", arrayOfEndpoints);
									}

								}
							}
							objectOfSourceFile.add("epComplexityDetails", arrayOfEndpoints);
							objectOfSourceFile.add("appComplexityDetails",
									buildApplicationInformation(complexityCalculation,
											objectOfSourceFile.getAsJsonArray("epComplexityDetails"),
											resultSetOfEffortEstimation));
							arrayOfSourceFiles.add(objectOfSourceFile);
							break;
						}
					}
				}
				objectOfSubJob.add("sourceFilesComplexityDetails", arrayOfSourceFiles);
				arrayOfSubJobs.add(objectOfSubJob);
			}
		}
		finalResponseJsonObject.add("subJobs", arrayOfSubJobs);
		return finalResponseJsonObject;
	}

	/* Build endpoint information */
	public JsonObject buildEndpointInformation(ComplexityCalculation complexityCalculation, Object[] endpointInfo,
			List<Object[]> resultsetOfRefWeight, List<Object[]> resultSetOfEffortEstimation)
			throws Exception {
		var refWeightInfo = resultsetOfRefWeight.get(0);
		JsonObject objectOfEndpoint = new JsonObject();
		objectOfEndpoint.addProperty("method", (String) endpointInfo[2]);
		objectOfEndpoint.addProperty("methodScore",
				complexityCalculation.calculateWeight(refWeightInfo[1].toString(), (String) endpointInfo[2]));
		objectOfEndpoint.addProperty("endpoint", (String) endpointInfo[1]);
		// COMMON_IIB_NODES type handle
		if (objectOfEndpoint.get("endpoint").getAsString().equals("COMMON_IIB_NODES")) {
			objectOfEndpoint.addProperty("endpointScore", 0);
		} else {
			objectOfEndpoint.addProperty("endpointScore",
					complexityCalculation.calculateWeight(refWeightInfo[0].toString(), (String) endpointInfo[11]));
			// Others endpoint type handle separately.
			if (objectOfEndpoint.get("endpointScore").getAsInt() == 0) {
				objectOfEndpoint.addProperty("endpointScore", 1);
			}
		}

		objectOfEndpoint.addProperty("nodeCount", (int) endpointInfo[5]);
		objectOfEndpoint.addProperty("nodeCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[2].toString(), (int) endpointInfo[5]));
		objectOfEndpoint.addProperty("connectorsCount", (int) endpointInfo[4]);
		objectOfEndpoint.addProperty("connectorsCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[3].toString(), (int) endpointInfo[4]));
		objectOfEndpoint.addProperty("transformNodeCount", (int) endpointInfo[6]);
		objectOfEndpoint.addProperty("transformNodeCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[4].toString(), (int) endpointInfo[6]));
		objectOfEndpoint.addProperty("transformLoCCount", (int) endpointInfo[7]);
		objectOfEndpoint.addProperty("transformLoCCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[6].toString(), (int) endpointInfo[7]));
		objectOfEndpoint.addProperty("transformLoopCount", (int) endpointInfo[8]);
		objectOfEndpoint.addProperty("transformLoopCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[7].toString(), (int) endpointInfo[8]));
		objectOfEndpoint.addProperty("messageRoutingPathCount", (int) endpointInfo[9]);
		objectOfEndpoint.addProperty("messageRoutingPathCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[8].toString(), (int) endpointInfo[9]));
		objectOfEndpoint.addProperty("schemaCount", (int) endpointInfo[10]);
		objectOfEndpoint.addProperty("schemaCountScore",
				complexityCalculation.calculateWeight(refWeightInfo[9].toString(), (int) endpointInfo[10]));
		objectOfEndpoint.addProperty("endpointTotalScore", complexityCalculation.calculateTotalScore(objectOfEndpoint));
		objectOfEndpoint.addProperty("complexityLevel", complexityCalculation.findComplexityLevel(
				resultSetOfEffortEstimation, objectOfEndpoint.get("endpointTotalScore").getAsInt()));
		objectOfEndpoint.addProperty("estimatedHours", complexityCalculation.calculateEffortTime(
				resultSetOfEffortEstimation, objectOfEndpoint.get("endpointTotalScore").getAsInt()));
		return objectOfEndpoint;
		

	}

	/* Build Application information */
	public JsonObject buildApplicationInformation(ComplexityCalculation complexityCalculation,
			JsonArray arrayOfEndpoints, List<Object[]> resultSetOfEffortEstimation) throws Exception {
		JsonObject objectOfApplication = new JsonObject();
		objectOfApplication.addProperty("endpointCount", arrayOfEndpoints.size());
		objectOfApplication.addProperty("nodeCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "nodeCount"));
		objectOfApplication.addProperty("connectorsCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "connectorsCount"));
		objectOfApplication.addProperty("messageRoutingPathCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "messageRoutingPathCount"));
		objectOfApplication.addProperty("transformLoCCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "transformLoCCount"));
		objectOfApplication.addProperty("transformLoopCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "transformLoopCount"));
		objectOfApplication.addProperty("transformNodeCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "transformNodeCount"));
		objectOfApplication.addProperty("schemaCount",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "schemaCount"));
		objectOfApplication.addProperty("totalScore",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "endpointTotalScore"));
		objectOfApplication.addProperty("complexityLevel", complexityCalculation
				.findComplexityLevel(resultSetOfEffortEstimation, objectOfApplication.get("totalScore").getAsInt()));
		objectOfApplication.addProperty("estimatedHours",
				complexityCalculation.calculateScoreForApp(arrayOfEndpoints, "estimatedHours"));

		return objectOfApplication;
	}
}

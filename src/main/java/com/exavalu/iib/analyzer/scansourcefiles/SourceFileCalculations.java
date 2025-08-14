package com.exavalu.iib.analyzer.scansourcefiles;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import com.exavalu.iib.analyzer.feature.EndpointMethodFinder;
import com.exavalu.iib.analyzer.feature.MessageEnrichment;
import com.exavalu.iib.analyzer.feature.SoapProjectCalculations;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import jakarta.persistence.EntityManager;

public class SourceFileCalculations {
	public boolean nodesComplexity(JsonArray connectedNodes, JsonObject sourceFilePathsJsonObject,
			List<String> applicationTypes, String user_name, String sourceFileMasterId, String sourceFileName,
			EntityManager entityManager) {
		boolean nodesComplexityStatus = true;
		int commonNodeCount = 0, commonConnectorCount = 0, commonTransformationCount = 0, commonTransformationLoc = 0,
				commonTransformationLoops = 0, commonSchemaCount = 0, commonRoutes = 0;
		JsonArray anyDetails = new JsonArray();
		ScanJobsDbOperations dbOperation = new ScanJobsDbOperations();
		int listenerSchemaCount = 0;
		Gson gsonObj = new Gson();
		boolean dbSuccess = false;
		String applicationType = "";
		int endpointCount = 0;

		for (int mainNodesItr = 0; mainNodesItr < connectedNodes.size(); mainNodesItr++) {

			if (!(mainNodesItr >= applicationTypes.size())) {
				applicationType = applicationTypes.get(mainNodesItr);
			} else if (!applicationType.trim().equals("REST API")) {
				applicationType = "";
			}

			if (applicationType.trim().equals("REST API")) {
				anyDetails = EndpointMethodFinder.fetchMethodRest(sourceFilePathsJsonObject);
			} else if (applicationType.trim().equals("SOAP")) {
				anyDetails = EndpointMethodFinder.fetchMethodSOAP(sourceFilePathsJsonObject);
			} else if (applicationType.trim().equals("HTTP")) {
				anyDetails = EndpointMethodFinder.fetchMethodHttp(sourceFilePathsJsonObject);
			}
			// Added Common Condition to get details - 30/09/2024
			else {
				if (getKeyByValue(applicationType.trim()) != null) {
					anyDetails = EndpointMethodFinder.fetchMethodEndpoints(sourceFilePathsJsonObject,
							applicationType.trim(), getKeyByValue(applicationType.trim()),
							applicationType.trim() + " Endpoint",
							AppGlobalDeclaration.iibInputEndPointMapping.get(applicationType.trim()));
				}

			}

			if (applicationType.trim().equals("REST API") && mainNodesItr == 0)
				endpointCount = connectedNodes.get(0).getAsJsonArray().size();

			commonNodeCount = 0;
			commonConnectorCount = 0;
			commonTransformationCount = 0;
			commonTransformationLoc = 0;
			commonTransformationLoops = 0;
			listenerSchemaCount = 0;
			commonSchemaCount = 0;
			String commonUrl = "", commonMethod = "";
			SoapProjectCalculations soapPrjCalc = new SoapProjectCalculations();
			// *** Calculate complexity for common nodes *** //
			if (connectedNodes.get(mainNodesItr).getAsJsonArray().get(0).getAsJsonObject()
					.get("endpointName") == null) {

				if (applicationType.trim().equals("REST API"))
					commonRoutes = AppGlobalDeclaration.commonRoutesArr.get(0);
				else
					commonRoutes = AppGlobalDeclaration.commonRoutesArr.get(mainNodesItr);

				for (int internalNodesItr = 0; internalNodesItr < connectedNodes.get(mainNodesItr).getAsJsonArray()
						.size(); internalNodesItr++) {
					commonNodeCount++;
					JsonObject nodeObject = connectedNodes.get(mainNodesItr).getAsJsonArray().get(internalNodesItr)
							.getAsJsonObject();

					// Added Schema Count logic 01-10-2024
					if (nodeObject.get("schemaPrefix") != null) {
						commonSchemaCount++;
					}

					String nodeType = nodeObject.get("type").getAsString().substring(0,
							nodeObject.get("type").getAsString().indexOf(":"));

					if (applicationType.trim().equals("SOAP")) {
						if (nodeType.equals("ComIbmSOAPInput.msgnode")) {
							String wsdlFileName = nodeObject.get("wsdlFileName").getAsString();
							String targetNamespace = nodeObject.get("targetNamespace").getAsString();
							listenerSchemaCount = soapPrjCalc.wsdlCounter(wsdlFileName, targetNamespace,
									sourceFilePathsJsonObject, 0);
						} else if (nodeType.equals("ComIbmSOAPRequest.msgnode")
								|| nodeType.equals("ComIbmSOAPAsyncRequest.msgnode")) {
							String wsdlFileName = nodeObject.get("wsdlFileName").getAsString();
							String targetNamespace = nodeObject.get("targetNamespace").getAsString();
							commonSchemaCount += soapPrjCalc.wsdlCounter(wsdlFileName, targetNamespace,
									sourceFilePathsJsonObject, 0);
						}
					}

					if (Arrays.stream(AppGlobalDeclaration.preDefineIIBConnectors).anyMatch(nodeType::equals)) {
						/*
						 * Keep this for reference String value =
						 * Arrays.stream(AppGlobalDeclaration.preDefineIIBConnectors)
						 * .filter(nodeType::equals).findFirst().get();
						 */
						commonConnectorCount++;
					}

					if (Arrays.stream(AppGlobalDeclaration.preDefineIIBTransformations).anyMatch(nodeType::equals)) {
						commonTransformationCount++;
						MessageEnrichment messageEnrichment = new MessageEnrichment();
						JsonObject transformNodeJsonObj = messageEnrichment.transformNodeCalculations(nodeObject,
								sourceFilePathsJsonObject);

						commonTransformationLoc += transformNodeJsonObj.get("transformNodeLoc").getAsInt();
						commonTransformationLoops += transformNodeJsonObj.get("loopsCount").getAsInt();
					}

					// Create Common Logic to set commoUrl and commonMethod- 30/09/2024
					if (anyDetails != null) {
						List<String> attributeListBasedOnApplicationtype = AppGlobalDeclaration.iibInputEndPointMapping
								.get(applicationType.trim());
						for (int epItr = 0; epItr < anyDetails.size(); epItr++) {
							if (attributeListBasedOnApplicationtype != null) {
								for (String attributeName : attributeListBasedOnApplicationtype) {
									if (nodeObject.get(attributeName) != null) {
										if (nodeObject.get(attributeName).getAsString().replace("%MQSI_DELIMITER%", ",")
												.equals(anyDetails.get(epItr).getAsJsonObject().get("Endpoint")
														.getAsString())) {
											commonUrl = anyDetails.get(epItr).getAsJsonObject().get("Endpoint")
													.getAsString();
											commonMethod = anyDetails.get(epItr).getAsJsonObject()
													.get("Endpoint_Method").getAsString();
										}
									}else if(anyDetails.get(epItr).getAsJsonObject().get("Endpoint").getAsString().equals("Scheduler") && nodeObject.get(attributeName) == null ) {
										commonUrl = anyDetails.get(epItr).getAsJsonObject().get("Endpoint").getAsString();
										commonMethod = anyDetails.get(epItr).getAsJsonObject().get("Endpoint_Method").getAsString();
									}

								}
							}

						}

					}

					if (applicationType.isBlank() || applicationType.trim().equals("REST API")) {
						commonUrl = AppGlobalDeclaration.COMMON_NODE_URL;
						commonMethod = AppGlobalDeclaration.COMMON_METHOD;
					}

				}

				commonSchemaCount = commonSchemaCount + listenerSchemaCount;

				if (endpointCount != 1) {
					if (commonUrl.trim().equals("COMMON_IIB_NODES"))
						dbSuccess = dbOperation.saveMessageOrchestrationDetails(sourceFileMasterId, sourceFileName,
								commonUrl, "N/A", commonMethod, listenerSchemaCount, commonConnectorCount,
								commonNodeCount, commonTransformationCount, commonTransformationLoc,
								commonTransformationLoops, commonRoutes, commonSchemaCount, user_name, entityManager);
					else if (applicationType.trim().equals("REST API"))
						dbSuccess = dbOperation.saveMessageOrchestrationDetails(sourceFileMasterId, sourceFileName,
								commonUrl, applicationType.trim().substring(0, 4), commonMethod, listenerSchemaCount,
								commonConnectorCount, commonNodeCount, commonTransformationCount,
								commonTransformationLoc, commonTransformationLoops, commonRoutes, commonSchemaCount,
								user_name, entityManager);
					else
						dbSuccess = dbOperation.saveMessageOrchestrationDetails(sourceFileMasterId, sourceFileName,
								commonUrl, applicationType.trim(), commonMethod, listenerSchemaCount,
								commonConnectorCount, commonNodeCount, commonTransformationCount,
								commonTransformationLoc, commonTransformationLoops, commonRoutes, commonSchemaCount,
								user_name, entityManager);

					if (!dbSuccess)
						nodesComplexityStatus = false;
				}

			}
		}
		// *** Calculate complexity for each endpoint nodes *** //
		for (int mainNodesItr = 0; mainNodesItr < connectedNodes.size(); mainNodesItr++) {
			if (connectedNodes.get(mainNodesItr).getAsJsonArray().get(0).getAsJsonObject()
					.get("endpointName") != null) {

				for (int endpointsItr = 0; endpointsItr < connectedNodes.get(mainNodesItr).getAsJsonArray()
						.size(); endpointsItr++) {
					int endpointNodeCount = 0, endpointConnectorCount = 0, endpointTransformationCount = 0,
							endpointTransformationLoops = 0, endpointTransformationLoc = 0, endpointSchemaCount = 0;
					int endpointRoutes = AppGlobalDeclaration.endpointRoutesArr.get(endpointsItr);
					String endpointLabelName = connectedNodes.get(mainNodesItr).getAsJsonArray().get(endpointsItr)
							.getAsJsonObject().get("endpointName").getAsString();
					JsonArray endpointNodesArr = connectedNodes.get(mainNodesItr).getAsJsonArray().get(endpointsItr)
							.getAsJsonObject().getAsJsonArray("endpointNodes");

					for (int endpointNodesItr = 0; endpointNodesItr < endpointNodesArr.size(); endpointNodesItr++) {
						endpointNodeCount++;
						JsonObject nodeObject = endpointNodesArr.get(endpointNodesItr).getAsJsonObject();
						String nodeType = nodeObject.get("type").getAsString().substring(0,
								nodeObject.get("type").getAsString().indexOf(":"));

						if (Arrays.stream(AppGlobalDeclaration.preDefineIIBConnectors).anyMatch(nodeType::equals)) {
							endpointConnectorCount++;
						}

						if (Arrays.stream(AppGlobalDeclaration.preDefineIIBTransformations)
								.anyMatch(nodeType::equals)) {
							endpointTransformationCount++;

							MessageEnrichment messageEnrichment = new MessageEnrichment();
							JsonObject transformNodeJsonObj = messageEnrichment.transformNodeCalculations(nodeObject,
									sourceFilePathsJsonObject);

							endpointTransformationLoc += transformNodeJsonObj.get("transformNodeLoc").getAsInt();
							endpointTransformationLoops += transformNodeJsonObj.get("loopsCount").getAsInt();
						}
					}

					// *** Add common counts to individual endpoints if there's only one endpoint
					// *** //

					if (endpointCount == 1) {
						endpointNodeCount = endpointNodeCount + commonNodeCount;
						endpointConnectorCount = endpointConnectorCount + commonConnectorCount;
						endpointTransformationCount = endpointTransformationCount + commonTransformationCount;

						endpointTransformationLoc = endpointTransformationLoc + commonTransformationLoc;
						endpointTransformationLoops = endpointTransformationLoops + commonTransformationLoops;
					}

					String endpointUrl = "", endpointMethod = "";
					for (int epItr = 0; epItr < anyDetails.size(); epItr++) {
						for (int opIdItr = 0; opIdItr < anyDetails.get(epItr).getAsJsonObject()
								.getAsJsonArray("OperationId").size(); opIdItr++) {
							if (anyDetails.get(epItr).getAsJsonObject().getAsJsonArray("OperationId").get(opIdItr)
									.getAsString().contains(endpointLabelName)) {

								if (anyDetails.get(epItr).getAsJsonObject().get("Endpoint").getAsString()
										.endsWith("/")) {
									endpointUrl = anyDetails.get(epItr).getAsJsonObject().get("Endpoint").getAsString()
											+ anyDetails.get(epItr).getAsJsonObject().getAsJsonArray("OperationId")
													.get(opIdItr).getAsString();
								} else {
									endpointUrl = anyDetails.get(epItr).getAsJsonObject().get("Endpoint").getAsString()
											+ "/" + anyDetails.get(epItr).getAsJsonObject()
													.getAsJsonArray("OperationId").get(opIdItr).getAsString();
								}

								List<String> applicationTypesList = gsonObj.fromJson(
										anyDetails.get(epItr).getAsJsonObject().get("Endpoint_Method").getAsString(),
										new TypeToken<List<String>>() {
										}.getType());

								endpointMethod = applicationTypesList.get(opIdItr).toUpperCase();
							}
						}

					}
					// *** Database Operation *** //
					if (applicationType.trim().equals("REST API"))
						dbSuccess = dbOperation.saveMessageOrchestrationDetails(sourceFileMasterId, sourceFileName,
								endpointUrl, applicationType.trim().substring(0, 4), endpointMethod,
								chekingScores(listenerSchemaCount), chekingScores(endpointConnectorCount),
								chekingScores(endpointNodeCount), chekingScores(endpointTransformationCount),
								chekingScores(endpointTransformationLoc), chekingScores(endpointTransformationLoops),
								chekingScores(endpointRoutes), chekingScores(endpointSchemaCount), user_name,
								entityManager);
					else
						dbSuccess = dbOperation.saveMessageOrchestrationDetails(sourceFileMasterId, sourceFileName,
								endpointUrl, applicationType.trim(), endpointMethod, chekingScores(listenerSchemaCount),
								chekingScores(endpointConnectorCount), chekingScores(endpointNodeCount),
								chekingScores(endpointTransformationCount), chekingScores(endpointTransformationLoc),
								chekingScores(endpointTransformationLoops), chekingScores(endpointRoutes),
								chekingScores(endpointSchemaCount), user_name, entityManager);

					if (!dbSuccess)
						nodesComplexityStatus = false;
				}
			}
		}

		return nodesComplexityStatus;
	}

	// Checking score is a positive number or not
	public int chekingScores(int score) {
		if (score < 0)
			return 0;
		return score;
	}

	// Added Function to get Map Key from Map Value 30-09-24
	private String getKeyByValue(String value) {
		return AppGlobalDeclaration.iibInputXmiTypeMap.entrySet().stream()
				.filter(entry -> entry.getValue().equals(value)).map(Map.Entry::getKey).findFirst().orElse(null);
	}

}

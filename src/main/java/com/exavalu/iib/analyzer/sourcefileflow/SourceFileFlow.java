package com.exavalu.iib.analyzer.sourcefileflow;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.scansourcefiles.ScanJobsDbOperations;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class SourceFileFlow {
	private static final Logger log = LoggerFactory.getLogger(SourceFileFlow.class);
	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("unchecked")
	@GetMapping("/source-file-flow")
	public String retrieveConnectionTree(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> requestParams) {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/source-file-flow endpoint invoked.");
		}

		String finalResponseObjectAsString = "";
		SourceFileFlowContainer container = new SourceFileFlowContainer();
		container.setApplicationMasterId(requestParams.get("source_file_id"));

		String selectQueryTofetchConnectionTree = "SELECT source_file_name, connection_tree, nodes FROM message_orchestration_trees WHERE source_file_master_id= :sourceFleMasterId";
		Query query = entityManager.createNativeQuery(selectQueryTofetchConnectionTree)
				.setParameter("sourceFleMasterId", container.getApplicationMasterId());
		List<Object[]> resultSet = query.getResultList();
		var resultIndex = resultSet.get(0);

		Gson gson = new Gson();
		JsonArray connectionTree = new JsonArray();
		connectionTree = gson.fromJson(resultIndex[1].toString(), connectionTree.getClass());

		JsonArray nodesArray = new JsonArray();
		nodesArray = gson.fromJson(resultIndex[2].toString(), nodesArray.getClass());

		ScanJobsDbOperations dbOperation = new ScanJobsDbOperations();
		JsonObject sourceFilePathsJsonObject = dbOperation.getSourceFilePathDetails(container.getApplicationMasterId(),
				entityManager);

		List<String> applicationTypes = dbOperation.getApplicationType(container.getApplicationMasterId(),
				entityManager);

		// *** Start building nodes and edges for visual representation *** //

		JsonArray nodes = new JsonArray();
		JsonArray edges = new JsonArray();

		JsonObject nodesAndEdgesObject = new JsonObject();
		nodesAndEdgesObject.addProperty("applicationName", resultIndex[0].toString());
		nodesAndEdgesObject.add("nodes", nodes);
		nodesAndEdgesObject.add("edges", edges);

		this.nodesAndEdgesBuilder(connectionTree, nodesAndEdgesObject, nodesArray, sourceFilePathsJsonObject,
				applicationTypes, true);

		finalResponseObjectAsString = nodesAndEdgesObject.toString();
		// System.out.println("connectionTree: " + connectionTree);
		// System.out.println("nodesArray: "+ nodesArray);
		// System.out.println("finalConnection: "+nodesAndEdgesObject);
		return finalResponseObjectAsString;
	}

	public JsonObject nodesAndEdgesBuilder(JsonArray connectionTree, JsonObject nodesAndEdgesObject,
			JsonArray nodesArray, JsonObject sourceFilePathsJsonObject, List<String> applicationTypes,
			boolean mainCall) {

		JsonArray nodes = nodesAndEdgesObject.getAsJsonArray("nodes");
		JsonArray edges = nodesAndEdgesObject.getAsJsonArray("edges");

		String applicationType = "";

		for (int mainConnTreeItr = 0; mainConnTreeItr < connectionTree.size(); mainConnTreeItr++) {

			if (mainCall)
				applicationType = applicationTypes.get(mainConnTreeItr);

			for (int internalConnTreeItr = 0; internalConnTreeItr < connectionTree.get(mainConnTreeItr).getAsJsonArray()
					.size(); internalConnTreeItr++) {

				if (connectionTree.get(mainConnTreeItr).getAsJsonArray().get(internalConnTreeItr).getClass().toString()
						.contains("JsonObject")) {

					JsonObject connObject = connectionTree.get(mainConnTreeItr).getAsJsonArray()
							.get(internalConnTreeItr).getAsJsonObject();
					JsonObject nodeObject = new JsonObject();

					String sourceId = connObject.get("sourceFolder").getAsString() + ":"
							+ connObject.get("flowName").getAsString() + ":"
							+ connObject.get("sourceNode").getAsString();

					nodeObject.addProperty("id", sourceId);
					nodeObject.addProperty("label", connObject.get("sourceLabel").getAsString());

					if (!hasValue(nodes, "id", sourceId)) {
						nodes.add(nodeObject);
					}

					nodeObject = new JsonObject();

					String targetId = connObject.get("sourceFolder").getAsString() + ":"
							+ connObject.get("flowName").getAsString() + ":"
							+ connObject.get("targetNode").getAsString();

					nodeObject.addProperty("id", targetId);
					nodeObject.addProperty("label", connObject.get("targetLabel").getAsString());

					if (!hasValue(nodes, "id", targetId)) {
						nodes.add(nodeObject);
					}

					JsonArray edgeArray = new JsonArray();
					edgeArray.add(sourceId);
					edgeArray.add(targetId);

					if (!hasValue(edges, sourceId, targetId))
						edges.add(edgeArray);

					/*
					 * Adding an edge to connect subflow ref, esql propagate, callable to their
					 * inputs Also connect route to label to labels for REST apis
					 */
					for (int nodesItr = 0; nodesItr < nodesArray.size(); nodesItr++) {

						boolean flowFound = false;

						if (nodesArray.get(nodesItr).getAsJsonArray().get(0).getAsJsonObject().get("flowName")
								.getAsString().equals(connObject.get("flowName").getAsString())) {

							flowFound = true;

							for (int flowNodeItr = 0; flowNodeItr < nodesArray.get(nodesItr).getAsJsonArray()
									.size(); flowNodeItr++) {

								JsonObject nodeObj = nodesArray.get(nodesItr).getAsJsonArray().get(flowNodeItr)
										.getAsJsonObject();
								String nodeType = "";
								String nodeFlowName = nodeObj.get("flowName").getAsString();
								if (nodeObj.get("id").getAsString()
										.equals(connObject.get("targetNode").getAsString())) {
									nodeType = nodeObj.get("type").getAsString();
								}

								if (nodeType.contains(".subflow")) {
									for (int intNodesItr = 0; intNodesItr < nodesArray.size(); intNodesItr++) {

										if (nodeType.contains(nodesArray.get(intNodesItr).getAsJsonArray().get(0)
												.getAsJsonObject().get("flowName").getAsString())) {

											for (int intFlowNodeItr = 0; intFlowNodeItr < nodesArray.get(intNodesItr)
													.getAsJsonArray().size(); intFlowNodeItr++) {

												JsonObject intNodeObj = nodesArray.get(intNodesItr).getAsJsonArray()
														.get(intFlowNodeItr).getAsJsonObject();
												if (intNodeObj.get("id").getAsString().contains(".Input")) {
													sourceId = targetId;
													targetId = intNodeObj.get("sourceFolder").getAsString() + ":"
															+ intNodeObj.get("flowName").getAsString() + ":"
															+ intNodeObj.get("id").getAsString();

													edgeArray = new JsonArray();
													edgeArray.add(sourceId);
													edgeArray.add(targetId);

													if (!hasValue(edges, sourceId, targetId))
														edges.add(edgeArray);
													break;
												}
											}
										}
									}
								}

								if (nodeType.contains("ComIbmCompute")) {
									int index1 = nodeObj.get("computeExpression").getAsString().indexOf("#") + 1;
									int index2 = nodeObj.get("computeExpression").getAsString().lastIndexOf(".");
									String esqlModuleName = nodeObj.get("computeExpression").getAsString()
											.substring(index1, index2);
									String esqlSourceFolder = nodeObj.get("sourceFolder").getAsString();

									addPropagationEdge(esqlModuleName, esqlSourceFolder, sourceFilePathsJsonObject,
											nodesArray, nodeFlowName, targetId, edges);
								}

								if (nodeType.contains("ComIbmCallableFlowInvoke") || nodeType.contains("ComIbmCallableFlowAsyncInvoke")) {
									
									String callableTargetApplication = nodeObj.get("targetApplication").getAsString();
									String endPointName = nodeObj.get("targetEndpointName").getAsString();
									
									for (int intNodesItr = 0; intNodesItr < nodesArray.size(); intNodesItr++) {

										if (nodesArray.get(intNodesItr).getAsJsonArray().get(0).getAsJsonObject().get("sourceFolder").getAsString().equals(callableTargetApplication)) {

											for (int intFlowNodeItr = 0; intFlowNodeItr < nodesArray.get(intNodesItr).getAsJsonArray().size(); intFlowNodeItr++) {

												JsonObject intNodeObj = nodesArray.get(intNodesItr).getAsJsonArray().get(intFlowNodeItr).getAsJsonObject();
												if (intNodeObj.get("type").getAsString().contains("ComIbmCallableFlowInput")) {
													
													String callableEndpoint = intNodeObj.get("callableInputEndpoint").getAsString();
												
													if(endPointName.equalsIgnoreCase(callableEndpoint)) {
														
														sourceId = targetId;
														targetId = intNodeObj.get("sourceFolder").getAsString() + ":"
																+ intNodeObj.get("flowName").getAsString() + ":"
																+ intNodeObj.get("id").getAsString();

														edgeArray = new JsonArray();
														edgeArray.add(sourceId);
														edgeArray.add(targetId);

														if (!hasValue(edges, sourceId, targetId))
															edges.add(edgeArray);

														break;
													}
												}
											}
										}
									}
								}

								if (applicationType.trim().equals("REST API")) {

									if (nodeType.contains("ComIbmRouteToLabel")) {

										for (int intNodesItr = 0; intNodesItr < nodesArray.size(); intNodesItr++) {

											if (nodesArray.get(intNodesItr).getAsJsonArray().get(0).getAsJsonObject()
													.get("flowName").getAsString().equals(nodeFlowName)) {

												for (int flowNodeIterator = 0; flowNodeIterator < nodesArray
														.get(intNodesItr).getAsJsonArray().size(); flowNodeIterator++) {
													JsonObject intNodeObj = nodesArray.get(intNodesItr).getAsJsonArray()
															.get(flowNodeIterator).getAsJsonObject();
													if (intNodeObj.get("type").getAsString().contains("ComIbmLabel")) {

														String epSourceId = targetId;
														String epTargetId = intNodeObj.get("sourceFolder").getAsString()
																+ ":" + intNodeObj.get("flowName").getAsString() + ":"
																+ intNodeObj.get("id").getAsString();

														edgeArray = new JsonArray();
														edgeArray.add(epSourceId);
														edgeArray.add(epTargetId);

														if (!hasValue(edges, epSourceId, epTargetId))
															edges.add(edgeArray);
													}
												}
											}
										}
									}
								}
								if (!nodeType.equals(""))
									break;
							}
						}
						if (flowFound)
							break;
					}
				} else {
					JsonArray subConnectionTree = connectionTree.get(mainConnTreeItr).getAsJsonArray()
							.get(internalConnTreeItr).getAsJsonArray();
					this.nodesAndEdgesBuilder(subConnectionTree, nodesAndEdgesObject, nodesArray,
							sourceFilePathsJsonObject, applicationTypes, false);
				}
			}
		}

		nodesAndEdgesObject.add("nodes", nodes);
		nodesAndEdgesObject.add("edges", edges);

		return nodesAndEdgesObject;

	}

	public JsonArray addPropagationEdge(String esqlModuleName, String esqlSourceFolder,
			JsonObject sourceFilePathsJsonObject, JsonArray nodesArray, String nodeFlowName, String targetId,
			JsonArray edges) {

		if (!sourceFilePathsJsonObject.getAsJsonObject().get("projectDetails").isJsonNull()) {

			String basePath = sourceFilePathsJsonObject.getAsJsonObject().get("projectDetails").getAsJsonObject()
					.get("baseUrl").getAsString();
			JsonArray serviceDeatilsArray = sourceFilePathsJsonObject.getAsJsonObject()
					.getAsJsonObject("projectDetails").getAsJsonArray("serviceDetails");
			JsonArray esqlsJsonArray = new JsonArray();

			for (int serviceArrItr = 0; serviceArrItr < serviceDeatilsArray.size(); serviceArrItr++) {
				if (serviceDeatilsArray.get(serviceArrItr).getAsJsonObject().get("serviceName").getAsString()
						.equals(esqlSourceFolder)) {
					esqlsJsonArray = serviceDeatilsArray.get(serviceArrItr).getAsJsonObject().getAsJsonArray("esql");
				}
			}

			for (int esqlsItr = 0; esqlsItr < esqlsJsonArray.size(); esqlsItr++) {
				boolean moduleExist = false;
				String esqlFilePath = basePath + esqlsJsonArray.get(esqlsItr).getAsString();
				File esqlFile = new File(esqlFilePath);
				try (BufferedReader esqlReader = new BufferedReader(new FileReader(esqlFile))) {
					String line = null;
					while ((line = esqlReader.readLine()) != null) {
						String labelName = "";
						StringBuilder stringBuilder1 = new StringBuilder();
						stringBuilder1.append(line);
						String lineContent = stringBuilder1.toString();

						if (lineContent.contains("CREATE COMPUTE MODULE") && lineContent.contains(esqlModuleName)) {
							moduleExist = true;
							continue;
						}
						if (moduleExist && lineContent.contains("END MODULE")) {
							break;
						}
						if (moduleExist) {
							if (lineContent.contains("PROPAGATE TO LABEL")) {
								labelName = lineContent.substring(lineContent.indexOf("\'") + 1,
										lineContent.lastIndexOf("\'"));
							}

							if (!labelName.isEmpty()) {
								for (int intNodesItr = 0; intNodesItr < nodesArray.size(); intNodesItr++) {

									if (nodesArray.get(intNodesItr).getAsJsonArray().get(0).getAsJsonObject()
											.get("flowName").getAsString().equals(nodeFlowName)) {

										for (int flowNodeIterator = 0; flowNodeIterator < nodesArray.get(intNodesItr)
												.getAsJsonArray().size(); flowNodeIterator++) {
											JsonObject intNodeObj = nodesArray.get(intNodesItr).getAsJsonArray()
													.get(flowNodeIterator).getAsJsonObject();
											if (intNodeObj.get("labelName") != null) {
												if (intNodeObj.get("labelName").getAsString().equals(labelName)) {
													String sourceId = targetId;
													targetId = intNodeObj.get("sourceFolder").getAsString() + ":"
															+ intNodeObj.get("flowName").getAsString() + ":"
															+ intNodeObj.get("id").getAsString();

													JsonArray edgeArray = new JsonArray();
													edgeArray.add(sourceId);
													edgeArray.add(targetId);

													if (!hasValue(edges, sourceId, targetId))
														edges.add(edgeArray);
													break;
												}
											}
										}
									}
								}
							}
						}

					}

					if (moduleExist)
						break;
				} catch (Exception exception) {
					if (AppGlobalDeclaration.isErrorLogEnabled) {
						log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Add Propagation Edge :: "
								+ exception.getMessage());
						if (AppGlobalDeclaration.isStackTraceLogEnabled) {
							exception.getStackTrace();
						}
					}
				}
			}

		}

		return edges;
	}

	public boolean hasValue(JsonArray json, String key, String value) {
		for (int i = 0; i < json.size(); i++) { // iterate through the JsonArray
			// first I get the 'i' JsonElement as a JsonObject, then I get the key as a
			// string and I compare it with the value
			if (json.get(i).getClass().toString().contains("JsonObject")) {
				if (json.get(i).getAsJsonObject().get(key).getAsString().equals(value))
					return true;
			} else if (json.get(i).getClass().toString().contains("JsonPrimitive")) {
				if (json.get(i).getAsString().equals(value))
					return true;
			} else if (json.get(i).getClass().toString().contains("JsonArray")) {
				String value1 = key;
				String value2 = value;
				if (json.get(i).getAsJsonArray().get(0).getAsString().equals(value1)
						&& json.get(i).getAsJsonArray().get(1).getAsString().equals(value2)) {
					return true;
				}
			}
		}
		return false;
	}

}
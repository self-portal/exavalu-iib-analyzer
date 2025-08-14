package com.exavalu.iib.analyzer.orchestration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class MessageOrchestration {
	private static final Logger log = LoggerFactory.getLogger(MessageOrchestration.class);

	public JsonArray routing(JsonArray listAllIIBInputArr, JsonArray allConnectionList, JsonArray allNodeList,
			JsonObject fileUploadRespJsonObj, List<String> applicationTypes) {
		JsonArray allIIBInputJsonArray = new JsonArray();
		if (!listAllIIBInputArr.isEmpty()) {
			// Loop with List of IIB Input Nodes
			for (int eachIIBInput = 0; eachIIBInput < listAllIIBInputArr.size(); eachIIBInput++) {
				String applicationType = applicationTypes.get(eachIIBInput);
				JsonArray connectionsTree = new JsonArray();
				String inputNodeFlowName = listAllIIBInputArr.get(eachIIBInput).getAsJsonObject().get("flowName")
						.getAsString();
				String inputNodeId = listAllIIBInputArr.get(eachIIBInput).getAsJsonObject().get("id").getAsString();

				// Call MessageOrchestration to build the connection tree
				connectionsTree = findMsgFlowConnections(allConnectionList, inputNodeFlowName, inputNodeId, allNodeList,
						fileUploadRespJsonObj, applicationType.trim(), "", "");
				allIIBInputJsonArray.add(connectionsTree);
			}
		}
		return allIIBInputJsonArray;
	}

	// To find connections in msgflow.
	public JsonArray findMsgFlowConnections(JsonArray allConnectionList, String inputNodeFlowName, String inputNodeId,
			JsonArray allNodeList, JsonObject fileUploadRespJsonObj, String applicationType, String nodeIdForConnection,
			String connectionType) {

		JsonArray connectionsTree = new JsonArray();
		JsonArray subFlowConnTree = new JsonArray();
		JsonArray callableFlowConnTree = new JsonArray();
		JsonArray endpointConnTree = new JsonArray();

		for (int connNodeIterator = 0; connNodeIterator < allConnectionList.getAsJsonArray()
				.size(); connNodeIterator++) {
			if (allConnectionList.get(connNodeIterator).getAsJsonArray().get(0).getAsJsonObject().get("flowName")
					.getAsString().equals(inputNodeFlowName)) {

				int msgFlowIndex = connNodeIterator;

				String endpointConnNodeId = "";

				// Add all input node connections first
				for (int flowConnIterator = 0; flowConnIterator < allConnectionList.get(connNodeIterator)
						.getAsJsonArray().size(); flowConnIterator++) {

					String connSourceNodeId = allConnectionList.get(connNodeIterator).getAsJsonArray()
							.get(flowConnIterator).getAsJsonObject().get("sourceNode").getAsString();
					String connTargetNodeid = allConnectionList.get(connNodeIterator).getAsJsonArray()
							.get(flowConnIterator).getAsJsonObject().get("targetNode").getAsString();

					if (connSourceNodeId.equals(inputNodeId)) {
						if (nodeIdForConnection.isEmpty()) {
							for (int flowNodeIterator = 0; flowNodeIterator < allNodeList.get(connNodeIterator)
									.getAsJsonArray().size(); flowNodeIterator++) {
								JsonObject nodeObject = allNodeList.get(connNodeIterator).getAsJsonArray()
										.get(flowNodeIterator).getAsJsonObject();
								if (nodeObject.get("id").getAsString().equals(connTargetNodeid)) {
									if (nodeObject.get("type").getAsString().contains("ComIbmRouteToLabel")) {
										endpointConnNodeId = connTargetNodeid;
									}
								}
							}
							connectionsTree.add(allConnectionList.get(connNodeIterator).getAsJsonArray()
									.get(flowConnIterator).getAsJsonObject());
						} else {
							JsonObject connectionToInsert = allConnectionList.get(connNodeIterator).getAsJsonArray()
									.get(flowConnIterator).getAsJsonObject();
							connectionToInsert.addProperty("connectionType", connectionType);
							connectionToInsert.addProperty("connectionSourceNode", nodeIdForConnection);
							connectionsTree.add(connectionToInsert);
						}

					}
				}

				if (nodeIdForConnection.isEmpty()) {

					if (applicationType.equals("REST API")) {

						for (int connIterator = 0; connIterator < allConnectionList.get(connNodeIterator)
								.getAsJsonArray().size(); connIterator++) {

							String eachConnSourceNode = allConnectionList.get(connNodeIterator).getAsJsonArray()
									.get(connIterator).getAsJsonObject().get("sourceNode").getAsString();

							for (int flowNodeIterator = 0; flowNodeIterator < allNodeList.get(connNodeIterator)
									.getAsJsonArray().size(); flowNodeIterator++) {

								JsonObject nodeObject = allNodeList.get(connNodeIterator).getAsJsonArray()
										.get(flowNodeIterator).getAsJsonObject();

								if (nodeObject.get("id").getAsString().equals(eachConnSourceNode)) {

									if (nodeObject.get("type").getAsString().contains("ComIbmLabel")) {
										String endpointFlowName = allConnectionList.get(connNodeIterator)
												.getAsJsonArray().get(connIterator).getAsJsonObject().get("flowName")
												.getAsString();
										endpointConnTree.add(findMsgFlowConnections(allConnectionList, endpointFlowName,
												eachConnSourceNode, allNodeList, fileUploadRespJsonObj, applicationType,
												endpointConnNodeId, "Endpoint"));
									}
									break;

								}
							}

						}

					}

				}

				// Add rest all connections
				for (int flowConnIterator = 0; flowConnIterator < connectionsTree.size(); flowConnIterator++) {
					String eachConnTargetNode = connectionsTree.get(flowConnIterator).getAsJsonObject()
							.get("targetNode").getAsString();

					for (int flowNodeIterator = 0; flowNodeIterator < allNodeList.get(connNodeIterator).getAsJsonArray()
							.size(); flowNodeIterator++) {

						JsonObject nodeObject = allNodeList.get(connNodeIterator).getAsJsonArray().get(flowNodeIterator)
								.getAsJsonObject();

						if (nodeObject.get("id").getAsString().equals(eachConnTargetNode)) {

							// Add connections from esql files
							if (nodeObject.get("type").getAsString().contains("ComIbmCompute")) {

								int index1 = nodeObject.get("computeExpression").getAsString().indexOf("#") + 1;
								int index2 = nodeObject.get("computeExpression").getAsString().lastIndexOf(".");
								String esqlModuleName = nodeObject.get("computeExpression").getAsString()
										.substring(index1, index2);
								String esqlSourceFolder = nodeObject.get("sourceFolder").getAsString();

								connectionsTree = findEsqlRouting(esqlModuleName, esqlSourceFolder,
										fileUploadRespJsonObj, connectionsTree, allConnectionList, allNodeList,
										connNodeIterator, eachConnTargetNode);
							}

							// Build subflow connections tree
							if (nodeObject.get("type").getAsString().contains(".subflow")) {
								String subFlowReference = nodeObject.get("type").getAsString().substring(0,
										nodeObject.get("type").getAsString().lastIndexOf(":"));
								boolean subFlowExist = false;
								if (!subFlowConnTree.isEmpty()) {
									for (int subFlowTreeItr = 0; subFlowTreeItr < subFlowConnTree
											.size(); subFlowTreeItr++) {
										if (subFlowReference
												.contains(subFlowConnTree.get(subFlowTreeItr).getAsJsonArray().get(0)
														.getAsJsonObject().get("flowName").getAsString())) {
											subFlowExist = true;
										}
									}
								}

								if (subFlowExist)
									continue;

								JsonArray returnTree = findSubFlowConnections(msgFlowIndex, allConnectionList,
										allNodeList, subFlowReference, eachConnTargetNode, fileUploadRespJsonObj, applicationType);

								if (returnTree.size() != 0)
									subFlowConnTree.add(returnTree);

							}

							if (nodeObject.get("type").getAsString().contains("ComIbmCallableFlowInvoke") || nodeObject.get("type").getAsString().contains("ComIbmCallableFlowAsyncInvoke")) {
							
								String callableTargetApplication = nodeObject.get("targetApplication").getAsString();
								String endPointName = nodeObject.get("targetEndpointName").getAsString();
								
								// System.out.println("endPointName: " + endPointName);
								// System.out.println("allConnectionList: "+ allConnectionList.getAsJsonArray());
								// System.out.println("allNodeList: "+ allNodeList.getAsJsonArray());
								// System.out.println("nodeObject: "+nodeObject);
								
								for (int subConnNodeIterator = 0; subConnNodeIterator < allConnectionList.getAsJsonArray().size(); subConnNodeIterator++) {
									if (subConnNodeIterator == msgFlowIndex) {
										continue;
									} else {
										String subInputNodeFlowName = "";
										String subInputNodeId = "";
										for (int subFlowNodeIterator = 0; subFlowNodeIterator < allNodeList.get(subConnNodeIterator).getAsJsonArray().size(); subFlowNodeIterator++) {
											if (allNodeList.get(subConnNodeIterator).getAsJsonArray().get(0).getAsJsonObject().get("sourceFolder").getAsString().equals(callableTargetApplication)) {
												JsonObject subNodeObject = allNodeList.get(subConnNodeIterator).getAsJsonArray().get(subFlowNodeIterator).getAsJsonObject();
												if (subNodeObject.get("type").getAsString().contains("ComIbmCallableFlowInput")) {
													String callableEndpoint = subNodeObject.get("callableInputEndpoint").getAsString();
													if(endPointName.equalsIgnoreCase(callableEndpoint)) {
														subInputNodeFlowName = subNodeObject.get("flowName").getAsString();
														subInputNodeId = subNodeObject.get("id").getAsString();
														break;
													}
												}
											}

										}
										if (subInputNodeFlowName != "" && subInputNodeId != "") {
											callableFlowConnTree
													.add(findMsgFlowConnections(allConnectionList, subInputNodeFlowName,
															subInputNodeId, allNodeList, fileUploadRespJsonObj,
															applicationType, eachConnTargetNode, "Callable"));
											break;
										}
									}
								}
							}
							break;
						}
					}

					// Loop - Check all the node and add into the list where current node's target
					// has the source node of other.
					for (int i = 0; i < allConnectionList.get(connNodeIterator).getAsJsonArray().size(); i++) {
						String eachConnSourceNode = allConnectionList.get(connNodeIterator).getAsJsonArray().get(i)
								.getAsJsonObject().get("sourceNode").getAsString();

						String eachConnListId = allConnectionList.get(connNodeIterator).getAsJsonArray().get(i)
								.getAsJsonObject().get("id").getAsString();

						// Add if the connection is not present in array.
						if (eachConnTargetNode.equals(eachConnSourceNode)) {
							// Add same source node object to the array.
							if (!hasValue(connectionsTree, "id", eachConnListId)) {
								connectionsTree.add(allConnectionList.get(connNodeIterator).getAsJsonArray().get(i)
										.getAsJsonObject());
							}
						}
					}
				}
			}
		}

		if (!subFlowConnTree.isEmpty())
			connectionsTree.add(subFlowConnTree);
		if (!callableFlowConnTree.isEmpty())
			connectionsTree.add(callableFlowConnTree);
		if (!endpointConnTree.isEmpty())
			connectionsTree.add(endpointConnTree);

		return connectionsTree;
	}

	// To find connection in subflow files.
	public JsonArray findSubFlowConnections(int msgFlowIndex, JsonArray allConnectionList, JsonArray allNodeList,
			String subFlowReference, String nodeIdForConnection, JsonObject fileUploadRespJsonObj,  String applicationType) {
		JsonArray subFlowConnTree = new JsonArray();
		JsonArray tempSubFlowConnTree = new JsonArray();
		JsonArray callableFlowConnTree = new JsonArray();

		for (int connNodeIterator = 0; connNodeIterator < allConnectionList.getAsJsonArray()
				.size(); connNodeIterator++) {
			if (connNodeIterator == msgFlowIndex) {
				continue;
			} else {
				if (subFlowReference.contains(allConnectionList.get(connNodeIterator).getAsJsonArray().get(0)
						.getAsJsonObject().get("flowName").getAsString())) {

					// Add all input node connections first
					for (int flowConnIterator = 0; flowConnIterator < allConnectionList.get(connNodeIterator)
							.getAsJsonArray().size(); flowConnIterator++) {
						JsonObject connObject = allConnectionList.get(connNodeIterator).getAsJsonArray()
								.get(flowConnIterator).getAsJsonObject();
						String connSourceNodeId = allConnectionList.get(connNodeIterator).getAsJsonArray()
								.get(flowConnIterator).getAsJsonObject().get("sourceNode").getAsString();
						if (connSourceNodeId.contains(".Input")) {
							connObject.addProperty("connectionType", "Subflow");
							connObject.addProperty("connectionSourceNode", nodeIdForConnection);
							subFlowConnTree.add(connObject);
						}
					}

					// Add rest all connections
					for (int flowConnIterator = 0; flowConnIterator < allConnectionList.get(connNodeIterator)
							.getAsJsonArray().size(); flowConnIterator++) {
						JsonObject connObject = allConnectionList.get(connNodeIterator).getAsJsonArray()
								.get(flowConnIterator).getAsJsonObject();

						String connTargetNodeId = connObject.get("targetNode").getAsString();
						for (int flowNodeIterator = 0; flowNodeIterator < allNodeList.get(connNodeIterator)
								.getAsJsonArray().size(); flowNodeIterator++) {

							JsonObject nodeObject = allNodeList.get(connNodeIterator).getAsJsonArray()
									.get(flowNodeIterator).getAsJsonObject();

							if (nodeObject.get("id").getAsString().equals(connTargetNodeId)) {

								if (nodeObject.get("type").getAsString().contains(".subflow")) {
									String internalSubFlowReference = nodeObject.get("type").getAsString().substring(0,
											nodeObject.get("type").getAsString().lastIndexOf(":"));

									boolean subFlowExist = false;
									if (!tempSubFlowConnTree.isEmpty()) {
										for (int subFlowTreeItr = 0; subFlowTreeItr < tempSubFlowConnTree
												.size(); subFlowTreeItr++) {
											if (internalSubFlowReference
													.contains(tempSubFlowConnTree.get(subFlowTreeItr).getAsJsonArray()
															.get(0).getAsJsonObject().get("flowName").getAsString())) {
												subFlowExist = true;
											}
										}
									}

									if (subFlowExist)
										continue;

									JsonArray returnedTree = findSubFlowConnections(msgFlowIndex, allConnectionList,
											allNodeList, internalSubFlowReference, connTargetNodeId,
											fileUploadRespJsonObj, applicationType);

									if (returnedTree.size() != 0)
										tempSubFlowConnTree.add(returnedTree);

								} else if (nodeObject.get("type").getAsString().contains("ComIbmCompute")) {
									int index1 = nodeObject.get("computeExpression").getAsString().indexOf("#") + 1;
									int index2 = nodeObject.get("computeExpression").getAsString().lastIndexOf(".");
									String esqlFileName = nodeObject.get("computeExpression").getAsString()
											.substring(index1, index2);
									String esqlSourceFolder = nodeObject.get("sourceFolder").getAsString();

									subFlowConnTree = findEsqlRouting(esqlFileName, esqlSourceFolder,
											fileUploadRespJsonObj, subFlowConnTree, allConnectionList, allNodeList,
											connNodeIterator, connTargetNodeId);
								}
								
								if (nodeObject.get("type").getAsString().contains("ComIbmCallableFlowInvoke") || nodeObject.get("type").getAsString().contains("ComIbmCallableFlowAsyncInvoke")) {
									String callableTargetApplication = nodeObject.get("targetApplication").getAsString();
									String endPointName = nodeObject.get("targetEndpointName").getAsString();

									for (int subConnNodeIterator = 0; subConnNodeIterator < allConnectionList.getAsJsonArray().size(); subConnNodeIterator++) {
										if (subConnNodeIterator == msgFlowIndex) {
											continue;
										} else {
											String subInputNodeFlowName = "";
											String subInputNodeId = "";
											for (int subFlowNodeIterator = 0; subFlowNodeIterator < allNodeList.get(subConnNodeIterator).getAsJsonArray().size(); subFlowNodeIterator++) {
												if (allNodeList.get(subConnNodeIterator).getAsJsonArray().get(0).getAsJsonObject().get("sourceFolder").getAsString().equals(callableTargetApplication)) {
													JsonObject subNodeObject = allNodeList.get(subConnNodeIterator).getAsJsonArray().get(subFlowNodeIterator).getAsJsonObject();
													if (subNodeObject.get("type").getAsString().contains("ComIbmCallableFlowInput")) {
														String callableEndpoint = subNodeObject.get("callableInputEndpoint").getAsString();
														if (endPointName.equalsIgnoreCase(callableEndpoint)) {
															subInputNodeFlowName = subNodeObject.get("flowName")
																	.getAsString();
															subInputNodeId = subNodeObject.get("id").getAsString();
															break;
														}
													}
												}

											}
											if (subInputNodeFlowName != "" && subInputNodeId != "") {
												callableFlowConnTree
														.add(findMsgFlowConnections(allConnectionList, subInputNodeFlowName,
																subInputNodeId, allNodeList, fileUploadRespJsonObj,
																applicationType, connTargetNodeId, "Callable"));
												break;
											}
										}
									}
								}
							}
						}

						for (int subIterator = 0; subIterator < allConnectionList.get(connNodeIterator).getAsJsonArray()
								.size(); subIterator++) {

							JsonObject subConnObject = allConnectionList.get(connNodeIterator).getAsJsonArray()
									.get(subIterator).getAsJsonObject();

							String subConnSourceNode = allConnectionList.get(connNodeIterator).getAsJsonArray()
									.get(subIterator).getAsJsonObject().get("sourceNode").getAsString();

							String subConnListId = allConnectionList.get(connNodeIterator).getAsJsonArray()
									.get(subIterator).getAsJsonObject().get("id").getAsString();

							// Add if the connection is not present in array.
							if (connTargetNodeId.equals(subConnSourceNode)) {

								// Add same source node object to the array.
								if (!hasValue(subFlowConnTree, "id", subConnListId)) {
									subFlowConnTree.add(subConnObject);
								}
							}
						}
					}
				}
			}
		}
		if (!tempSubFlowConnTree.isEmpty())
			subFlowConnTree.add(tempSubFlowConnTree);
		if (!callableFlowConnTree.isEmpty())
			subFlowConnTree.add(callableFlowConnTree);
		return subFlowConnTree;
	}

	// To read esql files and add connection to labels
	public JsonArray findEsqlRouting(String esqlModuleName, String esqlSourceFolder, JsonObject fileUploadRespJsonObj,
			JsonArray connectionsTree, JsonArray allConnectionList, JsonArray allNodeList, int connNodeIterator,
			String esqlNodeId) {
		if (!fileUploadRespJsonObj.getAsJsonObject().get("projectDetails").isJsonNull()) {
			String basePath = fileUploadRespJsonObj.getAsJsonObject().get("projectDetails").getAsJsonObject()
					.get("baseUrl").getAsString();
			JsonArray serviceDeatilsArray = fileUploadRespJsonObj.getAsJsonObject().getAsJsonObject("projectDetails")
					.getAsJsonArray("serviceDetails");
			JsonArray esqlsJsonArray = new JsonArray();

			for (int serviceArrItr = 0; serviceArrItr < serviceDeatilsArray.size(); serviceArrItr++) {
				if (serviceDeatilsArray.get(serviceArrItr).getAsJsonObject().get("serviceName").getAsString()
						.equals(esqlSourceFolder)) {
					esqlsJsonArray = serviceDeatilsArray.get(serviceArrItr).getAsJsonObject().getAsJsonArray("esql");
				}
			}

			try {
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
								String labelNodeId = "";
								if (!labelName.isEmpty()) {
									for (int flowNodeIterator = 0; flowNodeIterator < allNodeList.get(connNodeIterator)
											.getAsJsonArray().size(); flowNodeIterator++) {
										JsonObject nodeObject = allNodeList.get(connNodeIterator).getAsJsonArray()
												.get(flowNodeIterator).getAsJsonObject();
										if (nodeObject.get("labelName") != null) {
											if (nodeObject.get("labelName").getAsString().equals(labelName)) {
												labelNodeId = nodeObject.get("id").getAsString();
												break;
											}
										}
									}
									for (int flowConnIterator = 0; flowConnIterator < allConnectionList
											.get(connNodeIterator).getAsJsonArray().size(); flowConnIterator++) {
										JsonObject connObject = allConnectionList.get(connNodeIterator).getAsJsonArray()
												.get(flowConnIterator).getAsJsonObject();
										String connSourceNodeId = connObject.get("sourceNode").getAsString();
										String connId = connObject.get("id").getAsString();
										if (connSourceNodeId.equals(labelNodeId)) {
											if (hasValue(connectionsTree, "id", connId)) {
												JsonArray connectionSourceNodes = connObject
														.getAsJsonArray("connectionSourceNode");
												if (!hasValue(connectionSourceNodes, "", esqlNodeId)) {
													connectionSourceNodes.add(esqlNodeId);
													connObject.add("connectionSourceNode", connectionSourceNodes);
												}
											} else {
												JsonArray connectionSourceNodes = new JsonArray();
												connectionSourceNodes.add(esqlNodeId);

												connObject.addProperty("connectionType", "Esql");
												connObject.add("connectionSourceNode", connectionSourceNodes);
												connectionsTree.add(connObject);
											}
										}
									}
								}
							}
						}
						esqlReader.close();
						if (moduleExist)
							break;
					}
				}

			} catch (Exception exception) {
				if (AppGlobalDeclaration.isErrorLogEnabled) {
					log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Find Esql Routing :: "
							+ exception.getMessage());
					if (AppGlobalDeclaration.isStackTraceLogEnabled) {
						exception.getStackTrace();
					}
				}
			}
		}

		return connectionsTree;
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
			}
		}
		return false;
	}

}

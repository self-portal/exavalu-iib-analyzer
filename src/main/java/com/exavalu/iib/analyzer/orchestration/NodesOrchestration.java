package com.exavalu.iib.analyzer.orchestration;

import java.util.ArrayList;
import java.util.List;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class NodesOrchestration {
	public JsonArray createConnectedNodes(JsonArray connectionTree, JsonArray allSourceFileNodeList,
			JsonArray commonNodes, JsonArray commonNodesNonRest, JsonArray connectedNodes, List<String> applicationTypes, int finalReturnCheck) {
		JsonArray finalConnectedNodes = new JsonArray();
		List<String> applicationTypesForRecCall = new ArrayList<String>();
		String applicationType = "";

		for (int mainConnTreeItr = 0; mainConnTreeItr < connectionTree.size(); mainConnTreeItr++) {

			if (finalReturnCheck == 1) {
				commonNodes = new JsonArray();
				applicationType = applicationTypes.get(mainConnTreeItr);
				
				applicationTypesForRecCall.add(applicationTypes.get(mainConnTreeItr));
			} else {
				applicationType = applicationTypes.get(0);
				applicationTypesForRecCall = applicationTypes;
			}
				

			for (int internalConnTreeItr = 0; internalConnTreeItr < connectionTree.get(mainConnTreeItr).getAsJsonArray()
					.size(); internalConnTreeItr++) {
				if (connectionTree.get(mainConnTreeItr).getAsJsonArray().get(internalConnTreeItr).getClass().toString()
						.contains("JsonObject")) {

//					AppGlobalDeclaration.commonRoutes++;

					JsonObject connObject = connectionTree.get(mainConnTreeItr).getAsJsonArray()
							.get(internalConnTreeItr).getAsJsonObject();

					if (connObject.get("connectionSourceNode") == null
							|| !(connObject.get("connectionType").getAsString().equals("Endpoint"))) {
						for (int nodesItr = 0; nodesItr < allSourceFileNodeList.size(); nodesItr++) {

							String nodeId = allSourceFileNodeList.get(nodesItr).getAsJsonObject().get("id")
									.getAsString();
							String nodeFlowName = allSourceFileNodeList.get(nodesItr).getAsJsonObject().get("flowName")
									.getAsString();

							if (nodeId.equals(connObject.get("sourceNode").getAsString())
									&& nodeFlowName.equals(connObject.get("flowName").getAsString())) {

								if(!applicationType.equals("REST API")) {
									if(connectedNodes.size() != 0) {
										boolean commonNodeFound = false;
										
										for(int allCommonNodesItr = 0; allCommonNodesItr < connectedNodes.size(); allCommonNodesItr++) {
//											
											if (connectedNodes.get(allCommonNodesItr).getAsJsonArray()
													.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
												commonNodeFound = true;
//												AppGlobalDeclaration.commonRoutesNonRest++;
												// Add common node to common array
												if (!commonNodesNonRest.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
													commonNodesNonRest.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
												}
												
											}
										}
										if(!commonNodeFound) {
//											AppGlobalDeclaration.commonRoutes++;
											if (!hasValue(commonNodes, "id", "flowName", nodeId, nodeFlowName)) {
												commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
											}
										}
									} else {
//										AppGlobalDeclaration.commonRoutes++;
										if (!hasValue(commonNodes, "id", "flowName", nodeId, nodeFlowName)) {
											commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
										}
									}
								} else {
//									AppGlobalDeclaration.commonRoutes++;
									if (!hasValue(commonNodes, "id", "flowName", nodeId, nodeFlowName)) {
										commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
									}
								}
								

							}
							if (nodeId.equals(connObject.get("targetNode").getAsString())
									&& nodeFlowName.equals(connObject.get("flowName").getAsString())) {
								
								if(!applicationType.equals("REST API")) {
									if(connectedNodes.size() != 0) {
										boolean commonNodeFound = false;
										
										for(int allCommonNodesItr = 0; allCommonNodesItr < connectedNodes.size(); allCommonNodesItr++) {
//											
											if (connectedNodes.get(allCommonNodesItr).getAsJsonArray()
													.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
												commonNodeFound = true;
												AppGlobalDeclaration.commonRoutesNonRest++;
												// Add common node to common array
												if (!commonNodesNonRest.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
													commonNodesNonRest.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
												}
												
											}
										}
										if(!commonNodeFound) {
											AppGlobalDeclaration.commonRoutes++;
											if (!hasValue(commonNodes, "id", "flowName", nodeId, nodeFlowName)) {
												commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
											}
										}
									} else {
										AppGlobalDeclaration.commonRoutes++;
										if (!hasValue(commonNodes, "id", "flowName", nodeId, nodeFlowName)) {
											commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
										}
									}
								} else {
									AppGlobalDeclaration.commonRoutes++;
									if (!hasValue(commonNodes, "id", "flowName", nodeId, nodeFlowName)) {
										commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
									}
								}
								
							}
						}
					} else if (connObject.get("connectionType").getAsString().equals("Endpoint")) {
						JsonArray endpointNodes = new JsonArray();
						JsonArray allEndpointNodes = new JsonArray();
						int commonRoutesBeforeEpCall = AppGlobalDeclaration.commonRoutes;
						allEndpointNodes = connectedNodesPerEp(connectionTree, allSourceFileNodeList, allEndpointNodes, endpointNodes, commonNodes, 1);
						
						// *** Fetch first ep nodes for removal of common nodes *** //
						JsonArray firstEpNodes = new JsonArray();
						if(allEndpointNodes.get(0).getAsJsonObject().get("endpointName") != null) {
							firstEpNodes = allEndpointNodes.get(0).getAsJsonObject()
									.getAsJsonArray("endpointNodes");
						}
						
						// *** comparison with common nodes and removal from first ep nodes*** //
						for(int firstEpNodesItr = 0; firstEpNodesItr < firstEpNodes.size(); firstEpNodesItr++) {
							if(commonNodes.contains(firstEpNodes.get(firstEpNodesItr).getAsJsonObject())) {
								
								epCommonRoutesRemover(connectionTree.get(0).getAsJsonArray(), firstEpNodes.get(firstEpNodesItr).getAsJsonObject(), 0);
								firstEpNodes.remove(firstEpNodes.get(firstEpNodesItr).getAsJsonObject());
								firstEpNodesItr--;
								
							}
						}
//						int epCommonRoutes = AppGlobalDeclaration.commonRoutes - commonRoutesBeforeEpCall;
//						int firstEpRoutes = AppGlobalDeclaration.endpointRoutesArr.get(0);
//						firstEpRoutes -= epCommonRoutes;
//						AppGlobalDeclaration.endpointRoutesArr.remove(0);
//						AppGlobalDeclaration.endpointRoutesArr.add(0, firstEpRoutes);
						
						return allEndpointNodes;
					}

				} else {
					JsonArray subConnectionTree = connectionTree.get(mainConnTreeItr).getAsJsonArray()
							.get(internalConnTreeItr).getAsJsonArray();
					if (subConnectionTree.get(0).getAsJsonArray().get(0).getAsJsonObject().get("connectionType")
							.getAsString().equals("Endpoint"))
						connectedNodes
								.add(createConnectedNodes(subConnectionTree, allSourceFileNodeList, commonNodes, commonNodesNonRest, connectedNodes, applicationTypesForRecCall, 0));
					else
						createConnectedNodes(subConnectionTree, allSourceFileNodeList, commonNodes, commonNodesNonRest, connectedNodes, applicationTypesForRecCall, 0);
				}

			}

			if (finalReturnCheck == 1) {
				connectedNodes.add(commonNodes);
				
				finalConnectedNodes = connectedNodes;
				AppGlobalDeclaration.commonRoutesArr.add(AppGlobalDeclaration.commonRoutes);
				AppGlobalDeclaration.commonRoutes = 0;
//				JsonArray inputArray = new JsonArray();
//				JsonObject inputObject = new JsonObject();
//				String urlSpecifier = commonNodes.get(0).getAsJsonObject().get("URLSpecifier").getAsString();
//				String inputName = urlSpecifier.substring(urlSpecifier.lastIndexOf("/")+1, urlSpecifier.length()) + " Input";
//				inputObject.addProperty("inputName", inputName);
//				inputObject.add("inputNodes", commonNodes);
//				
//				inputArray.add(inputObject);
//				
//				connectedNodes.add(inputArray);
			} else
				finalConnectedNodes = commonNodes;

		}

		return finalConnectedNodes;
	}
	
	public void epCommonRoutesRemover(JsonArray epArray, JsonObject nodeToBeRemoved, int recursiveCallInd) {
		if(recursiveCallInd == 1) {
			for(int recArrItr=0; recArrItr<epArray.size(); recArrItr++) {
				JsonArray intArray = epArray.get(recArrItr).getAsJsonArray();
				for(int intArrItr=0; intArrItr<intArray.size(); intArrItr++) {
					if(intArray.get(intArrItr).getClass().toString().contains("JsonObject")) {
						JsonObject epConnObj = intArray.get(intArrItr).getAsJsonObject();
						String flowName = epConnObj.get("flowName").getAsString();
						String sourceNode = epConnObj.get("sourceNode").getAsString();
						String targetNode = epConnObj.get("targetNode").getAsString();
						int firstEpRoutes = AppGlobalDeclaration.endpointRoutesArr.get(0);
//						if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && sourceNode.equals(nodeToBeRemoved.get("id").getAsString())) {
//							
//							firstEpRoutes--;
//							AppGlobalDeclaration.endpointRoutesArr.remove(0);
//							AppGlobalDeclaration.endpointRoutesArr.add(0, firstEpRoutes);
//						}
						if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && targetNode.equals(nodeToBeRemoved.get("id").getAsString())) {
							
							firstEpRoutes--;
							AppGlobalDeclaration.endpointRoutesArr.remove(0);
							AppGlobalDeclaration.endpointRoutesArr.add(0, firstEpRoutes);
						}
					} 
					else if(intArray.get(intArrItr).getClass().toString().contains("JsonArray")) {
						epCommonRoutesRemover(intArray.get(intArrItr).getAsJsonArray(), nodeToBeRemoved, 1);
					}
				}
			}
		} else {
			for(int epArrItr=0; epArrItr<epArray.size(); epArrItr++) {
				if(epArray.get(epArrItr).getClass().toString().contains("JsonObject")) {
					JsonObject epConnObj = epArray.get(epArrItr).getAsJsonObject();
					String flowName = epConnObj.get("flowName").getAsString();
					String sourceNode = epConnObj.get("sourceNode").getAsString();
					String targetNode = epConnObj.get("targetNode").getAsString();
					int firstEpRoutes = AppGlobalDeclaration.endpointRoutesArr.get(0);
//					if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && sourceNode.equals(nodeToBeRemoved.get("id").getAsString())) {
//						
//						firstEpRoutes--;
//						AppGlobalDeclaration.endpointRoutesArr.remove(0);
//						AppGlobalDeclaration.endpointRoutesArr.add(0, firstEpRoutes);
//					}
					if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && targetNode.equals(nodeToBeRemoved.get("id").getAsString())) {
						
						firstEpRoutes--;
						AppGlobalDeclaration.endpointRoutesArr.remove(0);
						AppGlobalDeclaration.endpointRoutesArr.add(0, firstEpRoutes);
					}
				} 
				else if(epArray.get(epArrItr).getClass().toString().contains("JsonArray")) {
					epCommonRoutesRemover(epArray.get(epArrItr).getAsJsonArray(), nodeToBeRemoved, 1);
				}
			}
		}
	}

	public JsonArray connectedNodesPerEp(JsonArray epConnectionTree, JsonArray allSourceFileNodeList,
			JsonArray allEndpointNodes, JsonArray endpointNodes, JsonArray commonNodes, int endpointCompleteCheck) {

		for (int mainConnTreeItr = 0; mainConnTreeItr < epConnectionTree.size(); mainConnTreeItr++) {

			for (int internalConnTreeItr = 0; internalConnTreeItr < epConnectionTree.get(mainConnTreeItr)
					.getAsJsonArray().size(); internalConnTreeItr++) {
				if (epConnectionTree.get(mainConnTreeItr).getAsJsonArray().get(internalConnTreeItr).getClass()
						.toString().contains("JsonObject")) {

					AppGlobalDeclaration.endpointRoutes++;

					JsonObject connObject = epConnectionTree.get(mainConnTreeItr).getAsJsonArray()
							.get(internalConnTreeItr).getAsJsonObject();
					

					for (int nodesItr = 0; nodesItr < allSourceFileNodeList.size(); nodesItr++) {
						String nodeId = allSourceFileNodeList.get(nodesItr).getAsJsonObject().get("id").getAsString();
						String nodeFlowName = allSourceFileNodeList.get(nodesItr).getAsJsonObject().get("flowName")
								.getAsString();
						String nodeType = allSourceFileNodeList.get(nodesItr).getAsJsonObject().get("type").getAsString();
						String nodeName = "";
						String keyForCommonNodeCheck = "";
						if(allSourceFileNodeList.get(nodesItr).getAsJsonObject().getAsJsonObject("translation").get("string") != null) {
							nodeName = allSourceFileNodeList.get(nodesItr).getAsJsonObject().getAsJsonObject("translation").get("string").getAsString();
							keyForCommonNodeCheck = "string";
						} else {
							nodeName = allSourceFileNodeList.get(nodesItr).getAsJsonObject().getAsJsonObject("translation").get("key").getAsString();
							keyForCommonNodeCheck = "key";
						}
						
						if (nodeId.equals(connObject.get("sourceNode").getAsString())
								&& nodeFlowName.equals(connObject.get("flowName").getAsString())) {
							
							
							if(allEndpointNodes.size() != 0) {
								boolean commonNodeFound = false;
								
								for(int allEpNodesItr = 0; allEpNodesItr < allEndpointNodes.size(); allEpNodesItr++) {
									
									if (allEndpointNodes.get(allEpNodesItr).getAsJsonObject().getAsJsonArray("endpointNodes")
											.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
										commonNodeFound = true;
//										AppGlobalDeclaration.commonRoutes++;
//										AppGlobalDeclaration.endpointRoutes--;
										// Add common node to common array
										if (!commonNodes.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
											commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
										}
										
									}
								}
								if(!commonNodeFound) {
									if (!hasValue(endpointNodes, "id", "flowName", nodeId, nodeFlowName)) {
										endpointNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
									}
								}
								
								
							}
							else {
								if (!hasValue(endpointNodes, "id", "flowName", nodeId, nodeFlowName)) {
									endpointNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
								}
							}

						}
						if (nodeId.equals(connObject.get("targetNode").getAsString())
								&& nodeFlowName.equals(connObject.get("flowName").getAsString())) {

							if(allEndpointNodes.size() != 0) {
								boolean commonNodeFound = false;
								
								for(int allEpNodesItr = 0; allEpNodesItr < allEndpointNodes.size(); allEpNodesItr++) {

									if (allEndpointNodes.get(allEpNodesItr).getAsJsonObject().getAsJsonArray("endpointNodes")
											.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
										commonNodeFound = true;
										AppGlobalDeclaration.commonRoutes++;
										AppGlobalDeclaration.endpointRoutes--;
										// Add common node to common array
										if (!commonNodes.contains(allSourceFileNodeList.get(nodesItr).getAsJsonObject())) {
											commonNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
										}
										
									}
								}
								if(!commonNodeFound) {
									if (!hasValue(endpointNodes, "id", "flowName", nodeId, nodeFlowName)) {
										endpointNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
									}
								}
							}
							else {
								if (!hasValue(endpointNodes, "id", "flowName", nodeId, nodeFlowName)) {
									endpointNodes.add(allSourceFileNodeList.get(nodesItr).getAsJsonObject());
								}
							}

						}
					}

				} else {
					connectedNodesPerEp(epConnectionTree.get(mainConnTreeItr).getAsJsonArray().get(internalConnTreeItr)
							.getAsJsonArray(), allSourceFileNodeList, allEndpointNodes, endpointNodes, commonNodes, 0);
				}

			}

			if (endpointCompleteCheck == 1) {
				JsonObject endpointNodesObj = new JsonObject();
				String endpointNameStr = epConnectionTree.get(mainConnTreeItr).getAsJsonArray().get(0).getAsJsonObject()
						.get("sourceLabel").getAsString();
				String endpointName = endpointNameStr.substring(0, endpointNameStr.indexOf("(")).trim();
				endpointNodesObj.addProperty("endpointName", endpointName);
				endpointNodesObj.add("endpointNodes", endpointNodes);
				allEndpointNodes.add(endpointNodesObj);
				AppGlobalDeclaration.endpointRoutesArr.add(AppGlobalDeclaration.endpointRoutes);
				AppGlobalDeclaration.endpointRoutes = 0;
				endpointNodes = new JsonArray();
			}

		}

		if (endpointCompleteCheck != 1)
			allEndpointNodes = endpointNodes;

		return allEndpointNodes;
	}

	public boolean hasValue(JsonArray json, String key1, String key2, String value1, String value2) {
		for (int i = 0; i < json.size(); i++) { // iterate through the JsonArray
			// first I get the 'i' JsonElement as a JsonObject, then I get the key as a
			// string and I compare it with the value
			if (json.get(i).getAsJsonObject().get(key1).getAsString().equals(value1)
					&& json.get(i).getAsJsonObject().get(key2).getAsString().equals(value2))
				return true;
		}
		return false;
	}
	
	public boolean commonNodeChecker(JsonArray json, String key1, String key2, String value1, String value2) {
		for (int i = 0; i < json.size(); i++) { // iterate through the JsonArray
			// first I get the 'i' JsonElement as a JsonObject, then I get the key as a
			// string and I compare it with the value
			if(json.get(i).getAsJsonObject().getAsJsonObject("translation").get(key1) != null) {
				if (json.get(i).getAsJsonObject().getAsJsonObject("translation").get(key1).getAsString().equals(value1)
						&& json.get(i).getAsJsonObject().get(key2).getAsString().equals(value2))
					return true;
			}
		}
		return false;
	}
	
	

}

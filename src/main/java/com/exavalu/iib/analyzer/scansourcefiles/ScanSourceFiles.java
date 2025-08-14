package com.exavalu.iib.analyzer.scansourcefiles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.file.manager.DirectoryManager;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;
import com.exavalu.iib.analyzer.orchestration.MessageOrchestration;
import com.exavalu.iib.analyzer.orchestration.NodesOrchestration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class ScanSourceFiles {
	private static final Logger log = LoggerFactory.getLogger(ScanSourceFiles.class);

	@Autowired
	private EntityManager entityManager;
	@Value("${rootDirectory}")
	public String rootDirecttory;

	@SuppressWarnings("unused")
	@PostMapping("/scan-source-files")
	@Transactional
	public String scanSourceFiles(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestBody List<ScanJobsPojo> body, final HttpServletResponse httpServletResponse,
			@RequestParam(required = true) String user_name) throws IOException {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/scan-source-files endpoint invoked.");
		}

		String finalResponseStr = "";
		JsonArray finalResponseJsonArray = new JsonArray();
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		DirectoryManager directoryManager = new DirectoryManager();
		ScanJobsDbOperations dbOperation = new ScanJobsDbOperations();
		JsonArray allConnectionTrees = new JsonArray();
		JsonArray allConnectedNodes = new JsonArray();
		String jobId = body.get(0).getJobId();
		// CAPTURE EACH *** CONNECTION ***
		try {
			JsonArray allNodeList = new JsonArray();

			for (int subJobsItr = 0; subJobsItr < body.get(0).getSubJobs().size(); subJobsItr++) {

				JsonArray subJobsResponseArray = new JsonArray();

				String subJobId = body.get(0).getSubJobs().get(subJobsItr).getSubJobId();
				String subJobName = body.get(0).getSubJobs().get(subJobsItr).getSubJobName();
				List<SourceFiles> sourceFileList = body.get(0).getSubJobs().get(subJobsItr).getSourceFiles();

				for (int sourceFileItr = 0; sourceFileItr < sourceFileList.size(); sourceFileItr++) {

					boolean connectionTreeSaved = false;
					boolean nodesComplexityStatus = false;

					String sourceFileMasterId = jobId + "_" + subJobId + "_"
							+ sourceFileList.get(sourceFileItr).getSourceFileId();

					List<String> applicationTypes = dbOperation.getApplicationType(sourceFileMasterId, entityManager);
					JsonObject sourceFilePathsJsonObject = dbOperation.getSourceFilePathDetails(sourceFileMasterId,
							entityManager);
					JsonObject nodesAndConnectionsJsonObject = dbOperation.getNodesAndConnections(sourceFileMasterId,
							entityManager);

					if (!applicationTypes.isEmpty() && !sourceFilePathsJsonObject.isEmpty()
							&& !nodesAndConnectionsJsonObject.isEmpty()) {
						JsonArray connectionList = new JsonArray();
						JsonArray eachRelativePathArr = new JsonArray();
						JsonArray jArrayConnection = nodesAndConnectionsJsonObject.getAsJsonObject()
								.getAsJsonArray("connections");
						JsonArray jArrayNode = nodesAndConnectionsJsonObject.getAsJsonObject().getAsJsonArray("nodes");
						for (int eachJsonArrConnection = 0; eachJsonArrConnection < jArrayConnection
								.size(); eachJsonArrConnection++) {
							JsonObject buildEachConnObj = new JsonObject();
							String firstStr = "", secondStr = "";
							String sourceNodeId = jArrayConnection.get(eachJsonArrConnection).getAsJsonObject()
									.get("sourceNode").getAsString();
							String targetNodeId = jArrayConnection.get(eachJsonArrConnection).getAsJsonObject()
									.get("targetNode").getAsString();
							String connFlowName = jArrayConnection.get(eachJsonArrConnection).getAsJsonObject()
									.get("flowName").getAsString();

							try {
								buildEachConnObj.addProperty("sourceFolder", jArrayConnection.get(eachJsonArrConnection)
										.getAsJsonObject().get("sourceFolder").getAsString());
								buildEachConnObj.addProperty("flowName", jArrayConnection.get(eachJsonArrConnection)
										.getAsJsonObject().get("flowName").getAsString());
								buildEachConnObj.addProperty("id", jArrayConnection.get(eachJsonArrConnection)
										.getAsJsonObject().get("id").getAsString());
								buildEachConnObj.addProperty("sourceNode", jArrayConnection.get(eachJsonArrConnection)
										.getAsJsonObject().get("sourceNode").getAsString());
								buildEachConnObj.addProperty("targetNode", jArrayConnection.get(eachJsonArrConnection)
										.getAsJsonObject().get("targetNode").getAsString());

								for (int eachJsonArrNode = 0; eachJsonArrNode < jArrayNode.size(); eachJsonArrNode++) {
									JsonObject nodeObject = jArrayNode.get(eachJsonArrNode).getAsJsonObject();
									if (nodeObject.get("id").getAsString().equals(sourceNodeId)
											&& nodeObject.get("flowName").getAsString().equals(connFlowName)) {
										if (nodeObject.get("translation").getAsJsonObject().get("string") == null) {
											buildEachConnObj.addProperty("sourceLabel", nodeObject.get("translation")
													.getAsJsonObject().get("key").getAsString());
										} else {
											buildEachConnObj.addProperty("sourceLabel", nodeObject.get("translation")
													.getAsJsonObject().get("string").getAsString());
										}
									}
									if (nodeObject.get("id").getAsString().equals(targetNodeId)
											&& nodeObject.get("flowName").getAsString().equals(connFlowName)) {
										if (nodeObject.get("translation").getAsJsonObject().get("string") == null) {
											buildEachConnObj.addProperty("targetLabel", nodeObject.get("translation")
													.getAsJsonObject().get("key").getAsString());
										} else {
											buildEachConnObj.addProperty("targetLabel", nodeObject.get("translation")
													.getAsJsonObject().get("string").getAsString());
										}
									}
								}

								firstStr = jArrayConnection.get(eachJsonArrConnection).getAsJsonObject().get("flowName")
										.getAsString();

								if (jArrayConnection.get(eachJsonArrConnection + 1).getAsJsonObject().get("flowName")
										.getAsString() != null) {
									secondStr = jArrayConnection.get(eachJsonArrConnection + 1).getAsJsonObject()
											.get("flowName").getAsString();
								} else {
									secondStr = "";
								}
							} catch (Exception e) {
								// Do nothing here
							}
							if (firstStr.equals(secondStr)) {
								eachRelativePathArr.add(buildEachConnObj);
							} else {
								eachRelativePathArr.add(buildEachConnObj);
								connectionList.add(eachRelativePathArr);
								eachRelativePathArr = new JsonArray();
							}
						}
						if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
							log.info("All Core Connection list :: " + connectionList.toString());
						}
						// CAPTURE ALL EACH *** NODE & LIST ALL THE IIB SOURCE FILE INPUT NODES OF THE
						// EACH APPLICATION. ***
						JsonArray nodeList = new JsonArray();
						JsonArray eachRelativeNodeArr = new JsonArray();
						JsonArray listAllIIBInputArr = new JsonArray();

						for (int eachJsonArrNode = 0; eachJsonArrNode < jArrayNode.size(); eachJsonArrNode++) {
							String firstStr = "", secondStr = "";

							// Start - CODE for List all Input Nodes
							String nodeTypeStr = jArrayNode.get(eachJsonArrNode).getAsJsonObject().get("type")
									.getAsString();

							if (nodeTypeStr.contains(".")) {
								String iibInputNodeType = nodeTypeStr.substring(0, nodeTypeStr.indexOf("."));
								if (Arrays.stream(AppGlobalDeclaration.preDefineIIBInputs)
										.anyMatch(iibInputNodeType::equals)) {
									if(!jArrayNode.get(eachJsonArrNode).getAsJsonObject().get("sourceFolder").getAsString().contains("CLLBL")) {
										listAllIIBInputArr.add(jArrayNode.get(eachJsonArrNode).getAsJsonObject());
									}
//									listAllIIBInputArr.add(jArrayNode.get(eachJsonArrNode).getAsJsonObject());
								}
							}
							// End - CODE for List all Input Nodes

							try {
								firstStr = jArrayNode.get(eachJsonArrNode).getAsJsonObject().get("flowName")
										.getAsString();

								if (jArrayNode.get(eachJsonArrNode + 1).getAsJsonObject().get("flowName")
										.getAsString() != null) {
									secondStr = jArrayNode.get(eachJsonArrNode + 1).getAsJsonObject().get("flowName")
											.getAsString();
								} else {
									secondStr = "";
								}
							} catch (Exception e) {
								// Do nothing here
							}
							if (firstStr.equals(secondStr)) {
								eachRelativeNodeArr.add(jArrayNode.get(eachJsonArrNode).getAsJsonObject());
							} else {
								eachRelativeNodeArr.add(jArrayNode.get(eachJsonArrNode).getAsJsonObject());
								nodeList.add(eachRelativeNodeArr);
								eachRelativeNodeArr = new JsonArray();
							}
						}
						if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
							log.info("All IIB Input Node List :: " + listAllIIBInputArr.toString());
							log.info("All Core Node List :: " + nodeList.toString());
						}

						// Create a object of MessageOrchestration
						MessageOrchestration messageOrchestration = new MessageOrchestration();

						// **** Call MessageOrchestration routing **** //
						JsonArray sourceFileConnectionTree = messageOrchestration.routing(listAllIIBInputArr,
								connectionList, nodeList, sourceFilePathsJsonObject, applicationTypes);
						if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
							log.info(AppGlobalDeclaration.getxRequestId() + " :: "
									+ "Connection Tree of the Source File :: " + sourceFileConnectionTree);
						}

						if (sourceFileConnectionTree.size() != 0) {
							connectionTreeSaved = dbOperation.saveConnectionTree(sourceFileMasterId,
									sourceFileList.get(sourceFileItr).getSourceFileName(), sourceFileConnectionTree,
									nodeList, user_name, entityManager);

							// **** Build Connected nodes tree for the source file **** //
							NodesOrchestration nodesOrch = new NodesOrchestration();
							AppGlobalDeclaration.commonRoutes = 0;
							AppGlobalDeclaration.endpointRoutes = 0;
							AppGlobalDeclaration.commonRoutesArr = new ArrayList<>();
							AppGlobalDeclaration.endpointRoutesArr = new ArrayList<>();
							JsonArray connectedNodes = new JsonArray();
							JsonArray commonNodesNonRest = new JsonArray();
							connectedNodes = nodesOrch.createConnectedNodes(sourceFileConnectionTree,
									jArrayNode, new JsonArray(), commonNodesNonRest, connectedNodes, applicationTypes, 1);
							
							if(!commonNodesNonRest.isEmpty()) {
								// *** remove common nodes from first input by comparing it with commonNodesNonRest *** //
								for(int firstInputNodeItr = 0; firstInputNodeItr < connectedNodes.get(0).getAsJsonArray().size(); firstInputNodeItr++) {
									if(commonNodesNonRest.contains(connectedNodes.get(0).getAsJsonArray().get(firstInputNodeItr).getAsJsonObject())) {
										commonRoutesRemover(sourceFileConnectionTree.get(0).getAsJsonArray(), connectedNodes.get(0).getAsJsonArray().get(firstInputNodeItr).getAsJsonObject(), 0);
										connectedNodes.get(0).getAsJsonArray().remove(firstInputNodeItr);
										firstInputNodeItr--;
									}
								}
								connectedNodes.add(commonNodesNonRest);
								AppGlobalDeclaration.commonRoutesArr.add(AppGlobalDeclaration.commonRoutesNonRest);
//								int firstInputRouteCount = AppGlobalDeclaration.commonRoutesArr.get(0);
//								firstInputRouteCount -= AppGlobalDeclaration.commonRoutesNonRest;
//								AppGlobalDeclaration.commonRoutesArr.remove(0);
//								AppGlobalDeclaration.commonRoutesArr.add(0, firstInputRouteCount);
							}
							
							
							if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
								log.info(AppGlobalDeclaration.getxRequestId() + " :: "
										+ "Connected Nodes of the Source File :: " + connectedNodes);
							}

							SourceFileCalculations sourceFileCalculator = new SourceFileCalculations();

							nodesComplexityStatus = sourceFileCalculator.nodesComplexity(connectedNodes,
									sourceFilePathsJsonObject, applicationTypes, user_name, sourceFileMasterId,
									sourceFileList.get(sourceFileItr).getSourceFileName(), entityManager);

							allNodeList.add(jArrayNode);

							allConnectionTrees.add(sourceFileConnectionTree);

							allConnectedNodes.add(connectedNodes);

						}

						if (connectionTreeSaved && nodesComplexityStatus) {
//							directoryManager.deleteDirectory(rootDirecttory,
//									sourceFileList.get(sourceFileItr).getSourceFileId());
							genericStatusResponse.setProjectStatusCode("200");
							genericStatusResponse.setProjectStatusReasonPhrase("OK");
							genericStatusResponse.setFileName(sourceFileList.get(sourceFileItr).getSourceFileName());
							genericStatusResponse.setProjectStatusMessage("Source file scanned successfully");
						} else {
							genericStatusResponse.setProjectStatusCode("400");
							genericStatusResponse.setProjectStatusReasonPhrase("Bad Request");
							genericStatusResponse.setFileName(sourceFileList.get(sourceFileItr).getSourceFileName());
							genericStatusResponse.setProjectStatusMessage("Source file scan was unsuccessful");
							httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						}
					} else {
						genericStatusResponse.setProjectStatusCode("400");
						genericStatusResponse.setProjectStatusReasonPhrase("Bad Request");
						genericStatusResponse.setFileName(sourceFileList.get(sourceFileItr).getSourceFileName());
						genericStatusResponse.setProjectStatusMessage(
								"Source file scan unsuccessful! Please upload the file first.");
						httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					}

					subJobsResponseArray.add(genericStatusResponse.getProjectStatusResponse());

				}

				JsonObject subJobsResponseObj = new JsonObject();
				subJobsResponseObj.addProperty("subJobName", subJobName);
				subJobsResponseObj.add("subJobDetails", subJobsResponseArray);

				finalResponseJsonArray.add(subJobsResponseObj);

			}
			if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
				log.info("All Connections :: " + allConnectionTrees);
				log.info("All Connected Nodes :: " + allConnectedNodes);
			}
		} catch (Exception exception) {
			genericStatusResponse.setProjectStatusCode("400");
			genericStatusResponse.setProjectStatusReasonPhrase("Bad Request");
			genericStatusResponse.setFileName("");
			genericStatusResponse.setProjectStatusMessage(exception.getMessage());
			httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

			finalResponseJsonArray.add(genericStatusResponse.getProjectStatusResponse());

			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Scan Source Files :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}

		finalResponseStr = finalResponseJsonArray.toString();

		return finalResponseStr;
	}
	
	public void commonRoutesRemover(JsonArray inputArray, JsonObject nodeToBeRemoved, int recursiveCallInd) {
		if(recursiveCallInd == 1) {
			for(int recArrItr=0; recArrItr<inputArray.size(); recArrItr++) {
				JsonArray intArray = inputArray.get(recArrItr).getAsJsonArray();
				for(int intArrItr=0; intArrItr<intArray.size(); intArrItr++) {
					if(intArray.get(intArrItr).getClass().toString().contains("JsonObject")) {
						JsonObject inputConnObj = intArray.get(intArrItr).getAsJsonObject();
						String flowName = inputConnObj.get("flowName").getAsString();
						String sourceNode = inputConnObj.get("sourceNode").getAsString();
						String targetNode = inputConnObj.get("targetNode").getAsString();
						int firstInputRouteCount = AppGlobalDeclaration.commonRoutesArr.get(0);
//						if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && sourceNode.equals(nodeToBeRemoved.get("id").getAsString())) {
//							
//							firstInputRouteCount--;
//							AppGlobalDeclaration.commonRoutesArr.remove(0);
//							AppGlobalDeclaration.commonRoutesArr.add(0, firstInputRouteCount);
//						}
						if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && targetNode.equals(nodeToBeRemoved.get("id").getAsString())) {
							
							firstInputRouteCount--;
							AppGlobalDeclaration.commonRoutesArr.remove(0);
							AppGlobalDeclaration.commonRoutesArr.add(0, firstInputRouteCount);
						}
					} 
					else if(intArray.get(intArrItr).getClass().toString().contains("JsonArray")) {
						commonRoutesRemover(intArray.get(intArrItr).getAsJsonArray(), nodeToBeRemoved, 1);
					}
				}
			}
		} else {
			for(int inputArrItr=0; inputArrItr<inputArray.size(); inputArrItr++) {
				if(inputArray.get(inputArrItr).getClass().toString().contains("JsonObject")) {
					JsonObject inputConnObj = inputArray.get(inputArrItr).getAsJsonObject();
					String flowName = inputConnObj.get("flowName").getAsString();
					String sourceNode = inputConnObj.get("sourceNode").getAsString();
					String targetNode = inputConnObj.get("targetNode").getAsString();
					int firstInputRouteCount = AppGlobalDeclaration.commonRoutesArr.get(0);
//					if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && sourceNode.equals(nodeToBeRemoved.get("id").getAsString())) {
//						
//						firstInputRouteCount--;
//						AppGlobalDeclaration.commonRoutesArr.remove(0);
//						AppGlobalDeclaration.commonRoutesArr.add(0, firstInputRouteCount);
//					}
					if(flowName.equals(nodeToBeRemoved.get("flowName").getAsString()) && targetNode.equals(nodeToBeRemoved.get("id").getAsString())) {
						
						firstInputRouteCount--;
						AppGlobalDeclaration.commonRoutesArr.remove(0);
						AppGlobalDeclaration.commonRoutesArr.add(0, firstInputRouteCount);
					}
				} 
				else if(inputArray.get(inputArrItr).getClass().toString().contains("JsonArray")) {
					commonRoutesRemover(inputArray.get(inputArrItr).getAsJsonArray(), nodeToBeRemoved, 1);
				}
			}
		}
	}

}

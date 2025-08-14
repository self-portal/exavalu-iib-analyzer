package com.exavalu.iib.analyzer.generatereports;

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
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class GenerateReports {
	private static final Logger log = LoggerFactory.getLogger(GenerateReports.class);
	
	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings("rawtypes")
	@GetMapping("/generate-report")
	public String generateReport(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> requestParams) {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/generate-report endpoint invoked.");
		}

		String finalResponseObjectAsString = "";
		JsonObject finalresponseObject = new JsonObject();
		// String userName = requestParams.get("user_name");
		String jobId = requestParams.get("job_id");
		Gson gson = new Gson();
		List resultSetOfNodesAndConnections = null;
		var query = callGetVisualChartInfoStoreProcedure(jobId);
		query.execute();
		var resultSetIndex = 0;
		/* Assign result set to list */
		do {
			var resultSet = query.getResultStream();
			switch (resultSetIndex) {
			case 0 -> resultSetOfNodesAndConnections = resultSet.toList();
			}
			resultSetIndex++;
		} while (query.hasMoreResults());
		JsonObject connectorObject = new JsonObject();

		for (int index = 0; index < resultSetOfNodesAndConnections.size(); index++) {
			JsonObject nodesAndconnection = new JsonObject();
			nodesAndconnection = gson.fromJson(resultSetOfNodesAndConnections.get(index).toString(),
					nodesAndconnection.getClass());
			JsonArray nodesInfo = new JsonArray();
			nodesInfo = nodesAndconnection.get("nodes").getAsJsonArray();
			for (int nodeCount = 0; nodeCount < nodesInfo.size(); nodeCount++) {
				String nodeType = nodesInfo.get(nodeCount).getAsJsonObject().get("type").getAsString();
				switch (nodeType.substring(0, nodeType.indexOf(":"))) {
				case "ComIbmWSInput.msgnode":
					connectorObject.addProperty("httpConnector",
							addConnectorCount(connectorObject, "httpConnector", 1));
					break;
				case "ComIbmMQInput.msgnode":
					connectorObject.addProperty("mqConnector", addConnectorCount(connectorObject, "mqConnector", 1));
					break;
				case "ComIbmSOAPInput.msgnode":
					connectorObject.addProperty("soapConnector",
							addConnectorCount(connectorObject, "soapConnector", 1));
					break;
				case "ComIbmCICSIPICRequest.msgnode":
					connectorObject.addProperty("cicsConnector",
							addConnectorCount(connectorObject, "cicsConnector", 1));
					break;
				case "ComIbmJMSClientInput.msgnode":
					connectorObject.addProperty("jmsConnector", addConnectorCount(connectorObject, "jmsConnector", 1));
					break;
				case "ComIbmSCAInput.msgnode":
					connectorObject.addProperty("scaConnector", addConnectorCount(connectorObject, "scaConnector", 1));
					break;
				case "ComIbmPeopleSoftInput.msgnode":
					connectorObject.addProperty("peopleSoftConnector",
							addConnectorCount(connectorObject, "peopleSoftConnector", 1));
					break;
				case "ComIbmSAPInput.msgnode":
					connectorObject.addProperty("sapConnector", addConnectorCount(connectorObject, "sapConnector", 1));
					break;
				case "ComIbmSiebelInput.msgnode":
					connectorObject.addProperty("siebelConnector",
							addConnectorCount(connectorObject, "siebelConnector", 1));
					break;
				case "ComIbmDotNetInput.msgnode":
					connectorObject.addProperty("dotNetConnector",
							addConnectorCount(connectorObject, "dotNetConnector", 1));
					break;
				case "ComIbmDatabaseInput.msgnode":
					connectorObject.addProperty("dbConnector", addConnectorCount(connectorObject, "dbConnector", 1));
					break;
				case "ComIbmFileInput.msgnode":
					connectorObject.addProperty("fileConnector",
							addConnectorCount(connectorObject, "fileConnector", 1));
					break;
				case "ComIbmFTEInput.msgnode":
					connectorObject.addProperty("mqConnector", addConnectorCount(connectorObject, "mqConnector", 1));
					if (connectorObject.get("fteConnector") == null) {
						connectorObject.addProperty("fteConnector", 1);
					} else {
						connectorObject.addProperty("fteConnector", connectorObject.get("fteConnector").getAsInt() + 1);
					}
					break;
				case "ComIbmCDInput.msgnode":
					connectorObject.addProperty("cdConnector", addConnectorCount(connectorObject, "cdConnector", 1));
					break;
				case "ComIbmEmailInput.msgnode":
					connectorObject.addProperty("emailConnector",
							addConnectorCount(connectorObject, "emailConnector", 1));
					break;
				case "ComIbmTCPIPClientInput.msgnode":
					connectorObject.addProperty("tcpClientConnector",
							addConnectorCount(connectorObject, "tcpClientConnector", 1));
					break;
				case "ComIbmTCPIPServerInput.msgnode":
					connectorObject.addProperty("tcpServerConnector",
							addConnectorCount(connectorObject, "tcpServerConnector", 1));
					break;
				default:
					break;
				}
			}
		}
		finalresponseObject.add("connectorInfo", connectorObject);
		finalResponseObjectAsString = finalresponseObject.toString();
		return finalResponseObjectAsString;
	}

	public int addConnectorCount(JsonObject connectorObject, String connectorType, int count) {
		int totalCount = 0;
		if (connectorObject.get(connectorType) == null) {
			totalCount = totalCount + 1;
		} else {
			totalCount = connectorObject.get(connectorType).getAsInt() + 1;
		}
		return totalCount;
	}

	public StoredProcedureQuery callGetVisualChartInfoStoreProcedure(String jobId) {
		var query = entityManager.createStoredProcedureQuery("GetVisualChartInfo");
		query.registerStoredProcedureParameter("jobId", String.class, ParameterMode.IN);
		query.setParameter("jobId", jobId);
		return query;
	}
}

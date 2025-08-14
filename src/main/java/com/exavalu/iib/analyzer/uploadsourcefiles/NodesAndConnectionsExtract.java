package com.exavalu.iib.analyzer.uploadsourcefiles;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;

public class NodesAndConnectionsExtract {
	private static final Logger log = LoggerFactory.getLogger(NodesAndConnectionsExtract.class);

	public int nodesAndConnectionsExtractor(JsonObject projectsObject, String userName, String id,
			EntityManager entityManager) throws IOException {
		String projectName = "";
		JsonArray nodesAndConnectionsJsonArray = new JsonArray();
		ObjectMapper objectMapper = new ObjectMapper();
		int dataValidation = 0;

		if (!projectsObject.get("projectDetails").isJsonNull()) {
			JsonObject nodesAndConnectionsJsonObject = new JsonObject();
			JsonArray nodesJsonArray = new JsonArray();
			JsonArray connectionsJsonArray = new JsonArray();
			List<String> projectFilesPathList = new ArrayList<String>();

			projectName = projectsObject.getAsJsonObject("projectDetails").get("projectName").toString();
			String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
			Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
					.getAsJsonArray("serviceDetails").iterator();
			while (serviceArrayIterator.hasNext()) {
				JsonObject servicesObject = (JsonObject) serviceArrayIterator.next();
				List<String> msgPathList = objectMapper.readValue(servicesObject.getAsJsonArray("msgflow").toString(),
						new TypeReference<List<String>>() {
						});
				List<String> subPathList = objectMapper.readValue(servicesObject.getAsJsonArray("subflow").toString(),
						new TypeReference<List<String>>() {
						});
				projectFilesPathList.addAll(msgPathList);
				projectFilesPathList.addAll(subPathList);
			}
			projectFilesPathList
					.replaceAll(varPath -> basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\") + varPath);

			ListIterator<String> iterator = projectFilesPathList.listIterator();
			while (iterator.hasNext()) {
				try {
					String path = iterator.next();
					String sourceFolderName = path.substring(StringUtils.ordinalIndexOf(path, "\\", 5) + 1,
							StringUtils.ordinalIndexOf(path, "\\", 6));
					File inputFile = new File(path);

					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: "
								+ "Each Source File msgflow/subflow name :: " + inputFile.getName());
					}

					SAXReader reader = new SAXReader();
					Document document = reader.read(inputFile);
					List<Node> nodes = document.selectNodes("/EPackage/eClassifiers/composition/nodes");
					List<Node> connections = document.selectNodes("/EPackage/eClassifiers/composition/connections");

					for (Node node : nodes) {
						Element nodeElement = (Element) node;
						JsonObject nodeJsonObject = new JsonObject();
						nodeJsonObject.addProperty("sourceFolder", sourceFolderName);
						nodeJsonObject.addProperty("flowName", inputFile.getName());
						for (Attribute nodeAttribute : nodeElement.attributes()) {
							nodeJsonObject.addProperty(nodeAttribute.getName(), nodeAttribute.getValue());
						}
						List<Node> childNodes = node.selectNodes("*");
						for (Node childNode : childNodes) {
							JsonObject chileNodesJsonObject = new JsonObject();
							Element childnodeElement = (Element) childNode;
							for (Attribute childAttribute : childnodeElement.attributes()) {
								chileNodesJsonObject.addProperty(childAttribute.getName(), childAttribute.getValue());
							}
							nodeJsonObject.add(childnodeElement.getName(), chileNodesJsonObject);
						}
						nodesJsonArray.add(nodeJsonObject);
					}

					if (connections != null) {
						for (Node connection : connections) {
							Element connectionElement = (Element) connection;
							JsonObject connectionsJsonObject = new JsonObject();

							connectionsJsonObject.addProperty("sourceFolder", sourceFolderName);
							connectionsJsonObject.addProperty("flowName", inputFile.getName());
							connectionsJsonObject.addProperty("id", connectionElement.attributeValue("id"));
							connectionsJsonObject.addProperty("sourceNode",
									connectionElement.attributeValue("sourceNode"));
							connectionsJsonObject.addProperty("targetNode",
									connectionElement.attributeValue("targetNode"));
							// adding sourceTerminalName 29-08-2024
							connectionsJsonObject.addProperty("sourceTerminalName", connectionElement.attributeValue("sourceTerminalName"));
							connectionsJsonArray.add(connectionsJsonObject);
						}
					}
				} catch (DocumentException documentException) {
					if (AppGlobalDeclaration.isErrorLogEnabled) {
						log.error(AppGlobalDeclaration.getxRequestId() + " :: "
								+ "Nodes And Connections Extract's Document Exception :: "
								+ documentException.getMessage());
						if (AppGlobalDeclaration.isStackTraceLogEnabled) {
							documentException.printStackTrace();
						}
					}
				}
			}
			nodesAndConnectionsJsonObject.add("nodes", nodesJsonArray);
			nodesAndConnectionsJsonObject.add("connections", connectionsJsonArray);
			nodesAndConnectionsJsonArray.add(nodesAndConnectionsJsonObject);

			// need to be modified after merging
			JsonArray jArrayNode = nodesAndConnectionsJsonObject.get("nodes").getAsJsonArray();
			JsonArray listAllIIBInputArr = new JsonArray();

			for (int eachJsonArrNode = 0; eachJsonArrNode < jArrayNode.size(); eachJsonArrNode++) {
				// Start - CODE for List all Input Nodes
				String nodeTypeStr = jArrayNode.get(eachJsonArrNode).getAsJsonObject().get("type").getAsString();

				if (nodeTypeStr.contains(".")) {
					String iibInputNodeType = nodeTypeStr.substring(0, nodeTypeStr.indexOf("."));
					if (Arrays.stream(AppGlobalDeclaration.preDefineIIBInputs).anyMatch(iibInputNodeType::equals)) {
						listAllIIBInputArr.add(jArrayNode.get(eachJsonArrNode).getAsJsonObject());
					}
				}
			}
			UploadSourceFileDbOperations saveJobDetails = new UploadSourceFileDbOperations();
			dataValidation = saveJobDetails.saveNodeDetails(projectName, nodesAndConnectionsJsonObject,
					listAllIIBInputArr, userName, id, entityManager);

			if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
				log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Nodes & Connections list :: "
						+ nodesAndConnectionsJsonArray);
			}
		}

		return dataValidation;
	}
}

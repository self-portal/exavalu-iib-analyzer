package com.exavalu.iib.analyzer.feature;

import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ProjectTypeFinder {
	private static final Logger log = LoggerFactory.getLogger(ProjectTypeFinder.class);

	public static String findProjectType(JsonObject projectsObject) {
		String projectType = "";
		Boolean isLibrary = true;
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				JsonArray msgFlowArray = serviceDetailsObject.getAsJsonArray("msgflow");
				if (!msgFlowArray.isEmpty()) {
					isLibrary = false;
				}
				Iterator<JsonElement> descriptorArrayIterator = serviceDetailsObject.getAsJsonArray("descriptor")
						.iterator();
				while (descriptorArrayIterator.hasNext()) {
					String descriptorPath = descriptorArrayIterator.next().getAsString();
					List<Node> projectNodesName = null;
					SAXReader saxReader = new SAXReader();
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					String fullDescriptorFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
							+ descriptorPath;
					Document inDoc = saxReader.read(fullDescriptorFilePath);
					projectNodesName = inDoc.selectNodes("/restapiDescriptor");
					if (!projectNodesName.isEmpty()) {
						for (Node node : projectNodesName) {
							//Update for OpenAPISpec v3 03-10-24
							if (node.valueOf("@definitionType").contains("swagger") || node.valueOf("@definitionType").contains("openapi_3") ) {
								projectType = "REST API";
								return projectType;
							}
						}
					}
				}
			}
			if (isLibrary) {
				projectType = "Library";
				return projectType;
			}
			if (!projectType.equals("REST API")) {
				projectType = "IIB Application";
				return projectType;
			}

		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Find Project Type :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}
		return projectType;
	}
}

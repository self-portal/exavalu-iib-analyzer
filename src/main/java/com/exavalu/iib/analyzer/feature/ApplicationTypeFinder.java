package com.exavalu.iib.analyzer.feature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class ApplicationTypeFinder {
	private static final Logger log = LoggerFactory.getLogger(ApplicationTypeFinder.class);

	public static ArrayList<String> findApplicationType(JsonObject projectsObject) {
		String projectType = ProjectTypeFinder.findProjectType(projectsObject);
		String applicationTypeStr = "";
		ArrayList<String> applicationTypes = new ArrayList<String>();
		if (projectType == "IIB Application") {
			String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
			Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
					.getAsJsonArray("serviceDetails").iterator();
			try {

				while (serviceArrayIterator.hasNext()) {
					JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
					Iterator<JsonElement> msgflowArrayIterator = serviceDetailsObject.getAsJsonArray("msgflow")
							.iterator();
					while (msgflowArrayIterator.hasNext()) {
						String msgflowPath = msgflowArrayIterator.next().getAsString();
						List<Node> projectNodesName = null;
						SAXReader saxReader = new SAXReader();
						saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
						String fullmsgflowFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
								+ msgflowPath;
						Document inDoc = saxReader.read(fullmsgflowFilePath);
						projectNodesName = inDoc.selectNodes("/EPackage/eClassifiers/composition/nodes");

						if (!projectNodesName.isEmpty()) {
							for (Node node : projectNodesName) {
								if (node.valueOf("@xmi:type").contains("ComIbmCallableFlowInput")) {
									applicationTypeStr = "Callable";
								} else {
									
									// Added Dynamic logic to get Application Type from Input Node 27-09-24
									
									String xmiNodeType = node.valueOf("@xmi:type").toString(); //eg-> ComIbmFTEInput.msgnode:FCMComposite_1
									String nodeName = xmiNodeType.split("\\.")[0];  //eg-> ComIbmFTEInput
									
									// Converting InputNode Array to InputNode ArrayList
									List<String> inputNodeList = new ArrayList<>(Arrays.asList(AppGlobalDeclaration.preDefineIIBInputs));
									
									//check if it is input Node
									if (inputNodeList.contains(nodeName)) {
										String applicationType = AppGlobalDeclaration.iibInputXmiTypeMap.get(nodeName);	//get the application type from iibInputXmiTypeMap
										applicationTypes.add(applicationType);
									}
								}

							}
						}

						if (applicationTypeStr == "" && applicationTypes.size() == 0) {
							applicationTypeStr = "Other Application";
						}
					}
				}

			} catch (Exception exception) {
				if (AppGlobalDeclaration.isErrorLogEnabled) {
					log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Application Type Finder :: "
							+ exception.getMessage());
					if (AppGlobalDeclaration.isStackTraceLogEnabled) {
						exception.getStackTrace();
					}
				}
			}
		} else
			applicationTypeStr = projectType;

		if (applicationTypes.size() != 0) {
			return applicationTypes;
		} else {
			applicationTypes.add(applicationTypeStr);
			return applicationTypes;
		}
	}

}
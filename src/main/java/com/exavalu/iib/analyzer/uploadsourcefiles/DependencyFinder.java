package com.exavalu.iib.analyzer.uploadsourcefiles;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.exavalu.iib.analyzer.feature.ApplicationTypeFinder;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;

public class DependencyFinder {
	private static final Logger log = LoggerFactory.getLogger(DependencyFinder.class);

	public static String dependencyFinder(JsonObject projectsObject) {
		JsonArray projectsList = new JsonArray(100);
		JsonArray libraryStore = new JsonArray();
		JsonArray dependeciesStore = new JsonArray();
		JsonObject libraryDetail = new JsonObject();
		JsonObject finalResponse = new JsonObject();

		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				JsonObject serviceObject = new JsonObject();
				JsonArray serviceArray = new JsonArray();
				serviceArray.add(serviceDetailsObject);
				serviceObject.add("serviceDetails", serviceArray);
				JsonObject projectDetails = new JsonObject();
				serviceObject.addProperty("baseUrl", basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\"));
				projectDetails.add("projectDetails", serviceObject);

				Iterator<JsonElement> projectIterator = serviceDetailsObject.getAsJsonArray("project").iterator();
				while (projectIterator.hasNext()) {
					String projectPath = projectIterator.next().getAsString();
					libraryDetail = new JsonObject();
					dependeciesStore = new JsonArray();
					libraryDetail.addProperty("projectName", serviceDetailsObject.get("serviceName").getAsString());
					libraryDetail.addProperty("projectType",
							ApplicationTypeFinder.findApplicationType(projectDetails).toString());
					List<Node> projectNodesName = null;
					SAXReader saxReader = new SAXReader();
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					String fullprojectPath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
							+ projectPath;
					Document inDoc = saxReader.read(fullprojectPath);
					projectNodesName = inDoc.selectNodes("/projectDescription/projects/project");
					if (!projectNodesName.isEmpty()) {
						for (Node node : projectNodesName) {
							// projectList is storing all the dependencies required by the whole project
							projectsList.add(node.getStringValue());
							// dependeciesStore only store the dependencies required by each
							// project/libraries
							dependeciesStore.add(node.getStringValue());
						}
					}
					libraryDetail.add("dependencies", dependeciesStore);
					libraryStore.add(libraryDetail.getAsJsonObject());
				}
			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Dependency Finder :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}
		finalResponse.add("projectDependenciesInfo", libraryStore);
		return finalResponse.toString();
	}

	public static String dependencyValidator(JsonObject projectsObject) {
		boolean valid = true;
		JsonArray projectsList = new JsonArray();
		ArrayList<String> requiredLibraries = new ArrayList<String>();
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				JsonObject serviceObject = new JsonObject();
				JsonArray serviceArray = new JsonArray();
				serviceArray.add(serviceDetailsObject);
				serviceObject.add("serviceDetails", serviceArray);
				JsonObject projectDetails = new JsonObject();
				serviceObject.addProperty("baseUrl", basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\"));
				projectDetails.add("projectDetails", serviceObject);

				Iterator<JsonElement> projectIterator = serviceDetailsObject.getAsJsonArray("project").iterator();
				while (projectIterator.hasNext()) {
					String projectPath = projectIterator.next().getAsString();
					List<Node> projectNodesName = null;
					SAXReader saxReader = new SAXReader();
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					String fullprojectPath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
							+ projectPath;
					Document inDoc = saxReader.read(fullprojectPath);
					projectNodesName = inDoc.selectNodes("/projectDescription/projects/project");
					if (!projectNodesName.isEmpty()) {
						for (Node node : projectNodesName) {
							// projectList is storing all the dependencies required by the whole project
							projectsList.add(node.getStringValue());
						}
					}
				}

			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Dependency Validator :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}

		if (!projectsList.isEmpty()) {
			for (int projectIterator = 0; projectIterator < projectsList.size(); projectIterator++) {
				valid = false;
				String dependencyName = "";
				Iterator<JsonElement> serviceArrayIterator1 = projectsObject.getAsJsonObject("projectDetails")
						.getAsJsonArray("serviceDetails").iterator();
				while (serviceArrayIterator1.hasNext()) {
					dependencyName = projectsList.get(projectIterator).getAsString();
					JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator1.next();
					if (dependencyName.contentEquals(serviceDetailsObject.get("serviceName").getAsString())) {
						valid = true;
						continue;
					}
				}
				if (valid == false) {
					requiredLibraries.add(dependencyName);
				}
			}
		}

		if (requiredLibraries.size() == 0)
			return "true";
		else {
			return requiredLibraries.toString();
		}
	}
}

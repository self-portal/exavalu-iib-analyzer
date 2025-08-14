package com.exavalu.iib.analyzer.feature;

import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;

public class EndpointMethodFinder {
	private static final Logger log = LoggerFactory.getLogger(EndpointMethodFinder.class);

	public static JsonArray fetchMethodRest(JsonObject projectsObject) {
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		JsonArray endpointsAndMethodsJsonArray = new JsonArray();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				String swaggerFileName = null;
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
								swaggerFileName = node.valueOf("@definitionFile");
								break;
							}
						}
					}
					Iterator<JsonElement> jsonIterator = serviceDetailsObject.getAsJsonArray("json").iterator();
					while (jsonIterator.hasNext()) {
						String jsonFilePath = jsonIterator.next().toString();
						String jsonFile = jsonFilePath.substring(jsonFilePath.lastIndexOf("\\\\") + 2,
								jsonFilePath.length() - 1);
						if (jsonFile.equals(swaggerFileName)) {
							String path = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
									+ jsonFilePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\");
							endpointsAndMethodsJsonArray = SwaggerParser.pathReader(path);
						}
					}
				}

			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Fetch Method REST :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}
		return endpointsAndMethodsJsonArray;
	}

	public static JsonArray fetchMethodSOAP(JsonObject projectsObject) {
		JsonObject SOAPendpointsAndMethodsJsonObject = new JsonObject();
		JsonArray SOAPendpointsAndMethodsJsonArray = new JsonArray();
		JsonArray soapOperations = new JsonArray();
		SOAPendpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "SOAP");
		SOAPendpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "SOAP Endpoint");
		String wsdlFileName = "";
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				Iterator<JsonElement> msgflowArrayIterator = serviceDetailsObject.getAsJsonArray("msgflow").iterator();
				if (wsdlFileName.equals("")) {
					while (msgflowArrayIterator.hasNext()) {
						String msgflowPath = msgflowArrayIterator.next().getAsString();
						List<Node> projectNodesName = null;
						SAXReader saxReader = new SAXReader();
						saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
						String fullmsgflowFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
								+ msgflowPath;
						Document inDoc = saxReader.read(fullmsgflowFilePath);
						projectNodesName = inDoc.selectNodes("/EPackage/eClassifiers/composition/nodes");

						for (Node node : projectNodesName) {
							if (!projectNodesName.isEmpty()) {
								if (node.valueOf("@xmi:type").contains("ComIbmSOAPInput")) {
									SOAPendpointsAndMethodsJsonObject.addProperty("Base_URL",
											node.valueOf("@targetNamespace"));
									SOAPendpointsAndMethodsJsonObject.addProperty("Endpoint",
											node.valueOf("@urlSelector"));
									wsdlFileName = node.valueOf("@wsdlFileName");
									serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
											.getAsJsonArray("serviceDetails").iterator();
								}
							}
						}
					}
				} else {

					Iterator<JsonElement> wsdlIterator = serviceDetailsObject.getAsJsonArray("wsdl").iterator();
					while (wsdlIterator.hasNext()) {
						String wsdlFile = wsdlIterator.next().toString().replaceAll("^\"|\"$", "")
								.replaceAll("\\\\\\\\", "\\\\");
						if (wsdlFile.contains(wsdlFileName)) {
							List<Node> projectNodesName = null;
							SAXReader saxReader = new SAXReader();
							saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
							String fullwsdlFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
									+ wsdlFile;
							Document inDoc = saxReader.read(fullwsdlFilePath);
							projectNodesName = inDoc.selectNodes("/definitions/wsdl:binding/wsdl:operation");

							for (Node node : projectNodesName) {
								if (!projectNodesName.isEmpty()) {
									soapOperations.add(node.valueOf("@name"));
								}
							}
							SOAPendpointsAndMethodsJsonObject.add("Endpoint_Method", soapOperations);
						}
					}
				}
			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Fetch Method SOAP :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}
		SOAPendpointsAndMethodsJsonArray.add(SOAPendpointsAndMethodsJsonObject);
		return SOAPendpointsAndMethodsJsonArray;
	}

	public static JsonArray fetchMethodMQ(JsonObject projectsObject) {
//		JsonObject MQendpointsAndMethodsJsonObject = new JsonObject();
		JsonArray MQendpointsAndMethodsJsonArray = new JsonArray();
		JsonArray endPointMethod = new JsonArray();
		endPointMethod.add("N/A");
//		MQendpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
//		MQendpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "MQ");
//		MQendpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "MQ Endpoint");
//		MQendpointsAndMethodsJsonObject.addProperty("Base_URL", "");
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				Iterator<JsonElement> msgflowArrayIterator = serviceDetailsObject.getAsJsonArray("msgflow").iterator();
				while (msgflowArrayIterator.hasNext()) {
					String msgflowPath = msgflowArrayIterator.next().getAsString();
					List<Node> projectNodesName = null;
					SAXReader saxReader = new SAXReader();
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					String fullmsgflowFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
							+ msgflowPath;
					Document inDoc = saxReader.read(fullmsgflowFilePath);
					projectNodesName = inDoc.selectNodes("/EPackage/eClassifiers/composition/nodes");

					for (Node node : projectNodesName) {
						if (!projectNodesName.isEmpty()) {
							if (node.valueOf("@xmi:type").contains("ComIbmMQInput")) {
								JsonObject MQendpointsAndMethodsJsonObject = new JsonObject();
								MQendpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
								MQendpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "MQ");
								MQendpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "MQ Endpoint");
								MQendpointsAndMethodsJsonObject.addProperty("Base_URL", "");
								MQendpointsAndMethodsJsonObject.addProperty("Endpoint", node.valueOf("@queueName"));

								MQendpointsAndMethodsJsonArray.add(MQendpointsAndMethodsJsonObject);
							}
						}
					}
				}
			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(
						AppGlobalDeclaration.getxRequestId() + " :: " + "Fetch Method MQ :: " + exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}

//		MQendpointsAndMethodsJsonArray.add(MQendpointsAndMethodsJsonObject);
		return MQendpointsAndMethodsJsonArray;
	}

	public static JsonArray fetchMethodHttp(JsonObject projectsObject) {
//		JsonObject httpEndpointsAndMethodsJsonObject = new JsonObject();
		JsonArray httpEndpointsAndMethodsJsonArray = new JsonArray();
		JsonArray endPointMethod = new JsonArray();
		endPointMethod.add("N/A");
//		httpEndpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
//		httpEndpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "HTTP");
//		httpEndpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "HTTP Endpoint");
//		httpEndpointsAndMethodsJsonObject.addProperty("Base_URL", "localhost:8081");
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				Iterator<JsonElement> msgflowArrayIterator = serviceDetailsObject.getAsJsonArray("msgflow").iterator();
				while (msgflowArrayIterator.hasNext()) {
					String msgflowPath = msgflowArrayIterator.next().getAsString();
					List<Node> projectNodesName = null;
					SAXReader saxReader = new SAXReader();
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					String fullmsgflowFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
							+ msgflowPath;
					Document inDoc = saxReader.read(fullmsgflowFilePath);
					projectNodesName = inDoc.selectNodes("/EPackage/eClassifiers/composition/nodes");

					for (Node node : projectNodesName) {
						if (!projectNodesName.isEmpty()) {
							if (node.valueOf("@xmi:type").contains("ComIbmWSInput")) {
								JsonObject httpEndpointsAndMethodsJsonObject = new JsonObject();
								httpEndpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
								httpEndpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "HTTP");
								httpEndpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "HTTP Endpoint");
								httpEndpointsAndMethodsJsonObject.addProperty("Base_URL", "localhost:8081");
								httpEndpointsAndMethodsJsonObject.addProperty("Endpoint",
										node.valueOf("@URLSpecifier"));

								httpEndpointsAndMethodsJsonArray.add(httpEndpointsAndMethodsJsonObject);
							}
						}
					}
				}
			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Fetch Method Http :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}

//		httpEndpointsAndMethodsJsonArray.add(httpEndpointsAndMethodsJsonObject);
		return httpEndpointsAndMethodsJsonArray;
	}

	public static JsonArray fetchMethodEmail(JsonObject projectsObject) {
//		JsonObject httpEndpointsAndMethodsJsonObject = new JsonObject();
		JsonArray emailEndpointsAndMethodsJsonArray = new JsonArray();
		JsonArray endPointMethod = new JsonArray();
		endPointMethod.add("N/A");
//		httpEndpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
//		httpEndpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "HTTP");
//		httpEndpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "HTTP Endpoint");
//		httpEndpointsAndMethodsJsonObject.addProperty("Base_URL", "localhost:8081");
		String basePath = projectsObject.getAsJsonObject("projectDetails").get("baseUrl").toString();
		Iterator<JsonElement> serviceArrayIterator = projectsObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails").iterator();
		try {
			while (serviceArrayIterator.hasNext()) {
				JsonObject serviceDetailsObject = (JsonObject) serviceArrayIterator.next();
				Iterator<JsonElement> msgflowArrayIterator = serviceDetailsObject.getAsJsonArray("msgflow").iterator();
				while (msgflowArrayIterator.hasNext()) {
					String msgflowPath = msgflowArrayIterator.next().getAsString();
					List<Node> projectNodesName = null;
					SAXReader saxReader = new SAXReader();
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					String fullmsgflowFilePath = basePath.replaceAll("^\"|\"$", "").replaceAll("\\\\\\\\", "\\\\")
							+ msgflowPath;
					Document inDoc = saxReader.read(fullmsgflowFilePath);
					projectNodesName = inDoc.selectNodes("/EPackage/eClassifiers/composition/nodes");

					for (Node node : projectNodesName) {
						if (!projectNodesName.isEmpty()) {
							if (node.valueOf("@xmi:type").contains("ComIbmEmailInput")) {
								JsonObject emailEndpointsAndMethodsJsonObject = new JsonObject();
								emailEndpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
								emailEndpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "Email");
								emailEndpointsAndMethodsJsonObject.addProperty("Endpoint_Description",
										"Email Endpoint");
								emailEndpointsAndMethodsJsonObject.addProperty("Base_URL", "localhost:8081");
								emailEndpointsAndMethodsJsonObject.addProperty("Endpoint",
										node.valueOf("@emailServer"));

								emailEndpointsAndMethodsJsonArray.add(emailEndpointsAndMethodsJsonObject);
							}
						}
					}
				}
			}
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Fetch Method Email :: "
						+ exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
		}

//		httpEndpointsAndMethodsJsonArray.add(httpEndpointsAndMethodsJsonObject);
		return emailEndpointsAndMethodsJsonArray;
	}

	// Added Common Method to Fetch EndPoints- 27/09/2024
	public static JsonArray fetchMethodEndpoints(JsonObject projectsObject, String endpointType, String nodeType,
			String endpointDescription, List<String> endpointFields) {
		if (endpointFields != null) {
			JsonArray endpointsAndMethodsJsonArray = new JsonArray();
			JsonArray endPointMethod = new JsonArray();
			endPointMethod.add("N/A");

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

						for (Node node : projectNodesName) {
							if (!projectNodesName.isEmpty()) {
								if (node.valueOf("@xmi:type").contains(nodeType)) {
									JsonObject endpointsAndMethodsJsonObject = new JsonObject();
									endpointsAndMethodsJsonObject.add("Endpoint_Method", endPointMethod);
									endpointsAndMethodsJsonObject.addProperty("Endpoint_Type", endpointType);
									endpointsAndMethodsJsonObject.addProperty("Endpoint_Description",
											endpointDescription);
									endpointsAndMethodsJsonObject.addProperty("Base_URL", "");

									// Construct the endpoint string based on the provided fields
									StringBuilder endpointBuilder = new StringBuilder();
									for (String field : endpointFields) {
										String value = node.valueOf("@" + field);
										if (value != null && !value.isEmpty()) {
											value = value.replace("%MQSI_DELIMITER%", ",");
											if (endpointBuilder.length() > 0) {
												endpointBuilder.append("_"); // Use a comma for separation
											}
											endpointBuilder.append(value);
										}
										else if(endpointType.equals("Scheduler") && (value.isEmpty())) {
											endpointBuilder.append("Scheduler");
										}
									}
									endpointsAndMethodsJsonObject.addProperty("Endpoint", endpointBuilder.toString());

									endpointsAndMethodsJsonArray.add(endpointsAndMethodsJsonObject);
								}
							}
						}
					}
				}
			} catch (Exception exception) {
				if (AppGlobalDeclaration.isErrorLogEnabled) {
					log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Fetch Method " + endpointType + " :: "
							+ exception.getMessage());
					if (AppGlobalDeclaration.isStackTraceLogEnabled) {
						exception.printStackTrace();
					}
				}
			}

			return endpointsAndMethodsJsonArray;
		} else {
			return null;
		}
	}

}

package com.exavalu.iib.analyzer.feature;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class SwaggerParser {
	private static final Logger log = LoggerFactory.getLogger(SwaggerParser.class);

	@SuppressWarnings("rawtypes")
	public static JsonArray pathReader(String filePath) {

		// JSON parser object to parse read file
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "pathReader called and filePath is: " + filePath);
		}

		JsonObject endpointsAndMethodsJsonObject = null;
		JsonArray endpointsAndMethodsJsonArray = new JsonArray();
		JsonObject jsonObject = new JsonObject();
		String MSG = "";
		try {
			File file = new File(filePath);
			String content = FileUtils.readFileToString(file, "utf-8");
			Gson gsonObj = new Gson();
			jsonObject = gsonObj.fromJson(content, jsonObject.getClass());
			Map endpointsAndMethods = fetchEndpointsAndMethodsFromSwagger(jsonObject);
			Iterator mapIterator = endpointsAndMethods.entrySet().iterator();
			while (mapIterator.hasNext()) {
				endpointsAndMethodsJsonObject = new JsonObject();
				Map.Entry mapElement = (Map.Entry) mapIterator.next();
				String endpointMethod = mapElement.getValue().toString();
				String endpointName = mapElement.getKey().toString();

				List<String> endpointMethodList = gsonObj.fromJson(endpointMethod, new TypeToken<List<String>>() {
				}.getType());

				JsonArray operationIdJsonArr = new JsonArray();
				for (int epMethodItr = 0; epMethodItr < endpointMethodList.size(); epMethodItr++) {

					String operationId = jsonObject.getAsJsonObject("paths").getAsJsonObject(endpointName)
							.getAsJsonObject(endpointMethodList.get(epMethodItr)).get("operationId").getAsString();
					operationIdJsonArr.add(operationId);
				}

				endpointsAndMethodsJsonObject.addProperty("Endpoint_Method", endpointMethod);
				endpointsAndMethodsJsonObject.addProperty("Endpoint_Type", "REST");
				endpointsAndMethodsJsonObject.addProperty("Endpoint_Description", "REST Endpoint");
				endpointsAndMethodsJsonObject.addProperty("Base_URL", "localhost:8081");
				endpointsAndMethodsJsonObject.addProperty("Endpoint", endpointName);
				endpointsAndMethodsJsonObject.add("OperationId", operationIdJsonArr);

				endpointsAndMethodsJsonArray.add(endpointsAndMethodsJsonObject);
			}
			MSG = "Successful";
		} catch (Exception exception) {
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Path Reader :: " + exception.getMessage());
				if (AppGlobalDeclaration.isStackTraceLogEnabled) {
					exception.getStackTrace();
				}
			}
			MSG = "Unsuccessful";
		}
		return endpointsAndMethodsJsonArray;
	}

	public static Map<String, Set<String>> fetchEndpointsAndMethodsFromSwagger(JsonObject swagger) throws Exception {
		// Create the Map object to store the EPs/Methods.
		Map<String, Set<String>> endpointsAndMethods = new HashMap<String, Set<String>>();
		// Iterate over the paths array in swagger to parse each of the
		// endpoints and their allowed methods.
		for (String endpoint : swagger.getAsJsonObject("paths").keySet()) {
			// Fetch the supported methods.
			Set<String> supportedMethods = swagger.getAsJsonObject("paths").getAsJsonObject(endpoint).keySet();
			// Put the data in the Map object.
			// String method = String.join("", supportedMethods);
			endpointsAndMethods.put(endpoint, supportedMethods);
			// System.out.println("Endpoint: " + endpoint + " | Supported Method/s: " +
			// method);
		}
		return endpointsAndMethods;
	}
}

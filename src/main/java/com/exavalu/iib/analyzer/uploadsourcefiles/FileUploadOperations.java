package com.exavalu.iib.analyzer.uploadsourcefiles;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.exavalu.iib.analyzer.file.manager.SaveSourceCodeFiles;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.utils.FileNameExtensionExtract;

public class FileUploadOperations {
	private static final Logger log = LoggerFactory.getLogger(FileUploadOperations.class);

	public JsonObject saveUploadedFilesDetails(String userRequestId, MultipartFile uploadedFile, String userName,
			String zipFileName) throws IOException {
		BufferedInputStream bufferedInputStream = new BufferedInputStream(uploadedFile.getInputStream());
		ZipInputStream zipInputStream = new ZipInputStream(bufferedInputStream);

		BufferedInputStream bInputStreamPrevious = new BufferedInputStream(uploadedFile.getInputStream());
		ZipInputStream zInputStreamPrevious = new ZipInputStream(bInputStreamPrevious);

		ZipEntry zipEntry, zipEntryPrevious;
		String[] paths = new String[2];
		String variablePath = "", commonPath = "", fullPath = "";

		JsonObject eachProjectObject = new JsonObject();
		JsonObject eachFilePathObject = new JsonObject();
		JsonObject allFilesJsonObject = new JsonObject();
		JsonArray servicesArray = new JsonArray();
		JsonArray msgFlowArray = new JsonArray();
		JsonArray subFlowArray = new JsonArray();
		JsonArray dotProjectArray = new JsonArray();
		JsonArray jsonFileArray = new JsonArray();
		JsonArray esqlArray = new JsonArray();
		JsonArray descriptorArray = new JsonArray();
		JsonArray wsdlArray = new JsonArray();
		JsonArray xsdArray = new JsonArray();
		JsonArray cpyArray = new JsonArray();
		JsonArray yamlArray = new JsonArray();
		JsonArray propertiesArray = new JsonArray();
		JsonArray otherArray = new JsonArray();
		String applicationName = "";
		String currentService = "";
		String previousService = "";

		if ((zipEntryPrevious = zInputStreamPrevious.getNextEntry()) != null) {
			previousService = zipEntryPrevious.getName().substring(0, zipEntryPrevious.getName().indexOf("/"));
			while ((zipEntry = zipInputStream.getNextEntry()) != null) {
				String fileExtensions = FileNameExtensionExtract.getFileExtension(zipEntry.getName()).toLowerCase();

				// continue in case of blank folders
				if (fileExtensions.isBlank()) {
					continue;
				}

				String projectName = "";
				String fileName;
				fileName = zipEntry.getName();
				projectName = zipEntry.getName().substring(0, zipEntry.getName().indexOf("/"));

				if (zipFileName.substring(0, zipFileName.indexOf(".")).contains(projectName)) {
					applicationName = projectName;
				}

				// SAVE PROJECT FILE TO THE LOCAL STORAGE.
				paths = SaveSourceCodeFiles.saveProjectFilesToLocal(fileName, userName, zipFileName, 1, zipInputStream,
						projectName);

				commonPath = paths[0];
				variablePath = paths[1];
				fullPath = commonPath + variablePath;

				currentService = projectName;

				if (currentService.equals(previousService)) {
					switch (fileExtensions) {
					case "project":
						dotProjectArray.add(variablePath);
						break;
					case "msgflow":
						msgFlowArray.add(variablePath);
						break;
					case "subflow":
						subFlowArray.add(variablePath);
						break;
					case "esql":
						esqlArray.add(variablePath);
						break;
					case "json":
						jsonFileArray.add(variablePath);
						break;
					case "descriptor":
						descriptorArray.add(variablePath);
						break;
					case "wsdl":
						wsdlArray.add(variablePath);
						break;
					case "xsd":
						xsdArray.add(variablePath);
						break;
					case "cpy":
						cpyArray.add(variablePath);
						break;
					case "yaml":
						yamlArray.add(variablePath);
						break;
					case "properties":
						propertiesArray.add(variablePath);
						break;
					default:
						otherArray.add(variablePath);
					}
				} else {
					eachFilePathObject.addProperty("serviceName", previousService);
					eachFilePathObject.add("msgflow", msgFlowArray);
					eachFilePathObject.add("subflow", subFlowArray);
					eachFilePathObject.add("project", dotProjectArray);
					eachFilePathObject.add("json", jsonFileArray);
					eachFilePathObject.add("esql", esqlArray);
					eachFilePathObject.add("descriptor", descriptorArray);
					eachFilePathObject.add("wsdl", wsdlArray);
					eachFilePathObject.add("xsd", xsdArray);
					eachFilePathObject.add("cpy", cpyArray);
					eachFilePathObject.add("yaml", yamlArray);
					eachFilePathObject.add("properties", propertiesArray);
					eachFilePathObject.add("otherFiles", otherArray);

					servicesArray.add(eachFilePathObject);

					eachFilePathObject = new JsonObject();
					msgFlowArray = new JsonArray();
					subFlowArray = new JsonArray();
					dotProjectArray = new JsonArray();
					jsonFileArray = new JsonArray();
					esqlArray = new JsonArray();
					descriptorArray = new JsonArray();
					wsdlArray = new JsonArray();
					xsdArray = new JsonArray();
					cpyArray = new JsonArray();
					yamlArray = new JsonArray();
					propertiesArray = new JsonArray();
					otherArray = new JsonArray();

					switch (fileExtensions) {
					case "project":
						dotProjectArray.add(variablePath);
						break;
					case "msgflow":
						msgFlowArray.add(variablePath);
						break;
					case "subflow":
						subFlowArray.add(variablePath);
						break;
					case "esql":
						esqlArray.add(variablePath);
						break;
					case "json":
						jsonFileArray.add(variablePath);
						break;
					case "descriptor":
						descriptorArray.add(variablePath);
						break;
					case "wsdl":
						wsdlArray.add(variablePath);
						break;
					case "xsd":
						xsdArray.add(variablePath);
						break;
					case "cpy":
						cpyArray.add(variablePath);
						break;
					case "yaml":
						yamlArray.add(variablePath);
						break;
					case "properties":
						propertiesArray.add(variablePath);
						break;
					default:
						otherArray.add(variablePath);
					}
					previousService = currentService;
				}
			}
			eachFilePathObject.addProperty("serviceName", previousService);
			eachFilePathObject.add("msgflow", msgFlowArray);
			eachFilePathObject.add("subflow", subFlowArray);
			eachFilePathObject.add("project", dotProjectArray);
			eachFilePathObject.add("json", jsonFileArray);
			eachFilePathObject.add("esql", esqlArray);
			eachFilePathObject.add("descriptor", descriptorArray);
			eachFilePathObject.add("wsdl", wsdlArray);
			eachFilePathObject.add("xsd", xsdArray);
			eachFilePathObject.add("cpy", cpyArray);
			eachFilePathObject.add("yaml", yamlArray);
			eachFilePathObject.add("properties", propertiesArray);
			eachFilePathObject.add("otherFiles", otherArray);

			servicesArray.add(eachFilePathObject);

			allFilesJsonObject.addProperty("projectName", applicationName);
			allFilesJsonObject.addProperty("baseUrl", commonPath);
			allFilesJsonObject.add("serviceDetails", servicesArray);

			if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
				log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Each Project JSON Response :: "
						+ allFilesJsonObject);
			}

			eachProjectObject.add("projectStatus", new JsonObject());
			eachProjectObject.add("projectDetails", allFilesJsonObject);

		}

		return eachProjectObject;
	}
}

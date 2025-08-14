package com.exavalu.iib.analyzer.feature;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class SoapProjectCalculations {
	private static final Logger log = LoggerFactory.getLogger(SoapProjectCalculations.class);

	public int wsdlCounter(String wsdlFileName, String targetNamespace, JsonObject sourceFilePathsJsonObject,
			int schemaCount) {

		String sourceFileBasePath = sourceFilePathsJsonObject.getAsJsonObject("projectDetails").get("baseUrl")
				.getAsString();
		JsonArray servicesArray = sourceFilePathsJsonObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails");

		for (int servicesItr = 0; servicesItr < servicesArray.size(); servicesItr++) {
			JsonArray wsdlFiles = servicesArray.get(servicesItr).getAsJsonObject().getAsJsonArray("wsdl");

			for (int wsdlItr = 0; wsdlItr < wsdlFiles.size(); wsdlItr++) {
				if (wsdlFiles.get(wsdlItr).getAsString().contains(wsdlFileName)) {
					String wsdlFilePath = sourceFileBasePath + wsdlFiles.get(wsdlItr).getAsString();

					try {
						File inputFile = new File(wsdlFilePath);
						SAXReader reader = new SAXReader();
						reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
						Document document = reader.read(inputFile);
						List<Node> types = document.selectNodes("/definitions/types");
						if (types.size() == 0)
							types = document.selectNodes("/wsdl:definitions/wsdl:types");

						// *** Check for schemas in wsdl *** //
						if (types.size() != 0) {
							String schemaName = "";
							List<Node> schemas = document.selectNodes("/definitions/types/schema");
							if (schemas.size() == 0)
								schemas = document.selectNodes("/wsdl:definitions/wsdl:types/xsd:schema");

							if (schemas.size() != 0) {
								for (Node schema : schemas) {
									if (schema.valueOf("@targetNamespace").equals(targetNamespace)) {
										schemaCount++;
										List<Node> childNodes = schema.selectNodes("*");
										for (Node childNode : childNodes) {
											if (childNode.getName().equals("include")) {
												schemaName = childNode.valueOf("@schemaLocation");
											}
										}
										// *** read the xsd file for further imported schema files and count them *** //
										this.xsdCounter(schemaName, sourceFilePathsJsonObject, schemaCount,
												schema.valueOf("@targetNamespace"));
									}
								}
							}
						}

						List<Node> imports = document.selectNodes("/wsdl:definitions/wsdl:import");

						// *** Check for imported wsdl files *** //
						if (imports.size() != 0) {
							for (Node importNode : imports) {
								String importWsdlFileName = importNode.valueOf("@location");
								String importWsdlNamespace = importNode.valueOf("@namespace");

								// *** Recursive call for each imported wsdl file *** //
								this.wsdlCounter(importWsdlFileName, importWsdlNamespace, sourceFilePathsJsonObject,
										schemaCount);
							}
						}

					} catch (Exception exception) {
						if (AppGlobalDeclaration.isErrorLogEnabled) {
							log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "WSDL Counter :: "
									+ exception.getMessage());
							if (AppGlobalDeclaration.isStackTraceLogEnabled) {
								exception.getStackTrace();
							}
						}
					}
				}
			}
		}

		return schemaCount;
	}

	public int xsdCounter(String xsdFileName, JsonObject sourceFilePathsJsonObject, int schemaCount,
			String targetNamespace) {

		String sourceFileBasePath = sourceFilePathsJsonObject.getAsJsonObject("projectDetails").get("baseUrl")
				.getAsString();
		JsonArray servicesArray = sourceFilePathsJsonObject.getAsJsonObject("projectDetails")
				.getAsJsonArray("serviceDetails");

		for (int servicesItr = 0; servicesItr < servicesArray.size(); servicesItr++) {
			JsonArray xsdFiles = servicesArray.get(servicesItr).getAsJsonObject().getAsJsonArray("xsd");

			for (int xsdItr = 0; xsdItr < xsdFiles.size(); xsdItr++) {
				if (xsdFiles.get(xsdItr).getAsString().contains(xsdFileName)) {
					String xsdFilePath = sourceFileBasePath + xsdFiles.get(xsdItr).getAsString();

					try {
						File inputFile = new File(xsdFilePath);
						SAXReader reader = new SAXReader();
						reader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
						Document document = reader.read(inputFile);
						List<Node> schemas = document.selectNodes("/schema");
						if (schemas.size() == 0)
							schemas = document.selectNodes("/xsd:schema");

						if (schemas.size() != 0) {
							for (Node schema : schemas) {
								if (schema.valueOf("@targetNamespace").equals(targetNamespace)) {

									List<Node> importNodes = document.selectNodes("/schema/import");
									if (importNodes.size() == 0)
										importNodes = document.selectNodes("/xsd:schema/xsd:import");

									// *** Check for imported xsd files *** //
									if (importNodes.size() != 0) {
										for (Node importNode : importNodes) {
											schemaCount++;
											String importXsdFileName = importNode.valueOf("@schemaLocation");
											String importXsdNamespace = importNode.valueOf("@namespace");

											// *** Recursive call for each imported xsd file *** //
											this.xsdCounter(importXsdFileName, sourceFilePathsJsonObject, schemaCount,
													importXsdNamespace);
										}
									}
								}
							}
						}

					} catch (Exception exception) {
						if (AppGlobalDeclaration.isErrorLogEnabled) {
							log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "XSD Counter :: "
									+ exception.getMessage());
							if (AppGlobalDeclaration.isStackTraceLogEnabled) {
								exception.getStackTrace();
							}
						}
					}
				}
			}
		}

		return schemaCount;
	}

}

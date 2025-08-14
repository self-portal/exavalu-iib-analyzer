package com.exavalu.iib.analyzer.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;

public class MessageEnrichment {
	private static final Logger log = LoggerFactory.getLogger(MessageEnrichment.class);

	public JsonObject transformNodeCalculations(JsonObject nodeObject, JsonObject sourceFilePathsJsonObject) {

		JsonObject transformNodeJsonObj = new JsonObject();
		int transformNodeLoc = 0, conditionalCount = 0, loopsCount = 0;

		if (!sourceFilePathsJsonObject.get("projectDetails").isJsonNull()) {
//			int index1 = nodeObject.get("computeExpression").getAsString().indexOf("#") + 1;
//			int index2 = nodeObject.get("computeExpression").getAsString().lastIndexOf(".");
//			String transformNodeFileName = nodeObject.get("computeExpression").getAsString().substring(index1, index2);
//			String transformNodeSourceFolder = nodeObject.get("sourceFolder").getAsString();
			String basePath = sourceFilePathsJsonObject.get("projectDetails").getAsJsonObject().get("baseUrl")
					.getAsString();

			JsonArray serviceDeatilsArray = sourceFilePathsJsonObject.getAsJsonObject()
					.getAsJsonObject("projectDetails").getAsJsonArray("serviceDetails");

			try {
				BufferedReader transformNodeReader = null;
				String line = null;
				boolean multiLineComment = false;

				if (nodeObject.get("type").getAsString().contains("ComIbmCompute")) {
					int index1 = nodeObject.get("computeExpression").getAsString().indexOf("#") + 1;
					int index2 = nodeObject.get("computeExpression").getAsString().lastIndexOf(".");
					String transformNodeFileName = nodeObject.get("computeExpression").getAsString().substring(index1, index2);
					String transformNodeSourceFolder = nodeObject.get("sourceFolder").getAsString();
					
					String esqlModuleName = transformNodeFileName;
					JsonArray esqlsJsonArray = new JsonArray();

					for (int serviceArrItr = 0; serviceArrItr < serviceDeatilsArray.size(); serviceArrItr++) {
						if (serviceDeatilsArray.get(serviceArrItr).getAsJsonObject().get("serviceName").getAsString()
								.equals(transformNodeSourceFolder)) {
							esqlsJsonArray = serviceDeatilsArray.get(serviceArrItr).getAsJsonObject()
									.getAsJsonArray("esql");
						}
					}

					for (int esqlsItr = 0; esqlsItr < esqlsJsonArray.size(); esqlsItr++) {

						boolean moduleExist = false;
						String esqlFilePath = basePath + esqlsJsonArray.get(esqlsItr).getAsString();
						File esqlFile = new File(esqlFilePath);
						transformNodeReader = new BufferedReader(new FileReader(esqlFile));

						line = null;
						while ((line = transformNodeReader.readLine()) != null) {
							if (line.contains("CREATE COMPUTE MODULE") && line.contains(esqlModuleName)) {
								moduleExist = true;
							}

							if (moduleExist) {
								if (line.trim().startsWith("--")) {
									continue;
								}
								if (line.trim().equals("")) {
									continue;
								}
								if (line.startsWith("/*")) {
									multiLineComment = true;
								}
								if (multiLineComment) {
									if (line.endsWith("*/")) {
										multiLineComment = false;
									}
								}
								if (!multiLineComment) {
									if (line.endsWith("*/")) {
										continue;
									}
									transformNodeLoc++;

									if (line.contains("END IF") || line.contains("END CASE")) {
										conditionalCount++;
									}
									if (line.contains("END FOR") || line.contains("END WHILE")) {
										loopsCount++;
									}
								}
							}

							if (moduleExist && line.contains("END MODULE")) {
								break;
							}
						}
						transformNodeReader.close();
					}
				}
				if (nodeObject.get("type").getAsString().contains("ComIbmMSLMapping")) {
					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "map file found");
					}
					
					//Setting Map File - 03-10-2024
					String mapFileName= nodeObject.get("mappingExpression").toString().split("#")[1].replace("\"", ""); //ex: CreateIncident_Mapping
					basePath = (Paths.get("").toAbsolutePath().getRoot().toString()+ basePath).replace("\\\\", "\\");
					File mapFile = findFilebyFileName(basePath,mapFileName+".map");
					transformNodeReader = new BufferedReader(new FileReader(mapFile));

					line = null;
					while ((line = transformNodeReader.readLine()) != null) {
						if (line.contains("<!--")) {
							multiLineComment = true;
						}
						if (multiLineComment) {
							if (line.contains("-->")) {
								multiLineComment = false;
							}
						}
						if (!multiLineComment) {
							if (line.contains("-->")) {
								continue;
							}
							transformNodeLoc++;

							if (line.contains("</condition>")) {
								conditionalCount++;
							}
							if (line.contains("</foreach>")) {
								loopsCount++;
							}
						}
					}

				}
				if (nodeObject.get("type").getAsString().contains("ComIbmJavaCompute")) {
					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "java file found");
					}
					line = null;
					
					//Setting Java File - 03-10-2024
					String javaFileName= nodeObject.get("javaClass").toString().replace("\"", ""); //ex: TestClass
					javaFileName = javaFileName.substring(javaFileName.lastIndexOf('.') + 1); // Removing package name from class if present
					basePath = (Paths.get("").toAbsolutePath().getRoot().toString()+ basePath).replace("\\\\", "\\");
					File javaFile = findFilebyFileName(basePath,javaFileName+".java");
					transformNodeReader = new BufferedReader(new FileReader(javaFile));
					while ((line = transformNodeReader.readLine()) != null) {
						if (line.startsWith("//")) {
							continue;
						}
						if (line.trim().equals("")) {
							continue;
						}
						if (line.startsWith("/*")) {
							multiLineComment = true;
						}
						if (multiLineComment) {
							if (line.contains("*/")) {
								multiLineComment = false;
							}
						}
						if (!multiLineComment) {
							if (line.contains("*/")) {
								continue;
							}
							transformNodeLoc++;

							if (line.contains("if") || line.contains("else if") || line.contains("case")) {
								// correct it to condition count  03-10-24
								conditionalCount++;
							}
							if (line.contains("while") || line.contains("for")) {
								loopsCount++;
							}
						}
					}

				}
				if (nodeObject.get("type").getAsString().contains("ComIbmXslMqsi")) {
					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "xsl file found");
					}
					line = null;
					//Setting XSL File - 04-10-2024
					String xslFileName= nodeObject.get("stylesheetName").toString().replace("\"", ""); 
					basePath = (Paths.get("").toAbsolutePath().getRoot().toString()+ basePath).replace("\\\\", "\\");
					File xslFile = findFilebyFileName(basePath,xslFileName);
					transformNodeReader = new BufferedReader(new FileReader(xslFile));
					
					while ((line = transformNodeReader.readLine()) != null) {
						if (line.contains("<!--")) {
							multiLineComment = true;
						}
						if (multiLineComment) {
							if (line.contains("-->")) {
								multiLineComment = false;
							}
						}
						if (!multiLineComment) {
							if (line.contains("-->")) {
								continue;
							}
							transformNodeLoc++;

							if (line.contains("</xsl:if>") || line.contains("</xsl:choose>")) {
								// correct it to condition count  03-10-24
								conditionalCount++;
							}
							if (line.contains("</xsl:for-each>")) {
								loopsCount++;
							}
						}
					}

				}

				transformNodeReader.close();

			} catch (Exception exception) {
				if (AppGlobalDeclaration.isErrorLogEnabled) {
					log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Transform Node Calculations :: "
							+ exception.getMessage());
					if (AppGlobalDeclaration.isStackTraceLogEnabled) {
						exception.getStackTrace();
					}
				}
			}
		}

		transformNodeJsonObj.addProperty("transformNodeLoc", transformNodeLoc);
		transformNodeJsonObj.addProperty("conditionalCount", conditionalCount);
		transformNodeJsonObj.addProperty("loopsCount", loopsCount);

		return transformNodeJsonObj;
	}
	
	
	// Added New Function to Get file by File Name 03-10-24
	private File findFilebyFileName(String searchDirectory, String fileName) {
        File directory = new File(searchDirectory);
        
        // Check if the directory exists and is a directory
        if (!directory.exists() || !directory.isDirectory()) {
            return null;
        }

        // List all files and directories in the current directory
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                // If it's a directory, search recursively
                if (file.isDirectory()) {
                    File result = findFilebyFileName(file.getAbsolutePath(), fileName);
                    if (result != null) {
                        return result; // Found the file in a subdirectory
                    }
                } else if (file.isFile() && file.getName().equals(fileName)) {
                    return file; // Found the file
                }
            }
        }
        return null; // File not found
    }
}

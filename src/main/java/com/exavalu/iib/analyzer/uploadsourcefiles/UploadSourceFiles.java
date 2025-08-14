package com.exavalu.iib.analyzer.uploadsourcefiles;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.exavalu.iib.analyzer.feature.ApplicationTypeFinder;
import com.exavalu.iib.analyzer.file.manager.DirectoryManager;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class UploadSourceFiles {
	private static final Logger log = LoggerFactory.getLogger(UploadSourceFiles.class);
	@Autowired
	private EntityManager entityManager;

	@Value("${numberOfFilesToBeAllowed}")
	public int numberOfFilesAreAllowed;

	@Value("${rootDirectory}")
	public String rootDirectory;

	@PostMapping("/upload-source-files")
	@Transactional
	public String singleAppUpload(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestBody MultipartFile[] projectFiles, final HttpServletResponse httpServletResponse,
			@RequestParam(required = true) Map<String, String> params) throws Exception {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/upload-project-files endpoint invoked.");
		}

		int dbUploadResponse = 0;
		int nodesAndConnections = 0;
		String finalResponseAsString = "Empty Response.";
		JsonObject fileUploadRespJsonObj = new JsonObject();
		String userName = params.get("user_name");
		String jobName = params.get("job_name");
		String subJobName = params.get("sub_job_name");
		String requestorURL = "";
		String applicationName = "";
		String projectName = "";
		String masterId = "";
		String createGenericIdForFolder = "";
		JsonArray projectsArray = new JsonArray();
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		if (projectFiles.length <= numberOfFilesAreAllowed) {
			// Set the xRequestId as response header.
			httpServletResponse.setHeader("x-request-id", AppGlobalDeclaration.getxRequestId());

			ArrayList<String> applicationTypes = new ArrayList<String>();
			String dependencyValidation = "";
			String dependencyList = "";
			// flag used for entering validation in sub_jobs table
			boolean validFile = true;
			// making object for storing in database
			UploadSourceFileDbOperations dbOperation = new UploadSourceFileDbOperations();
			// method for storing job details which will call another method to save sub_job
			// details in database
			int jobId = dbOperation.saveJobDetails(userName, jobName, entityManager);
			int subJobId = dbOperation.saveSubJobDetails(userName, jobId, subJobName, true, entityManager);
			// total no. of files currently in the single subJob
			int subJobTotalFiles = dbOperation.getScoreDetail(jobId, subJobId, entityManager);

			// Validating if the total file are not exceeding the limit of 5 in subJob
			if (subJobTotalFiles + projectFiles.length <= numberOfFilesAreAllowed) {

				// START - VALIDATE THE REQUEST. IF THE FILES ARE .zip
				for (MultipartFile eachZipProjectFile : projectFiles) {
					createGenericIdForFolder = UUID.randomUUID().toString();
					if (eachZipProjectFile.getOriginalFilename().toLowerCase().endsWith(".zip")) {
						DirectoryManager directoryManager = new DirectoryManager();
						directoryManager.createDirectory(createGenericIdForFolder, requestorURL, fileUploadRespJsonObj,
								rootDirectory);
						FileUploadOperations fileUploadoperations = new FileUploadOperations();
						JsonObject projectsObject = fileUploadoperations.saveUploadedFilesDetails(
								createGenericIdForFolder, eachZipProjectFile, userName,
								eachZipProjectFile.getOriginalFilename());
						applicationName = eachZipProjectFile.getOriginalFilename();
						projectName = projectsObject.get("projectDetails").getAsJsonObject().get("projectName")
								.getAsString();

						// Method for validating if all the libraries are present in zip and get the
						// list of dependencies and its type
						dependencyValidation = DependencyFinder.dependencyValidator(projectsObject);
						if (dependencyValidation.contentEquals("true")) {
							dependencyList = DependencyFinder.dependencyFinder(projectsObject);
							applicationTypes = ApplicationTypeFinder.findApplicationType(projectsObject);

							// if zip doesn't contain main project
							if (applicationTypes.get(0) == "Callable" || applicationTypes.get(0) == "Other Application"
									|| applicationTypes.get(0) == "Library") {
								httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
								projectsObject.add("projectStatus",
										buildResponse(projectName, "400", "Bad Request",
												"The uploaded file " + eachZipProjectFile.getOriginalFilename()
														+ " is not a valid project"));
								projectsObject.add("projectDetails", null);
								projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());
								// validation changed if project is invalid
								validFile = false;
								// updating value of sub_job_valid in sub_jobs table
								updateSubJobValidation(validFile, subJobId);
								masterId = dbOperation.saveScoreDetails(createGenericIdForFolder, applicationName,
										projectName, jobId, subJobId, validFile, applicationTypes, dependencyList,
										userName, entityManager);
								continue;
							} else {
								// if file is valid
								validFile = true;
								projectsObject.add("projectStatus",
										buildResponse(projectName, "200", "OK", "Source file uploaded successfully"));
								masterId = dbOperation.saveScoreDetails(createGenericIdForFolder, applicationName,
										projectName, jobId, subJobId, validFile, applicationTypes, dependencyList,
										userName, entityManager);
								dbUploadResponse = dbOperation.savePathDetails(masterId, projectsObject, projectName,
										userName, entityManager);

								// *** START - NODES AND CONNECTIONS EXTRACTION ***//
								NodesAndConnectionsExtract nodesAndConnectionsArray = new NodesAndConnectionsExtract();
								nodesAndConnections = nodesAndConnectionsArray.nodesAndConnectionsExtractor(
										projectsObject, userName, masterId, entityManager);
								// *** END - NODES AND CONNECTIONS EXTRACTION ***//

								if (dbUploadResponse == 0 || nodesAndConnections == 0) {
									httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
									projectsObject.add("projectStatus", buildResponse(projectName, "500",
											"Internal Server Error", "Source file not uploaded in database"));
									projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());
								} else {
									projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());
								}
							}
						} else {
							// if file doesn't contain required libraries
							httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
							dependencyList = DependencyFinder.dependencyFinder(projectsObject);
							projectsObject.add("projectStatus",
									buildResponse(projectName, "400", "Bad Request", "The uploaded file " + projectName
											+ " does not contain required libraries i.e" + dependencyValidation));
							// validation changed if extension is invalid
							validFile = false;
							projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());

							// updating value of sub_job_valid in sub_jobs table
							updateSubJobValidation(validFile, subJobId);
							masterId = dbOperation.saveScoreDetails(createGenericIdForFolder, applicationName,
									projectName, jobId, subJobId, validFile, applicationTypes, dependencyList, userName,
									entityManager);
							continue;
						}
					} else {
						// if file is not in a zip format
						JsonObject projectsObject = new JsonObject();
						httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
						projectsObject.add("projectStatus", buildResponse(projectName, "400", "Bad Request",
								"The uploaded file " + projectName + " is not a zip file"));
						// validation changed if extension is invalid
						validFile = false;
						projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());

						// updating value of sub_job_valid in sub_jobs table
						updateSubJobValidation(validFile, subJobId);
						masterId = dbOperation.saveScoreDetails(createGenericIdForFolder, applicationName, projectName,
								jobId, subJobId, validFile, applicationTypes, dependencyList, userName, entityManager);
						continue;
					}
				}
			} else {
				// if subJob limit exceeded
				genericStatusResponse.setStatusCode("400");
				genericStatusResponse.setStatusReasonPhrase("Bad Request");
				genericStatusResponse.setStatusMessage(
						"You have exceeded the limit of files in this sub job. Please put the file in new sub Job");
				genericStatusResponse.setStatusPath("/upload-project-files");
				genericStatusResponse.setStatusTrace("");
				JsonObject projectsObject = new JsonObject();
				projectsObject.add("projectStatus", buildResponse("", "400", "Bad Request",
						"Number of uploaded files exceeded the limit i.e., " + numberOfFilesAreAllowed));
				projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());
				httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

				return projectsArray.toString();
			}
			// END - VALIDATE THE REQUEST.

			// Start executing the business logic.
			fileUploadRespJsonObj.add("status", genericStatusResponse.getGenericStatusResponse());
			fileUploadRespJsonObj.add("projects", projectsArray);

			// checking if file details stored in database or not
			if (dbUploadResponse == 1 && nodesAndConnections == 1) {
				if (AppGlobalDeclaration.isLogEnabled) {
					log.info(AppGlobalDeclaration.getxRequestId() + " :: "
							+ "Project file details Json data got inserted.");
				}
			} else {
				// If data upload to database fails then delete the files from file storage.
				DirectoryManager directoryManager = new DirectoryManager();
				directoryManager.deleteDirectory(rootDirectory, createGenericIdForFolder);
			}
		} else {
			// if no. of files is more than limits
			genericStatusResponse.setStatusCode("400");
			genericStatusResponse.setStatusReasonPhrase("Bad Request");
			genericStatusResponse
					.setStatusMessage("Number of uploaded files exceeded the limit i.e., " + numberOfFilesAreAllowed);
			genericStatusResponse.setStatusPath("/upload-project-files");
			genericStatusResponse.setStatusTrace("");
			JsonObject projectsObject = new JsonObject();
			projectsObject.add("projectStatus", buildResponse("", "400", "Bad Request",
					"Number of uploaded files exceeded the limit i.e., " + numberOfFilesAreAllowed));
			projectsArray.add(projectsObject.get("projectStatus").getAsJsonObject());
			httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.info(AppGlobalDeclaration.getxRequestId() + " :: "
						+ "Number of uploaded files exceeded the limit i.e., " + numberOfFilesAreAllowed);
			}
		}

		finalResponseAsString = projectsArray.toString();
		return finalResponseAsString;
	}

	// update the validSubJob in sub_jobs table
	public void updateSubJobValidation(boolean update, int subJobId) {

		String sql = "UPDATE sub_jobs SET sub_job_valid=? WHERE sub_job_id=?";
		entityManager.createNativeQuery(sql).setParameter(1, update).setParameter(2, subJobId).executeUpdate();
	}

	// Build response structure for this endpoint
	public JsonObject buildResponse(String projectName, String projectStatucCode, String projectStatusResonPhrase,
			String projectStatusMessage) throws Exception {
		JsonObject finalResponseJsonObject = new JsonObject();
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		genericStatusResponse.setProjectStatusCode(projectStatucCode);
		genericStatusResponse.setProjectStatusReasonPhrase(projectStatusResonPhrase);
		genericStatusResponse.setFileName(projectName);
		genericStatusResponse.setProjectStatusMessage(projectStatusMessage);

		finalResponseJsonObject = genericStatusResponse.getProjectStatusResponse();
		return finalResponseJsonObject;
	}
}

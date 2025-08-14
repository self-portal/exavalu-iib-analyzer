package com.exavalu.iib.analyzer.retrievealljobs;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class RetrieveAllJobs {
	private static final Logger log = LoggerFactory.getLogger(RetrieveAllJobs.class);

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@GetMapping("/retrieve-all-jobs")
	public String retrieveAllJobs(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> requestParams) {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/retrieve-all-jobs endpoint invoked.");
		}

		String finalResponseObjectAsString = "";
		String userName = requestParams.get("user_name");

		var query = callRetrieveAllJobInfoStoreProcedure(userName);
		query.execute();
		var resultSetIndex = 0;
		List<Object[]> resultSetOfJobs = null;
		List<Object[]> resultSetOfSubJobs = null;
		List<Object[]> resultSetOfSouceFiles = null;
		List ressultSetOfScannedFiles = null;
		do {
			var resultSet = query.getResultStream();
			switch (resultSetIndex) {
			case 0 -> resultSetOfJobs = resultSet.toList();
			case 1 -> resultSetOfSubJobs = resultSet.toList();
			case 2 -> resultSetOfSouceFiles = resultSet.toList();
			case 3 -> ressultSetOfScannedFiles = resultSet.toList();
			}
			resultSetIndex++;
		} while (query.hasMoreResults());
		JsonArray arrayOfJobs = new JsonArray();
		if (!resultSetOfJobs.isEmpty()) {
			for (int jobCount = 0; jobCount < resultSetOfJobs.size(); jobCount++) {
				arrayOfJobs.add(buildJobDetails(resultSetOfJobs.get(jobCount), resultSetOfSubJobs,
						resultSetOfSouceFiles, ressultSetOfScannedFiles));
			}
		}
		finalResponseObjectAsString = arrayOfJobs.toString();
		return finalResponseObjectAsString;
	}

	/* Call RetrieveAllJobInfo Store Procedure */
	public StoredProcedureQuery callRetrieveAllJobInfoStoreProcedure(String userName) {
		var query = entityManager.createStoredProcedureQuery("RetrieveAllJobsInfo");
		query.registerStoredProcedureParameter("userName", String.class, ParameterMode.IN);
		query.setParameter("userName", userName);

		return query;
	}

	/* Build Job details */
	@SuppressWarnings("rawtypes")
	public JsonObject buildJobDetails(Object[] jobInfo, List<Object[]> resultSetOfSubJobs,
			List<Object[]> resultSetOfSouceFiles, List ressultSetOfScannedFiles) {
		JsonObject objectForJobs = new JsonObject();
		objectForJobs.addProperty("jobId", jobInfo[0].toString());
		objectForJobs.addProperty("jobName", jobInfo[1].toString());
		objectForJobs.addProperty("jobDescription", (String) jobInfo[2]);

		if (!resultSetOfSubJobs.isEmpty()) {
			JsonArray arrayForSubJobs = new JsonArray();
			for (int subJobCount = 0; subJobCount < resultSetOfSubJobs.size(); subJobCount++) {
				var subJobInfo = resultSetOfSubJobs.get(subJobCount);
				if (jobInfo[0].equals(subJobInfo[1])) {
					arrayForSubJobs.add(buidSubJobDetails(resultSetOfSubJobs.get(subJobCount), resultSetOfSouceFiles,
							ressultSetOfScannedFiles));
				}
			}
			objectForJobs.add("subJobs", arrayForSubJobs);
		}
		return objectForJobs;
	}

	/* Build sub job details */
	@SuppressWarnings("rawtypes")
	public JsonObject buidSubJobDetails(Object[] subJobInfo, List<Object[]> resultSetOfSouceFiles,
			List ressultSetOfScannedFiles) {
		JsonObject objectForSubJobs = new JsonObject();
		objectForSubJobs.addProperty("subJobId", subJobInfo[0].toString());
		objectForSubJobs.addProperty("subJobName", subJobInfo[2].toString());
		objectForSubJobs.addProperty("subJobDescription", (String) subJobInfo[3]);
		objectForSubJobs.addProperty("isValid", (Boolean) subJobInfo[4]);

		if (!resultSetOfSouceFiles.isEmpty()) {
			JsonArray arrayForSourceFiles = new JsonArray();
			for (int sourceFileCount = 0; sourceFileCount < resultSetOfSouceFiles.size(); sourceFileCount++) {
				var sourceFileInfo = resultSetOfSouceFiles.get(sourceFileCount);
				if (subJobInfo[0].equals(sourceFileInfo[2])) {
					arrayForSourceFiles.add(buildSourceFileDetails(resultSetOfSouceFiles.get(sourceFileCount),
							ressultSetOfScannedFiles));
				}
			}
			objectForSubJobs.add("sourceFiles", arrayForSourceFiles);
		}
		return objectForSubJobs;
	}

	/* Build Source files Details */
	@SuppressWarnings("rawtypes")
	public JsonObject buildSourceFileDetails(Object[] sourceFileInfo, List ressultSetOfScannedFiles) {
		JsonObject objectForSourceFiles = new JsonObject();
		objectForSourceFiles.addProperty("sourceFileId", sourceFileInfo[1].toString());
		objectForSourceFiles.addProperty("sourceFileName", sourceFileInfo[3].toString());
		objectForSourceFiles.addProperty("isValid", (Boolean) sourceFileInfo[4]);
		objectForSourceFiles.addProperty("isScanned",
				checkScannedStatus(sourceFileInfo[0].toString(), ressultSetOfScannedFiles));

		return objectForSourceFiles;
	}

	@SuppressWarnings("rawtypes")
	public Boolean checkScannedStatus(String masterId, List ressultSetOfScannedFiles) {
		Boolean scannedStatus = false;
		for (int scannedStatusCount = 0; scannedStatusCount < ressultSetOfScannedFiles.size(); scannedStatusCount++) {
			if (((String) ressultSetOfScannedFiles.get(scannedStatusCount)).equals(masterId))
				return scannedStatus = true;
		}
		return scannedStatus;
	}
}

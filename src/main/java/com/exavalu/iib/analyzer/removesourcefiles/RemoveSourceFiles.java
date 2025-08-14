package com.exavalu.iib.analyzer.removesourcefiles;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.file.manager.DirectoryManager;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class RemoveSourceFiles {
	private static final Logger log = LoggerFactory.getLogger(RemoveSourceFiles.class);

	@Autowired
	private EntityManager entityManager;
	@Value("${rootDirectory}")
	public String rootDirecttory;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@DeleteMapping("/remove-source-files")
	@Transactional
	public String deleteSourceFiles(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(required = true) Map<String, String> requestParams,
			@RequestBody RemoveSourceFilesContainer requestBody) throws Exception {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/remove-source-files endpoint invoked.");
		}

		// String userName = requestParams.get("user_name");
		String finalResponseObjectAsString = "";

		for (int subJobCount = 0; subJobCount < requestBody.getSubJobs().size(); subJobCount++) {
			var subJobInfo = requestBody.getSubJobs().get(subJobCount);

			if (!subJobInfo.getIsDeleted()) {
				if (deleteFilesFromLocalAndDB(requestBody.getJobId(), subJobInfo.getSubJobId(),
						(List) subJobInfo.getSourceFilesId(), rootDirecttory, entityManager)) {
					finalResponseObjectAsString = buildResponse("200", "Ok", "File Deleted Successfully",
							"/remove-source-files", "").toString();
				}
			} else {
				var fileDeletedSuccessfully = deleteFilesFromLocalAndDB(requestBody.getJobId(),
						subJobInfo.getSubJobId(), (List) subJobInfo.getSourceFilesId(), rootDirecttory, entityManager);
				var subJobDeleted = deleteSubJobRecordFromDB(entityManager, subJobInfo.getSubJobId());
				if (fileDeletedSuccessfully || subJobDeleted == 1) {
					finalResponseObjectAsString = buildResponse("200", "Ok", "SubJob Deleted Successfully",
							"/remove-source-files", "").toString();
				}
			}
		}
		return finalResponseObjectAsString;
	}

	public Boolean deleteFilesFromLocalAndDB(String jobId, String subJobId, List<Object> SourceFileInfo,
			String rootDirecttory, EntityManager entityManager) throws Exception {
		var recordDeletedFromAllPlaces = false;
		var recordNotDeletedFromDB = true;
		var recordNotDeletedFromLocal = true;
		for (int sourceFileCount = 0; sourceFileCount < SourceFileInfo.size(); sourceFileCount++) {
			var masterId = jobId + "_" + subJobId + "_" + SourceFileInfo.get(sourceFileCount);
			DirectoryManager directoryManager = new DirectoryManager();
			recordNotDeletedFromLocal = directoryManager.deleteDirectory(rootDirecttory,
					SourceFileInfo.get(sourceFileCount).toString());
			recordNotDeletedFromDB = deleteFileRecordFromDB(entityManager, masterId);
		}
		if ((!recordNotDeletedFromDB && !recordNotDeletedFromLocal)
				|| (!recordNotDeletedFromDB && recordNotDeletedFromLocal))
			recordDeletedFromAllPlaces = true;
		return recordDeletedFromAllPlaces;
	}

	/* Delete source files records from DB */
	public Boolean deleteFileRecordFromDB(EntityManager entityManager, String masterId) throws Exception {
		Boolean rowNotAffected = true;
		var query = entityManager.createStoredProcedureQuery("DeleteSourceFilesRecord");
		query.registerStoredProcedureParameter("masterId", String.class, ParameterMode.IN);
		query.setParameter("masterId", masterId);
		rowNotAffected = query.execute();

		return rowNotAffected;
	}

	/* Delete SubJobs records from DB */
	public int deleteSubJobRecordFromDB(EntityManager entityManager, String SubJobId) throws Exception {
		int rowNotAffected = 0;
		String deleteQueryToSubJobDetails = "DELETE FROM sub_jobs WHERE sub_job_id=?";
		var query = entityManager.createNativeQuery(deleteQueryToSubJobDetails);
		query.setParameter(1, SubJobId);
		rowNotAffected = query.executeUpdate();
		return rowNotAffected;
	}

	/* Build response structure */
	public JsonObject buildResponse(String statusCode, String statusReasonPhrase, String statusMessage,
			String statusPath, String statusTrace) throws Exception {
		JsonObject finalResponseJsonObject = new JsonObject();
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		genericStatusResponse.setStatusCode(statusCode);
		genericStatusResponse.setStatusReasonPhrase(statusReasonPhrase);
		genericStatusResponse.setStatusMessage(statusMessage);
		genericStatusResponse.setStatusPath(statusPath);
		genericStatusResponse.setStatusTrace(statusTrace);

		finalResponseJsonObject = genericStatusResponse.getGenericStatusResponse();
		return finalResponseJsonObject;
	}
}

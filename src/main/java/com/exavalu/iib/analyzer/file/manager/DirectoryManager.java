package com.exavalu.iib.analyzer.file.manager;

import java.io.File;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;
import com.google.gson.JsonObject;

public class DirectoryManager {
	private static final Logger log = LoggerFactory.getLogger(DirectoryManager.class);
	private static String directoryLocation = "";

	public void createDirectory(String userRequestId, String requestorURL, JsonObject fileUploadResp,
			String rootDirectory) {
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		try {
			File mainDirectory = new File(rootDirectory + System.getProperty("file.separator") + userRequestId);
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Directory Location :: "
					+ mainDirectory.toString());
			DirectoryManager.directoryLocation = mainDirectory.toString();
			if (!mainDirectory.exists()) {
				boolean isDIrectory = mainDirectory.mkdirs();
				if (AppGlobalDeclaration.isLogEnabled) {
					if (isDIrectory == true) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Successfully created directory :: "
								+ userRequestId);
					} else {
						log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Unable to create directory :: "
								+ userRequestId);
					}
				}
			}
		} catch (Exception exception) {
			// If the Directory could not be created for the USER.
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "Unable to create directory :: "
						+ userRequestId);
			}

			genericStatusResponse.setStatusCode(AppGlobalDeclaration.STATUS_FAILED);
			genericStatusResponse.setStatusReasonPhrase("Bad Request");
			genericStatusResponse.setStatusMessage("Unable to create directory for current user");
			genericStatusResponse.setStatusPath(requestorURL);
			genericStatusResponse.setStatusTrace("");
		}
	}

	public static String getDirectory() {
		return directoryLocation;
	}

	public Boolean deleteDirectory(String rootDirectory, String userRequestId) {
		Boolean recordNotDeleted = true;
		try {
			String fileUploadedPath = rootDirectory + System.getProperty("file.separator") + userRequestId;
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Directory Location :: " + fileUploadedPath);
			File mainDirectory = new File(fileUploadedPath);
			FileUtils.deleteDirectory(mainDirectory);
			recordNotDeleted = mainDirectory.delete();
			if (AppGlobalDeclaration.isLogEnabled) {
				if (!mainDirectory.exists()) {
					log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Successfully delete directory :: "
							+ userRequestId);
				} else {
					log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "##### Unable to delete directory :: "
							+ userRequestId);
				}
			}
		} catch (Exception exception) {
			// If the Directory could not be deleted for the USER.
			if (AppGlobalDeclaration.isErrorLogEnabled) {
				log.error(AppGlobalDeclaration.getxRequestId() + " :: " + "##### Unable to delete directory :: "
						+ userRequestId);
			}
		}
		return recordNotDeleted;
	}
}

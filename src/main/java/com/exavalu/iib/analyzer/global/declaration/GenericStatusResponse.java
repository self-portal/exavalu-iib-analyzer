package com.exavalu.iib.analyzer.global.declaration;

import java.sql.Timestamp;
import com.google.gson.JsonObject;

public class GenericStatusResponse {
	Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	private String statusCode = AppGlobalDeclaration.STATUS_OK_CODE;
	private String statusReasonPhrase = AppGlobalDeclaration.STATUS_OK_PHRASE;
	private String projectFileName = "";
	private String statusMessage = AppGlobalDeclaration.STATUS_SUCCESS;
	private String statusPath = "";
	private String statusTrace = "Error Occured";

	private String projectStatusCode = AppGlobalDeclaration.STATUS_OK_CODE;
	private String projectStatusMessage = AppGlobalDeclaration.STATUS_SUCCESS;
	private String projectStatusReasonPhrase = AppGlobalDeclaration.STATUS_OK_PHRASE;

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String errStatus) {
		this.statusCode = errStatus;
	}

	public String getStatusReasonPhrase() {
		return statusReasonPhrase;
	}

	public void setStatusReasonPhrase(String statusReasonPhrase) {
		this.statusReasonPhrase = statusReasonPhrase;
	}

	public String getStatusMessage() {
		return statusMessage;
	}

	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
	}

	public String getStatusPath() {
		return statusPath;
	}

	public void setStatusPath(String statusPath) {
		this.statusPath = statusPath;
	}

	public String getStatusTrace() {
		return statusTrace;
	}

	public void setStatusTrace(String statusTrace) {
		this.statusTrace = statusTrace;
	}

	public String getProjectStatusCode() {
		return projectStatusCode;
	}

	public void setProjectStatusCode(String errStatus) {
		this.projectStatusCode = errStatus;
	}

	public String getProjectStatusReasonPhrase() {
		return projectStatusReasonPhrase;
	}

	public void setProjectStatusReasonPhrase(String statusReasonPhrase) {
		this.projectStatusReasonPhrase = statusReasonPhrase;
	}

	public void setFileName(String fileName) {
		this.projectFileName = fileName;
	}

	public String getFileName() {
		return projectFileName;
	}

	public String getProjectStatusMessage() {
		return projectStatusMessage;
	}

	public void setProjectStatusMessage(String statusMessage) {
		this.projectStatusMessage = statusMessage;
	}

	public JsonObject getGenericStatusResponse() {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("timestamp", currentTimestamp.toString());
		jsonObject.addProperty("statusCode", getStatusCode());
		jsonObject.addProperty("reasonPhrase", getStatusReasonPhrase());
		jsonObject.addProperty("message", getStatusMessage());
		jsonObject.addProperty("path", getStatusPath());
		if (!getStatusCode().equals(AppGlobalDeclaration.STATUS_OK_CODE)) {
			jsonObject.addProperty("trace", getStatusTrace());
		}

		return jsonObject;
	}

	public JsonObject getProjectStatusResponse() {
		JsonObject jsonObject = new JsonObject();

		jsonObject.addProperty("timestamp", currentTimestamp.toString());
		jsonObject.addProperty("statusCode", getProjectStatusCode());
		jsonObject.addProperty("reasonPhrase", getProjectStatusReasonPhrase());
		jsonObject.addProperty("fileName", getFileName());
		jsonObject.addProperty("message", getProjectStatusMessage());

		return jsonObject;
	}
}

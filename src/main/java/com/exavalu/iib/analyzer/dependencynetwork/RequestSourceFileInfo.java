package com.exavalu.iib.analyzer.dependencynetwork;

/**
 *
 * Last Updated: 2023-08-21 12:36
 *
 * Description: The `RequestSourceFileInfo` class holds the details of a source
 * file dependency network request. It includes various attributes like
 * `sourceFileMasterId`, `sourceFileId`, `parentJobId`, `parentSubJobId`, and
 * `sourceFileName`, that help identify the source file in the DB table and
 * fetch the project type and dependencies related to that.
 */

public class RequestSourceFileInfo {
	private String sourceFileMasterId;
	private String sourceFileName;

	public String getSourceFileMasterId() {
		return sourceFileMasterId;
	}

	public void setSourceFileMasterId(String sourceFileMasterId) {
		this.sourceFileMasterId = sourceFileMasterId;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}
}

package com.exavalu.iib.analyzer.myactivity;

/**
 *
 * Last Updated: 2023-08-11 21:08
 *
 * Description: The `SourceFileContainer` class serves as a data container for
 * information related to a source file (IIB Project Archive - ZIP File).
 */

public class SourceFileContainer {
	private String sourceFileMasterId;
	private String sourceFileId;
	private int parentJobId;
	private int parentSubJobId;
	private String sourceFileName;

	public String getSourceFileMasterId() {
		return sourceFileMasterId;
	}

	public void setSourceFileMasterId(String sourceFileMasterId) {
		this.sourceFileMasterId = sourceFileMasterId;
	}

	public String getSourceFileId() {
		return sourceFileId;
	}

	public void setSourceFileId(String sourceFileId) {
		this.sourceFileId = sourceFileId;
	}

	public int getParentJobId() {
		return parentJobId;
	}

	public void setParentJobId(int parentJobId) {
		this.parentJobId = parentJobId;
	}

	public int getParentSubJobId() {
		return parentSubJobId;
	}

	public void setParentSubJobId(int parentSubJobId) {
		this.parentSubJobId = parentSubJobId;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}

}

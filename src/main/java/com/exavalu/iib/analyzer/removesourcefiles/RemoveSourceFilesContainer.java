package com.exavalu.iib.analyzer.removesourcefiles;

import java.util.List;

public class RemoveSourceFilesContainer {
	private String jobId;
	private List<SubJobs> subJobs;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public List<SubJobs> getSubJobs() {
		return subJobs;
	}

	public void setSubJobs(List<SubJobs> subJobs) {
		this.subJobs = subJobs;
	}
}

class SubJobs {
	private String subJobId;
	private Boolean isDeleted;
	private List<String> sourceFilesId;

	public String getSubJobId() {
		return subJobId;
	}

	public void setSubJobId(String subJobId) {
		this.subJobId = subJobId;
	}

	public List<String> getSourceFilesId() {
		return sourceFilesId;
	}

	public void setSourceFilesId(List<String> sourceFilesId) {
		this.sourceFilesId = sourceFilesId;
	}

	public Boolean getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(Boolean isDeleted) {
		this.isDeleted = isDeleted;
	}
}

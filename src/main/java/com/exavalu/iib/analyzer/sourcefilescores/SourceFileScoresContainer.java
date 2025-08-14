package com.exavalu.iib.analyzer.sourcefilescores;

import java.util.List;

public class SourceFileScoresContainer {
	private String jobId;
	private String jobName;
	private String jobDescription;
	private List<SubJobs> subJobs;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getJobDescription() {
		return jobDescription;
	}

	public void setJobDescription(String jobDescription) {
		this.jobDescription = jobDescription;
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
	private String subJobName;
	private String subJobDescription;
	private List<SourceFiles> sourceFiles;

	public String getSubJobId() {
		return subJobId;
	}

	public void setSubJobId(String subJobId) {
		this.subJobId = subJobId;
	}

	public String getSubJobName() {
		return subJobName;
	}

	public void setSubJobName(String subJobName) {
		this.subJobName = subJobName;
	}

	public String getSubJobDescription() {
		return subJobDescription;
	}

	public void setSubJobDescription(String subJobDescription) {
		this.subJobDescription = subJobDescription;
	}

	public List<SourceFiles> getSourceFiles() {
		return sourceFiles;
	}

	public void setSourceFiles(List<SourceFiles> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}
}

class SourceFiles {
	private String sourceFileId;
	private String sourceFileName;

	public String getSourceFileId() {
		return sourceFileId;
	}

	public void setSourceFileId(String sourceFileId) {
		this.sourceFileId = sourceFileId;
	}

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}
}

package com.exavalu.iib.analyzer.myactivity;

import java.util.List;

/**
 *
 * Last Updated: 2023-08-11 21:12
 *
 * Description: The `MyActivityContainer` class acts as a container for user
 * activity data, encapsulating details such as total job counts, total sub-job
 * counts, job details, and login history. Nested within are three additional
 * classes: JobDetail, SubJob, LoginHistory.
 */

public class MyActivityContainer {
	private int totalJobsCount;
	private int totalSubJobsCount;
	private List<JobDetail> jobDetails;
	private LoginHistory loginHistory;

	public int getTotalJobsCount() {
		return totalJobsCount;
	}

	public void setTotalJobsCount(int totalJobsCount) {
		this.totalJobsCount = totalJobsCount;
	}

	public int getTotalSubJobsCount() {
		return totalSubJobsCount;
	}

	public void setTotalSubJobsCount(int totalSubJobsCount) {
		this.totalSubJobsCount = totalSubJobsCount;
	}

	public List<JobDetail> getJobDetails() {
		return jobDetails;
	}

	public void setJobDetails(List<JobDetail> jobDetails) {
		this.jobDetails = jobDetails;
	}

	public LoginHistory getLoginHistory() {
		return loginHistory;
	}

	public void setLoginHistory(LoginHistory loginHistory) {
		this.loginHistory = loginHistory;
	}
}

class JobDetail {
	private String jobName;
	private int jobId;
	private List<SubJob> subJobs;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public List<SubJob> getSubJobs() {
		return subJobs;
	}

	public void setSubJobs(List<SubJob> subJobs) {
		this.subJobs = subJobs;
	}
}

class SubJob {
	private String subJobName;
	private int subJobId;
	private List<SourceFile> sourceFiles;

	public String getSubJobName() {
		return subJobName;
	}

	public void setSubJobName(String subJobName) {
		this.subJobName = subJobName;
	}

	public int getSubJobId() {
		return subJobId;
	}

	public void setSubJobId(int subJobId) {
		this.subJobId = subJobId;
	}

	public List<SourceFile> getsourceFiles() {
		return sourceFiles;
	}

	public void setsourceFiles(List<SourceFile> sourceFiles) {
		this.sourceFiles = sourceFiles;
	}
}

class LoginHistory {
	private String lastLoginTimestamp;

	public String getLastLoginTimestamp() {
		return lastLoginTimestamp;
	}

	public void setLastLoginTimestamp(String lastLoginTimestamp) {
		this.lastLoginTimestamp = lastLoginTimestamp;
	}
}

class SourceFile {
	private String sourceFileMasterId;
	private String sourceFileId;
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

	public String getSourceFileName() {
		return sourceFileName;
	}

	public void setSourceFileName(String sourceFileName) {
		this.sourceFileName = sourceFileName;
	}
}
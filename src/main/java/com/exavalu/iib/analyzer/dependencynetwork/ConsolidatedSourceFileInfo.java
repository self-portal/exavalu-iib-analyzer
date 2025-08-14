package com.exavalu.iib.analyzer.dependencynetwork;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Last Updated: 2023-08-21 12:36
 *
 * Description: The `ConsolidatedSourceFileInfo` class encapsulates the
 * requested and retrieved (from the DB) information related to source files. It
 * consists of two main components, `RequestSourceFileInfo` representing the
 * requested information, and `RetrievedSourceFileInfo` representing the
 * retrieved details such as project type and dependencies.
 */

public class ConsolidatedSourceFileInfo {
	private RequestSourceFileInfo requestSourceFileInfo;
	private RetrievedSourceFileInfo retrievedSourceFileInfo;

	public ConsolidatedSourceFileInfo(RequestSourceFileInfo requestSourceFileInfo,
			RetrievedSourceFileInfo retrievedSourceFileInfo) {
		this.requestSourceFileInfo = requestSourceFileInfo;
		this.retrievedSourceFileInfo = retrievedSourceFileInfo;
	}

	public RequestSourceFileInfo getRequestSourceFileInfo() {
		return requestSourceFileInfo;
	}

	public void setRequestSourceFileInfo(RequestSourceFileInfo requestSourceFileInfo) {
		this.requestSourceFileInfo = requestSourceFileInfo;
	}

	public RetrievedSourceFileInfo getRetrievedSourceFileInfo() {
		return retrievedSourceFileInfo;
	}

	public void setRetrievedSourceFileInfo(RetrievedSourceFileInfo retrievedSourceFileInfo) {
		this.retrievedSourceFileInfo = retrievedSourceFileInfo;
	}
}

class RetrievedSourceFileInfo {
	private String sourceFileProjectType;
	private List<SourceFileDependency> sourceFileDependencies;

	public RetrievedSourceFileInfo(String sourceFileProjectType, List<SourceFileDependency> sourceFileDependencies) {
		this.sourceFileProjectType = sourceFileProjectType;
		setSourceFileDependencies(sourceFileDependencies);
	}

	public String getSourceFileProjectType() {
		return sourceFileProjectType;
	}

	public void setSourceFileProjectType(String sourceFileProjectType) {
		this.sourceFileProjectType = sourceFileProjectType;
	}

	public List<SourceFileDependency> getSourceFileDependencies() {
		return sourceFileDependencies == null ? null : new ArrayList<>(sourceFileDependencies);
	}

	public void setSourceFileDependencies(List<SourceFileDependency> sourceFileDependencies) {
		this.sourceFileDependencies = sourceFileDependencies;
	}

	static class SourceFileDependency {
		private String projectName;
		private String projectType;
		private List<String> dependencies;

		public String getProjectName() {
			return projectName;
		}

		public void setProjectName(String projectName) {
			this.projectName = projectName;
		}

		public String getProjectType() {
			return projectType;
		}

		public void setProjectType(String projectType) {
			this.projectType = projectType;
		}

		public List<String> getDependencies() {
			return dependencies == null ? null : new ArrayList<>(dependencies);
		}

		public void setDependencies(List<String> dependencies) {
			this.dependencies = dependencies;
		}
	}
}

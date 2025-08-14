package com.exavalu.iib.analyzer.dependencynetwork;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.exavalu.iib.analyzer.dependencynetwork.RetrievedSourceFileInfo.SourceFileDependency;
import com.exavalu.iib.analyzer.dependencynetwork.SourceTypeRepository.RetrievedSourceFileInfoProjection;

@Service
public class DependencyNetworkService {

	@Autowired
	private SourceTypeRepository sourceTypeRepository;

	// The primary method to fetch the dependency network
	public DependencyNetworkInfo fetchDependencyNetwork(List<RequestSourceFileInfo> requestSourceFileInfoList) {

		List<ConsolidatedSourceFileInfo> consolidatedList = new ArrayList<>();
		HashSet<String> unresolvedDependenciesFiles = new HashSet<>();

		List<ConsolidatedSourceFileInfo> consolidatedSourceFileInfoList = getConsolidatedSourceFileInfo(
				requestSourceFileInfoList, consolidatedList, unresolvedDependenciesFiles);

		HashSet<String> nodes = new HashSet<>();
		HashSet<LinkedHashSet<String>> edges = new HashSet<>();
		HashMap<String, String> projectTypes = new HashMap<>();

		for (ConsolidatedSourceFileInfo consolidatedSourceFileInfo : consolidatedSourceFileInfoList) {

			for (SourceFileDependency sourceFileDependency : consolidatedSourceFileInfo.getRetrievedSourceFileInfo()
					.getSourceFileDependencies()) {

				projectTypes.put(sourceFileDependency.getProjectName(), sourceFileDependency.getProjectType());

				nodes.add(sourceFileDependency.getProjectName());
				for (String dependency : sourceFileDependency.getDependencies()) {
					LinkedHashSet<String> edge = new LinkedHashSet<>();
					edge.add(sourceFileDependency.getProjectName());
					edge.add(dependency);
					edges.add(edge);
				}
			}
		}
		return new DependencyNetworkInfo(nodes, edges, projectTypes, unresolvedDependenciesFiles);
	}

	private List<ConsolidatedSourceFileInfo> getConsolidatedSourceFileInfo(
			List<RequestSourceFileInfo> requestSourceFileInfoList, List<ConsolidatedSourceFileInfo> consolidatedList,
			HashSet<String> unresolvedDependenciesFiles) {
		for (RequestSourceFileInfo requestSourceFileInfo : requestSourceFileInfoList) {
			boolean hasRecords = processRequestSourceFileInfo(consolidatedList, requestSourceFileInfo);
			if (!hasRecords) {
				unresolvedDependenciesFiles.add(requestSourceFileInfo.getSourceFileName());
			}
		}
		return consolidatedList;
	}

	// Process each Request & Retrieved SourceFileInfo and add to the consolidated
	// list
	private boolean processRequestSourceFileInfo(List<ConsolidatedSourceFileInfo> consolidatedSourceFileInfoList,
			RequestSourceFileInfo requestSourceFileInfo) {
		RetrievedSourceFileInfoProjection retrievedSourceFileInfoProjection = sourceTypeRepository
				.fetchDependenciesAndType(requestSourceFileInfo.getSourceFileMasterId(),
						requestSourceFileInfo.getSourceFileName());

		if (retrievedSourceFileInfoProjection == null) {
			return false; // Return false if no records found
		}

		JSONObject dependenciesObj = parseDependencies(retrievedSourceFileInfoProjection.getSourceFileDependencies());

		List<SourceFileDependency> sourceFileDependencies = processProjectDependenciesInfo(
				dependenciesObj.getJSONArray("projectDependenciesInfo"));

		consolidatedSourceFileInfoList
				.add(new ConsolidatedSourceFileInfo(createRequestSourceFileInfo(requestSourceFileInfo),
						new RetrievedSourceFileInfo(retrievedSourceFileInfoProjection.getSourceFileProjectType(),
								sourceFileDependencies)));
		return true;
	}

	// Parse the JSON string of source file dependencies
	private JSONObject parseDependencies(String sourceFileDependenciesString) {
		/*
		 * sourceFileDependenciesString = sourceFileDependenciesString.replace("\\\"",
		 * "\""); return new JSONObject(sourceFileDependenciesString.substring(1,
		 * sourceFileDependenciesString.length() - 1));
		 */
		return new JSONObject(sourceFileDependenciesString);
	}

	// Process the project dependencies information and return a list of
	// SourceFileDependency
	private List<SourceFileDependency> processProjectDependenciesInfo(JSONArray projectDependenciesInfo) {
		List<SourceFileDependency> sourceFileDependencies = new ArrayList<>();
		for (int i = 0; i < projectDependenciesInfo.length(); i++) {
			JSONObject projectDependency = projectDependenciesInfo.getJSONObject(i);
			sourceFileDependencies.add(createSourceFileDependency(projectDependency));
		}
		return sourceFileDependencies;
	}

	// Map data to sourceFileDependencies in retrievedSourceFileInfo
	private SourceFileDependency createSourceFileDependency(JSONObject projectDependency) {
		SourceFileDependency sourceFileDependency = new SourceFileDependency();
		sourceFileDependency.setProjectName(projectDependency.getString("projectName"));
		sourceFileDependency.setProjectType(projectDependency.getString("projectType").replaceAll("[\\[\\]{}]", ""));
		sourceFileDependency.setDependencies(getDependencies(projectDependency.getJSONArray("dependencies")));
		return sourceFileDependency;
	}

	// Extract dependencies from the retrieved JSON array of dependencies
	private List<String> getDependencies(JSONArray dependenciesToBeMapped) {
		List<String> dependencies = new ArrayList<>();
		for (int j = 0; j < dependenciesToBeMapped.length(); j++) {
			dependencies.add(dependenciesToBeMapped.getString(j)); // get the string at index j
		}
		return dependencies;
	}

	// Create and map RequestSourceFileInfo to ConsolidatedSourceFileInfo
	private RequestSourceFileInfo createRequestSourceFileInfo(RequestSourceFileInfo requestSourceFileInfo) {
		return new RequestSourceFileInfo() {
			{
				setSourceFileMasterId(requestSourceFileInfo.getSourceFileMasterId());
				setSourceFileName(requestSourceFileInfo.getSourceFileName());
			}
		};
	}
}

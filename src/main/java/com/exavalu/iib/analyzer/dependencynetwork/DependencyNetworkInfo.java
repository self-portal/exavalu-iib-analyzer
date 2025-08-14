package com.exavalu.iib.analyzer.dependencynetwork;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.HashMap;

public class DependencyNetworkInfo {
	private HashSet<String> nodes;
	private HashSet<LinkedHashSet<String>> edges;
	private HashMap<String, String> projectTypes;
	private HashSet<String> unresolvedDependenciesFiles;

	public DependencyNetworkInfo(HashSet<String> nodes, HashSet<LinkedHashSet<String>> edges,
			HashMap<String, String> projectTypes, HashSet<String> unresolvedDependenciesFiles) {
		this.nodes = nodes;
		this.edges = edges;
		this.unresolvedDependenciesFiles = unresolvedDependenciesFiles;
		this.projectTypes = projectTypes;
	}

	public HashSet<String> getNodes() {
		return nodes;
	}

	public void setNodes(HashSet<String> nodes) {
		this.nodes = nodes;
	}

	public HashSet<LinkedHashSet<String>> getEdges() {
		return edges;
	}

	public void setEdges(HashSet<LinkedHashSet<String>> edges) {
		this.edges = edges;
	}

	public HashMap<String, String> getProjectTypes() {
		return projectTypes;
	}

	public void setProjectTypes(HashMap<String, String> projectTypes) {
		this.projectTypes = projectTypes;
	}

	public HashSet<String> getUnresolvedDependenciesFiles() {
		return unresolvedDependenciesFiles;
	}

	public void setUnresolvedDependenciesFiles(HashSet<String> unresolvedDependenciesFiles) {
		this.unresolvedDependenciesFiles = unresolvedDependenciesFiles;
	}
}

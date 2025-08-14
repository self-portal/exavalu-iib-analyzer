package com.exavalu.iib.analyzer.admin.operation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParameterManagementContainer {
	@JsonProperty
	private String jobName;
	@JsonProperty
	private GeneralStructure_Listener_Method refListener;
	@JsonProperty
	private GeneralStructure_Listener_Method refMethod;
	@JsonProperty
	private GeneralStructure refNode;
	@JsonProperty
	private GeneralStructure refConnector;
	@JsonProperty
	private GeneralStructure refNodeProto;
	@JsonProperty
	private GeneralStructure refTransformNode;
	@JsonProperty
	private GeneralStructure refTransformLoc;
	@JsonProperty
	private GeneralStructure refTransformLoop;
	@JsonProperty
	private GeneralStructure refRoutePath;
	@JsonProperty
	private GeneralStructure refSchema;
	private List<RefEstmitationEffort> refEstmitationEffort;

	public List<RefEstmitationEffort> getRefEstmitationEffort() {
		return refEstmitationEffort;
	}

	public void setRefEstmitationEffort(List<RefEstmitationEffort> refEstmitationEffort) {
		this.refEstmitationEffort = refEstmitationEffort;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public GeneralStructure_Listener_Method getRefListner() {
		return refListener;
	}

	public void setRefListner(GeneralStructure_Listener_Method refListener) {
		this.refListener = refListener;
	}

	public GeneralStructure_Listener_Method getRefMethod() {
		return refMethod;
	}

	public void setRefMethod(GeneralStructure_Listener_Method refMethod) {
		this.refMethod = refMethod;
	}

	public GeneralStructure getRefNode() {
		return refNode;
	}

	public void setRefNode(GeneralStructure refNode) {
		this.refNode = refNode;
	}

	public GeneralStructure getRefConnector() {
		return refConnector;
	}

	public void setRefConnector(GeneralStructure refConnector) {
		this.refConnector = refConnector;
	}

	public GeneralStructure getRefNodeProto() {
		return refNodeProto;
	}

	public void setRefNodeProto(GeneralStructure refNodeProto) {
		this.refNodeProto = refNodeProto;
	}

	public GeneralStructure getRefTransformNode() {
		return refTransformNode;
	}

	public void setRefTransformNode(GeneralStructure refTransformNode) {
		this.refTransformNode = refTransformNode;
	}

	public GeneralStructure getRefTransformLoc() {
		return refTransformLoc;
	}

	public void setRefTransformLoc(GeneralStructure refTransformLoc) {
		this.refTransformLoc = refTransformLoc;
	}

	public GeneralStructure getRefTransformLoop() {
		return refTransformLoop;
	}

	public void setRefTransformLoop(GeneralStructure refTransformLoop) {
		this.refTransformLoop = refTransformLoop;
	}

	public GeneralStructure getRefRoutePath() {
		return refRoutePath;
	}

	public void setRefRoutePath(GeneralStructure refRoutePath) {
		this.refRoutePath = refRoutePath;
	}

	public GeneralStructure getRefSchema() {
		return refSchema;
	}

	public void setRefSchema(GeneralStructure refSchema) {
		this.refSchema = refSchema;
	}

}

class GeneralStructure {
	private String weightType;
	private int minLimit;
	private int maxLimit;

	public String getWeightType() {
		return weightType;
	}

	public void setWeightType(String weightType) {
		this.weightType = weightType;
	}

	public int getMinLimit() {
		return minLimit;
	}

	public void setMinLimit(int minLimit) {
		this.minLimit = minLimit;
	}

	public int getMaxLimit() {
		return maxLimit;
	}

	public void setMaxLimit(int maxLimit) {
		this.maxLimit = maxLimit;
	}

	public List<WeightInfo> getWeightInfo() {
		return weightInfo;
	}

	public void setWeightInfo(List<WeightInfo> weightInfo) {
		this.weightInfo = weightInfo;
	}

	private List<WeightInfo> weightInfo;
}

class WeightInfo {
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private int min;
	private int max;
	private int weight;

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}
}

class GeneralStructure_Listener_Method {
	private String weightType;
	private List<MethodWeightInfo> weightInfo;

	public String getWeightType() {
		return weightType;
	}

	public void setWeightType(String weightType) {
		this.weightType = weightType;
	}

	public List<MethodWeightInfo> getWeightInfo() {
		return weightInfo;
	}

	public void setWeightInfo(List<MethodWeightInfo> weightInfo) {
		this.weightInfo = weightInfo;
	}
}

class MethodWeightInfo {
	private int id;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	private String type;
	private int weight;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}

class RefEstmitationEffort {
	private int scoreMin;
	private int scoreMax;
	private String complexityLevel;
	private int estimatedHours;

	public int getScoreMin() {
		return scoreMin;
	}

	public void setScoreMin(int scoreMin) {
		this.scoreMin = scoreMin;
	}

	public int getScoreMax() {
		return scoreMax;
	}

	public void setScoreMax(int scoreMax) {
		this.scoreMax = scoreMax;
	}

	public String getComplexityLevel() {
		return complexityLevel;
	}

	public void setComplexityLevel(String complexityLevel) {
		this.complexityLevel = complexityLevel;
	}

	public int getEstimatedHours() {
		return estimatedHours;
	}

	public void setEstimatedHours(int estimatedHours) {
		this.estimatedHours = estimatedHours;
	}
}
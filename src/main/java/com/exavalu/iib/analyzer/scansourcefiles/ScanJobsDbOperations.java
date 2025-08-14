package com.exavalu.iib.analyzer.scansourcefiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class ScanJobsDbOperations {

	@SuppressWarnings("rawtypes")
	public JsonObject getNodesAndConnections(String sourceFileMasterId, EntityManager entityManager) {
		JsonObject nodesAndConnectionsJsonObject = new JsonObject();
		String nodesAndConnectionsStr = "";

		String statementToFindNodesAndConnections = "SELECT node_and_connection FROM nodes_connections WHERE source_file_master_id=?";
		Query queryToFindNodesAndConnections = entityManager.createNativeQuery(statementToFindNodesAndConnections)
				.setParameter(1, sourceFileMasterId);
		List resultSetOfNodesAndConnections = queryToFindNodesAndConnections.getResultList();

		if (!resultSetOfNodesAndConnections.isEmpty()) {
			nodesAndConnectionsStr = resultSetOfNodesAndConnections.get(0).toString();
		}

		Gson gsonObj = new Gson();
		nodesAndConnectionsJsonObject = gsonObj.fromJson(nodesAndConnectionsStr,
				nodesAndConnectionsJsonObject.getClass());

		return nodesAndConnectionsJsonObject;
	}

	@SuppressWarnings("rawtypes")
	public JsonObject getSourceFilePathDetails(String sourceFileMasterId, EntityManager entityManager) {
		JsonObject sourceFilePathsJsonObject = new JsonObject();
		String sourceFilePathsString = "";

		String statementToFindSourceFilePaths = "SELECT path_location FROM source_file_path_locations WHERE source_file_master_id=?";
		Query queryToFindSourceFilePaths = entityManager.createNativeQuery(statementToFindSourceFilePaths)
				.setParameter(1, sourceFileMasterId);
		List resultSetOfSourceFilePaths = queryToFindSourceFilePaths.getResultList();

		if (!resultSetOfSourceFilePaths.isEmpty()) {
			sourceFilePathsString = resultSetOfSourceFilePaths.get(0).toString();
		}

		Gson gsonObj = new Gson();
		sourceFilePathsJsonObject = gsonObj.fromJson(sourceFilePathsString, sourceFilePathsJsonObject.getClass());
		return sourceFilePathsJsonObject;
	}

	@SuppressWarnings("rawtypes")
	public List<String> getApplicationType(String sourceFileMasterId, EntityManager entityManager) {
		String applicationType = "";
		List<String> applicationTypes = new ArrayList<String>();
		String statementToFindApplicationType = "SELECT source_file_project_type FROM source_file_info WHERE source_file_master_id = ?";
		Query queryToFindApplicationType = entityManager.createNativeQuery(statementToFindApplicationType)
				.setParameter(1, sourceFileMasterId);
		List resultSetOfApplicationType = queryToFindApplicationType.getResultList();

		if (!resultSetOfApplicationType.isEmpty()) {
			applicationType = resultSetOfApplicationType.get(0).toString();
			String appTypeWithoutBrackets = applicationType.replaceAll("[\\[\\]{}]", "");
			applicationTypes = Arrays.asList(appTypeWithoutBrackets.split(","));
		}

		return applicationTypes;
	}

	public boolean saveConnectionTree(String sourceFileMasterId, String sourceFileName,
			JsonArray sourceFileConnectionTree, JsonArray nodeList, String user_name, EntityManager entityManager) {
		boolean dataGotInserted = false;
		int rowAffected = 0;
		String insertStatementForConnectionTree = """
                INSERT INTO message_orchestration_trees \
                (source_file_master_id, source_file_name, connection_tree, nodes, updated_by)\
                VALUES (?,?,?,?,?)\
                """;
		Query query = entityManager.createNativeQuery(insertStatementForConnectionTree)
				.setParameter(1, sourceFileMasterId).setParameter(2, sourceFileName)
				.setParameter(3, sourceFileConnectionTree.toString()).setParameter(4, nodeList.toString())
				.setParameter(5, user_name);

		rowAffected = query.executeUpdate();

		if (rowAffected == 1)
			dataGotInserted = true;

		return dataGotInserted;
	}

	public boolean saveMessageOrchestrationDetails(String sourceFileMasterId, String sourceFileName, String endpointUrl, String endpointType,
			String endpointMethod, int listenerSchemaCount, int endpointConnectorCount, int endpointNodeCount,
			int endpointTransformationCount, int endpointTransformationLoc, int endpointTransformationLoops,
			int endpointRoutes, int schemaCount, String user_name, EntityManager entityManager) {
		boolean dataGotInserted = false;
		int rowAffected = 0;
		String insertStatementForOrchestrationDetails = """
                insert into message_orchestration_details(source_file_master_id, source_file_name, mo_listener_url, \
                mo_endpoint_type, mo_listener_url_method, mo_listener_schema, mo_connectors, mo_nodes, mo_transform_nodes, mo_transform_loc, \
                mo_transform_loop, mo_message_routes, mo_schemas, updated_by) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)\
                """;
		Query query = entityManager.createNativeQuery(insertStatementForOrchestrationDetails)
				.setParameter(1, sourceFileMasterId).setParameter(2, sourceFileName).setParameter(3, endpointUrl).setParameter(4, endpointType)
				.setParameter(5, endpointMethod).setParameter(6, listenerSchemaCount)
				.setParameter(7, endpointConnectorCount).setParameter(8, endpointNodeCount)
				.setParameter(9, endpointTransformationCount).setParameter(10, endpointTransformationLoc)
				.setParameter(11, endpointTransformationLoops).setParameter(12, endpointRoutes)
				.setParameter(13, schemaCount).setParameter(14, user_name);

		rowAffected = query.executeUpdate();

		if (rowAffected == 1)
			dataGotInserted = true;

		return dataGotInserted;
	}
}

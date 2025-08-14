package com.exavalu.iib.analyzer.uploadsourcefiles;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

public class UploadSourceFileDbOperations {

	// inserting values into jobs table
	public int saveJobDetails(String userName, String jobName, EntityManager entityManager) {
		int rowAffected = 0;
		int jobId = getJobDetails(jobName, entityManager);
		if (jobId == 0) {
			String sql = "insert into jobs(job_name, updated_by) values(?,?)";
			Query query = entityManager.createNativeQuery(sql).setParameter(1, jobName).setParameter(2, userName);
			rowAffected = query.executeUpdate();
			// TODO: need to fetch jobId while creation(if possible) without calling for
			// select operation
			if (rowAffected == 1) {
				jobId = getJobDetails(jobName, entityManager);
			}
		}
		return jobId;
	}

	// getting value from jobs table
	@SuppressWarnings("unchecked")
	public int getJobDetails(String jobName, EntityManager entityManager) {
		int jobId = 0;
		String sql = "select job_id from jobs where job_name=?";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, jobName);
		List<Long> resultSetOfJobId = query.getResultList();
		if (!resultSetOfJobId.isEmpty()) {
			jobId = (int) (long) resultSetOfJobId.get(0);
		}
		return jobId;
	}

	// Inserting value in sub_jobs table
	public int saveSubJobDetails(String userName, int jobId, String subJobName, boolean validFile,
			EntityManager entityManager) {
		int rowAffected = 0;
		int subJobId = getSubJobDetails(jobId, subJobName, entityManager);
		if (subJobId == 0) {
			String sql = "insert into sub_jobs(parent_job_id, sub_job_name, sub_job_valid, updated_by) values(?,?,?,?)";
			Query query = entityManager.createNativeQuery(sql).setParameter(1, jobId).setParameter(2, subJobName)
					.setParameter(3, validFile).setParameter(4, userName);
			rowAffected = query.executeUpdate();

			// TODO: need to fetch subJobId while creation(if possible) without calling for
			// select
			// operation
			if (rowAffected == 1) {
				subJobId = getSubJobDetails(jobId, subJobName, entityManager);
			}
		}
		return subJobId;
	}

	// getting value from sub_jobs table
	@SuppressWarnings("unchecked")
	public int getSubJobDetails(int jobId, String subJobName, EntityManager entityManager) {
		int subJobId = 0;
		String sql = "select sub_job_id from sub_jobs where sub_job_name=? AND parent_job_id=?";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, subJobName).setParameter(2, jobId);
		List<Long> resultSetOfSubJobId = query.getResultList();
		if (!resultSetOfSubJobId.isEmpty()) {
			subJobId = (int) (long) resultSetOfSubJobId.get(0);
		}
		return subJobId;
	}

	// inserting value in source_file_path_location
	public int savePathDetails(String id, JsonObject projectDetails, String projectName, String userName,
			EntityManager entityManager) {
		String projectDetail = projectDetails.toString();
		String sql = "INSERT INTO source_file_path_locations(source_file_master_id, source_file_name, path_location, updated_by) VALUES (?,?,?,?)";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, id).setParameter(2, projectName)
				.setParameter(3, projectDetail).setParameter(4, userName);
		int rowAffected = query.executeUpdate();
		return rowAffected;
	}

	// inserting value in nodes_connection
	public int saveNodeDetails(String projectName, JsonObject nodesAndConnectionsJsonObject,
			JsonArray listAllIIBInputArr, String userName, String iD, EntityManager entityManager) {
		String nodesAndConnectionDetail = nodesAndConnectionsJsonObject.toString();
		String inputNode = listAllIIBInputArr.toString();
		String sql = "INSERT INTO nodes_connections(source_file_master_id, source_file_name, node_and_connection, input_nodes, updated_by) VALUES (?,?,?,?,?)";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, iD).setParameter(2, projectName)
				.setParameter(3, nodesAndConnectionDetail).setParameter(4, inputNode).setParameter(5, userName);
		int rowAffected = query.executeUpdate();
		return rowAffected;
	}

	// inserting value in source_file_score table
	public String saveScoreDetails(String uuid, String applicationName, String projectName, int joId, int subJobId,
			boolean validFile, ArrayList<String> applicationTypes, String dependeciesType, String userName,
			EntityManager entityManager) {
		String masterId = String.valueOf(joId) + "_" + String.valueOf(subJobId) + "_" + uuid;
		String sql = "INSERT INTO source_file_info(source_file_master_id, source_file_id, parent_job_id, parent_sub_job_id, source_file_original_name,source_file_name, source_file_valid, source_file_project_type, 	source_file_dependencies, updated_by) VALUES (?,?,?,?,?,?,?,?,?,?)";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, masterId).setParameter(2, uuid)
				.setParameter(3, joId).setParameter(4, subJobId).setParameter(5, applicationName)
				.setParameter(6, projectName).setParameter(7, validFile).setParameter(8, applicationTypes.toString())
				.setParameter(9, dependeciesType).setParameter(10, userName);
		query.executeUpdate();
		return masterId;
	}

	// Getting total number of APIs under the same job and sub_job from
	// source_file_score table
	@SuppressWarnings("unchecked")
	public int getScoreDetail(int jobId, int subJobId, EntityManager entityManager) {
		String sql = "select * from source_file_info where parent_job_id=? AND parent_sub_job_id=?";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, jobId).setParameter(2, subJobId);
		List<Object[]> resultSetOfScoreDetail = query.getResultList();
		return resultSetOfScoreDetail.size();
	}

}

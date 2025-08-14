package com.exavalu.iib.analyzer.myactivity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * Last Updated: 2023-08-11 21:05
 *
 * Description: The `MyActivityService` class retrieves and organizes user
 * activity data for a dashboard. Utilizing a stored procedure query, it
 * extracts details about jobs, sub-jobs, source files, and login history, and
 * assembles this information into a structured format for display in a user's
 * my activity dashboard.
 */

@Service
public class MyActivityService {
	// Autowiring the EntityManager to manage persistence context
	@Autowired
	private EntityManager entityManager;

	// Method to get my activity dashboard data for the given user
	@SuppressWarnings("unchecked")
	public MyActivityContainer getMyActivityDashboardData(String userName) {
		// Create the stored procedure query using the userName
		var query = createQuery(userName);
		query.execute();

		// Initialize containers for the my activity dashboard data
		var myActivityContainer = new MyActivityContainer();
		var jobs = new TreeMap<Integer, String>();
		var subJobs = new HashMap<Integer, List<Map<Integer, String>>>();
		var sourceFiles = new ArrayList<SourceFileContainer>();

		int resultSetIndex = 0;

		// Process the result sets returned from the stored procedure
		do {
			var resultSet = query.getResultList();
			switch (resultSetIndex) {
			case 0 -> processTotalCounts(resultSet, myActivityContainer); // Process total job & sub_jobs counts
			case 1 -> processJobs(resultSet, jobs); // Process jobs
			case 2 -> processSubJobs(resultSet, subJobs); // Process sub-jobs
			case 3 -> processSourceFiles(resultSet, sourceFiles); // Process source files
			case 4 -> processLoginHistory(resultSet, myActivityContainer, userName); // Process login history
			}
			resultSetIndex++;
		} while (query.hasMoreResults());

		buildFinalJobDetailsArray(jobs, subJobs, sourceFiles, myActivityContainer);

		return myActivityContainer;
	}

	// Method to create the stored procedure query
	private StoredProcedureQuery createQuery(String userName) {
		var query = entityManager.createStoredProcedureQuery("GetMyActivityDashboardData");
		query.registerStoredProcedureParameter("userName", String.class, ParameterMode.IN);
		query.setParameter("userName", userName);
		return query;
	}

	// Method to process the total counts of jobs and sub-jobs
	private void processTotalCounts(List<Object[]> resultSet, MyActivityContainer container) {
		var counts = resultSet.get(0);
		container.setTotalJobsCount(((Long) counts[0]).intValue());
		container.setTotalSubJobsCount(((Long) counts[1]).intValue());
	}

	// Method to process job details and add them to the jobs map
	private void processJobs(List<Object[]> resultSet, Map<Integer, String> jobs) {
		resultSet.forEach(job -> jobs.put(((Long) job[0]).intValue(), job[1].toString()));
	}

	// Method to process sub-job details and add them to the subJobs map
	private void processSubJobs(List<Object[]> resultSet, Map<Integer, List<Map<Integer, String>>> subJobs) {
		resultSet.forEach(subJob -> {
			var parentJobId = ((Long) subJob[1]).intValue();
			var subJobList = subJobs.computeIfAbsent(parentJobId, k -> new ArrayList<>());
			subJobList.add(Map.of(((Long) subJob[0]).intValue(), subJob[2].toString()));
		});
	}

	// Method to process source file details and add them to the sourceFiles list
	private void processSourceFiles(List<Object[]> resultSet, List<SourceFileContainer> sourceFiles) {
		resultSet.forEach(row -> {
			var sourceFileContainer = new SourceFileContainer();
			sourceFileContainer.setSourceFileMasterId(row[0].toString());
			sourceFileContainer.setSourceFileId(row[1].toString());
			sourceFileContainer.setParentJobId(((Long) row[2]).intValue());
			sourceFileContainer.setParentSubJobId(((Long) row[3]).intValue());
			sourceFileContainer.setSourceFileName(row[4].toString());
			sourceFiles.add(sourceFileContainer);
		});
	}

	// Method to process login history and add it to the container
	private void processLoginHistory(List<Object[]> resultSet, MyActivityContainer container, String userName) {
		var loginHistory = new LoginHistory();
		if (!resultSet.isEmpty()) {
			Object latestLoginRecord = resultSet.get(resultSet.size() - 1);
			Timestamp latestLoginTimestamp = (Timestamp) latestLoginRecord;
			loginHistory.setLastLoginTimestamp(latestLoginTimestamp.toString());
		} else {
			loginHistory.setLastLoginTimestamp("No login history found for the user - " + userName);
		}
		container.setLoginHistory(loginHistory);
	}

	// Method to construct the final Job Details Array and add it to the container
	private void buildFinalJobDetailsArray(TreeMap<Integer, String> jobs,
			HashMap<Integer, List<Map<Integer, String>>> subJobs, ArrayList<SourceFileContainer> sourceFiles,
			MyActivityContainer container) {

		// List to hold all job details
		List<JobDetail> jobDetailsList = new ArrayList<>();

		// Iterate over each job in the TreeMap
		for (Map.Entry<Integer, String> jobEntry : jobs.entrySet()) {
			Integer jobId = jobEntry.getKey();
			String jobName = jobEntry.getValue();

			// Create a new JobDetail object and set the job name
			JobDetail jobDetail = new JobDetail();
			jobDetail.setJobName(jobName);
			jobDetail.setJobId(jobId);

			// Get the associated sub-jobs for the current job ID
			List<Map<Integer, String>> associatedSubJobs = subJobs.get(jobId);
			List<SubJob> subJobsList = new ArrayList<>();

			// If there are associated sub-jobs, iterate over them
			if (associatedSubJobs != null) {
				for (Map<Integer, String> subJobMap : associatedSubJobs) {
					subJobMap.forEach((sub_job_id, sub_job_name) -> {
						// Create a new SubJob object and set the sub-job name
						SubJob subJob = new SubJob();
						subJob.setSubJobName(sub_job_name);
						subJob.setSubJobId(sub_job_id);

						// Filter the source files based on the current job ID and sub-job ID
						// and collect them into a list of SourceFile objects
						List<SourceFile> sourceFilesList = sourceFiles.stream()
								.filter(sf -> sf.getParentJobId() == jobId && sf.getParentSubJobId() == sub_job_id)
								.map(sf -> {
									SourceFile sourceFile = new SourceFile();
									sourceFile.setSourceFileMasterId(sf.getSourceFileMasterId());
									sourceFile.setSourceFileId(sf.getSourceFileId());
									sourceFile.setSourceFileName(sf.getSourceFileName());
									return sourceFile;
								}).collect(Collectors.toList());

						// Set the source file names for the current sub-job
						subJob.setsourceFiles(sourceFilesList);
						// Add the sub-job to the list of sub-jobs for the current job
						subJobsList.add(subJob);
					});
				}
			}

			// Set the sub-jobs for the current job detail
			jobDetail.setSubJobs(subJobsList);
			// Add the job detail to the list of all job details
			jobDetailsList.add(jobDetail);
		}

		// Set the job details in the container object
		container.setJobDetails(jobDetailsList);
	}
}

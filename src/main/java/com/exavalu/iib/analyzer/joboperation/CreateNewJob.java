package com.exavalu.iib.analyzer.joboperation;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class CreateNewJob {
	private static final Logger log = LoggerFactory.getLogger(CreateNewJob.class);

	@Autowired
	private EntityManager entityManager;

	@PostMapping("/create-job")
	@Transactional

	public String creatingJob(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestBody CreateJobContainer createJobRequest,
			@RequestParam(required = true) Map<String, String> params) {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/create-job endpoint invoked.");
		}

		String finalResponse = "";
		String userName = params.get("user_name");
		JsonObject finalResponseObject = new JsonObject();

		int jobId = getJobDetails(createJobRequest.getJobName());

		if (jobId == 0) {
			int rowAffected = 0;
			String sql = "insert into jobs(job_name, updated_by) values(?,?)";
			Query query = entityManager.createNativeQuery(sql).setParameter(1, createJobRequest.getJobName())
					.setParameter(2, userName);

			rowAffected = query.executeUpdate();
			if (rowAffected == 1) {
				jobId = getJobDetails(createJobRequest.getJobName());
			}

			finalResponseObject.addProperty("jobId", jobId);
			finalResponseObject.addProperty("message",
					("Job Name :: " + createJobRequest.getJobName() + " Successfully Created"));

		} else {
			finalResponseObject.addProperty("jobId", jobId);
			finalResponseObject.addProperty("message",
					("Job Name :: " + createJobRequest.getJobName() + " is already created"));
		}

		finalResponse = finalResponseObject.toString();
		return finalResponse;
	}

	@SuppressWarnings("unchecked")
	public int getJobDetails(String jobName) {
		int jobId = 0;
		String sql = "select job_id from jobs where job_name=?";
		Query query = entityManager.createNativeQuery(sql).setParameter(1, jobName);

		List<Long> resultSetOfJobId = query.getResultList();

		if (!resultSetOfJobId.isEmpty()) {
			jobId = (int) (long) resultSetOfJobId.get(0);
		}
		return jobId;
	}

}

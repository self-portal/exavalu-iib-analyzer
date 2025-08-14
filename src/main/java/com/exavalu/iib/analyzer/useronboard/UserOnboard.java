package com.exavalu.iib.analyzer.useronboard;

import java.sql.Timestamp;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.exavalu.iib.analyzer.global.declaration.GenericStatusResponse;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Query;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class UserOnboard {
	private static final Logger log = LoggerFactory.getLogger(UserOnboard.class);

	@Autowired
	private EntityManager entityManager;

	@SuppressWarnings({ "unchecked", "unused" })
	@PostMapping("/user-onboard")
	@Transactional
	public String validatingUserCredentials(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestHeader("x-region") String regionDetails, @RequestHeader("x-consumer") String browserDetails,
			@RequestHeader("x-tracking-id") String userTrackingId, @RequestBody UserOnboardContainer onboardingRequest,
			final HttpServletResponse httpServletResponse) throws Exception {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/user-onboard endpoint invoked.");
		}

		String finalResponseObjectAsString = "";
		String sqlQueryToFindUserCredentials = "SELECT user_name, email_id, password, first_name, mobile_no, active_status, role_id FROM users WHERE user_name = :userName";
		Query query = entityManager.createNativeQuery(sqlQueryToFindUserCredentials).setParameter("userName",
				onboardingRequest.getUserName());
		List<Object[]> resultSet = query.getResultList();
		if (!resultSet.isEmpty()) {
			var resultSetIndex = resultSet.get(0);
			if ((boolean) resultSetIndex[5]) {
				if (resultSetIndex[0].equals(onboardingRequest.getUserName()) && onboardingRequest.getEmailId() != null
						&& onboardingRequest.getMobileNo() != null) {
					finalResponseObjectAsString = buildResponse("400", "Bad Request", "UserName already registered!",
							"/user-onboard", "").toString();
					httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "UserName already registered !!!");
					}
				} else if ((!resultSetIndex[0].equals(onboardingRequest.getUserName())
						|| !resultSetIndex[2].equals(onboardingRequest.getPassword()))
						&& onboardingRequest.getEmailId() == null) {

					finalResponseObjectAsString = buildResponse("400", "Bad Request",
							"Log in Failed! Username or password mismatched", "/user-onboard", "").toString();

					httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.warn(AppGlobalDeclaration.getxRequestId() + " :: "
								+ "Log in Failed! Username or Password mismatched");
					}
				} else if (resultSetIndex[0].equals(onboardingRequest.getUserName())
						&& resultSetIndex[1].equals(onboardingRequest.getEmailId())) {
					if (updatePassword(onboardingRequest.getPassword(), resultSetIndex[0].toString()) == 1) {

						finalResponseObjectAsString = buildResponse("200", "OK", "Password updated successfully",
								"/user-onboard", "").toString();

						if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
							log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Password updated successfully");
						}
					} else {

						finalResponseObjectAsString = buildResponse("500", "Internal Server Error.",
								"Password updation failed.", "/user-onboard", "").toString();
						httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
						if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
							log.warn(AppGlobalDeclaration.getxRequestId() + " :: " + "Password updation failed.");
						}
					}

				} else if (resultSetIndex[0].equals(onboardingRequest.getUserName())
						&& onboardingRequest.getEmailId() != null
						&& !resultSetIndex[1].equals(onboardingRequest.getEmailId())) {
					finalResponseObjectAsString = buildResponse("400", "Bad Request", "Email Id is not correct",
							"/user-onboard", "").toString();
					httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.warn(AppGlobalDeclaration.getxRequestId() + " :: " + "Email Id is not correct");
					}
				} else if (resultSetIndex[0].equals(onboardingRequest.getUserName())
						&& resultSetIndex[2].equals(onboardingRequest.getPassword())) {
					updateLoginHistory(onboardingRequest.getUserName(), userTrackingId, browserDetails, regionDetails);
					JsonObject finalResponseObject = new JsonObject();

					finalResponseObject = buildResponse("200", "OK", "Sucessfully logged in", "/user-onboard", "");
					finalResponseObject.addProperty("firstName", resultSetIndex[3].toString());
					finalResponseObject.addProperty("UserRole", (int) resultSetIndex[6]);

					finalResponseObjectAsString = finalResponseObject.toString();

					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "Sucessfully logged in");
					}
				}
			} else {
				finalResponseObjectAsString = buildResponse("400", "Bad Request",
						"User is not active. Please contact to your administrator", "/user-onboard", "").toString();
				httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}
		} else {
			if (onboardingRequest.getEmailId() != null && onboardingRequest.getMobileNo() != null) {
				if (!callRegisterUserStoreProcedure(onboardingRequest, userTrackingId, browserDetails, regionDetails)) {
					finalResponseObjectAsString = buildResponse("201", "Created", "User Successfully Registered !!!",
							"/user-onboard", "").toString();
					httpServletResponse.setStatus(HttpServletResponse.SC_CREATED);

					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "User Successfully Registered !!!");
					}
				} else {
					finalResponseObjectAsString = buildResponse("500", "Internal Server Error",
							"User Registration Failed.", "/user-onboard", "").toString();
					httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

					if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
						log.warn(AppGlobalDeclaration.getxRequestId() + " :: " + "User Registration Failed.");
					}
				}
			} else {
				finalResponseObjectAsString = buildResponse("400", "Bad Request",
						"Log in Failed! Username is not registered.", "/user-onboard", "").toString();
				httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);

				if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
					log.warn(AppGlobalDeclaration.getxRequestId() + " :: "
							+ "Log in Failed! Username is not registered.");
				}
			}
		}

		return finalResponseObjectAsString;
	}

	// Update password for existing user
	public int updatePassword(String updatedPassword, String userName) throws Exception {
		int rowAffected = 0;
		String sqlForPasswordUpdate = "UPDATE users SET password=? where user_name=?";
		rowAffected = entityManager.createNativeQuery(sqlForPasswordUpdate).setParameter(1, updatedPassword)
				.setParameter(2, userName).executeUpdate();
		return rowAffected;
	}

	// Register a new user
	public Boolean callRegisterUserStoreProcedure(UserOnboardContainer onboardingRequest, String regionDetails,
			String browserDetails, String userTrackingId) throws Exception {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		Boolean userNotRegistered = true;
		var query = entityManager.createStoredProcedureQuery("RegisterUser");
		query.registerStoredProcedureParameter("userName", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("emailId", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("password", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("firstName", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("middleName", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("lastName", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("mobileNo", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("jobTitle", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("region", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("company", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("createTimestamp", Timestamp.class, ParameterMode.IN)
				.registerStoredProcedureParameter("location", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("browserDetails", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("ipAddress", String.class, ParameterMode.IN);

		query.setParameter("userName", onboardingRequest.getUserName())
				.setParameter("emailId", onboardingRequest.getEmailId())
				.setParameter("password", onboardingRequest.getPassword())
				.setParameter("firstName", onboardingRequest.getFirstName())
				.setParameter("middleName", onboardingRequest.getMiddleName())
				.setParameter("lastName", onboardingRequest.getLastName())
				.setParameter("mobileNo", onboardingRequest.getMobileNo())
				.setParameter("jobTitle", onboardingRequest.getJobTitle())
				.setParameter("region", onboardingRequest.getRegion())
				.setParameter("company", onboardingRequest.getCompany()).setParameter("createTimestamp", timestamp)
				.setParameter("location", regionDetails).setParameter("browserDetails", browserDetails)
				.setParameter("ipAddress", userTrackingId);
		userNotRegistered = query.execute();
		return userNotRegistered;
	}

	// Build response structure for this endpoint
	public JsonObject buildResponse(String statusCode, String statusReasonPhrase, String statusMessage,
			String statusPath, String statusTrace) throws Exception {
		JsonObject finalResponseJsonObject = new JsonObject();
		GenericStatusResponse genericStatusResponse = new GenericStatusResponse();
		genericStatusResponse.setStatusCode(statusCode);
		genericStatusResponse.setStatusReasonPhrase(statusReasonPhrase);
		genericStatusResponse.setStatusMessage(statusMessage);
		genericStatusResponse.setStatusPath(statusPath);
		genericStatusResponse.setStatusTrace(statusTrace);

		finalResponseJsonObject = genericStatusResponse.getGenericStatusResponse();
		return finalResponseJsonObject;
	}

	// Update Login history
	public void updateLoginHistory(String userName, String userTrackingId, String browserDetails, String regionDetails)
			throws Exception {
		Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		String updateStatement = "UPDATE login_history SET login_timestamp=?, login_location=?, login_browser=?, login_ip_address=? where user_name=?";
		Query query = entityManager.createNativeQuery(updateStatement).setParameter(1, timestamp)
				.setParameter(2, regionDetails).setParameter(3, browserDetails).setParameter(4, userTrackingId)
				.setParameter(5, userName);
		query.executeUpdate();
	}
}
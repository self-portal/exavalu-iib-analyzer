package com.exavalu.iib.analyzer.admin.operation;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class UserManagement {
	private static final Logger log = LoggerFactory.getLogger(UserManagement.class);

	Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
	@Autowired
	private EntityManager entityManager;

	@PutMapping("/role-management")
	@Transactional
	public String userManagement(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestBody List<UsersPojo> body, @RequestParam String user_name,
			final HttpServletResponse httpServletResponse) throws Exception {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/role-management endpoint invoked.");
		}

		String response = "";
		JsonObject responseJsonObj = new JsonObject();
		UserDbOperations dbOperation = new UserDbOperations();

		boolean operationStatus1 = false;
		boolean operationStatus2 = true;

		// *** Iterate for all users in payload *** //
		for (int usersItr = 0; usersItr < body.size(); usersItr++) {

			String userName = body.get(usersItr).getUserName();
			int activeStatus = body.get(usersItr).getActiveStatus();
			int roleId = body.get(usersItr).getRoleId();

			boolean userUpdated = dbOperation.updateUser(userName, activeStatus, roleId, entityManager);

			if (userUpdated) {
				operationStatus1 = true;
			} else {
				operationStatus2 = false;
			}

		}

		if (operationStatus1 && operationStatus2) {
			// success
			responseJsonObj.addProperty("timestamp", currentTimestamp.toString());
			responseJsonObj.addProperty("statusCode", "200");
			responseJsonObj.addProperty("reasonPhrase", "OK");
			responseJsonObj.addProperty("message", "Users Updated");
		} else if (!operationStatus1 && !operationStatus2) {
			// failure
			responseJsonObj.addProperty("timestamp", currentTimestamp.toString());
			responseJsonObj.addProperty("statusCode", "400");
			responseJsonObj.addProperty("reasonPhrase", "Bad Request");
			responseJsonObj.addProperty("message", "Users Updation Failed");
			httpServletResponse.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		} else if (operationStatus1 && !operationStatus2) {
			// partial
			responseJsonObj.addProperty("timestamp", currentTimestamp.toString());
			responseJsonObj.addProperty("statusCode", "200");
			responseJsonObj.addProperty("reasonPhrase", "OK");
			responseJsonObj.addProperty("message", "Users Partially Updated");
		}

		response = responseJsonObj.toString();

		return response;
	}

	@SuppressWarnings("unchecked")
	@GetMapping("/role-management")
	public String getAllUsers(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam String user_name) throws Exception {
		String allUsersStr = "";
		JsonArray allUsersJsonArray = new JsonArray();
		UserDbOperations dbOperation = new UserDbOperations();

		// *** Build response *** //
		List<Object[]> allUsersList = dbOperation.retrieveUsers(entityManager);

		for (int userItr = 0; userItr < allUsersList.size(); userItr++) {
			var userVar = allUsersList.get(userItr);
			JsonObject userResponseObject = new JsonObject();
			userResponseObject.addProperty("allUsersStr", userItr);
			userResponseObject.addProperty("userName", userVar[0].toString());
			userResponseObject.addProperty("emailId", userVar[1].toString());
			userResponseObject.addProperty("firstName", userVar[2].toString());
			if (userVar[3] != null) {
				userResponseObject.addProperty("middleName", userVar[3].toString());
			} else {
				userResponseObject.addProperty("middleName", "");
			}
			userResponseObject.addProperty("lastName", userVar[4].toString());
			userResponseObject.addProperty("mobileNo", userVar[5].toString());
			userResponseObject.addProperty("jobTitle", userVar[6].toString());
			userResponseObject.addProperty("region", userVar[7].toString());
			if (userVar[8] != null) {
				userResponseObject.addProperty("company", userVar[8].toString());
			} else {
				userResponseObject.addProperty("company", "");
			}
			userResponseObject.addProperty("createdTimeStamp", userVar[9].toString());
			userResponseObject.addProperty("updatedTimeStamp", userVar[10].toString());
			userResponseObject.addProperty("activeStatus", userVar[11].toString());
			userResponseObject.addProperty("roleId", userVar[12].toString());

			allUsersJsonArray.add(userResponseObject);
		}
		allUsersStr = allUsersJsonArray.toString();
		return allUsersStr;
	}

}

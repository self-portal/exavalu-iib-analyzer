package com.exavalu.iib.analyzer.myactivity;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;

import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * Last Updated: 2023-08-19 12:10
 *
 * Description: The MyActivityController class serves as a REST controller in a
 * Spring-based application, defining an endpoint to retrieve user activity data
 * for a dashboard.
 */
//Sets the base path and content type for this controller
@RestController
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class MyActivityController {
	private static final Logger log = LoggerFactory.getLogger(MyActivityController.class);
	private final MyActivityService myActivityService;

	// Constants for header and parameter names to improve maintainability
	// private static final String X_REQUEST_ID_HEADER = "x-request-id";
	private static final String USER_NAME_PARAM = "user_name";

	// Constructor-based dependency injection for the MyActivityService
	public MyActivityController(MyActivityService myActivityService) {
		this.myActivityService = myActivityService;
	}

	// Endpoint to retrieve the user's dashboard activity
	@GetMapping("/my-activity")
	public MyActivityContainer getMyActivity(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(USER_NAME_PARAM) final String userName, // Request parameter containing the user name
			HttpServletResponse response) // HttpServletResponse to modify the response headers
			throws IOException {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/my-activity endpoint invoked.");
		}

		// Set the "x-request-id" header in the response with the value from the request
		// header
		response.setHeader("x-request-id", AppGlobalDeclaration.getxRequestId());

		// Calls the service method to fetch the dashboard data and returns the result
		return myActivityService.getMyActivityDashboardData(userName);
	}
}

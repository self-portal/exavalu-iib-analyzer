package com.exavalu.iib.analyzer.dependencynetwork;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.exavalu.iib.analyzer.global.declaration.AppGlobalDeclaration;

@RestController
// Set the base path and content type for this controller
@RequestMapping(path = "${apiPrefix}" + "${securedString}", produces = "application/json")
public class DependencyNetworkController {
	private static final Logger log = LoggerFactory.getLogger(DependencyNetworkController.class);

	private final DependencyNetworkService dependencyNetworkService;

	// Constants for header and parameter names to improve maintainability
	private static final String X_REQUEST_ID_HEADER = "x-request-id";
	private static final String USER_NAME_PARAM = "user_name";

	// Constructor-based dependency injection for the DependencyNetworkService
	public DependencyNetworkController(DependencyNetworkService dependencyNetworkService) {
		this.dependencyNetworkService = dependencyNetworkService;
	}

	// Endpoint to retrieve the IIB project dependency network
	@PostMapping("/dependency-network")
	public ResponseEntity<?> getDependencyNetwork(@RequestHeader MultiValueMap<String, String> requestHeaders,
			@RequestParam(USER_NAME_PARAM) final String userName,
			@RequestBody List<RequestSourceFileInfo> requestSourceFileInfoList, HttpServletResponse response)
			throws IOException {

		AppGlobalDeclaration.setGenericRequestHeaders(requestHeaders);
		if (AppGlobalDeclaration.isLogEnabled && AppGlobalDeclaration.isDebugLogEnabled) {
			log.info("## Caller Request ID :: " + AppGlobalDeclaration.getxRequestId());
		}
		if (AppGlobalDeclaration.isLogEnabled) {
			log.info(AppGlobalDeclaration.getxRequestId() + " :: " + "/dependency-network endpoint invoked.");
		}

		// Set the "x-request-id" header in the response with the value from the request
		// header
		response.setHeader(X_REQUEST_ID_HEADER, AppGlobalDeclaration.getxRequestId());

		DependencyNetworkInfo dependencyNetworkInfo = dependencyNetworkService
				.fetchDependencyNetwork(requestSourceFileInfoList);

		return new ResponseEntity<>(dependencyNetworkInfo, HttpStatus.OK);
	}
}

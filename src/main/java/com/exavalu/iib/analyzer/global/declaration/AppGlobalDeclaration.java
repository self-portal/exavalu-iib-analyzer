package com.exavalu.iib.analyzer.global.declaration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.util.MultiValueMap;

public class AppGlobalDeclaration {
	/* ### LOG Flag ### */
	// isLogEnabled & isErrorLogEnabled flags are related to activity logging.
	public static final boolean isLogEnabled = true;
	public static final boolean isErrorLogEnabled = true;
	// isDebugLogEnabled & isDebugErrorLogEnabled flags are related to payload
	// logging.
	public static final boolean isDebugLogEnabled = false;
	public static final boolean isDebugErrorLogEnabled = false;

	public static final boolean isStackTraceLogEnabled = true;

	/* ### RESPONSE STATUS MESSAGE ### */
	public static final String STATUS_SUCCESS = "Success";
	public static final String STATUS_FAILED = "Failed";

	public static final String STATUS_OK_CODE = "200";
	public static final String STATUS_OK_PHRASE = "OK";
	public static final String STATUS_BAD_REQUEST_CODE = "400";
	public static final String STATUS_BAD_REQUEST_PHRASE = "Bad Request";

	// List Down Pre-Defined IIB Inputs 27-09-24
	public static final String[] preDefineIIBInputs = {
		    "ComIbmSAPInput",
		    "ComIbmSiebelInput",
		    "ComIbmPeopleSoftInput",
		    "ComIbmJDEdwardsInput",
		    "ComIbmDotNetInput",
		    "ComIbmJMSClientInput",
		    "ComIbmDatabaseInput",
		    "ComIbmEmailInput",
		    "ComIbmFileInput",
		    "ComIbmTCPIPClientInput",
		    "ComIbmTCPIPServerInput",
		    "ComIbmSOAPInput",
		    "ComIbmMQInput",
		    "ComIbmFTEInput",
		    "ComIbmCDInput",
		    "ComIbmWSInput",
		    "ComIbmApplicationConnectorInput_asana",
		    "ComIbmApplicationConnectorInput_cmis",
		    "ComIbmApplicationConnectorInput_coupa",
		    "ComIbmApplicationConnectorInput_github",
		    "ComIbmApplicationConnectorInput_gmail",
		    "ComIbmApplicationConnectorInput_googlecalendar",
		    "ComIbmApplicationConnectorInput_googlepubsub",
		    "ComIbmApplicationConnectorInput_googlesheet",
		    "ComIbmApplicationConnectorInput_ibmewm",
		    "ComIbmApplicationConnectorInput_ibmopenpages",
		    "ComIbmApplicationConnectorInput_insightly",
		    "ComIbmApplicationConnectorInput_servicenow",
		    "ComIbmApplicationConnectorInput_salesforce",
		    "ComIbmApplicationConnectorInput_surveymonkey",
		    "ComIbmApplicationConnectorInput_shopify",
		    "ComIbmApplicationConnectorInput_mondaydotcom",
		    "ComIbmApplicationConnectorInput_yammer",
		    "ComIbmApplicationConnectorInput_msteams",
		    "ComIbmApplicationConnectorInput_mssharepoint",
		    "ComIbmApplicationConnectorInput_msexchange",
		    "ComIbmApplicationConnectorInput_msexcel",
		    "ComIbmApplicationConnectorInput_msad",
		    "ComIbmApplicationConnectorInput_msdynamicscrmrest",
		    "ComIbmApplicationConnectorInput_mailchimp",
		    "ComIbmApplicationConnectorInput_oraclehcm",
		    "ComIbmApplicationConnectorInput_jira",
		    "ComIbmApplicationConnectorInput_wufoo",
		    "ComIbmApplicationConnectorInput_zendeskservice",
		    "com_ibm_connector_mqtt_ComIbmEventInput",
		    "com_ibm_connector_kafka_ComIbmEventInput",
		    "ComIbmScheduler"
		    };

	// List Down Pre-Defined IIB Connectors 27-09-24
	public static final String[] preDefineIIBConnectors = {
		    "ComIbmWSInput.msgnode",
		    "ComIbmApplicationConnectorRequest_amazoncloudwatch.msgnode",
		    "ComIbmApplicationConnectorRequest_amazondynamodb.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonec2.msgnode",
		    "ComIbmApplicationConnectorRequest_amazoneventbridge.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonkinesis.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonrds.msgnode",
		    "ComIbmApplicationConnectorRequest_amazons3.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonses.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonsns.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonsqs.msgnode",
		    "ComIbmApplicationConnectorRequest_anaplan.msgnode",
		    "ComIbmApplicationConnectorInput_asana.msgnode",
		    "ComIbmApplicationConnectorRequest_asana.msgnode",
		    "ComIbmApplicationConnectorRequest_amazonlambda.msgnode",
		    "ComIbmApplicationConnectorRequest_bamboohr.msgnode",
		    "ComIbmApplicationConnectorRequest_box.msgnode",
		    "ComIbmApplicationConnectorRequest_calendly.msgnode",
		    "ComIbmApplicationConnectorInput_cmis.msgnode",
		    "ComIbmApplicationConnectorRequest_cmis.msgnode",
		    "ComIbmApplicationConnectorRequest_confluence.msgnode",
		    "ComIbmCORBARequest.msgnode",
		    "ComIbmApplicationConnectorInput_coupa.msgnode",
		    "ComIbmApplicationConnectorRequest_coupa.msgnode",
		    "ComIbmDatabaseInput.msgnode",
		    "ComIbmDatabase.msgnode",
		    "ComIbmDatabaseRetrieve.msgnode",
		    "ComIbmChangeDataCapture.msgnode",
		    "ComIbmDatabaseRoute.msgnode",
		    "ComIbmApplicationConnectorRequest_docusign.msgnode",
		    "ComIbmApplicationConnectorRequest_dropbox.msgnode",
		    "ComIbmEmailInput.msgnode",
		    "ComIbmEmailOutput.msgnode",
		    "ComIbmApplicationConnectorRequest_eventbrite.msgnode",
		    "ComIbmApplicationConnectorRequest_expensify.msgnode",
		    "ComIbmFileInput.msgnode",
		    "ComIbmFileOutput.msgnode",
		    "ComIbmFileRead.msgnode",
		    "ComIbmFileExists.msgnode",
		    "ComIbmFileIterator.msgnode",
		    "ComIbmApplicationConnectorRequest_flexengage.msgnode",
		    "ComIbmApplicationConnectorInput_github.msgnode",
		    "ComIbmApplicationConnectorRequest_github.msgnode",
		    "ComIbmApplicationConnectorRequest_gitlab.msgnode",
		    "ComIbmApplicationConnectorInput_gmail.msgnode",
		    "ComIbmApplicationConnectorRequest_gmail.msgnode",
		    "ComIbmApplicationConnectorRequest_googlecalendar.msgnode",
		    "ComIbmApplicationConnectorInput_googlecalendar.msgnode",
		    "ComIbmApplicationConnectorRequest_googlebigquery.msgnode",
		    "ComIbmApplicationConnectorInput_googlepubsub.msgnode",
		    "ComIbmApplicationConnectorRequest_googlepubsub.msgnode",
		    "ComIbmApplicationConnectorRequest_googlecloudstorage.msgnode",
		    "ComIbmApplicationConnectorRequest_googlecontacts.msgnode",
		    "ComIbmApplicationConnectorRequest_googledrive.msgnode",
		    "ComIbmApplicationConnectorInput_googlesheet.msgnode",
		    "ComIbmApplicationConnectorRequest_googlesheet.msgnode",
		    "ComIbmApplicationConnectorRequest_googletranslate.msgnode",
		    "ComIbmApplicationConnectorRequest_greenhouse.msgnode",
		    "ComIbmWSReply.msgnode",
		    "ComIbmWSRequest.msgnode",
		    "ComIbmHTTPHeader.msgnode",
		    "ComIbmHTTPAsyncRequest.msgnode",
		    "ComIbmHTTPAsyncResponse.msgnode",
		    "ComIbmApplicationConnectorRequest_hubspotcrm.msgnode",
		    "ComIbmApplicationConnectorRequest_hubspotmarketing.msgnode",
		    "ComIbmCICSIPICRequest.msgnode",
		    "ComIbmApplicationConnectorRequest_ibmcoss3.msgnode",
		    "ComIbmApplicationConnectorRequest_cloudantdb.msgnode",
		    "ComIbmApplicationConnectorInput_ibmewm.msgnode",
		    "ComIbmApplicationConnectorRequest_ibmewm.msgnode",
		    "ComIbmIMSRequest.msgnode",
		    "ComIbmApplicationConnectorRequest_maximo.msgnode",
		    "ComIbmMQInput.msgnode",
		    "ComIbmMQOutput.msgnode",
		    "ComIbmMQReply.msgnode",
		    "ComIbmMQGet.msgnode",
		    "ComIbmMQHeader.msgnode",
		    "ComIbmPublication.msgnode",
		    "ComIbmFTEInput.msgnode",
		    "ComIbmFTEOutput.msgnode",
		    "ComIbmODMRules.msgnode",
		    "ComIbmApplicationConnectorInput_ibmopenpages.msgnode",
		    "ComIbmApplicationConnectorRequest_ibmopenpages.msgnode",
		    "ComIbmCDInput.msgnode",
		    "ComIbmCDOutput.msgnode",
		    "ComIbmApplicationConnectorRequest_ibmsterlingsci.msgnode",
		    "ComIbmApplicationConnectorInput_insightly.msgnode",
		    "ComIbmApplicationConnectorRequest_insightly.msgnode",
		    "ComIbmTCPIPClientInput.msgnode",
		    "ComIbmTCPIPServerOutput.msgnode",
		    "ComIbmTCPIPServerReceive.msgnode",
		    "ComIbmTCPIPServerInput.msgnode",
		    "ComIbmTCPIPClientOutput.msgnode",
		    "ComIbmTCPIPClientReceive.msgnode",
		    "ComIbmSOAPInput.msgnode",
		    "ComIbmSOAPReply.msgnode",
		    "ComIbmSOAPAsyncRequest.msgnode",
		    "ComIbmSOAPAsyncResponse.msgnode",
		    "ComIbmSOAPRequest.msgnode",
		    "ComIbmSOAPEnvelope.msgnode",
		    "ComIbmSOAPExtract.msgnode",
		    "SRRetrieveEntity.msgnode",
		    "SRRetrieveITService.msgnode",
		    "ComIbmApplicationConnectorRequest_snowflake.msgnode",
		    "ComIbmApplicationConnectorRequest_slack.msgnode",
		    "ComIbmApplicationConnectorInput_shopify.msgnode",
		    "ComIbmApplicationConnectorRequest_shopify.msgnode",
		    "ComIbmApplicationConnectorInput_servicenow.msgnode",
		    "ComIbmApplicationConnectorRequest_servicenow.msgnode",
		    "ComIbmApplicationConnectorRequest_sapsuccessfactors.msgnode",
		    "ComIbmApplicationConnectorRequest_sapariba.msgnode",
		    "ComIbmSAPInput.msgnode",
		    "ComIbmSAPRequest.msgnode",
		    "ComIbmSAPReply.msgnode",
		    "ComIbmApplicationConnectorRequest_sapodata.msgnode",
		    "ComIbmApplicationConnectorRequest_salesforcemc.msgnode",
		    "ComIbmApplicationConnectorRequest_salesforceae.msgnode",
		    "ComIbmApplicationConnectorInput_salesforce.msgnode",
		    "ComIbmApplicationConnectorRequest_salesforce.msgnode",
		    "com_ibm_connector_salesforce_ComIbmRequest.msgnode",
		    "ComIbmRESTRequest.msgnode",
		    "ComIbmRESTAsyncRequest.msgnode",
		    "ComIbmRESTAsyncResponse.msgnode",
		    "ComIbmAppConnectRESTRequest.msgnode",
		    "ComIbmSiebelInput.msgnode",
		    "ComIbmSiebelRequest.msgnode",
		    "ComIbmPeopleSoftInput.msgnode",
		    "ComIbmPeopleSoftRequest.msgnode",
		    "ComIbmJDEdwardsInput.msgnode",
		    "ComIbmJDEdwardsRequest.msgnode",
		    "ComIbmApplicationConnectorInput_oraclehcm.msgnode",
		    "ComIbmApplicationConnectorRequest_oraclehcm.msgnode",
		    "ComIbmApplicationConnectorRequest_oracleebs.msgnode",
		    "ComIbmDotNetInput.msgnode",
		    "com_ibm_connector_mqtt_ComIbmEventInput.msgnode",
		    "com_ibm_connector_mqtt_ComIbmOutput.msgnode",
		    "ComIbmApplicationConnectorInput_mondaydotcom.msgnode",
		    "ComIbmApplicationConnectorRequest_mondaydotcom.msgnode",
		    "ComIbmApplicationConnectorInput_yammer.msgnode",
		    "ComIbmApplicationConnectorRequest_yammer.msgnode",
		    "ComIbmApplicationConnectorRequest_mstodo.msgnode",
		    "ComIbmApplicationConnectorInput_msteams.msgnode",
		    "ComIbmApplicationConnectorRequest_msteams.msgnode",
		    "ComIbmApplicationConnectorInput_mssharepoint.msgnode",
		    "ComIbmApplicationConnectorRequest_mssharepoint.msgnode",
		    "ComIbmApplicationConnectorRequest_mspowerbi.msgnode",
		    "ComIbmApplicationConnectorRequest_msonenote.msgnode",
		    "ComIbmApplicationConnectorRequest_msonedrive.msgnode",
		    "ComIbmApplicationConnectorInput_msexchange.msgnode",
		    "ComIbmApplicationConnectorRequest_msexchange.msgnode",
		    "ComIbmApplicationConnectorInput_msexcel.msgnode",
		    "ComIbmApplicationConnectorRequest_msexcel.msgnode",
		    "ComIbmApplicationConnectorRequest_azuread.msgnode",
		    "ComIbmApplicationConnectorInput_msdynamicscrmrest.msgnode",
		    "ComIbmApplicationConnectorRequest_msdynamicscrmrest.msgnode",
		    "ComIbmApplicationConnectorRequest_msdynamicsfando.msgnode",
		    "ComIbmApplicationConnectorRequest_azureblobstorage.msgnode",
		    "ComIbmApplicationConnectorInput_msad.msgnode",
		    "ComIbmApplicationConnectorRequest_msad.msgnode",
		    "ComIbmApplicationConnectorRequest_marketo.msgnode",
		    "ComIbmApplicationConnectorInput_surveymonkey.msgnode",
		    "ComIbmApplicationConnectorRequest_surveymonkey.msgnode",
		    "ComIbmApplicationConnectorRequest_square.msgnode",
		    "ComIbmApplicationConnectorInput_mailchimp.msgnode",
		    "ComIbmApplicationConnectorRequest_mailchimp.msgnode",
		    "ComIbmApplicationConnectorRequest_magento.msgnode",
		    "com_ibm_connector_loopback_ComIbmRequest.msgnode",
		    "com_ibm_connector_kafka_ComIbmEventInput.msgnode",
		    "com_ibm_connector_kafka_ComIbmRequest.msgnode",
		    "com_ibm_connector_kafka_ComIbmOutput.msgnode",
		    "ComIbmJMSClientInput.msgnode",
		    "ComIbmJMSClientOutput.msgnode",
		    "ComIbmJMSClientReply.msgnode",
		    "ComIbmJMSClientReceive.msgnode",
		    "ComIbmJMSHeader.msgnode",
		    "ComIbmJMSMQTransform.msgnode",
		    "ComIbmMQJMSTransform.msgnode",
		    "ComIbmApplicationConnectorInput_jira.msgnode",
		    "ComIbmApplicationConnectorRequest_jira.msgnode",
		    "ComIbmApplicationConnectorRequest_jenkins.msgnode",
		    "ComIbmApplicationConnectorRequest_twilio.msgnode",
		    "ComIbmApplicationConnectorRequest_trello.msgnode",
		    "ComIbmApplicationConnectorRequest_kronos.msgnode",
		    "ComIbmApplicationConnectorRequest_wordpress.msgnode",
		    "ComIbmApplicationConnectorInput_wufoo.msgnode",
		    "ComIbmApplicationConnectorRequest_wufoo.msgnode",
		    "ComIbmApplicationConnectorInput_zendeskservice.msgnode",
		    "ComIbmApplicationConnectorRequest_zendeskservice.msgnode",
		    "ComIbmSCAInput.msgnode",
		    "ComIbmTimeoutNotification.msgnode"
		};

	public static final String[] preDefineIIBTransformations = { 
			"ComIbmCompute.msgnode", 
			"ComIbmMSLMapping.msgnode",
			"ComIbmXslMqsi.msgnode", 
			"ComIbmJavaCompute.msgnode" 
			};
	
	 // Mapping xmi-type to Application-Type 27-09-24
	 public static final Map<String, String> iibInputXmiTypeMap = Map.ofEntries(
		        Map.entry("ComIbmWSInput", "HTTP"),
		        Map.entry("ComIbmMQInput", "Pub/Sub (MQ) Based"),
		        Map.entry("ComIbmSOAPInput", "SOAP"),
		        Map.entry("ComIbmJMSClientInput", "JMS"),
		        Map.entry("ComIbmPeopleSoftInput", "PeopleSoft"),
		        Map.entry("ComIbmSAPInput", "SAP"),
		        Map.entry("ComIbmSiebelInput", "Siebel"),
		        Map.entry("ComIbmDotNetInput", ".NET"),
		        Map.entry("ComIbmDatabaseInput", "Database"),
		        Map.entry("ComIbmFileInput", "File"),
		        Map.entry("ComIbmFTEInput", "FTE"),
		        Map.entry("ComIbmCDInput", "CD"),
		        Map.entry("ComIbmEmailInput", "Email"),
		        Map.entry("ComIbmTCPIPClientInput", "TCP/IP Client"),
		        Map.entry("ComIbmTCPIPServerInput", "TCP/IP Server"),
		        Map.entry("ComIbmApplicationConnectorInput_asana", "Asana"),
		        Map.entry("ComIbmApplicationConnectorInput_cmis", "CMIS"),
		        Map.entry("ComIbmApplicationConnectorInput_coupa", "Coupa"),
		        Map.entry("ComIbmApplicationConnectorInput_github", "GitHub"),
		        Map.entry("ComIbmApplicationConnectorInput_gmail", "Gmail"),
		        Map.entry("ComIbmApplicationConnectorInput_googlecalendar", "Google Calendar"),
		        Map.entry("ComIbmApplicationConnectorInput_googlepubsub", "Google Pub/Sub"),
		        Map.entry("ComIbmApplicationConnectorInput_googlesheet", "Google Sheet"),
		        Map.entry("ComIbmApplicationConnectorInput_ibmewm", "IBM EWM"),
		        Map.entry("ComIbmApplicationConnectorInput_ibmopenpages", "IBM OpenPages"),
		        Map.entry("ComIbmApplicationConnectorInput_insightly", "Insightly"),
		        Map.entry("ComIbmApplicationConnectorInput_mondaydotcom", "Monday.com"),
		        Map.entry("ComIbmApplicationConnectorInput_yammer", "Yammer"),
		        Map.entry("ComIbmApplicationConnectorInput_msteams", "Microsoft Teams"),
		        Map.entry("ComIbmApplicationConnectorInput_mssharepoint", "Microsoft SharePoint"),
		        Map.entry("ComIbmApplicationConnectorInput_msexchange", "Microsoft Exchange"),
		        Map.entry("ComIbmApplicationConnectorInput_msexcel", "Microsoft Excel"),
		        Map.entry("ComIbmApplicationConnectorInput_msdynamicscrmrest", "Microsoft Dynamics CRM"),
		        Map.entry("ComIbmApplicationConnectorInput_msad", "Microsoft Active Directory"),
		        Map.entry("ComIbmApplicationConnectorInput_surveymonkey", "SurveyMonkey"),
		        Map.entry("ComIbmApplicationConnectorInput_mailchimp", "Mailchimp"),
		        Map.entry("com_ibm_connector_kafka_ComIbmEventInput", "Kafka"),
		        Map.entry("ComIbmApplicationConnectorInput_shopify", "Shopify"),
		        Map.entry("ComIbmApplicationConnectorInput_salesforce", "Salesforce"),
		        Map.entry("com_ibm_connector_mqtt_ComIbmEventInput", "MQTT"),
		        Map.entry("ComIbmJDEdwardsInput", "JD Edwards"),
		        Map.entry("ComIbmApplicationConnectorInput_oraclehcm", "Oracle HCM"),
		        Map.entry("ComIbmApplicationConnectorInput_jira", "Jira"),
		        Map.entry("ComIbmApplicationConnectorInput_wufoo", "Wufoo"),
		        Map.entry("ComIbmApplicationConnectorInput_zendeskservice", "Zendesk"),
		        Map.entry("ComIbmApplicationConnectorInput_servicenow","ServiceNow"),
		        Map.entry("ComIbmScheduler","Scheduler")
		    );
	 
	 // Mapping Application-Type with Endpoint-Specifier 30-09-24
	 public static final Map<String, List<String>> iibInputEndPointMapping = Map.ofEntries(
		        Map.entry("HTTP", Arrays.asList("URLSpecifier")),
		        Map.entry("Pub/Sub (MQ) Based", Arrays.asList("queueName")),
		        Map.entry("SOAP", Arrays.asList("urlSelector")),
		        Map.entry("Email", Arrays.asList("emailServer")),
		        Map.entry("FTE", Arrays.asList("inputDirectory")),
		        Map.entry("JMS", Arrays.asList("sourceQueueName", "topic")),
		        Map.entry("PeopleSoft", Arrays.asList("adapterComponent")),
		        Map.entry("SAP", Arrays.asList("adapterComponent")),
		        Map.entry("Siebel", Arrays.asList("adapterComponent")),
		        Map.entry(".NET", Arrays.asList("assemblyName")),
		        Map.entry("Database", Arrays.asList("dataSource")),
		        Map.entry("File", Arrays.asList("inputDirectory")),
		        Map.entry("CD", Arrays.asList("inputDirectory")),
		        Map.entry("TCP/IP Client", Arrays.asList("connectionDetails")),
		        Map.entry("TCP/IP Server", Arrays.asList("connectionDetails")),
		        Map.entry("Gmail", Arrays.asList("schemaPrefix")),
		        Map.entry("Asana", Arrays.asList("schemaPrefix")),
		        Map.entry("CMIS", Arrays.asList("schemaPrefix")),
		        Map.entry("Coupa", Arrays.asList("schemaPrefix")),
		        Map.entry("GitHub", Arrays.asList("schemaPrefix")),
		        Map.entry("Google Calendar", Arrays.asList("schemaPrefix")),
		        Map.entry("Google Pub/Sub", Arrays.asList("schemaPrefix")),
		        Map.entry("Google Sheet", Arrays.asList("schemaPrefix")),
		        Map.entry("IBM EWM", Arrays.asList("schemaPrefix")),
		        Map.entry("IBM OpenPages", Arrays.asList("schemaPrefix")),
		        Map.entry("Insightly", Arrays.asList("schemaPrefix")),
		        Map.entry("Monday.com", Arrays.asList("schemaPrefix")),
		        Map.entry("Yammer", Arrays.asList("schemaPrefix")),
		        Map.entry("Microsoft Teams", Arrays.asList("schemaPrefix")),
		        Map.entry("Microsoft SharePoint", Arrays.asList("schemaPrefix")),
		        Map.entry("Microsoft Exchange", Arrays.asList("schemaPrefix")),
		        Map.entry("Microsoft Excel", Arrays.asList("schemaPrefix")),
		        Map.entry("Microsoft Dynamics CRM", Arrays.asList("schemaPrefix")),
		        Map.entry("Microsoft Active Directory", Arrays.asList("schemaPrefix")),
		        Map.entry("SurveyMonkey", Arrays.asList("schemaPrefix")),
		        Map.entry("Mailchimp", Arrays.asList("schemaPrefix")),
		        Map.entry("Kafka", Arrays.asList("topicName")),
		        Map.entry("Shopify", Arrays.asList("schemaPrefix")),
		        Map.entry("Salesforce", Arrays.asList("schemaPrefix")),
		        Map.entry("MQTT", Arrays.asList("topicName")),
		        Map.entry("JD Edwards", Arrays.asList("adapterComponent")),
		        Map.entry("Oracle HCM", Arrays.asList("schemaPrefix")),
		        Map.entry("Jira", Arrays.asList("schemaPrefix")),
		        Map.entry("Wufoo", Arrays.asList("schemaPrefix")),
		        Map.entry("Zendesk", Arrays.asList("schemaPrefix")),
		        Map.entry("ServiceNow",Arrays.asList("schemaPrefix")),
		        Map.entry("Scheduler",Arrays.asList("scheduleIdentifier"))
		    );

	public static int commonRoutes = 0;
	public static int commonRoutesNonRest = 0;
	public static int endpointRoutes = 0;
	public static ArrayList<Integer> commonRoutesArr = new ArrayList<>();
	public static ArrayList<Integer> endpointRoutesArr = new ArrayList<>();

	// Setter & Getter for X-Request-ID
	private static String xRequestId = "Exa-Default-ID";

	public static String getxRequestId() {
		return xRequestId;
	}

	public static void setxRequestId(String xRequestId) {
		AppGlobalDeclaration.xRequestId = xRequestId;
	}

	// Setter Generic Header
	public static void setGenericRequestHeaders(MultiValueMap<String, String> requestHeaders) {
		String userRequestId = "";
		for (int eachHeader = 0; eachHeader < requestHeaders.size(); eachHeader++) {
			if (requestHeaders.get("x-request-id") != null && !(requestHeaders.get("x-request-id").isEmpty())) {
				userRequestId = requestHeaders.getFirst("x-request-id").toString();
				continue;
			}
		}

		if (userRequestId.isEmpty()) {
			userRequestId = UUID.randomUUID().toString();
		}

		setxRequestId(userRequestId);
	}

	public static final String COMMON_NODE_URL = "COMMON_IIB_NODES";
	public static final String COMMON_METHOD = "N/A";

}

package com.abi.util;

public class ClaimBookConstants {

	public static final String REQUEST_MAPPING_DASHBOARD_SUMMARY = "/dashboard/summary";
	public static final String REQUEST_MAPPING_DASHBOARD_CASE_HISTORY = "/dashboard/case/{tenantId}/{referenceId}/history";
	public static final String REQUEST_MAPPING_DASHBOARD_POLLING_HISTORY = "/dashboard/case/{tenantId}/{referenceId}/polling";
	public static final String REQUEST_MAPPING_DASHBOARD_EVENTS = "/dashboard/events";
	public static final String REQUEST_MAPPING_DASHBOARD_TENANTS = "/dashboard/tenants";
	public static final String REQUEST_MAPPING_DASHBOARD_TPAS = "/dashboard/tpas";
	public static final String REQUEST_MAPPING_DASHBOARD_FAILURE_CATEGORIES = "/dashboard/failure-categories";
	public static final String REQUEST_MAPPING_DASHBOARD_PREAUTH_TRANSACTIONS = "/dashboard/preauth/transactions";
	public static final String REQUEST_MAPPING_DASHBOARD_FAILURE_ANALYSIS = "/dashboard/failure-analysis";
	public static final String REQUEST_MAPPING_TRACE_EVENTS = "/trace/events";
	public static final String REQUEST_MAPPING_REPORT_PREAUTH_SUMMARY = "/reports/preauth-summary";
	public static final String REQUEST_MAPPING_REPORT_CLAIMS_SUMMARY = "/reports/claims-summary";
	public static final String REQUEST_MAPPING_REPORT_RETRY_ANALYSIS = "/reports/retry-analysis";
	public static final String REQUEST_MAPPING_REPORT_TPA_PERFORMANCE = "/reports/tpa-performance";
	public static final String CONTENT_TYPE_TEXT_EVENT_STREAM = "text/event-stream";
	public static final String INTEGRATION_DATE_FORMAT = "dd-MMM-yyyy";
	public static final String DEFAULT_JSON_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/*
	 * Content Types
	 */
	public static final String CONTENT_TYPE_APPLICATION_JSON = "application/json;charset=UTF-8";
	public static final String CONTENT_TYPE_MULTIPART_FORM_DATA = "multipart/form-data";
	public static final String CONTENT_TYPE_URL_ENCODED = "application/x-www-form-urlencoded";
	public static final String RESPONSE_OBJECT_NAME = "OBJECT_NAME";
	public static final String RESPONSE_AUTH_ERROR = "Auth Error";

	/*
	 * Request mapping URLs comes here
	 */

	public static final String REQUEST_MAPPING_GET_MASTERS = "/api/v1/despatch-tracker/masters";

}

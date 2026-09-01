package com.abi.service;

import java.util.Date;
import java.util.List;

import com.abi.dto.DashboardSummaryRequestVO;
import com.abi.dto.DashboardSummaryResponse;
import com.abi.dto.DashboardSummaryVO;
import com.abi.dto.FailureAnalysisRowVO;
import com.abi.dto.ModuleSummaryDTO;
import com.abi.dto.PreAuthTransactionVO;
import com.abi.dto.TenantVO;
import com.abi.dto.TpaVO;
import com.abi.dto.TraceHistoryVO;
import com.abi.exception.ValidationException;


public interface DashboardService {

    DashboardSummaryVO getSummary(DashboardSummaryRequestVO request) throws ValidationException;

    List<TraceHistoryVO> getCaseHistory(Integer tenantId, String referenceId);

    List<TraceHistoryVO> getPollingHistory(Integer tenantId, String referenceId);

    DashboardSummaryResponse getOverview(Integer tenantId, String hospitalId, String tpaName, String failureCategory,
	    String requestType, Date fromDate, Date toDate, Integer trendWindowDays) throws ValidationException;

    // Used by DashboardTransactionEventListener to embed a fresh module snapshot in each SSE
    // event, scoped to "today" for the event's own tenant/module.
    ModuleSummaryDTO computeModuleSummary(Integer tenantId, String hospitalId, Integer tpaId, String module);

    // Lookups for the dashboard's filter dropdowns (tenant switcher, TPA/Payer filter,
    // Failure Category filter).
    List<TenantVO> getTenants();

    List<TpaVO> getPayorTpas();

    List<String> getFailureCategories();

    // Dashboard's PreAuth Transactions table - one row per case, sourced entirely from
    // external_api_trace_log: case-level fields from the case's SUBMIT history, patient identity
    // fields parsed from the case's INITIAL submission source_payload. status filters on each
    // case's current status (vo.getStatus()), not on individual trace rows.
    List<PreAuthTransactionVO> getPreAuthTransactions(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException;

    // Builds a PREAUTH/CLAIMS module summary for arbitrary filters, including an optional
    // status filter that getOverview doesn't support - used by report generation so reports
    // stay consistent with the live dashboard's aggregation (same dedup/grouping rules).
    ModuleSummaryDTO buildModuleReport(String module, Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException;

    // Failure Analysis page - one row per FAILURE event on a SUBMIT or QUERY_REPLY attempt
    // (root-cause view, not deduped per case). module is "PREAUTH"/"CLAIMS"/null-or-"ALL" for
    // both, matching the page's All/Preauth/Claims tabs.
    List<FailureAnalysisRowVO> getFailureAnalysis(Integer tenantId, String tpaName, String module, Date fromDate,
	    Date toDate) throws ValidationException;

}

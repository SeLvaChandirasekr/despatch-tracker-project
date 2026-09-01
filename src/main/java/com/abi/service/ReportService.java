package com.abi.service;

import java.util.Date;
import java.util.List;

import com.abi.dto.ModuleSummaryDTO;
import com.abi.dto.PreauthSummaryReportVO;
import com.abi.dto.RetryAnalysisRowVO;
import com.abi.dto.TpaPerformanceRowVO;
import com.abi.exception.ValidationException;

/*-----------------------------------------------------------------------
 * Backs the dashboard's Reports page. Returns plain JSON data - the frontend renders/exports it
 * (CSV or otherwise), so this layer has no file-format concerns at all.
 *---------------------------------------------------------------------*/
public interface ReportService {

    // Row-level case list (same shape/source as /dashboard/preauth/transactions, patient
    // identity parsed from source_payload) plus a total count - not the module-aggregate
    // totals, since that's what this report is actually meant to show.
    PreauthSummaryReportVO buildPreauthSummaryReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException;

    ModuleSummaryDTO buildClaimsSummaryReport(Integer tenantId, String tpaName, String status, String requestType,
	    Date fromDate, Date toDate) throws ValidationException;

    // One row per retried case (SUBMIT attempts only - see DashboardServiceImpl.buildRetrySummary
    // for why), spanning both PREAUTH and CLAIMS.
    List<RetryAnalysisRowVO> buildRetryAnalysisReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException;

    // One row per TPA: success rate and average response time over the given filters/date range.
    List<TpaPerformanceRowVO> buildTpaPerformanceReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException;

}

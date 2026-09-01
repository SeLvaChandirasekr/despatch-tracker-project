package com.abi.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.abi.dto.DashboardSummaryRequestVO;
import com.abi.dto.DashboardSummaryResponse;
import com.abi.dto.DashboardSummaryVO;
import com.abi.dto.FailureAnalysisRowVO;
import com.abi.dto.PreAuthTransactionVO;
import com.abi.dto.TenantVO;
import com.abi.dto.TpaVO;
import com.abi.dto.TraceHistoryVO;
import com.abi.exception.BusinessException;
import com.abi.exception.ValidationException;
import com.abi.service.DashboardService;
import com.abi.util.ClaimBookConstants;


@RestController
public class DashboardController  extends ClaimBookConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    @Autowired
    private DashboardService dashboardService;

    @PostMapping(path = REQUEST_MAPPING_DASHBOARD_SUMMARY, produces = CONTENT_TYPE_APPLICATION_JSON)
    public DashboardSummaryVO getSummary(@RequestBody DashboardSummaryRequestVO requestVo)
	    throws ValidationException {
	if (requestVo == null || requestVo.getFunctionality() == null || requestVo.getActionType() == null) {
	    throw new ValidationException("functionality and actionType are required");
	}

	try {
	    return dashboardService.getSummary(requestVo);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getSummary:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_SUMMARY, produces = CONTENT_TYPE_APPLICATION_JSON)
    public DashboardSummaryResponse getOverview(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String hospital, @RequestParam(required = false) String tpa,
	    @RequestParam(required = false) String failureCategory, @RequestParam(required = false) String requestType,
	    @RequestParam(required = false) Integer trendWindowDays,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return dashboardService.getOverview(tenantId, hospital, tpa, failureCategory, requestType, fromDate,
		    toDate, trendWindowDays);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getOverview:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_TENANTS, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<TenantVO> getTenants() {
	try {
	    return dashboardService.getTenants();
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getTenants:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_TPAS, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<TpaVO> getPayorTpas() {
	try {
	    return dashboardService.getPayorTpas();
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getPayorTpas:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_FAILURE_CATEGORIES, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<String> getFailureCategories() {
	try {
	    return dashboardService.getFailureCategories();
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getFailureCategories:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_CASE_HISTORY, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<TraceHistoryVO> getCaseHistory(@PathVariable Integer tenantId, @PathVariable String referenceId) {
	try {
	    return dashboardService.getCaseHistory(tenantId, referenceId);
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getCaseHistory:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_POLLING_HISTORY, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<TraceHistoryVO> getPollingHistory(@PathVariable Integer tenantId, @PathVariable String referenceId) {
	try {
	    return dashboardService.getPollingHistory(tenantId, referenceId);
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getPollingHistory:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_PREAUTH_TRANSACTIONS, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<PreAuthTransactionVO> getPreAuthTransactions(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String tpa, @RequestParam(required = false) String status,
	    @RequestParam(required = false) String requestType,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return dashboardService.getPreAuthTransactions(tenantId, tpa, status, requestType, fromDate, toDate);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getPreAuthTransactions:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_DASHBOARD_FAILURE_ANALYSIS, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<FailureAnalysisRowVO> getFailureAnalysis(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String tpa, @RequestParam(required = false) String module,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return dashboardService.getFailureAnalysis(tenantId, tpa, module, fromDate, toDate);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while executing dashboard getFailureAnalysis:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }
}

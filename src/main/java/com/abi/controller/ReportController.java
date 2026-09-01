package com.abi.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.abi.dto.ModuleSummaryDTO;
import com.abi.dto.PreauthSummaryReportVO;
import com.abi.dto.RetryAnalysisRowVO;
import com.abi.dto.TpaPerformanceRowVO;
import com.abi.exception.BusinessException;
import com.abi.exception.ValidationException;
import com.abi.service.ReportService;
import com.abi.util.ClaimBookConstants;

// Returns plain JSON for all four reports - the frontend builds/downloads the CSV itself.
@RestController
public class ReportController extends ClaimBookConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportController.class);

    @Autowired
    private ReportService reportService;

    @GetMapping(path = REQUEST_MAPPING_REPORT_PREAUTH_SUMMARY, produces = CONTENT_TYPE_APPLICATION_JSON)
    public PreauthSummaryReportVO getPreauthSummaryReport(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String tpa, @RequestParam(required = false) String status,
	    @RequestParam(required = false) String requestType,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return reportService.buildPreauthSummaryReport(tenantId, tpa, status, requestType, fromDate, toDate);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while generating preauth summary report:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_REPORT_CLAIMS_SUMMARY, produces = CONTENT_TYPE_APPLICATION_JSON)
    public ModuleSummaryDTO getClaimsSummaryReport(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String tpa, @RequestParam(required = false) String status,
	    @RequestParam(required = false) String requestType,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return reportService.buildClaimsSummaryReport(tenantId, tpa, status, requestType, fromDate, toDate);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while generating claims summary report:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_REPORT_RETRY_ANALYSIS, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<RetryAnalysisRowVO> getRetryAnalysisReport(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String tpa, @RequestParam(required = false) String status,
	    @RequestParam(required = false) String requestType,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return reportService.buildRetryAnalysisReport(tenantId, tpa, status, requestType, fromDate, toDate);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while generating retry analysis report:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

    @GetMapping(path = REQUEST_MAPPING_REPORT_TPA_PERFORMANCE, produces = CONTENT_TYPE_APPLICATION_JSON)
    public List<TpaPerformanceRowVO> getTpaPerformanceReport(@RequestParam(required = false) Integer tenantId,
	    @RequestParam(required = false) String tpa, @RequestParam(required = false) String status,
	    @RequestParam(required = false) String requestType,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date fromDate,
	    @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date toDate)
	    throws ValidationException {
	try {
	    return reportService.buildTpaPerformanceReport(tenantId, tpa, status, requestType, fromDate, toDate);
	} catch (ValidationException e) {
	    throw e;
	} catch (Exception e) {
	    LOGGER.error("Error while generating tpa performance report:: " + e.getMessage());
	    throw new BusinessException(e.getMessage());
	}
    }

}

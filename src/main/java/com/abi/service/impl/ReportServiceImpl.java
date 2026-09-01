package com.abi.service.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abi.dto.ModuleSummaryDTO;
import com.abi.dto.PreAuthTransactionVO;
import com.abi.dto.PreauthSummaryReportVO;
import com.abi.dto.RetryAnalysisRowVO;
import com.abi.dto.TpaPerformanceRowVO;
import com.abi.entity.DashboardTransactionRow;
import com.abi.entity.MasterPayor;
import com.abi.entity.Tenant;
import com.abi.exception.ValidationException;
import com.abi.repository.DashboardTransactionRowRepository;
import com.abi.repository.MasterPayorRepository;
import com.abi.repository.TenantRepository;
import com.abi.service.DashboardService;
import com.abi.service.ReportService;

/*-----------------------------------------------------------------------
 * Preauth/Claims Summary Reports reuse DashboardService.buildModuleReport() so they stay
 * consistent with the live dashboard's aggregation (same dedup/grouping rules). Retry Analysis
 * and TPA Performance need row-level detail the aggregated ModuleSummaryDTO doesn't expose, so
 * those read straight from DashboardTransactionRowRepository instead. Everything here returns
 * plain JSON DTOs - the frontend handles turning that into a CSV download.
 *---------------------------------------------------------------------*/
@Service
public class ReportServiceImpl implements ReportService {

    private static final String ACTION_TYPE_SUBMIT = "SUBMIT";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private DashboardTransactionRowRepository dashboardTransactionRowRepository;

    @Autowired
    private MasterPayorRepository masterPayorRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    public PreauthSummaryReportVO buildPreauthSummaryReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException {
	Date[] range = resolveDateRange(fromDate, toDate);
	List<PreAuthTransactionVO> transactions = dashboardService.getPreAuthTransactions(tenantId, tpaName, status,
		requestType, range[0], range[1]);

	PreauthSummaryReportVO report = new PreauthSummaryReportVO();
	report.setTotalCount(transactions.size());
	report.setTransactions(transactions);
	return report;
    }

    @Override
    public ModuleSummaryDTO buildClaimsSummaryReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException {
	Date[] range = resolveDateRange(fromDate, toDate);
	return dashboardService.buildModuleReport("CLAIMS", tenantId, tpaName, status, requestType, range[0], range[1]);
    }

    @Override
    public List<RetryAnalysisRowVO> buildRetryAnalysisReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException {
	Date[] range = resolveDateRange(fromDate, toDate);
	Integer tpaId = resolveTpaId(tpaName);
	List<DashboardTransactionRow> rows = fetchFilteredRows(tenantId, tpaId, status, requestType, range);

	List<DashboardTransactionRow> submissionRows = rows.stream()
		.filter(r -> ACTION_TYPE_SUBMIT.equals(r.getActionType())).collect(Collectors.toList());

	// requestType is part of the grouping key so a case's separate lifecycle stages (e.g. an
	// INITIAL submission and a later, unrelated DISCHARGE submission) are never treated as
	// retries of each other - see DashboardServiceImpl.rootId() for the same rule.
	Map<String, List<DashboardTransactionRow>> chains = submissionRows.stream().collect(Collectors
		.groupingBy(r -> r.getTenantId() + "|" + r.getCaseId() + "|" + r.getModule() + "|" + r.getRequestType()));

	Map<Integer, String> tenantNames = loadTenantNames();

	List<RetryAnalysisRowVO> result = new ArrayList<>();
	for (List<DashboardTransactionRow> chain : chains.values()) {
	    if (chain.size() <= 1) {
		continue;
	    }
	    List<DashboardTransactionRow> sorted = chain.stream()
		    .sorted(Comparator.comparing(DashboardTransactionRow::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
		    .collect(Collectors.toList());

	    int firstSuccessIndex = -1;
	    for (int i = 0; i < sorted.size(); i++) {
		if (STATUS_SUCCESS.equalsIgnoreCase(sorted.get(i).getStatus())) {
		    firstSuccessIndex = i;
		    break;
		}
	    }

	    DashboardTransactionRow first = sorted.get(0);
	    DashboardTransactionRow last = sorted.get(sorted.size() - 1);
	    String outcome;
	    String finalStatus;
	    if (firstSuccessIndex < 0) {
		outcome = "FAILED_AFTER_RETRY";
		finalStatus = last.getStatus();
	    } else {
		Integer attemptNo = sorted.get(firstSuccessIndex).getRetryAttemptNumber();
		int attemptsBeforeSuccess = attemptNo != null && attemptNo > 0 ? attemptNo - 1 : firstSuccessIndex;
		outcome = attemptsBeforeSuccess <= 1 ? "SINGLE_RETRY_SUCCESS" : "MULTIPLE_RETRY_SUCCESS";
		finalStatus = STATUS_SUCCESS;
	    }

	    RetryAnalysisRowVO row = new RetryAnalysisRowVO();
	    row.setTenantId(first.getTenantId());
	    row.setTenantName(tenantNames.getOrDefault(first.getTenantId(), null));
	    row.setModule(first.getModule());
	    row.setReferenceId(first.getCaseId());
	    row.setAttemptCount(sorted.size());
	    row.setFirstAttemptTime(first.getCreatedAt());
	    row.setLastAttemptTime(last.getCreatedAt());
	    row.setFinalStatus(finalStatus);
	    row.setOutcome(outcome);
	    result.add(row);
	}

	result.sort(Comparator.comparing(RetryAnalysisRowVO::getLastAttemptTime, Comparator.nullsLast(Comparator.naturalOrder()))
		.reversed());
	return result;
    }

    @Override
    public List<TpaPerformanceRowVO> buildTpaPerformanceReport(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException {
	Date[] range = resolveDateRange(fromDate, toDate);
	Integer tpaId = resolveTpaId(tpaName);
	List<DashboardTransactionRow> rows = fetchFilteredRows(tenantId, tpaId, status, requestType, range);

	Map<Integer, List<DashboardTransactionRow>> byTpa = rows.stream().filter(r -> r.getTpaId() != null)
		.collect(Collectors.groupingBy(DashboardTransactionRow::getTpaId));
	Map<Integer, String> payorNames = loadPayorNames();

	List<TpaPerformanceRowVO> result = new ArrayList<>();
	for (Map.Entry<Integer, List<DashboardTransactionRow>> entry : byTpa.entrySet()) {
	    List<DashboardTransactionRow> tpaRows = entry.getValue();
	    long success = tpaRows.stream().filter(r -> STATUS_SUCCESS.equalsIgnoreCase(r.getStatus())).count();
	    long failure = tpaRows.stream().filter(r -> STATUS_FAILURE.equalsIgnoreCase(r.getStatus())).count();

	    TpaPerformanceRowVO row = new TpaPerformanceRowVO();
	    row.setTpaId(entry.getKey());
	    row.setTpaName(payorNames.getOrDefault(entry.getKey(), "TPA " + entry.getKey()));
	    row.setTotal(tpaRows.size());
	    row.setSuccess(success);
	    row.setFailure(failure);
	    row.setSuccessRatePct(successRatePct((int) success, (int) failure));
	    row.setAvgResponseTimeMs(averageElapsedMs(tpaRows));
	    result.add(row);
	}

	result.sort(Comparator.comparing(TpaPerformanceRowVO::getSuccessRatePct));
	return result;
    }

    private List<DashboardTransactionRow> fetchFilteredRows(Integer tenantId, Integer tpaId, String status,
	    String requestType, Date[] range) {
	List<DashboardTransactionRow> rows = dashboardTransactionRowRepository.findForOverview(tenantId, null, tpaId,
		null, requestType, range[0], addOneDay(range[1]));
	if (isRealStatus(status)) {
	    rows = rows.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).collect(Collectors.toList());
	}
	return rows;
    }

    private Long averageElapsedMs(List<DashboardTransactionRow> rows) {
	List<Long> successfulElapsed = rows.stream()
		.filter(r -> STATUS_SUCCESS.equalsIgnoreCase(r.getStatus()) && r.getElapsedTimeMs() != null)
		.map(DashboardTransactionRow::getElapsedTimeMs).collect(Collectors.toList());
	if (successfulElapsed.isEmpty()) {
	    return null;
	}
	long sum = 0;
	for (Long value : successfulElapsed) {
	    sum += value;
	}
	return sum / successfulElapsed.size();
    }

    private Date[] resolveDateRange(Date fromDate, Date toDate) throws ValidationException {
	if (fromDate == null && toDate == null) {
	    Date today = Date.from(LocalDate.now(ZoneId.systemDefault()).atStartOfDay(ZoneId.systemDefault()).toInstant());
	    return new Date[] { today, today };
	}
	if (fromDate == null || toDate == null) {
	    throw new ValidationException("fromDate and toDate must both be provided, or both omitted");
	}
	return new Date[] { fromDate, toDate };
    }

    private Integer resolveTpaId(String tpaName) throws ValidationException {
	if (tpaName == null || tpaName.isBlank()) {
	    return null;
	}
	return masterPayorRepository.findByTpaNameIgnoreCase(tpaName).map(MasterPayor::getMasterPayorId)
		.orElseThrow(() -> new ValidationException("Unknown TPA: " + tpaName));
    }

    private boolean isRealStatus(String status) {
	return status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status);
    }

    private Date addOneDay(Date date) {
	return Date.from(date.toInstant().plusSeconds(24 * 60 * 60));
    }

    private Map<Integer, String> loadPayorNames() {
	Map<Integer, String> names = new LinkedHashMap<>();
	for (MasterPayor payor : masterPayorRepository.findAll()) {
	    names.put(payor.getMasterPayorId(), payor.getTpaName());
	}
	return names;
    }

    private Map<Integer, String> loadTenantNames() {
	Map<Integer, String> names = new LinkedHashMap<>();
	for (Tenant tenant : tenantRepository.findAll()) {
	    names.put(tenant.getTenantId(), tenant.getTenantName());
	}
	return names;
    }

    private double successRatePct(int success, int failure) {
	int terminal = success + failure;
	return terminal == 0 ? 0.0 : (success * 100.0) / terminal;
    }

}

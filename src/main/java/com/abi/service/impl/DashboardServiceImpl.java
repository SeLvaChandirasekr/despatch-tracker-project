package com.abi.service.impl;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.abi.dto.ApiHealthDTO;
import com.abi.dto.ComparisonDTO;
import com.abi.dto.DashboardKpiDTO;
import com.abi.dto.DashboardSummaryRequestVO;
import com.abi.dto.DashboardSummaryResponse;
import com.abi.dto.DashboardSummaryVO;
import com.abi.dto.EndpointHealthDTO;
import com.abi.dto.FailureAnalysisRowVO;
import com.abi.dto.FailureReasonDTO;
import com.abi.dto.FlowSummaryDTO;
import com.abi.dto.ModuleSummaryDTO;
import com.abi.dto.PatientIdentityVO;
import com.abi.dto.PreAuthTransactionVO;
import com.abi.dto.QueryReplySummaryDTO;
import com.abi.dto.RecentAlertDTO;
import com.abi.dto.RecentTransactionDTO;
import com.abi.dto.RetrySummaryDTO;
import com.abi.dto.TenantVO;
import com.abi.dto.TpaVO;
import com.abi.dto.TraceHistoryVO;
import com.abi.dto.VolumeTrendPointDTO;
import com.abi.entity.DashboardTransactionRow;
import com.abi.entity.ExternalApiTraceLog;
import com.abi.entity.MasterPayor;
import com.abi.entity.Tenant;
import com.abi.exception.ValidationException;
import com.abi.repository.DashboardTransactionRowRepository;
import com.abi.repository.ExternalApiTraceRepository;
import com.abi.repository.MasterPayorRepository;
import com.abi.repository.TenantRepository;
import com.abi.service.DashboardService;
import com.abi.util.ClaimBookConstants;
import com.abi.util.FailureCategoryClassifier;
import com.abi.util.SourcePayloadParser;


/*-----------------------------------------------------------------------
 * Dashboard reads are aggregated in Java over a WHERE-filtered fetch of
 * vw_dashboard_transaction_events rows (never an unfiltered/full-table load) rather than
 * many separate GROUP BY queries - the breakdowns here (per-flow success/failure, retry-chain
 * grouping, top failure reasons, per-day trend) combine too many dimensions to keep readable
 * as SQL, and row volume per scoped query is small enough for this to comfortably meet the
 * <2s target.
 *
 * tenantId is optional throughout: omitted means "all tenants" (first-load default),
 * supplied means "just this tenant" (after the UI's tenant selector picks one). No per-user
 * tenant enforcement.
 *---------------------------------------------------------------------*/
@Service
public class DashboardServiceImpl extends ClaimBookConstants implements DashboardService {

    private static final String ACTION_TYPE_SUBMIT = "SUBMIT";
    private static final String ACTION_TYPE_PORTAL_STATUS = "PORTAL_STATUS";
    private static final String ACTION_TYPE_QUERY_REPLY = "QUERY_REPLY";
    private static final String FLOW_TYPE_INBOUND = "INBOUND";
    private static final String FLOW_TYPE_OUTBOUND = "OUTBOUND";
    private static final String MODULE_PREAUTH = "PREAUTH";
    private static final String MODULE_CLAIMS = "CLAIMS";
    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_FAILURE = "FAILURE";
    private static final long API_HEALTH_WINDOW_MINUTES = 15;
    private static final int DEFAULT_TREND_WINDOW_DAYS = 7;
    private static final int RECENT_TRANSACTIONS_LIMIT = 10;
    private static final int TOP_FAILURE_REASONS_LIMIT = 5;
    private static final double HIGH_FAILURE_RATE_THRESHOLD_PCT = 50.0;
    private static final double HIGH_RETRY_RATE_THRESHOLD_PCT = 30.0;
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Autowired
    private ExternalApiTraceRepository externalApiTraceRepository;

    @Autowired
    private DashboardTransactionRowRepository dashboardTransactionRowRepository;

    @Autowired
    private MasterPayorRepository masterPayorRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    public List<TenantVO> getTenants() {
	List<TenantVO> tenants = new ArrayList<>();
	for (Tenant tenant : tenantRepository.findAll()) {
	    TenantVO vo = new TenantVO();
	    vo.setTenantId(tenant.getTenantId());
	    vo.setTenantName(tenant.getTenantName());
	    tenants.add(vo);
	}
	return tenants;
    }

    @Override
    public List<TpaVO> getPayorTpas() {
	List<TpaVO> tpas = new ArrayList<>();
	for (MasterPayor payor : masterPayorRepository.findAll()) {
	    TpaVO vo = new TpaVO();
	    vo.setTpaId(payor.getMasterPayorId());
	    vo.setTpaName(payor.getTpaName());
	    tpas.add(vo);
	}
	return tpas;
    }

    @Override
    public List<String> getFailureCategories() {
	Set<String> categories = new TreeSet<>();
	for (String rawReason : dashboardTransactionRowRepository.findDistinctFailureReasons()) {
	    String category = FailureCategoryClassifier.classify(rawReason);
	    if (category != null) {
		categories.add(category);
	    }
	}
	return new ArrayList<>(categories);
    }

    @Override
    public DashboardSummaryVO getSummary(DashboardSummaryRequestVO request) throws ValidationException {
	if (request.getFromDate() == null && request.getToDate() == null) {
	    LocalDate today = LocalDate.now(ZoneId.systemDefault());
	    request.setFromDate(toDate(today));
	    request.setToDate(toDate(today));
	} else if (request.getFromDate() == null || request.getToDate() == null) {
	    throw new ValidationException("fromDate and toDate must both be provided, or both omitted");
	}

	Date toDateExclusive = addOneDay(request.getToDate());

	List<ExternalApiTraceLog> rows = externalApiTraceRepository.findByFunctionalityAndActionType(
		request.getTenantId(), request.getFunctionality(), request.getActionType(), request.getFromDate(),
		toDateExclusive);

	DashboardSummaryVO summary = new DashboardSummaryVO();
	summary.setSubmitted(rows.size());

	for (ExternalApiTraceLog row : rows) {
	    boolean success = STATUS_SUCCESS.equalsIgnoreCase(row.getStatus());
	    if (success) {
		summary.setSuccess(summary.getSuccess() + 1);
	    } else {
		summary.setFailure(summary.getFailure() + 1);
	    }

	    if (row.getRequestType() != null) {
		summary.getRequestTypeCounts().merge(row.getRequestType(), 1, Integer::sum);
	    }

	    boolean firstAttempt = row.getAttemptNo() == null || row.getAttemptNo() <= 1;
	    if (firstAttempt) {
		if (success) {
		    summary.setFirstAttemptSuccess(summary.getFirstAttemptSuccess() + 1);
		} else {
		    summary.setFirstAttemptFailure(summary.getFirstAttemptFailure() + 1);
		}
	    } else {
		if (success) {
		    summary.setMultiAttemptSuccess(summary.getMultiAttemptSuccess() + 1);
		} else {
		    summary.setMultiAttemptFailure(summary.getMultiAttemptFailure() + 1);
		}
	    }

	    if (row.isRetrySubmission()) {
		summary.setRetryCount(summary.getRetryCount() + 1);
	    }

	    if (row.getBusinessStatus() != null) {
		summary.getBusinessStatusCounts().merge(row.getBusinessStatus(), 1, Integer::sum);
	    }
	}

	return summary;
    }

    @Override
    public List<TraceHistoryVO> getCaseHistory(Integer tenantId, String referenceId) {
	return toHistoryVOs(externalApiTraceRepository.findByReferenceId(tenantId, referenceId));
    }

    @Override
    public List<TraceHistoryVO> getPollingHistory(Integer tenantId, String referenceId) {
	return toHistoryVOs(
		externalApiTraceRepository.findByReferenceId(tenantId, referenceId, ACTION_TYPE_PORTAL_STATUS));
    }

    @Override
    public DashboardSummaryResponse getOverview(Integer tenantId, String hospitalId, String tpaName,
	    String failureCategory, String requestType, Date fromDate, Date toDate, Integer trendWindowDays)
	    throws ValidationException {
	if (fromDate == null && toDate == null) {
	    LocalDate today = LocalDate.now(ZoneId.systemDefault());
	    fromDate = toDate(today);
	    toDate = fromDate;
	} else if (fromDate == null || toDate == null) {
	    throw new ValidationException("fromDate and toDate must both be provided, or both omitted");
	}

	Integer tpaId = resolveTpaId(tpaName);
	int trendDays = trendWindowDays == null ? DEFAULT_TREND_WINDOW_DAYS : trendWindowDays;

	Date toDateExclusive = addOneDay(toDate);
	List<DashboardTransactionRow> currentRows = applyFailureCategoryFilter(
		dashboardTransactionRowRepository.findForOverview(tenantId, hospitalId, tpaId, null, requestType,
			fromDate, toDateExclusive),
		failureCategory);

	long periodMillis = toDateExclusive.getTime() - fromDate.getTime();
	Date previousFromDate = new Date(fromDate.getTime() - periodMillis);
	List<DashboardTransactionRow> previousRows = applyFailureCategoryFilter(
		dashboardTransactionRowRepository.findForOverview(tenantId, hospitalId, tpaId, null, requestType,
			previousFromDate, fromDate),
		failureCategory);

	Map<Integer, String> payorNames = loadPayorNames();

	DashboardSummaryResponse response = new DashboardSummaryResponse();
	response.setKpis(buildKpis(currentRows, previousRows));
	response.setApiHealth(buildApiHealth(tenantId, hospitalId, tpaId, payorNames));

	ModuleSummaryDTO preauth = buildModuleSummary(currentRows, MODULE_PREAUTH);
	ModuleSummaryDTO claims = buildModuleSummary(currentRows, MODULE_CLAIMS);
	response.setPreauth(preauth);
	response.setClaims(claims);

	response.setVolumeTrend(buildVolumeTrend(tenantId, hospitalId, tpaId, failureCategory, requestType,
		trendDays, toDate));
	response.setRecentAlerts(buildRecentAlerts(response.getApiHealth(), preauth, claims));
	response.setRecentTransactions(buildRecentTransactions(currentRows, payorNames));
	return response;
    }

    @Override
    public ModuleSummaryDTO computeModuleSummary(Integer tenantId, String hospitalId, Integer tpaId, String module) {
	LocalDate today = LocalDate.now(ZoneId.systemDefault());
	Date fromDate = toDate(today);
	List<DashboardTransactionRow> rows = dashboardTransactionRowRepository.findForOverview(tenantId, hospitalId,
		tpaId, null, null, fromDate, addOneDay(fromDate));
	return buildModuleSummary(rows, module);
    }

    @Override
    public List<PreAuthTransactionVO> getPreAuthTransactions(Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException {
	if (fromDate == null && toDate == null) {
	    // No filter picked yet: default to a trailing window rather than just today, since
	    // "today" alone reads as broken (empty table) whenever nothing's been submitted yet today.
	    LocalDate today = LocalDate.now(ZoneId.systemDefault());
	    toDate = toDate(today);
	    fromDate = toDate(today.minusDays(DEFAULT_TREND_WINDOW_DAYS - 1));
	} else if (fromDate == null || toDate == null) {
	    throw new ValidationException("fromDate and toDate must both be provided, or both omitted");
	}

	Integer tpaId = resolveTpaId(tpaName);
	List<ExternalApiTraceLog> submittedCases = externalApiTraceRepository.findSubmittedCases(tenantId, tpaId,
		MODULE_PREAUTH, requestType, fromDate, addOneDay(toDate));
	if (submittedCases.isEmpty()) {
	    return new ArrayList<>();
	}

	// (tenantId, referenceId) pairs in scope, in submission order - referenceId alone isn't
	// guaranteed globally unique across tenants, so grouping keys on the pair throughout.
	Set<String> caseKeysInOrder = new LinkedHashSet<>();
	Set<String> referenceIds = new LinkedHashSet<>();
	for (ExternalApiTraceLog row : submittedCases) {
	    caseKeysInOrder.add(caseKey(row.getTenantId(), row.getReferenceId()));
	    referenceIds.add(row.getReferenceId());
	}

	List<ExternalApiTraceLog> allRows = externalApiTraceRepository.findAllByReferenceIds(tenantId, MODULE_PREAUTH,
		referenceIds);
	Map<String, List<ExternalApiTraceLog>> rowsByCase = allRows.stream().collect(Collectors
		.groupingBy(row -> caseKey(row.getTenantId(), row.getReferenceId()), LinkedHashMap::new, Collectors.toList()));

	Map<Integer, String> tenantNames = loadTenantNames();
	Map<Integer, String> payorNames = loadPayorNames();

	List<PreAuthTransactionVO> result = new ArrayList<>();
	for (String caseKey : caseKeysInOrder) {
	    List<ExternalApiTraceLog> caseRows = rowsByCase.get(caseKey);
	    if (caseRows == null || caseRows.isEmpty()) {
		continue;
	    }
	    result.add(buildCaseTransaction(caseRows, tenantNames, payorNames));
	}
	if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
	    result = result.stream().filter(vo -> status.equalsIgnoreCase(vo.getStatus())).collect(Collectors.toList());
	}
	result.sort(Comparator.comparing(PreAuthTransactionVO::getCreatedDate, Comparator.nullsLast(Comparator.naturalOrder()))
		.reversed());
	return result;
    }

    @Override
    public ModuleSummaryDTO buildModuleReport(String module, Integer tenantId, String tpaName, String status,
	    String requestType, Date fromDate, Date toDate) throws ValidationException {
	if (fromDate == null && toDate == null) {
	    LocalDate today = LocalDate.now(ZoneId.systemDefault());
	    fromDate = toDate(today);
	    toDate = fromDate;
	} else if (fromDate == null || toDate == null) {
	    throw new ValidationException("fromDate and toDate must both be provided, or both omitted");
	}

	Integer tpaId = resolveTpaId(tpaName);
	List<DashboardTransactionRow> rows = dashboardTransactionRowRepository.findForOverview(tenantId, null, tpaId,
		null, requestType, fromDate, addOneDay(toDate));
	if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
	    rows = rows.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).collect(Collectors.toList());
	}
	return buildModuleSummary(rows, module);
    }

    @Override
    public List<FailureAnalysisRowVO> getFailureAnalysis(Integer tenantId, String tpaName, String module,
	    Date fromDate, Date toDate) throws ValidationException {
	if (fromDate == null && toDate == null) {
	    LocalDate today = LocalDate.now(ZoneId.systemDefault());
	    fromDate = toDate(today);
	    toDate = fromDate;
	} else if (fromDate == null || toDate == null) {
	    throw new ValidationException("fromDate and toDate must both be provided, or both omitted");
	}

	Integer tpaId = resolveTpaId(tpaName);
	List<DashboardTransactionRow> rows = dashboardTransactionRowRepository.findForOverview(tenantId, null, tpaId,
		null, null, fromDate, addOneDay(toDate));

	boolean allModules = module == null || module.isBlank() || "ALL".equalsIgnoreCase(module);
	List<DashboardTransactionRow> failures = rows.stream().filter(r -> STATUS_FAILURE.equalsIgnoreCase(r.getStatus()))
		.filter(r -> ACTION_TYPE_SUBMIT.equals(r.getActionType()) || ACTION_TYPE_QUERY_REPLY.equals(r.getActionType()))
		.filter(r -> allModules || module.equalsIgnoreCase(r.getModule())).collect(Collectors.toList());

	Map<Integer, String> tenantNames = loadTenantNames();
	Map<Integer, String> payorNames = loadPayorNames();

	List<FailureAnalysisRowVO> result = new ArrayList<>();
	for (DashboardTransactionRow row : failures) {
	    FailureAnalysisRowVO vo = new FailureAnalysisRowVO();
	    vo.setReferenceId(row.getCaseId());
	    vo.setTenantId(row.getTenantId());
	    vo.setTenantName(row.getTenantId() == null ? null : tenantNames.get(row.getTenantId()));
	    vo.setPayerTpaId(row.getTpaId());
	    vo.setPayerTpaName(
		    row.getTpaId() == null ? null : payorNames.getOrDefault(row.getTpaId(), "TPA " + row.getTpaId()));
	    vo.setModule(row.getModule());
	    vo.setStage(ACTION_TYPE_QUERY_REPLY.equals(row.getActionType()) ? "Query Reply" : "Submission");
	    vo.setReason(row.getFailureReason());
	    vo.setOccurredAt(row.getCreatedAt());
	    result.add(vo);
	}
	result.sort(Comparator.comparing(FailureAnalysisRowVO::getOccurredAt, Comparator.nullsLast(Comparator.naturalOrder()))
		.reversed());
	return result;
    }

    private String caseKey(Integer tenantId, String referenceId) {
	return tenantId + ":" + referenceId;
    }

    // caseRows must be oldest-first (see findAllByReferenceIds). Identity fields come from the
    // case's INITIAL submission (the only event with the full source_payload); status/AL
    // number/payor case ref come from the latest event that actually has them, since those are
    // often only populated once the case progresses past its initial submission.
    private PreAuthTransactionVO buildCaseTransaction(List<ExternalApiTraceLog> caseRows,
	    Map<Integer, String> tenantNames, Map<Integer, String> payorNames) {
	ExternalApiTraceLog latest = caseRows.get(caseRows.size() - 1);
	ExternalApiTraceLog initialSubmit = caseRows.stream().filter(row -> ACTION_TYPE_SUBMIT.equals(row.getActionType()))
		.findFirst().orElse(latest);
	// requestType and status must describe the SAME submission - otherwise a case whose INITIAL
	// failed but whose later DISCHARGE succeeded would misleadingly show "INITIAL: SUCCESS"
	// (pairing INITIAL's requestType with the overall-latest event's status).
	ExternalApiTraceLog latestSubmitStage = resolveLatestSubmitStage(caseRows, initialSubmit);

	PatientIdentityVO identity = SourcePayloadParser.parsePreAuthIdentity(initialSubmit.getSourcePayload());

	PreAuthTransactionVO vo = new PreAuthTransactionVO();
	vo.setTenantId(latest.getTenantId());
	vo.setTenantName(latest.getTenantId() == null ? null : tenantNames.get(latest.getTenantId()));
	vo.setHospitalCode(latest.getHospitalCode());
	vo.setReferenceId(latest.getReferenceId());
	vo.setCaseId(identity.getCaseId());
	vo.setPayorReferenceNumber(lastNonNull(caseRows, ExternalApiTraceLog::getPayorCaseRefId));
	vo.setAlNumber(lastNonNull(caseRows, ExternalApiTraceLog::getAlNumber));
	vo.setPatientName(identity.getPatientName());
	vo.setMrn(identity.getMrn());
	vo.setDateOfAdmission(identity.getDateOfAdmission());
	vo.setDateOfDischarge(identity.getDateOfDischarge());
	vo.setPayerTpaId(latest.getTpaId());
	vo.setPayerTpaName(
		latest.getTpaId() == null ? null : payorNames.getOrDefault(latest.getTpaId(), "TPA " + latest.getTpaId()));
	vo.setPayorTpaMemberId(identity.getPayorTpaMemberId());
	vo.setPreAuthType(identity.getPreAuthType());
	vo.setRequestType(latestSubmitStage.getRequestType());
	vo.setStatus(latestSubmitStage.getStatus());
	vo.setCurrentStatus(latest.getBusinessStatus());
	vo.setInitiatedDate(initialSubmit.getRequestedTime());
	vo.setElapsedTime(formatElapsed(initialSubmit.getRequestedTime()));
	vo.setCreatedDate(latest.getRequestedTime());
	return vo;
    }

    // The case's current submission stage: SUBMIT rows are grouped by requestType (retries of
    // the same stage - e.g. three INITIAL attempts - share the same requestType), the stage
    // whose latest row is most recent overall is "current" (e.g. a DISCHARGE stage started after
    // INITIAL resolved), and within that stage a success wins over an even-later failed retry -
    // the same success-sticky rule used elsewhere (latestPerRoot) - otherwise its latest attempt
    // stands.
    private ExternalApiTraceLog resolveLatestSubmitStage(List<ExternalApiTraceLog> caseRows,
	    ExternalApiTraceLog fallback) {
	List<ExternalApiTraceLog> submitRows = caseRows.stream().filter(row -> ACTION_TYPE_SUBMIT.equals(row.getActionType()))
		.collect(Collectors.toList());
	if (submitRows.isEmpty()) {
	    return fallback;
	}

	Comparator<ExternalApiTraceLog> byRequestedTime = Comparator
		.comparing(ExternalApiTraceLog::getRequestedTime, Comparator.nullsLast(Comparator.naturalOrder()));

	String currentRequestType = submitRows.stream().max(byRequestedTime)
		.map(row -> row.getRequestType() == null ? "" : row.getRequestType()).orElse("");

	List<ExternalApiTraceLog> stageRows = submitRows.stream()
		.filter(row -> currentRequestType.equals(row.getRequestType() == null ? "" : row.getRequestType()))
		.collect(Collectors.toList());

	return stageRows.stream().filter(row -> STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())).max(byRequestedTime)
		.orElseGet(() -> stageRows.stream().max(byRequestedTime).orElse(fallback));
    }

    // "XD:YH:ZM" time since the case's initial submission - case age, matching the dashboard
    // UI's Elapsed Time column (not per-call latency, which is elapsedTimeMs elsewhere).
    private String formatElapsed(Date initiatedDate) {
	if (initiatedDate == null) {
	    return null;
	}
	long millis = Math.max(0, new Date().getTime() - initiatedDate.getTime());
	long totalMinutes = millis / 60000;
	long days = totalMinutes / (24 * 60);
	long hours = (totalMinutes % (24 * 60)) / 60;
	long minutes = totalMinutes % 60;
	return String.format("%dD:%dH:%dM", days, hours, minutes);
    }

    private String lastNonNull(List<ExternalApiTraceLog> caseRowsOldestFirst,
	    Function<ExternalApiTraceLog, String> getter) {
	String value = null;
	for (ExternalApiTraceLog row : caseRowsOldestFirst) {
	    String candidate = getter.apply(row);
	    if (candidate != null) {
		value = candidate;
	    }
	}
	return value;
    }

    private Map<Integer, String> loadTenantNames() {
	Map<Integer, String> names = new LinkedHashMap<>();
	for (Tenant tenant : tenantRepository.findAll()) {
	    names.put(tenant.getTenantId(), tenant.getTenantName());
	}
	return names;
    }

    private Integer resolveTpaId(String tpaName) throws ValidationException {
	if (tpaName == null || tpaName.isBlank()) {
	    return null;
	}
	return masterPayorRepository.findByTpaNameIgnoreCase(tpaName).map(MasterPayor::getMasterPayorId)
		.orElseThrow(() -> new ValidationException("Unknown TPA: " + tpaName));
    }

    private Map<Integer, String> loadPayorNames() {
	Map<Integer, String> names = new LinkedHashMap<>();
	for (MasterPayor payor : masterPayorRepository.findAll()) {
	    names.put(payor.getMasterPayorId(), payor.getTpaName());
	}
	return names;
    }

    // ---- KPIs -------------------------------------------------------------------------

    private DashboardKpiDTO buildKpis(List<DashboardTransactionRow> currentRows,
	    List<DashboardTransactionRow> previousRows) {
	DashboardKpiDTO kpi = new DashboardKpiDTO();

	List<DashboardTransactionRow> currentLogical = logicalTransactions(currentRows);
	kpi.setTotalTransactions(currentLogical.size());
	kpi.setSuccess((int) countStatus(currentLogical, STATUS_SUCCESS));
	kpi.setFailure((int) countStatus(currentLogical, STATUS_FAILURE));
	kpi.setQueryReply((int) countQueryReply(currentRows));

	RetrySummaryDTO retry = buildRetrySummary(currentRows);
	kpi.setRetryTransactions(retry.getRetriedTransactions());
	kpi.setTotalRetryAttempts(retry.getTotalRetryAttempts());

	kpi.setComparisonToPreviousPeriod(buildComparison(currentRows, previousRows, currentLogical, retry));
	return kpi;
    }

    private ComparisonDTO buildComparison(List<DashboardTransactionRow> currentRows,
	    List<DashboardTransactionRow> previousRows, List<DashboardTransactionRow> currentLogical,
	    RetrySummaryDTO currentRetry) {
	List<DashboardTransactionRow> previousLogical = logicalTransactions(previousRows);
	RetrySummaryDTO previousRetry = buildRetrySummary(previousRows);

	ComparisonDTO comparison = new ComparisonDTO();
	comparison.setTotalTransactions(pctChange(currentLogical.size(), previousLogical.size()));
	comparison.setSuccess(
		pctChange(countStatus(currentLogical, STATUS_SUCCESS), countStatus(previousLogical, STATUS_SUCCESS)));
	comparison.setFailure(
		pctChange(countStatus(currentLogical, STATUS_FAILURE), countStatus(previousLogical, STATUS_FAILURE)));
	comparison.setQueryReply(pctChange(countQueryReply(currentRows), countQueryReply(previousRows)));
	comparison.setRetryTransactions(
		pctChange(currentRetry.getRetriedTransactions(), previousRetry.getRetriedTransactions()));
	comparison.setTotalRetryAttempts(
		pctChange(currentRetry.getTotalRetryAttempts(), previousRetry.getTotalRetryAttempts()));
	return comparison;
    }

    private double pctChange(long current, long previous) {
	if (previous == 0) {
	    return current == 0 ? 0.0 : 100.0;
	}
	return ((current - previous) * 100.0) / previous;
    }

    private long countStatus(List<DashboardTransactionRow> rows, String status) {
	return rows.stream().filter(r -> status.equalsIgnoreCase(r.getStatus())).count();
    }

    private long countQueryReply(List<DashboardTransactionRow> rows) {
	return rows.stream().filter(r -> ACTION_TYPE_QUERY_REPLY.equals(r.getActionType())).count();
    }

    // ---- Module summary (Preauth / Claims) ---------------------------------------------

    private ModuleSummaryDTO buildModuleSummary(List<DashboardTransactionRow> rows, String module) {
	List<DashboardTransactionRow> moduleRows = rows.stream().filter(r -> module.equals(r.getModule()))
		.collect(Collectors.toList());

	// Raw (every attempt, uncollapsed) - failure-reason breakdowns need to see every failed
	// attempt, not just the one representative row a retried case collapses down to.
	List<DashboardTransactionRow> rawInboundRows = moduleRows.stream()
		.filter(r -> FLOW_TYPE_INBOUND.equals(r.getFlowType())).collect(Collectors.toList());
	List<DashboardTransactionRow> rawQueryReplyRows = moduleRows.stream()
		.filter(r -> ACTION_TYPE_QUERY_REPLY.equals(r.getActionType())).collect(Collectors.toList());
	List<DashboardTransactionRow> rawSubmissionRows = moduleRows.stream()
		.filter(r -> ACTION_TYPE_SUBMIT.equals(r.getActionType())).collect(Collectors.toList());

	// Deduped (SUBMIT retries collapsed to one row per case, everything else untouched) - the
	// rings/total/requestTypeCounts are derived from this same set so they sum consistently:
	// total == inbound.total + outbound.total + queryReply.total by construction.
	List<DashboardTransactionRow> dedupedModuleRows = logicalTransactions(moduleRows);
	List<DashboardTransactionRow> inboundRows = dedupedModuleRows.stream()
		.filter(r -> FLOW_TYPE_INBOUND.equals(r.getFlowType())).collect(Collectors.toList());
	List<DashboardTransactionRow> outboundRows = dedupedModuleRows.stream()
		.filter(r -> FLOW_TYPE_OUTBOUND.equals(r.getFlowType())
			&& !ACTION_TYPE_QUERY_REPLY.equals(r.getActionType()))
		.collect(Collectors.toList());
	List<DashboardTransactionRow> queryReplyRows = dedupedModuleRows.stream()
		.filter(r -> ACTION_TYPE_QUERY_REPLY.equals(r.getActionType())).collect(Collectors.toList());

	ModuleSummaryDTO summary = new ModuleSummaryDTO();
	summary.setTotal(dedupedModuleRows.size());
	summary.setInbound(toFlowSummary(inboundRows));
	summary.setOutbound(toFlowSummary(outboundRows));
	summary.setQueryReply(toQueryReplySummary(queryReplyRows));

	for (DashboardTransactionRow row : outboundRows) {
	    if (row.getRequestType() != null) {
		summary.getRequestTypeCounts().merge(row.getRequestType(), 1, Integer::sum);
	    }
	}

	summary.setRetry(buildRetrySummary(moduleRows));
	summary.setTopFailureReasons(buildTopFailureReasons(moduleRows));
	summary.setTopSubmissionFailureReasons(buildTopFailureReasons(rawSubmissionRows));
	summary.setTopQueryReplyFailureReasons(buildTopFailureReasons(rawQueryReplyRows));
	summary.setTopInboundFailureReasons(buildTopFailureReasons(rawInboundRows));
	return summary;
    }

    private FlowSummaryDTO toFlowSummary(List<DashboardTransactionRow> rows) {
	FlowSummaryDTO stats = new FlowSummaryDTO();
	stats.setTotal(rows.size());
	stats.setSuccess((int) countStatus(rows, STATUS_SUCCESS));
	stats.setFailure((int) countStatus(rows, STATUS_FAILURE));
	return stats;
    }

    private QueryReplySummaryDTO toQueryReplySummary(List<DashboardTransactionRow> rows) {
	QueryReplySummaryDTO stats = new QueryReplySummaryDTO();
	stats.setTotal(rows.size());
	long success = countStatus(rows, STATUS_SUCCESS);
	long failure = countStatus(rows, STATUS_FAILURE);
	stats.setSuccess((int) success);
	stats.setFailure((int) failure);
	stats.setPending((int) (rows.size() - success - failure));
	return stats;
    }

    private List<FailureReasonDTO> buildTopFailureReasons(List<DashboardTransactionRow> rows) {
	List<String> categories = rows.stream().filter(r -> STATUS_FAILURE.equalsIgnoreCase(r.getStatus()))
		.map(r -> FailureCategoryClassifier.classify(r.getFailureReason())).filter(c -> c != null)
		.collect(Collectors.toList());
	int totalFailures = categories.size();

	Map<String, Long> counts = categories.stream()
		.collect(Collectors.groupingBy(category -> category, Collectors.counting()));

	return counts.entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed())
		.limit(TOP_FAILURE_REASONS_LIMIT).map(entry -> {
		    FailureReasonDTO dto = new FailureReasonDTO();
		    dto.setReason(entry.getKey());
		    dto.setCount(entry.getValue().intValue());
		    dto.setPercent(rate(entry.getValue(), totalFailures));
		    return dto;
		}).collect(Collectors.toList());
    }

    // Java-side, since the category filter matches the normalized FailureCategoryClassifier
    // output, not the raw failureReason column the DB query filters on.
    private List<DashboardTransactionRow> applyFailureCategoryFilter(List<DashboardTransactionRow> rows,
	    String failureCategory) {
	if (failureCategory == null || failureCategory.isBlank()) {
	    return rows;
	}
	return rows.stream()
		.filter(r -> failureCategory.equalsIgnoreCase(FailureCategoryClassifier.classify(r.getFailureReason())))
		.collect(Collectors.toList());
    }

    // ---- Retry chains -------------------------------------------------------------------

    // A retry chain is every row sharing the same rootId (tenant+case+module+actionType) - see
    // rootId() for why this is grouped by identity rather than by the retry_submission flag: that
    // flag/attempt_no have been observed unset even on rows that are clearly repeat attempts.
    // Scoped to actionType=SUBMIT only - QUERY_REPLY/DOCUMENT_UPLOAD can legitimately repeat as
    // part of a normal back-and-forth with the payer, and INBOUND actionTypes like
    // PORTAL_STATUS/MEMBER_VALIDATION are routinely polled/re-checked - none of that is a retry
    // after a failure, so only the case's actual submission attempts count here.
    private RetrySummaryDTO buildRetrySummary(List<DashboardTransactionRow> rows) {
	List<DashboardTransactionRow> submissionRows = rows.stream()
		.filter(r -> ACTION_TYPE_SUBMIT.equals(r.getActionType())).collect(Collectors.toList());
	Map<String, List<DashboardTransactionRow>> allChains = submissionRows.stream().collect(Collectors.groupingBy(this::rootId));

	Map<String, List<DashboardTransactionRow>> chainsByRoot = allChains.entrySet().stream()
		.filter(e -> e.getValue().size() > 1)
		.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

	int retryAttempts = chainsByRoot.values().stream().mapToInt(chain -> chain.size() - 1).sum();

	int successfulAfterRetry = 0;
	int failedAfterRetry = 0;
	int singleRetrySuccess = 0;
	int multipleRetrySuccess = 0;

	for (List<DashboardTransactionRow> chain : chainsByRoot.values()) {
	    List<DashboardTransactionRow> chainOldestFirst = chain.stream()
		    .sorted(Comparator.comparing(DashboardTransactionRow::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
		    .collect(Collectors.toList());

	    int firstSuccessIndex = -1;
	    for (int i = 0; i < chainOldestFirst.size(); i++) {
		if (STATUS_SUCCESS.equalsIgnoreCase(chainOldestFirst.get(i).getStatus())) {
		    firstSuccessIndex = i;
		    break;
		}
	    }

	    if (firstSuccessIndex < 0) {
		failedAfterRetry++;
		continue;
	    }

	    successfulAfterRetry++;
	    // Prefer the row's own attempt_no when it's actually populated (durable even if an
	    // earlier failed attempt fell outside the caller's date filter); otherwise fall back to
	    // this attempt's position within the chain visible here.
	    Integer attemptNo = chainOldestFirst.get(firstSuccessIndex).getRetryAttemptNumber();
	    int attemptsBeforeSuccess = attemptNo != null && attemptNo > 0 ? attemptNo - 1 : firstSuccessIndex;
	    if (attemptsBeforeSuccess <= 1) {
		singleRetrySuccess++;
	    } else {
		multipleRetrySuccess++;
	    }
	}

	RetrySummaryDTO summary = new RetrySummaryDTO();
	summary.setRetriedTransactions(chainsByRoot.size());
	summary.setTotalRetryAttempts(retryAttempts);
	summary.setSuccessfulAfterRetry(successfulAfterRetry);
	summary.setFailedAfterRetry(failedAfterRetry);
	summary.setSingleRetrySuccess(singleRetrySuccess);
	summary.setMultipleRetrySuccess(multipleRetrySuccess);
	return summary;
    }

    // Collapses SUBMIT retry attempts into one representative row per case (root id), keeping the
    // latest/success-sticky attempt - this is what makes kpis.totalTransactions/success/failure
    // consistent with each other, and is also used for recentTransactions. Every other actionType
    // (QUERY_REPLY, PORTAL_STATUS, DOCUMENT_UPLOAD, ...) passes through untouched, one row each -
    // those repeat legitimately as separate events, not as retries of the same attempt, so
    // collapsing them here would undercount total vs. the inbound/outbound/queryReply flow sums.
    private List<DashboardTransactionRow> logicalTransactions(List<DashboardTransactionRow> rows) {
	List<DashboardTransactionRow> submissionRows = rows.stream()
		.filter(r -> ACTION_TYPE_SUBMIT.equals(r.getActionType())).collect(Collectors.toList());
	List<DashboardTransactionRow> otherRows = rows.stream()
		.filter(r -> !ACTION_TYPE_SUBMIT.equals(r.getActionType())).collect(Collectors.toList());

	Set<String> submissionRootIds = submissionRows.stream().map(this::rootId).collect(Collectors.toSet());
	List<DashboardTransactionRow> result = new ArrayList<>(latestPerRoot(submissionRows, submissionRootIds).values());
	result.addAll(otherRows);
	return result;
    }

    // Business rule: current status must always represent the case's final resolved outcome -
    // once a case has reached SUCCESS, a later failed retry must NOT downgrade it back to
    // FAILURE (every attempt still lands in history via ExternalApiTraceLog; this only affects
    // which single row represents the case for dashboard aggregation). So: if any attempt for a
    // root succeeded, the latest SUCCESS attempt represents it; only roots that never succeeded
    // fall back to their latest attempt overall (correctly still showing as FAILURE/pending).
    private Map<String, DashboardTransactionRow> latestPerRoot(List<DashboardTransactionRow> rows,
	    Set<String> rootIdsOfInterest) {
	Map<String, DashboardTransactionRow> latestByRoot = new LinkedHashMap<>();
	Map<String, DashboardTransactionRow> latestSuccessByRoot = new LinkedHashMap<>();
	for (DashboardTransactionRow row : rows) {
	    String root = rootId(row);
	    if (!rootIdsOfInterest.contains(root)) {
		continue;
	    }
	    DashboardTransactionRow current = latestByRoot.get(root);
	    if (current == null || isAfter(row.getCreatedAt(), current.getCreatedAt())) {
		latestByRoot.put(root, row);
	    }
	    if (STATUS_SUCCESS.equalsIgnoreCase(row.getStatus())) {
		DashboardTransactionRow currentSuccess = latestSuccessByRoot.get(root);
		if (currentSuccess == null || isAfter(row.getCreatedAt(), currentSuccess.getCreatedAt())) {
		    latestSuccessByRoot.put(root, row);
		}
	    }
	}
	latestByRoot.putAll(latestSuccessByRoot);
	return latestByRoot;
    }

    private boolean isAfter(Date candidate, Date current) {
	if (candidate == null) {
	    return false;
	}
	return current == null || candidate.after(current);
    }

    // Groups every attempt at the same logical case+flow+stage together, regardless of whether
    // the writer actually set retry_submission/attempt_no on the repeat attempt - those fields
    // have been observed unset even on rows that are clearly repeat SUBMITs for the same case
    // (e.g. a SUCCESS followed by a later FAILURE for the same referenceId/module/actionType),
    // which made parentTransactionId-based grouping silently undercount retries. tenantId is
    // included since referenceId isn't guaranteed unique across tenants. requestType is included
    // so a case's separate lifecycle stages (e.g. an INITIAL submission and a later, unrelated
    // DISCHARGE submission) are never treated as retries of each other - only repeat attempts of
    // the SAME requestType are a retry chain.
    private String rootId(DashboardTransactionRow row) {
	return row.getTenantId() + "|" + row.getCaseId() + "|" + row.getModule() + "|" + row.getActionType() + "|"
		+ row.getRequestType();
    }

    // ---- API health -----------------------------------------------------------------------

    private ApiHealthDTO buildApiHealth(Integer tenantId, String hospitalId, Integer tpaId,
	    Map<Integer, String> payorNames) {
	Date windowEnd = new Date();
	Date windowStart = Date.from(windowEnd.toInstant().minusSeconds(API_HEALTH_WINDOW_MINUTES * 60));
	List<DashboardTransactionRow> recentRows = dashboardTransactionRowRepository.findForOverview(tenantId,
		hospitalId, tpaId, null, null, windowStart, windowEnd);

	ApiHealthDTO health = new ApiHealthDTO();
	long success = countStatus(recentRows, STATUS_SUCCESS);
	long failure = countStatus(recentRows, STATUS_FAILURE);
	long terminal = success + failure;
	double overallPct = terminal == 0 ? 100.0 : rate(success, terminal);
	health.setOverallPct(overallPct);
	health.setOverallLabel(overallHealthLabel(overallPct));

	Map<Integer, List<DashboardTransactionRow>> byTpa = recentRows.stream().filter(r -> r.getTpaId() != null)
		.collect(Collectors.groupingBy(DashboardTransactionRow::getTpaId));

	List<EndpointHealthDTO> endpoints = new ArrayList<>();
	for (Map.Entry<Integer, List<DashboardTransactionRow>> entry : byTpa.entrySet()) {
	    List<DashboardTransactionRow> tpaRows = entry.getValue();
	    long tpaSuccess = countStatus(tpaRows, STATUS_SUCCESS);
	    long tpaFailure = countStatus(tpaRows, STATUS_FAILURE);
	    long tpaTerminal = tpaSuccess + tpaFailure;
	    double tpaPct = tpaTerminal == 0 ? 100.0 : rate(tpaSuccess, tpaTerminal);
	    String status = endpointStatus(tpaPct);

	    EndpointHealthDTO endpoint = new EndpointHealthDTO();
	    endpoint.setName(payorNames.getOrDefault(entry.getKey(), "TPA " + entry.getKey()));
	    endpoint.setStatus(status);
	    endpoint.setResponseTimeMs("DOWN".equals(status) ? null : averageElapsedMs(tpaRows));
	    endpoint.setLastCheckedSec(secondsSinceMostRecent(tpaRows, windowEnd));
	    endpoints.add(endpoint);
	}
	health.setEndpoints(endpoints);
	return health;
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

    private long secondsSinceMostRecent(List<DashboardTransactionRow> rows, Date now) {
	Date mostRecent = rows.stream().map(DashboardTransactionRow::getCreatedAt).filter(d -> d != null)
		.max(Comparator.naturalOrder()).orElse(null);
	if (mostRecent == null) {
	    return -1;
	}
	return Math.max(0, (now.getTime() - mostRecent.getTime()) / 1000);
    }

    private String overallHealthLabel(double pct) {
	if (pct >= 95) {
	    return "Healthy";
	}
	if (pct >= 80) {
	    return "Degraded";
	}
	return "Down";
    }

    private String endpointStatus(double pct) {
	if (pct >= 95) {
	    return "UP";
	}
	if (pct >= 80) {
	    return "DEGRADED";
	}
	return "DOWN";
    }

    // ---- Volume trend -----------------------------------------------------------------------

    private List<VolumeTrendPointDTO> buildVolumeTrend(Integer tenantId, String hospitalId, Integer tpaId,
	    String failureCategory, String requestType, int trendWindowDays, Date referenceDate) {
	LocalDate referenceLocalDate = referenceDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	LocalDate windowStart = referenceLocalDate.minusDays(trendWindowDays - 1L);
	Date fromDate = toDate(windowStart);
	Date toDateExclusive = addOneDay(toDate(referenceLocalDate));

	List<DashboardTransactionRow> rows = applyFailureCategoryFilter(dashboardTransactionRowRepository
		.findForOverview(tenantId, hospitalId, tpaId, null, requestType, fromDate, toDateExclusive),
		failureCategory);

	Map<LocalDate, VolumeTrendPointDTO> byDate = new LinkedHashMap<>();
	for (int i = 0; i < trendWindowDays; i++) {
	    LocalDate date = windowStart.plusDays(i);
	    VolumeTrendPointDTO point = new VolumeTrendPointDTO();
	    point.setDate(date.toString());
	    byDate.put(date, point);
	}

	for (DashboardTransactionRow row : rows) {
	    if (row.getCreatedAt() == null) {
		continue;
	    }
	    LocalDate date = row.getCreatedAt().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	    VolumeTrendPointDTO point = byDate.get(date);
	    if (point == null) {
		continue;
	    }
	    if (MODULE_PREAUTH.equals(row.getModule())) {
		point.setPreauth(point.getPreauth() + 1);
	    } else if (MODULE_CLAIMS.equals(row.getModule())) {
		point.setClaims(point.getClaims() + 1);
	    }

	    if (ACTION_TYPE_QUERY_REPLY.equals(row.getActionType())) {
		point.setQueryReply(point.getQueryReply() + 1);
	    } else if (FLOW_TYPE_INBOUND.equals(row.getFlowType())) {
		point.setInbound(point.getInbound() + 1);
	    } else if (FLOW_TYPE_OUTBOUND.equals(row.getFlowType())) {
		point.setOutbound(point.getOutbound() + 1);
	    }
	}
	return new ArrayList<>(byDate.values());
    }

    // ---- Recent alerts / transactions -----------------------------------------------------

    private List<RecentAlertDTO> buildRecentAlerts(ApiHealthDTO apiHealth, ModuleSummaryDTO preauth,
	    ModuleSummaryDTO claims) {
	List<RecentAlertDTO> alerts = new ArrayList<>();
	for (EndpointHealthDTO endpoint : apiHealth.getEndpoints()) {
	    if ("DOWN".equals(endpoint.getStatus())) {
		alerts.add(alert("alert-down-" + endpoint.getName(), "critical", "API Down", endpoint.getName(),
			"just now"));
	    }
	}
	addFailureRateAlert(alerts, "Preauth", preauth);
	addFailureRateAlert(alerts, "Claims", claims);
	addRetryRateAlert(alerts, "Preauth", preauth);
	addRetryRateAlert(alerts, "Claims", claims);
	return alerts;
    }

    private void addFailureRateAlert(List<RecentAlertDTO> alerts, String moduleLabel, ModuleSummaryDTO summary) {
	int total = summary.getInbound().getTotal() + summary.getOutbound().getTotal()
		+ summary.getQueryReply().getTotal();
	int failure = summary.getInbound().getFailure() + summary.getOutbound().getFailure()
		+ summary.getQueryReply().getFailure();
	if (total == 0) {
	    return;
	}
	double failureRate = rate(failure, total);
	if (failureRate >= HIGH_FAILURE_RATE_THRESHOLD_PCT) {
	    alerts.add(alert("alert-highfailure-" + moduleLabel, "warning", "High Failure Rate",
		    moduleLabel + " at " + Math.round(failureRate) + "%", "just now"));
	}
    }

    private void addRetryRateAlert(List<RecentAlertDTO> alerts, String moduleLabel, ModuleSummaryDTO summary) {
	if (summary.getTotal() == 0) {
	    return;
	}
	double retryRate = rate(summary.getRetry().getRetriedTransactions(), summary.getTotal());
	if (retryRate >= HIGH_RETRY_RATE_THRESHOLD_PCT) {
	    alerts.add(alert("alert-highretry-" + moduleLabel, "warning", "High Retry Rate",
		    moduleLabel + " at " + Math.round(retryRate) + "%", "just now"));
	}
    }

    private RecentAlertDTO alert(String id, String severity, String title, String detail, String time) {
	RecentAlertDTO dto = new RecentAlertDTO();
	dto.setId(id);
	dto.setSeverity(severity);
	dto.setTitle(title);
	dto.setDetail(detail);
	dto.setTime(time);
	return dto;
    }

    private List<RecentTransactionDTO> buildRecentTransactions(List<DashboardTransactionRow> rows,
	    Map<Integer, String> payorNames) {
	return logicalTransactions(rows).stream()
		.sorted(Comparator
			.comparing(DashboardTransactionRow::getCreatedAt, Comparator.nullsFirst(Comparator.naturalOrder()))
			.reversed())
		.limit(RECENT_TRANSACTIONS_LIMIT).map(row -> {
		    RecentTransactionDTO dto = new RecentTransactionDTO();
		    dto.setId(row.getCaseId());
		    dto.setCaseId(row.getCaseId());
		    dto.setModule(row.getModule());
		    dto.setFlow(ACTION_TYPE_QUERY_REPLY.equals(row.getActionType()) ? "QUERY_REPLY" : row.getFlowType());
		    dto.setApiTpa(row.getTpaId() == null ? null
			    : payorNames.getOrDefault(row.getTpaId(), "TPA " + row.getTpaId()));
		    dto.setStatus(row.getStatus());
		    dto.setTimestamp(formatTimestamp(row.getCreatedAt()));
		    return dto;
		}).collect(Collectors.toList());
    }

    private String formatTimestamp(Date date) {
	if (date == null) {
	    return null;
	}
	return TIMESTAMP_FORMATTER.format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    // ---- Shared helpers ---------------------------------------------------------------------

    private double rate(long numerator, long denominator) {
	return denominator == 0 ? 0.0 : (numerator * 100.0 / denominator);
    }

    private Date toDate(LocalDate localDate) {
	return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date addOneDay(Date date) {
	Instant instant = date.toInstant().plusSeconds(24 * 60 * 60);
	return Date.from(instant);
    }

    private List<TraceHistoryVO> toHistoryVOs(List<ExternalApiTraceLog> rows) {
	List<TraceHistoryVO> result = new ArrayList<>();
	for (ExternalApiTraceLog row : rows) {
	    TraceHistoryVO vo = new TraceHistoryVO();
	    vo.setExternalApiTraceLogId(row.getExternalApiTraceLogId());
	    vo.setFunctionality(row.getFunctionality());
	    vo.setRequestType(row.getRequestType());
	    vo.setActionType(row.getActionType());
	    vo.setStatus(row.getStatus());
	    vo.setBusinessStatus(row.getBusinessStatus());
	    vo.setAttemptNo(row.getAttemptNo());
	    vo.setRetrySubmission(row.isRetrySubmission());
	    vo.setRemarks(row.getRemarks());
	    vo.setExceptionMessage(row.getExceptionMessage());
	    vo.setRequestedTime(row.getRequestedTime());
	    vo.setResponseReceivedTime(row.getResponseReceivedTime());
	    vo.setElapsedTimeMs(row.getElapsedTimeMs());
	    vo.setEventLabel(buildEventLabel(row));
	    result.add(vo);
	}
	return result;
    }

    private String buildEventLabel(ExternalApiTraceLog row) {
	boolean success = STATUS_SUCCESS.equalsIgnoreCase(row.getStatus());
	String verb;
	String actionType = row.getActionType() == null ? "" : row.getActionType();
	switch (actionType) {
	case "SUBMIT":
	    verb = "Submission";
	    break;
	case "QUERY_REPLY":
	    verb = "Query Reply";
	    break;
	case ACTION_TYPE_PORTAL_STATUS:
	    verb = "Portal Status Poll";
	    break;
	case "MEMBER_VALIDATION":
	    verb = "Member Validation";
	    break;
	case "DOCUMENT_UPLOAD":
	    verb = "Document Upload";
	    break;
	default:
	    verb = actionType;
	}

	if (!success) {
	    return verb + " Failed";
	}
	return row.isRetrySubmission() ? verb + " Retried" : verb + " Succeeded";
    }

}

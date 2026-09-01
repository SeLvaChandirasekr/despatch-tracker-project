package com.abi.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import com.abi.entity.DashboardTransactionRow;


// Extends the plain Repository marker (not JpaRepository) - this is a read-only view, no
// save/delete should ever be exposed on it.
public interface DashboardTransactionRowRepository extends Repository<DashboardTransactionRow, Long> {

    // Restricted to module IN (PREAUTH, CLAIMS) - the only two functionality values the
    // dashboard has a card for. functionality itself isn't limited to those two (it can also be
    // e.g. MEMBER_VALIDATION, OTP, AUTH_TOKEN as a standalone call unrelated to any case), and
    // rows with those other values were previously leaking into the top-level KPI totals
    // (counted in kpis.total/failure/etc.) while being invisible on both module cards, since
    // buildModuleSummary only ever looks for module="PREAUTH"/"CLAIMS". This allowlist guarantees
    // kpis.total/success/failure/queryReply always equal preauth + claims, by construction.
    // Also excludes actionType=MEMBER_VALIDATION specifically within PREAUTH/CLAIMS - a
    // policy/member pre-check ahead of a case submission, not a case transaction in its own
    // right. Case history/polling endpoints go through ExternalApiTraceRepository instead and
    // still show all of this, since that's a full audit trail.
    @Query("SELECT r FROM DashboardTransactionRow r WHERE (:tenantId IS NULL OR r.tenantId = :tenantId) "
	    + "AND (:hospitalId IS NULL OR r.hospitalId = :hospitalId) "
	    + "AND (:tpaId IS NULL OR r.tpaId = :tpaId) "
	    + "AND (:failureReason IS NULL OR r.failureReason = :failureReason) "
	    + "AND (:requestType IS NULL OR r.requestType = :requestType) "
	    + "AND r.module IN ('PREAUTH', 'CLAIMS') "
	    + "AND r.actionType <> 'MEMBER_VALIDATION' "
	    + "AND r.createdAt >= :fromDate AND r.createdAt < :toDateExclusive")
    List<DashboardTransactionRow> findForOverview(@Param("tenantId") Integer tenantId,
	    @Param("hospitalId") String hospitalId, @Param("tpaId") Integer tpaId,
	    @Param("failureReason") String failureReason, @Param("requestType") String requestType,
	    @Param("fromDate") Date fromDate, @Param("toDateExclusive") Date toDateExclusive);

    // Powers the dashboard's "Failure Category" filter dropdown.
    @Query("SELECT DISTINCT r.failureReason FROM DashboardTransactionRow r WHERE r.failureReason IS NOT NULL "
	    + "ORDER BY r.failureReason")
    List<String> findDistinctFailureReasons();

}

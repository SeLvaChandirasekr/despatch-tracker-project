package com.abi.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.abi.entity.ExternalApiTraceLog;


public interface ExternalApiTraceRepository extends JpaRepository<ExternalApiTraceLog, Long> {

    @Query("SELECT e FROM ExternalApiTraceLog e WHERE e.tenantId = :tenantId AND e.referenceId = :referenceId "
	    + "ORDER BY e.requestedTime ASC")
    List<ExternalApiTraceLog> findByReferenceId(@Param("tenantId") Integer tenantId,
	    @Param("referenceId") String referenceId);

    @Query("SELECT e FROM ExternalApiTraceLog e WHERE e.tenantId = :tenantId AND e.referenceId = :referenceId "
	    + "AND e.actionType = :actionType ORDER BY e.requestedTime ASC")
    List<ExternalApiTraceLog> findByReferenceId(@Param("tenantId") Integer tenantId,
	    @Param("referenceId") String referenceId, @Param("actionType") String actionType);

    @Query("SELECT e FROM ExternalApiTraceLog e WHERE (:tenantId IS NULL OR e.tenantId = :tenantId) "
	    + "AND e.functionality = :functionality AND e.actionType = :actionType "
	    + "AND e.requestedTime >= :fromDate AND e.requestedTime < :toDateExclusive")
    List<ExternalApiTraceLog> findByFunctionalityAndActionType(@Param("tenantId") Integer tenantId,
	    @Param("functionality") String functionality, @Param("actionType") String actionType,
	    @Param("fromDate") Date fromDate, @Param("toDateExclusive") Date toDateExclusive);

    // Prior attempts for the same case+functionality+actionType, oldest first - used at write time
    // to work out previousStatus (last row's status) and parentTransactionId (first row's id) for
    // the dashboard event, without needing a stored transactionId or an upsert.
    List<ExternalApiTraceLog> findByReferenceIdAndFunctionalityAndActionTypeOrderByRequestedTimeAsc(
	    String referenceId, String functionality, String actionType);

    // Step 1 of the PreAuth Transactions table: which cases were SUBMITted within the filter
    // window - same "submitted in this window" semantics as the PREAUTH SUBMITTED KPI card.
    // Determines which cases are in scope; each case's full history is then pulled separately
    // via findAllByReferenceIds so a case isn't fragmented by the date filter.
    @Query("SELECT e FROM ExternalApiTraceLog e WHERE (:tenantId IS NULL OR e.tenantId = :tenantId) "
	    + "AND (:tpaId IS NULL OR e.tpaId = :tpaId) AND e.functionality = :functionality "
	    + "AND e.actionType = 'SUBMIT' AND (:requestType IS NULL OR e.requestType = :requestType) "
	    + "AND e.requestedTime >= :fromDate AND e.requestedTime < :toDateExclusive ORDER BY e.requestedTime ASC")
    List<ExternalApiTraceLog> findSubmittedCases(@Param("tenantId") Integer tenantId, @Param("tpaId") Integer tpaId,
	    @Param("functionality") String functionality, @Param("requestType") String requestType,
	    @Param("fromDate") Date fromDate, @Param("toDateExclusive") Date toDateExclusive);

    // Step 2: full trace history (every action type, unbounded by date) for a known set of
    // cases - used to resolve each case's current status/identity regardless of when those
    // events happened relative to the display window.
    @Query("SELECT e FROM ExternalApiTraceLog e WHERE (:tenantId IS NULL OR e.tenantId = :tenantId) "
	    + "AND e.functionality = :functionality AND e.referenceId IN :referenceIds ORDER BY e.requestedTime ASC")
    List<ExternalApiTraceLog> findAllByReferenceIds(@Param("tenantId") Integer tenantId,
	    @Param("functionality") String functionality, @Param("referenceIds") Collection<String> referenceIds);

}

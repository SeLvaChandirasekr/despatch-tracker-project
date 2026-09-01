-- ============================================================================
-- Dashboard Verification Queries
--
-- Independently verifies the numbers returned by /api/dashboard/summary,
-- /api/dashboard/preauth/transactions, and /api/reports/* by replicating the same
-- rules the Java code uses (DashboardServiceImpl / ReportServiceImpl):
--   - MEMBER_VALIDATION is excluded from every dashboard aggregation
--   - SUBMIT retries collapse to one row per (tenant, reference, functionality,
--     requestType) chain - success-sticky (a later failed retry never downgrades
--     a chain that already succeeded); every other action type counts as-is
--   - Two different requestTypes for the same case (e.g. an INITIAL submission and
--     a later, unrelated DISCHARGE submission) are NEVER the same chain
--
-- Before running: fill in the three placeholders used throughout this file -
--   <FUNCTIONALITY>  'PREAUTH' or 'CLAIMS'
--   <FROM_DATE>      e.g. '2026-08-25 00:00:00'
--   <TO_DATE>        e.g. '2026-08-26 00:00:00'   (exclusive upper bound)
-- Find/replace them across the file, then run each section separately.
-- ============================================================================


-- ----------------------------------------------------------------------------
-- 0. Sanity check - what functionality/action_type/request_type combinations
--    actually exist in this window, and how many rows each has.
-- ----------------------------------------------------------------------------
SELECT functionality, action_type, request_type, COUNT(*)
FROM external_api_trace_log
WHERE requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>'
GROUP BY functionality, action_type, request_type
ORDER BY 1, 2, 3;


-- ----------------------------------------------------------------------------
-- 1. Top-level KPIs (matches getOverview's "kpis" block: totalTransactions,
--    success, failure, queryReply) - across BOTH modules, restricted to
--    functionality IN (PREAUTH, CLAIMS) since those are the only two the
--    dashboard has a card for (functionality can also be e.g. MEMBER_VALIDATION,
--    OTP, AUTH_TOKEN as a standalone call unrelated to any case - those rows
--    used to leak into these totals while being invisible on both module cards).
--    actionType=MEMBER_VALIDATION is separately excluded within PREAUTH/CLAIMS.
--    SUBMIT chains deduped.
-- ----------------------------------------------------------------------------
WITH scoped AS (
    SELECT *
    FROM external_api_trace_log
    WHERE functionality IN ('PREAUTH', 'CLAIMS')
      AND action_type <> 'MEMBER_VALIDATION'
      AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>'
),
submit_dedup AS (
    -- One row per (tenant, reference, functionality, requestType) SUBMIT chain.
    -- ORDER BY (status='SUCCESS') DESC, requested_time DESC + DISTINCT ON gives the
    -- success-sticky pick: latest SUCCESS if one exists anywhere in the chain,
    -- otherwise the chain's latest row.
    SELECT DISTINCT ON (tenant_id, reference_id, functionality, request_type)
        tenant_id, reference_id, functionality, action_type, status
    FROM scoped
    WHERE action_type = 'SUBMIT'
    ORDER BY tenant_id, reference_id, functionality, request_type,
             (status = 'SUCCESS') DESC, requested_time DESC
),
logical AS (
    SELECT tenant_id, reference_id, functionality, action_type, status FROM submit_dedup
    UNION ALL
    SELECT tenant_id, reference_id, functionality, action_type, status FROM scoped WHERE action_type <> 'SUBMIT'
)
SELECT
    COUNT(*)                                                          AS total_transactions,
    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END)               AS success,
    SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END)               AS failure,
    (SELECT COUNT(*) FROM scoped WHERE action_type = 'QUERY_REPLY')   AS query_reply
FROM logical;


-- ----------------------------------------------------------------------------
-- 2. Module summary breakdown (matches preauth/claims .inbound/.outbound/.queryReply
--    and .total in getOverview) - same dedup rule as above, scoped to one module.
-- ----------------------------------------------------------------------------
WITH scoped AS (
    SELECT *
    FROM external_api_trace_log
    WHERE functionality = '<FUNCTIONALITY>'
      AND action_type <> 'MEMBER_VALIDATION'
      AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>'
),
submit_dedup AS (
    SELECT DISTINCT ON (tenant_id, reference_id, request_type)
        tenant_id, reference_id, action_type, status
    FROM scoped
    WHERE action_type = 'SUBMIT'
    ORDER BY tenant_id, reference_id, request_type, (status = 'SUCCESS') DESC, requested_time DESC
),
deduped AS (
    SELECT tenant_id, reference_id, action_type, status FROM submit_dedup
    UNION ALL
    SELECT tenant_id, reference_id, action_type, status FROM scoped WHERE action_type <> 'SUBMIT'
)
SELECT
    CASE
        WHEN action_type = 'QUERY_REPLY' THEN 'QUERY_REPLY'
        WHEN action_type IN ('SUBMIT', 'DOCUMENT_UPLOAD') THEN 'OUTBOUND'
        ELSE 'INBOUND'
    END AS flow,
    COUNT(*) AS total,
    SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) AS success,
    SUM(CASE WHEN status = 'FAILURE' THEN 1 ELSE 0 END) AS failure
FROM deduped
GROUP BY 1
ORDER BY 1;
-- .total on the dashboard = the sum of all three "total" values above.


-- ----------------------------------------------------------------------------
-- 3. Retry summary (matches preauth.retry / claims.retry) - SUBMIT only, chains
--    grouped by (tenant, reference, requestType) so different lifecycle stages
--    of the same case are never counted as retries of each other.
-- ----------------------------------------------------------------------------
WITH chains AS (
    SELECT
        tenant_id, reference_id, request_type, status,
        ROW_NUMBER() OVER (PARTITION BY tenant_id, reference_id, request_type
                            ORDER BY requested_time) AS attempt_seq,
        COUNT(*) OVER (PARTITION BY tenant_id, reference_id, request_type) AS attempt_count
    FROM external_api_trace_log
    WHERE functionality = '<FUNCTIONALITY>' AND action_type = 'SUBMIT'
      AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>'
),
retry_chains AS (
    SELECT * FROM chains WHERE attempt_count > 1
),
chain_outcomes AS (
    SELECT tenant_id, reference_id, request_type,
           MAX(attempt_count) AS attempt_count,
           MIN(CASE WHEN status = 'SUCCESS' THEN attempt_seq END) AS first_success_seq
    FROM retry_chains
    GROUP BY tenant_id, reference_id, request_type
)
SELECT
    COUNT(*)                                                       AS retried_transactions,
    SUM(attempt_count - 1)                                         AS total_retry_attempts,
    SUM(CASE WHEN first_success_seq IS NOT NULL THEN 1 ELSE 0 END) AS successful_after_retry,
    SUM(CASE WHEN first_success_seq IS NULL THEN 1 ELSE 0 END)     AS failed_after_retry,
    SUM(CASE WHEN first_success_seq - 1 <= 1 THEN 1 ELSE 0 END)    AS single_retry_success,
    SUM(CASE WHEN first_success_seq - 1 > 1 THEN 1 ELSE 0 END)     AS multiple_retry_success
FROM chain_outcomes;

-- 3b. Which specific cases are the retry chains above, with per-chain detail
--     (useful for spot-checking a specific case, or finding which ones never
--     succeeded / burned through many attempts).
WITH chains AS (
    SELECT
        tenant_id, reference_id, request_type, status, requested_time,
        ROW_NUMBER() OVER (PARTITION BY tenant_id, reference_id, request_type
                            ORDER BY requested_time) AS attempt_seq,
        COUNT(*) OVER (PARTITION BY tenant_id, reference_id, request_type) AS attempt_count
    FROM external_api_trace_log
    WHERE functionality = '<FUNCTIONALITY>' AND action_type = 'SUBMIT'
      AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>'
)
SELECT tenant_id, reference_id, request_type, attempt_seq, attempt_count, status, requested_time
FROM chains
WHERE attempt_count > 1
ORDER BY tenant_id, reference_id, request_type, attempt_seq;


-- ----------------------------------------------------------------------------
-- 4. MEMBER_VALIDATION count for the window - should NOT appear in any of the
--    sections above; this just confirms how many rows the exclusion is hiding.
-- ----------------------------------------------------------------------------
SELECT COUNT(*) AS member_validation_count
FROM external_api_trace_log
WHERE functionality = '<FUNCTIONALITY>' AND action_type = 'MEMBER_VALIDATION'
  AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>';


-- ----------------------------------------------------------------------------
-- 5. Raw failure reasons - approximation only. The API additionally runs these
--    through FailureCategoryClassifier's substring rules in Java (e.g. collapsing
--    every "POLICY_NOT_FOUND" variant into "Policy Not Found"), so exact text
--    here won't always match topFailureReasons' category labels 1:1.
-- ----------------------------------------------------------------------------
SELECT COALESCE(NULLIF(error_response, ''), NULLIF(exception_message, ''), NULLIF(remarks, '')) AS failure_reason,
       COUNT(*) AS occurrences
FROM external_api_trace_log
WHERE functionality = '<FUNCTIONALITY>' AND action_type <> 'MEMBER_VALIDATION' AND status = 'FAILURE'
  AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>'
GROUP BY 1
ORDER BY 2 DESC
LIMIT 10;


-- ----------------------------------------------------------------------------
-- 6. Preauth Summary Report totalCount - submission-only distinct case count.
--    This is deliberately NOT the same as section 2's OUTBOUND total (which also
--    includes DOCUMENT_UPLOAD attempts) - see the totalCount vs Outbound-ring
--    discussion: this is "how many cases were submitted", not "how many outbound
--    API calls happened".
-- ----------------------------------------------------------------------------
SELECT COUNT(DISTINCT tenant_id || ':' || reference_id) AS submitted_case_count
FROM external_api_trace_log
WHERE functionality = 'PREAUTH' AND action_type = 'SUBMIT'
  AND requested_time >= '<FROM_DATE>' AND requested_time < '<TO_DATE>';

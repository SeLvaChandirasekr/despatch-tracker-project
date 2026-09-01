package com.abi.util;

/*-----------------------------------------------------------------------
 * Normalizes the raw failure_reason text (see vw_dashboard_transaction_events - itself
 * COALESCE(error_response, exception_message, remarks)) into a small set of stable,
 * human-readable categories. Raw values are often templated JSON/exception text with
 * embedded requestIds, timestamps, or file paths that differ on every occurrence, which
 * would otherwise make every failure look like its own unique "reason" and defeat any
 * grouping (topFailureReasons, the Failure Category filter). Match rules are substring
 * checks against known noisy patterns; anything else passes through as-is (trimmed,
 * capped) rather than being swept into a vague "Other" bucket - most raw messages here
 * are already short and specific enough to stand on their own.
 *---------------------------------------------------------------------*/
public final class FailureCategoryClassifier {

    private static final int MAX_CATEGORY_LENGTH = 80;

    // Order matters: first match wins.
    private static final String[][] RULES = {
	    { "POLICY_NOT_FOUND", "Policy Not Found" },
	    { "HEADER_EXTRACTION_FAILED", "Header Extraction Failed" },
	    { "Decryption error", "Decryption Error" },
	    { "doctorSigned", "Document Not Signed" },
	    { "file does not exist", "File Not Found" },
	    { "For input string", "Invalid Data Format" },
	    { "No matching constant for", "Invalid Response Status Code" },
	    { "Unable to generate token", "Token Generation Failed" },
	    { "EXCEPTION OCCURRED WHILE CONSUMING WEBSERVICE", "External Webservice Error" },
	    { "timed out", "Payer Portal Timeout" },
	    { "Connection refused", "Payer Portal Unreachable" },
    };

    private FailureCategoryClassifier() {
    }

    public static String classify(String rawFailureReason) {
	if (rawFailureReason == null || rawFailureReason.isBlank()) {
	    return null;
	}
	for (String[] rule : RULES) {
	    if (rawFailureReason.contains(rule[0])) {
		return rule[1];
	    }
	}
	String trimmed = rawFailureReason.trim();
	return trimmed.length() > MAX_CATEGORY_LENGTH ? trimmed.substring(0, MAX_CATEGORY_LENGTH - 3) + "..."
		: trimmed;
    }

}

package com.abi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.abi.dto.PatientIdentityVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/*-----------------------------------------------------------------------
 * Pulls just the patient/case identity fields (name, MRN, admission/discharge dates) out of
 * external_api_trace_log.source_payload for a PREAUTH SUBMIT case - the only place those fields
 * exist, since case_current_status/vw_case_api_dashboard don't carry them as columns.
 *
 * source_payload is the original preAuthFormVO submission the hospital sent, and also carries
 * portal login credentials (username/password/otp/captcha/cookie) alongside it - this parser
 * deliberately reads only the four fixed paths below and never dumps/forwards the raw payload,
 * so those credentials never reach a response.
 *---------------------------------------------------------------------*/
public final class SourcePayloadParser {

    private static final Logger LOGGER = LoggerFactory.getLogger(SourcePayloadParser.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private SourcePayloadParser() {
    }

    public static PatientIdentityVO parsePreAuthIdentity(String rawSourcePayload) {
	PatientIdentityVO identity = new PatientIdentityVO();
	if (rawSourcePayload == null || rawSourcePayload.isBlank()) {
	    return identity;
	}

	try {
	    JsonNode form = MAPPER.readTree(rawSourcePayload).path("preAuthFormVO");
	    JsonNode person = form.path("personVO");
	    JsonNode preAuthorisation = form.path("preAuthorisationVO");
	    JsonNode policy = preAuthorisation.path("insurancePolicyVO");
	    JsonNode insuredPerson = policy.path("insuredPersonVO");
	    JsonNode billing = form.path("billingInfoVO").path("billingInfoDetailsVO");

	    identity.setPatientName(joinName(textOrNull(person.path("firstName")), textOrNull(person.path("lastName"))));
	    identity.setMrn(textOrNull(insuredPerson.path("patientMrNumber")));
	    identity.setDateOfAdmission(textOrNull(billing.path("dateofAdmission")));
	    identity.setDateOfDischarge(textOrNull(billing.path("dateofDischarge")));
	    // preAuthType/caseID live at the preAuthFormVO level - the same-named fields nested
	    // under preAuthorisationVO have been confirmed always null in real data, not a typo.
	    identity.setPreAuthType(textOrNull(form.path("preAuthType")));
	    identity.setCaseId(textOrNull(form.path("caseID")));
	    identity.setPayorTpaMemberId(textOrNull(policy.path("tpaMembershipId")));
	} catch (Exception ex) {
	    LOGGER.warn("Unable to parse source_payload for patient identity: {}", ex.getMessage());
	}

	return identity;
    }

    private static String textOrNull(JsonNode node) {
	return node.isMissingNode() || node.isNull() ? null : node.asText(null);
    }

    private static String joinName(String firstName, String lastName) {
	String first = firstName == null ? "" : firstName.trim();
	String last = lastName == null ? "" : lastName.trim();
	String joined = (first + " " + last).trim().replaceAll("\\s+", " ");
	return joined.isEmpty() ? null : joined;
    }

}

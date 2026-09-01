package com.abi.dto;

import java.io.Serializable;
import java.util.Date;

import lombok.Data;

/*-----------------------------------------------------------------------
 * One row of the dashboard's PreAuth Transactions table - one row per case (reference_id),
 * sourced entirely from external_api_trace_log: case-level fields from the case's SUBMIT
 * history, patient/case identity fields parsed from the case's INITIAL submission
 * source_payload. See DashboardServiceImpl.getPreAuthTransactions()/buildCaseTransaction().
 *---------------------------------------------------------------------*/
@Data
public class PreAuthTransactionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer tenantId;

    private String tenantName;

    private String hospitalCode;

    // Internal case correlation id (reference_id)
    private String referenceId;

    // The hospital's own case tracking id (source_payload's caseID) - not the same as
    // referenceId or payorReferenceNumber.
    private String caseId;

    // The payor/TPA's own reference for this case (payor_case_ref_id)
    private String payorReferenceNumber;

    private String alNumber;

    private String patientName;

    private String mrn;

    private String dateOfAdmission;

    private String dateOfDischarge;

    private Integer payerTpaId;

    private String payerTpaName;

    private String payorTpaMemberId;

    // In-Patient / Day-Care / MV(Other)
    private String preAuthType;

    // INITIAL / ENHANCEMENT / DISCHARGE / REVISION
    private String requestType;

    // SUCCESS / FAILURE - outcome of the case's most recent API call
    private String status;

    // Payor's resolved business status, e.g. Approved/Query/Rejected
    private String currentStatus;

    // When the case's INITIAL submission happened
    private Date initiatedDate;

    // Time since initiatedDate, formatted "XD:YH:ZM"
    private String elapsedTime;

    // Most recent event's timestamp
    private Date createdDate;

}

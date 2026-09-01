package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

/*-----------------------------------------------------------------------
 * Patient/case identity fields extracted from a PREAUTH case's source_payload - see
 * SourcePayloadParser. Deliberately only these fields; never a raw payload passthrough.
 *---------------------------------------------------------------------*/
@Data
public class PatientIdentityVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String patientName;

    private String mrn;

    // Raw dd/yyyy-MM-dd style string as submitted - not normalized, since source formatting
    // isn't guaranteed consistent across tenants/integrations.
    private String dateOfAdmission;

    private String dateOfDischarge;

    // In-Patient / Day-Care / MV(Other) - distinct from the DB request_type column
    // (INITIAL/ENHANCEMENT/DISCHARGE/REVISION), which is a separate concept.
    private String preAuthType;

    // The hospital's own case tracking id, separate from our internal reference_id and from
    // the payor's own reference number (payor_case_ref_id).
    private String caseId;

    private String payorTpaMemberId;

}

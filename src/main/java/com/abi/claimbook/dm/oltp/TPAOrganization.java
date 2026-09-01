package com.abi.claimbook.dm.oltp;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "oltp_tpa_organization_tb")
public class TPAOrganization implements java.io.Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 6589811156354033405L;

	@Id
	@Column(name = "oltp_tpa_organization_id", unique = true, nullable = false)
	private Integer tpaOrganizationId;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "mtdm_master_tpa_organization_id")
	private Integer masterTPAOrganizationId;

	@Column(name = "preauth_email_enabled")
	private boolean emailAllowed;

	@Column(name = "preauth_automation_enabled")
	private boolean preauthAutomationEnabled;

	@Column(name = "preauth_portal_enabled")
	private boolean preauthPortalEnabled;

	@Column(name = "preauth_url")
	private String preauthUrl;

	@Column(name = "preauth_username")
	private String preauthUserName;

	@Column(name = "preauth_password")
	private String preauthPassword;

	@Column(name = "claims_portal_enabled")
	private boolean claimsPortalEnabled;

	@Column(name = "claims_url")
	private String claimsUrl;

	@Column(name = "claims_username")
	private String claimsUserName;

	@Column(name = "claims_password")
	private String claimsPassword;

	@Column(name = "ebill_enabled")
	private boolean ebillEnabled;

	@Column(name = "hospital_code")
	private String hospitalCode;

	@Column(name = "query_reply_allowed")
	private boolean queryReplyAllowed;

	@Column(name = "claim_query_reply_allowed")
	private boolean claimQueryReplyAllowed;

	@Column(name = "address_line1")
	private String addressLine1;

	@Column(name = "address_line2")
	private String addressLine2;

	@Column(name = "city")
	private String city;

	@Column(name = "state")
	private String state;

	@Column(name = "contact_no")
	private String contactNumber;

	@Column(name = "contact_person")
	private String contactPerson;

	@Column(name = "portal_query_enabled")
	private boolean portalQueryEnabled;

	@Column(name = "rpa_enabled")
	private boolean rpaEnabled;

	@Column(name = "rpa_polling_enabled")
	private boolean rpaPollingEnabled;

	@Column(name = "claim_portal_query_enabled")
	private boolean claimPortalQueryEnabled;

	@Column(name = "claim_automation_allowed")
	private boolean claimAutomationAllowed;

	@Column(name = "claim_email_allowed")
	private boolean claimEmailAllowed;

	@Column(name = "claim_submission_allowed")
	private boolean claimSubmissionAllowed;

	/* **Added by Moumita** */
	@Column(name = "auth_token")
	private String authToken;

	/* **Added by Moumita** */
	@Column(name = "auth_token_polling_enabled")
	private boolean authTokenPollingEnabled;

	@Column(name = "is_api_enabled")
	private boolean isApiEnabled;

	/*
	 * @Column(name = "tpa_hospital_id") private String tpaHospitalId;
	 */

	@Column(name = "rpa_insurance_enabled")
	private boolean rpaInsuranceEnabled;

	@Column(name = "master_data_polling_enabled")
	private boolean masterDataPollingEnabled;

	@Column(name = "gst")
	private String gst;

	@Column(name = "tan")
	private String tan;

	@Column(name = "pan")
	private String pan;

	@Column(name = "is_courier_mandatory_enabled")
	private boolean courierMandatoryEnabled;

	@Column(name = "rpa_relationship_enabled")
	private boolean rpaRelationshipEnabled;

	@Column(name = "deleted")
	private boolean deleted;

	@Column(name = "enhance_during_query_reply")
	private boolean enhanceDuringQueryReply;

	@Column(name = "discharge_during_query_reply")
	private boolean dischargeDuringQueryReply;

	@Column(name = "enhancement_allowed_bofore_intial_approval")
	private boolean enhancementAllowedBoforeIntialApproval;

	@Column(name = "discharge_allowed_bofore_intial_approval")
	private boolean dischargeAllowedBoforeIntialApproval;

	@Column(name = "his_tpa_name")
	private String hisTpaName;

	@Column(name = "tpa_display_name")
	private String tpaDisplayName;

	@Column(name = "overall_file_size")
	private Integer overallFileSize;

	@Column(name = "individual_file_size")
	private Integer individualFileSize;

	@Column(name = "claim_overall_file_size")
	private Integer claimOverallFileSize;

	@Column(name = "claim_individual_file_size")
	private Integer claimIndividualFileSize;

	@Column(name = "portal_tpa_organization_id")
	private Integer portalTPAOrganizationId;

	@Column(name = "auto_creation_enabled")
	private boolean autoCreationEnabled;

	@Column(name = "ip_auto_case_creation_enabled")
	private boolean ipAutoCaseCreationEnabled;

	@Column(name = "op_auto_case_creation_enabled")
	private boolean opAutoCaseCreationEnabled;

	@Column(name = "tpa_forms")
	private String tpaForms;

	@Column(name = "validation_search_tpa_name")
	private String validationSearchTpaName;

	@Column(name = "validation_search_url")
	private String validationSearchUrl;

	@Column(name = "validation_search_username")
	private String validationSearchUserName;

	@Column(name = "validation_search_password")
	private String validationSearchPassword;

	@Column(name = "get_portal_update_enabled")
	private boolean portalUpdateEnabled;

	@Column(name = "get_portal_update_frequency")
	private Integer portalUpdateFrequency;

	@Column(name = "is_synchronous_submission")
	private boolean synchronousSubmission;

	@Column(name = "insurance_company_name_mandatory")
	private boolean insuranceCompanyNameMandatory;

	@Column(name = "corporate_name_mandatory")
	private boolean corporateNameMandatory;

	@Column(name = "tpa_insurance_company_same")
	private boolean tpaInsuranceCompanySame;

	@Column(name = "equivalent_insurance_company_id")
	private Integer equivalentInsuranceCompanyId;

	@Column(name = "courier_branch_required")
	private boolean courierBranchRequired;

	@Column(name = "retry_allowed")
	private boolean retryAllowed;

	@Column(name = "captcha_required")
	private boolean captchaRequired;

	@Column(name = "otp_required")
	private boolean otpRequired;

	@Column(name = "claim_captcha_required")
	private boolean claimCaptchaRequired;

	@Column(name = "claim_otp_required")
	private boolean claimOTPRequired;

	@Column(name = "polling_status_processing_with_document")
	private boolean pollingStatusProcessingWithDocument;

	@Column(name = "master_data_polling_after_validation")
	private boolean masterDataPollingAfterValidation;

	@Column(name = "preauth_polling_after_validation")
	private boolean preauthPollingAfterValidation;

	@Column(name = "claim_polling_after_validation")
	private boolean claimPollingAfterValidation;

	@Column(name = "last_master_data_polling_date")
	private Date lastMasterDataPollingDate;

	@Column(name = "claim_get_portal_update_enabled")
	private boolean claimPortalUpdateEnabled;

	@Column(name = "claim_get_portal_update_frequency")
	private Integer claimPortalUpdateFrequency;

	@Column(name = "is_claim_synchronous_submission")
	private boolean claimSynchronousSubmission;

	@Column(name = "claims_case_retry_allowed")
	private boolean claimsCaseRetryAllowed;

	@Column(name = "claims_query_retry_allowed")
	private boolean claimsQueryRetryAllowed;

	@Column(name = "claim_retry_submission_with_otp")
	private boolean claimRetrySubmissionWithOtp;

	@Column(name = "preauth_portal_overall_file_size")
	private Integer preauthPortalOverallFileSize;

	@Column(name = "preauth_portal_individual_file_size")
	private Integer preauthPortalIndividualFileSize;

	@Column(name = "claim_portal_overall_file_size")
	private Integer claimPortalOverallFileSize;

	@Column(name = "claim_portal_individual_file_size")
	private Integer claimPortalIndividualFileSize;

	@Column(name = "preauth_file_restriction_enabled")
	private boolean preauthFileRestrictionEnabled;

	@Column(name = "claim_file_restriction_enabled")
	private boolean claimFileRestrictionEnabled;

	@Column(name = "claim_rpa_polling_enabled")
	private boolean claimRpaPollingEnabled;

	@Column(name = "initial_submission_moved_to_manualupload_enabled")
	private boolean initialSubmissionMovedToManualupload;

	@Column(name = "bulk_settlement_fields")
	private String bulkSettlementFields;

	@Column(name = "bulk_settlement_date_format")
	private String bulkSettlementDateFormat;

	@Column(name = "bulk_settlement_sheet_name")
	private String bulkSettlementSheetName;

	@Column(name = "customer_code")
	private String customerCode;

	@Column(name = "customer_site_code")
	private String customerSiteCode;

	@Column(name = "member_id_validation_enabled")
	private boolean memberIdValidationEnabled;

	@Column(name = "new_portal_enabled")
	private boolean newPortalEnabled;

	@Column(name = "get_diagnosis_from_master_data")
	private boolean getDiagnosisFromMasterData;

	@Column(name = "cutoff_date")
	private Date cutOffDate;

	@Column(name = "hcx_integration_enabled")
	private Boolean hcxIntegrationEnabled;

	@Column(name = "hcx_payer_id")
	private String hcxPayerID;

	@Column(name = "hcx_payer_name")
	private String hcxPayerName;

	@Column(name = "payer_identifier_url")
	private String payerIdentifierUrl;

	@Column(name = "key_value_identifier")
	private Boolean keyValueIdentifier;

	@Column(name = "is_document_encoded")
	private Boolean isDocumentEncoded;

	@Column(name = "preauth_portal_status_keywords")
	private String preauthPortalStatusKeywords;

	@Column(name = "claim_portal_status_keywords")
	private String claimPortalStatusKeywords;

	@Column(name = "facility_id")
	private String facilityId;

	@Column(name = "preauth_disallowance_enabled")
	private boolean preauthDisallowanceEnabled;

	@Column(name = "is_document_pocessing_enabled")
	private Boolean isDocumentProcessingEnabled;

	@Column(name = "is_preauth_document_pocessing_enabled")
	private Boolean isPreauthDocumentProcessingEnabled;

	@Column(name = "resubmission_allowed")
	private boolean resubmissionAllowed;

	@Column(name = "generate_al_number")
	private Boolean generateAlNumber;

	@Column(name = "claim_api_cases_polling_allowed")
	private boolean claimApiCasesPollingAllowed;

	@Column(name = "hcx_subm_via_membvlidation")
	private boolean hcxSubmissionViaMembValidation;

	@Column(name = "get_procedure_from_master_data")
	private boolean getProcedureFromMasterData;

	@Column(name = "preauth_api_cases_polling_allowed")
	private boolean preauthApiCasesPollingAllowed;

	@Column(name = "old_preauth_url")
	private String oldPreauthUrl;

	@Column(name = "old_preauth_username")
	private String oldPreauthUserName;

	@Column(name = "old_preauth_password")
	private String oldPreauthPassword;

	@Column(name = "old_claims_url")
	private String oldClaimsUrl;

	@Column(name = "old_claims_username")
	private String oldClaimsUserName;

	@Column(name = "old_claims_password")
	private String oldClaimsPassword;

	@Column(name = "api_cancellation_allowed")
	private boolean apiCancellationAllowed;

	@Column(name = "load_procedure_category_from_master_data")
	private boolean procedureCategoryMasterData;

	@Column(name = "sdms_docket_enabled")
	private boolean sdmsDocketEnabled;

	@Column(name = "api_reopen_allowed")
	private boolean apiReopenAllowed;

	@Column(name = "offline_cases_enabled")
	private boolean offlineCasesEnabled;

	@Column(name = "mvf_intial_submission_via_email_allowed")
	private boolean mvfSubmissionEmailAllowed;

	@Column(name = "move_to_manual_on_api_failure")
	private boolean moveToMuOnApiFailure;

	@Column(name = "portal_master_data_search_enabled")
	private boolean portalMasterDataSearchEnabled;

	@Column(name = "packages_enabled")
	private boolean packagesEnabled;

	@Column(name = "tpa_followup_via_email_required")
	private boolean tpaFollowupRequired;

	@Column(name = "discharge_approved_polling")
	private boolean dischargeApprovedPolling;

}
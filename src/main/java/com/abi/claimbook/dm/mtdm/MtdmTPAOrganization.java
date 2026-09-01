package com.abi.claimbook.dm.mtdm;

import java.io.Serializable;
import java.util.List;

import com.abi.entity.Tenant;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

/**
 * @author Narendhar
 * @date 26-09-2016
 * @description used for InsuranceOrganization
 * @version 1.0
 * 
 */
@Data
@Table(name = "mtdm_tpa_organization_tb")
public class MtdmTPAOrganization implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = -321831697130272706L;
	@Id
	@SequenceGenerator(name = "mtdm_tpa_organization_tb_seq", sequenceName = "mtdm_tpa_organization_tb_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mtdm_tpa_organization_tb_seq")
	@Column(name = "tpa_organization_id", unique = true, nullable = false)
	private Integer tpaorganizationId;

	@Column(name = "name")
	private String tpaName;

	@Column(name = "tpa_registration_no")
	private String tpaRgistrationNo;

	@Column(name = "category")
	private String payorCategory;

	@Column(name = "type")
	private String payorType;

	@Column(name = "address_line1")
	private String payorAddressLine1;

	@Column(name = "address_line2")
	private String payorAddressLine2;

	@Column(name = "business_email")
	private String payorBusinessEmail;

	@Column(name = "business_contact_number")
	private String payerbusinessConactNumber;

	@Column(name = "domain")
	private String domain;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "contact_person")
	private String contactPerson;

	@Transient
	private Tenant tenant;

	@Column(name = "al_number_suffix")
	private String alNumberSuffix;

	@Column(name = "tpa_organization_code")
	private String tpaOrganizationCode;

	@Column(name = "query_reply_during_discharge")
	private boolean queryReplyDuringDischarge;

	@Column(name = "alnumber_clnumber_matched")
	private boolean alNumberCLNumberMatched;

	@Column(name = "add_manually")
	private boolean addManually;

	@Column(name = "preauth_submit_more_document_allowed")
	private boolean preauthSubmitMoreDocumentAllowed;

	@Column(name = "claim_submit_more_document_allowed")
	private boolean claimSubmitMoreDocumentAllowed;

	@Column(name = "is_reopen_allowed")
	private boolean reopenAllowed;

	@Column(name = "gst")
	private String gst;

	@Column(name = "tan")
	private String tan;

	@Column(name = "pan")
	private String pan;

	@Column(name = "is_cancellation_allowed")
	private boolean cancellationlAllowed;

	@Column(name = "is_cancellation_email_allowed")
	private boolean cancellationEmailAllowed;

	@Column(name = "is_reopen_email_allowed")
	private boolean reopenEmailAllowed;

	@Column(name = "is_status_processing_allowed")
	private boolean statusProcessingAllowed;

	@Column(name = "is_disallowance_allowed")
	private boolean disallowanceAllowed;

	@Column(name = "is_default")
	private boolean isDefault;

	@Column(name = "toll_free_mobile_number")
	private String tollFreeMobileNumber;

	@Column(name = "toll_free_fax")
	private String tollFreeFax;

	@Column(name = "govt_scheme_differentiation_enabled")
	private boolean govtSchemeDifferentiationEnabled;

	@Column(name = "preauth_url")
	private String preauthUrl;

	@Column(name = "preauth_username")
	private String preauthUsername;

	@Column(name = "preauth_password")
	private String preauthPassword;

	@Column(name = "claims_url")
	private String claimsUrl;

	@Column(name = "claims_username")
	private String claimsUsername;

	@Column(name = "claims_password")
	private String claimsPassword;

	@Column(name = "portal_tpa_name")
	private String portalTpaname;

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

	@Column(name = "dnf_prefix")
	private String dnfPrefix;

	@Column(name = "preauth_disallowance_parsing_enabled")
	private boolean preauthDisallowanceParsingAllowed;

	@Column(name = "max_file_size")
	private Integer maxFileSize;

	@Column(name = "individual_file_size")
	private Integer individualFileSize;

	@Column(name = "max_files_allowed ")
	private Integer maxFilesAllowed;

	@Column(name = "minimum_file_allowed")
	private Integer individualMinimumFileAllowed;

	@Column(name = "query_max_files_allowed ")
	private Integer queryMaxFilesAllowed;

	@Column(name = "query_minimum_file_allowed")
	private Integer queryIndividualMinimumFileAllowed;

	@Column(name = "query_overall_file_size")
	private Integer queryOverallFileSize;

	@Column(name = "query_individual_file_size")
	private Integer queryIndividualFileSize;

	@Column(name = "hcx_ins_cov_enabled")
	private boolean hcxInsPlanCoverageEnabled;;

	@Column(name = "is_hcx_discharge_as_claim_submission")
	private boolean isHCXDischargeAsClaimSubmission;

	@Column(name = "disposition_status_enabled")
	private boolean dispositionStatusEnabled;

	@Column(name = "disposition_status_keywords")
	private String dispositionStatusKeywords;

	@Column(name = "is_settlement_with_out_doc")
	private boolean isSettlementWithOutDoc;

	@Column(name = "sms_tpa_code")
	private String smsTpaCode;

	@Column(name = "is_hcx_member_validation_enabled")
	private boolean isHCXMemberValidationEnabled;

	@Column(name = "tpa_member_id_regex_pattern")
	private String tpaMemberIdRegexPattern;

	@Column(name = "tpa_member_id_length")
	private String tpaMemberIdLength;

	@Column(name = "is_presettlement_with_out_doc")
	private boolean isPreSettlementWithOutDoc;

	@Column(name = "case_sheet_master_data")
	private String caseSheetMasterData;

	@Column(name = "member_search_master_data")
	private String memberSearchMasterData;

	@Column(name = "is_cancel_with_out_doc")
	private boolean isCancelWithOutDoc;

	@Column(name = "tpa_followup_wait_duration")
	private Integer tpaFollowupWaitDuration;

}
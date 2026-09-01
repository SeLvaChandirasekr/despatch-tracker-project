package com.abi.claimbook.dm.mtdm;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "mtdm_master_provider_organization_tb")
public class MtdmMasterProviderOrganization implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8107665963785863313L;

	@Id
	@SequenceGenerator(name = "mtdm_master_provider_organization_seq", sequenceName = "mtdm_master_provider_organization_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mtdm_master_provider_organization_seq")
	@Column(name = "mtdm_master_provider_organization_id ", unique = true, nullable = false)
	private Integer mtdmmasterproviderorganizationId;

	@Column(name = "provider_name")
	private String providerName;

	@Column(name = "provider_registration")
	private String providerRegistration;

	@Column(name = "provider_address_line1")
	private String provideraddressLine1;

	@Column(name = "provider_address_line2")
	private String provideraddressLine2;

	@Column(name = "provider_business_contact_number")
	private String providerbusinesscontactNumber;

	@Column(name = "provider_business_email")
	private String providerbusinessEmail;

	@Column(name = "type")
	private String type;

	@Column(name = "category")
	private String category;

	@Column(name = "is_active")
	private boolean active;

}

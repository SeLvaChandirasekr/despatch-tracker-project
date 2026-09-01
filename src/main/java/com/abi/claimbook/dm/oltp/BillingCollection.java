package com.abi.claimbook.dm.oltp;

import java.io.Serializable;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Table(name = "oltp_billing_collection")
public class BillingCollection implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7070934711823583408L;

	@Id
	@SequenceGenerator(name = "oltp_billing_collection_billing_collection_id_seq", sequenceName = "oltp_billing_collection_billing_collection_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oltp_billing_collection_billing_collection_id_seq")
	@Column(name = "billing_collection_id", unique = true, nullable = false)
	private Integer billingcollectionId;

	@Column(name = "billing_collection_category")
	private String billingcollectionCategory;

	@Column(name = "billing_collection_value")
	private String billingcollectionValue;

	@Column(name = "is_active")
	private boolean isActive;

	@Column(name = "created_date")
	private Date createdDate;

}

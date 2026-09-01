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
@Table(name = "oltp_master_treatment")
public class MasterTreatment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8906181841453577355L;

	@Id
	@SequenceGenerator(name = "oltp_master_treatment_oltp_master_treatment_master_id_seq", sequenceName = "oltp_master_treatment_oltp_master_treatment_master_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oltp_master_treatment_oltp_master_treatment_master_id_seq")
	@Column(name = "oltp_master_treatment_master_id", unique = true, nullable = false)
	private Integer mastertreatmentId;

	@Column(name = "treatment_name")
	private String treatmentName;

	@Column(name = "description")
	private String description;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "created_date")
	private Date createdDate;

}

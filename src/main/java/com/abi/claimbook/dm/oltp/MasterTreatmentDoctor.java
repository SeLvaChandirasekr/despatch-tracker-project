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
@Table(name = "oltp_master_treatment_doctor")
public class MasterTreatmentDoctor implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8078996172599724377L;

	@Id
	@SequenceGenerator(name = "oltp_master_treatment_doctor_oltp_master_treatment_doctor_i_seq", sequenceName = "oltp_master_treatment_doctor_oltp_master_treatment_doctor_i_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oltp_master_treatment_doctor_oltp_master_treatment_doctor_i_seq")
	@Column(name = "oltp_master_treatment_doctor_id", unique = true, nullable = false)
	private Integer mastertreatmentdoctorId;

	@Column(name = "treatment_doctor_name")
	private String treatmentdoctorName;

	@Column(name = "speciality")
	private String speciality;

	@Column(name = "is_active")
	private boolean active;

	@Column(name = "created_date")
	private Date createdDate;

	@Column(name = "department_id")
	private Integer departmentId;

	@Column(name = "contact_number")
	private String contactNumber;

	@Column(name = "qualification")
	private String qualification;

	@Column(name = "email_address")
	private String emailAddress;

}

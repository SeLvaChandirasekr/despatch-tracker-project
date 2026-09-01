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
@Table(name = "oltp_master_department")
public class MasterDepartment implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8433592496620341390L;

	@Id
	@SequenceGenerator(name = "oltp_master_department_oltp_master_department_id_seq", sequenceName = "oltp_master_department_oltp_master_department_id_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oltp_master_department_oltp_master_department_id_seq")
	@Column(name = "oltp_master_department_id", unique = true, nullable = false)
	private Integer masterdepartmentId;

	/*@Column(name = "department_name")
	private String departmentName;

	@Column(name = "description")
	private String description;
*/
	@Column(name = "is_active")
	private boolean active;

	@Column(name = "created_date")
	private Date createdDate;
	
	
	@Column(name = "mtdm_department_id")
	private Integer departmentId;
	
	@Column(name = "department")
	private String department;


	
}

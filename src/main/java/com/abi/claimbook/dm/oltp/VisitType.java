/**
 * 
 */
package com.abi.claimbook.dm.oltp;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * @author Pandiyan
 *
 */
@Data
@Table(name = "oltp_master_preauth_visit_type_tb")
public class VisitType implements Serializable {

	private static final long serialVersionUID = 9085301760861757043L;

	@Id
	@SequenceGenerator(name = "master_preauth_visit_type_seq", sequenceName = "master_preauth_visit_type_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "master_preauth_visit_type_seq")
	@Column(name = "visit_type_id", unique = true, nullable = false)
	private Integer visitTypeId;

	@Column(name = "visit_type")
	private String visitType;

	@Column(name = "description")
	private String description;

}

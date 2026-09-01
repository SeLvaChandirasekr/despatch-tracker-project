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

@Data
@Table(name = "oltp_master_courier")
public class OltpMasterCourier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4097639475622490240L;

	@Id
	@SequenceGenerator(name = "oltp_master_courier_seq", sequenceName = "oltp_master_courier_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "oltp_master_courier_seq")
	@Column(name = "courier_id", unique = true, nullable = false)
	private Integer oltpmastercourierId;

	@Column(name = "mtdm_master_courier_id")
	private Integer mtmdmastercourierId;

	@Column(name = "active")
	private boolean active;

}

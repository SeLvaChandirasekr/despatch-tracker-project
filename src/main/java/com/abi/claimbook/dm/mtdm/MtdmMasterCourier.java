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
@Table(name = "mtdm_master_courier")
public class MtdmMasterCourier implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5596909635676109652L;

	@Id
	@SequenceGenerator(name = "mtdm_master_courier_seq", sequenceName = "mtdm_master_courier_seq", allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mtdm_master_courier_seq")
	@Column(name = "courier_id ", unique = true, nullable = false)
	private Integer courierId;

	@Column(name = "courier_vendor")
	private String courierVendor;
	
	
	@Column(name = "registration_number")
	private String registrationNumber;

	@Column(name = "description")
	private String description;

	@Column(name = "active")
	private boolean active;

	@Override
	public String toString() {
		return "MtdmMasterCourier [courierId=" + courierId + ", courierVendor=" + courierVendor
				+ ", registrationNumber=" + registrationNumber + ", description=" + description + ", active=" + active
				+ "]";
	}

	public Integer getCourierId() {
		return courierId;
	}

	public String getCourierVendor() {
		return courierVendor;
	}

	public void setCourierVendor(String courierVendor) {
		this.courierVendor = courierVendor;
	}

	public void setCourierId(Integer courierId) {
		this.courierId = courierId;
	}

	public String getRegistrationNumber() {
		return registrationNumber;
	}

	public void setRegistrationNumber(String registrationNumber) {
		this.registrationNumber = registrationNumber;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

}

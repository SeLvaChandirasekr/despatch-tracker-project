package com.abi.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MasterResponseDTO {

	private List<String> hospitals;
	private List<DepartmentDTO> departments;
	private List<String> tpas;
	private List<String> insurances;
	private List<String> admissionTypes;
	private List<String> billingTypes;
	private List<String> policyLayers;
	private List<String> paymentStatuses;
	private List<String> courierVendors;
}
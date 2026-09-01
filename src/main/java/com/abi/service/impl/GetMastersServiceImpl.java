package com.abi.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.abi.service.MasterService;

public class GetMastersServiceImpl implements MasterService {

	@Override
	public Map<String, Object> getMaster(String tenantId, String username) {
		Map<String, Object> resultMap = new HashMap<>();
		// populate hospitals, departments+doctors, TPAs, insurers, courier vendors
		// via repository; add fixed enums (admission types, billing types, etc.)
		return resultMap;
	}

}

package com.abi.service;

import java.util.Map;

public interface MasterService {

	Map<String, Object> getMaster(String tenantId, String username);

}

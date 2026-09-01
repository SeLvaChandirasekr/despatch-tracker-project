/*
 * package com.abi.controller;
 * 
 * import java.util.HashMap; import java.util.Map;
 * 
 * import org.springframework.beans.factory.annotation.Autowired; import
 * org.springframework.web.bind.annotation.PostMapping; import
 * org.springframework.web.bind.annotation.RequestBody; import
 * com.abi.service.MasterService; import com.abi.util.ClaimBookConstants;
 * 
 * public class DespatchController extends ClaimBookConstants {
 * 
 * @Autowired private MasterService masterService;
 * 
 * @PostMapping(path = REQUEST_MAPPING_GET_MASTERS, produces =
 * CONTENT_TYPE_APPLICATION_JSON) public Map<String, Object>
 * getMaster(@RequestBody HashMap<String, Object> requestMap) throws
 * RuntimeException {
 * 
 * Map<String, Object> resultMap = new HashMap<>();
 * 
 * String tenantId = (String) requestMap.get("tenantId"); String remarkType =
 * (String) requestMap.get("remarkType"); String username = (String)
 * requestMap.get("username");
 * 
 * System.out.println("tenantId : " + tenantId);
 * 
 * System.out.println("remarkType : " + remarkType);
 * 
 * if (StringUtils.isNotNullAndNotEmpty(tenantId)) {
 * multitenantUtility.setCurrentTenant(tenantId); }
 * 
 * resultMap = masterService.getMaster(tenantId, username); return resultMap;
 * 
 * } }
 */
package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TenantVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer tenantId;

    private String tenantName;

}

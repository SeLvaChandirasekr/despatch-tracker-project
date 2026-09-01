package com.abi.dto;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Data;

/*-----------------------------------------------------------------------
 * Input to the dashboard summary/history endpoints. functionality/actionType are the
 * same literals ConvertBeanToVO.logExternalApiTrace() call sites already write, e.g.
 * functionality=PREAUTH|CLAIMS, actionType=SUBMIT|QUERY_REPLY|PORTAL_STATUS.
 *---------------------------------------------------------------------*/
@Data
public class DashboardSummaryRequestVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer tenantId;

    private String functionality;

    private String actionType;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date fromDate;

    @JsonDeserialize(using = CustomDateDeserializer.class)
    @JsonSerialize(using = CustomDateSerializer.class)
    private Date toDate;

}

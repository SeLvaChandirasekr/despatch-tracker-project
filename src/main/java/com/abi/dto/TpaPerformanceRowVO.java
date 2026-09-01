package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

/*-----------------------------------------------------------------------
 * One row of the TPA Performance Report - success rate and average response time for one
 * payor/TPA over the report's filters/date range. See ReportServiceImpl.buildTpaPerformanceReport().
 *---------------------------------------------------------------------*/
@Data
public class TpaPerformanceRowVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer tpaId;

    private String tpaName;

    private int total;

    private long success;

    private long failure;

    private double successRatePct;

    // Average elapsedTimeMs across successful rows - null if none had a recorded value.
    private Long avgResponseTimeMs;

}

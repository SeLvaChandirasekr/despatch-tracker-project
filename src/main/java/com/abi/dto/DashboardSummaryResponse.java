package com.abi.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class DashboardSummaryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private DashboardKpiDTO kpis = new DashboardKpiDTO();

    private ApiHealthDTO apiHealth = new ApiHealthDTO();

    private ModuleSummaryDTO preauth = new ModuleSummaryDTO();

    private ModuleSummaryDTO claims = new ModuleSummaryDTO();

    private List<VolumeTrendPointDTO> volumeTrend = new ArrayList<>();

    private List<RecentAlertDTO> recentAlerts = new ArrayList<>();

    private List<RecentTransactionDTO> recentTransactions = new ArrayList<>();

}

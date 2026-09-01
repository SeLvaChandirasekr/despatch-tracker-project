package com.abi.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/*-----------------------------------------------------------------------
 * Response for the Preauth Summary Report - the same row-level case data as
 * /dashboard/preauth/transactions, plus the total row count. Unlike that endpoint this always
 * returns every matching row (no pagination), since the frontend needs the complete set to
 * build a CSV export.
 *---------------------------------------------------------------------*/
@Data
public class PreauthSummaryReportVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalCount;

    private List<PreAuthTransactionVO> transactions = new ArrayList<>();

}

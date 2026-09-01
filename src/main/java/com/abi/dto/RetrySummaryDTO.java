package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RetrySummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int retriedTransactions;

    private int totalRetryAttempts;

    private int successfulAfterRetry;

    private int failedAfterRetry;

    // successfulAfterRetry split by how many failed attempts preceded the eventual success -
    // singleRetrySuccess + multipleRetrySuccess == successfulAfterRetry.
    private int singleRetrySuccess;

    private int multipleRetrySuccess;

}

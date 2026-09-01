package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

// Percentage change vs. the immediately preceding period of equal length.
@Data
public class ComparisonDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private double totalTransactions;

    private double success;

    private double failure;

    private double queryReply;

    private double retryTransactions;

    private double totalRetryAttempts;

}

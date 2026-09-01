package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class QueryReplySummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int total;

    private int success;

    private int failure;

    private int pending;

}

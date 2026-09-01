package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class FailureReasonDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String reason;

    private int count;

    private double percent;

}

package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class VolumeTrendPointDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // yyyy-MM-dd
    private String date;

    private int preauth;

    private int claims;

    private int inbound;

    private int outbound;

    private int queryReply;

}

package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RecentAlertDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // Deterministic per rule+scope, so repeated evaluations of an ongoing condition reuse
    // the same id instead of minting a new one each time.
    private String id;

    // critical / warning / info
    private String severity;

    private String title;

    private String detail;

    // human-readable relative time, e.g. "30s ago"
    private String time;

}

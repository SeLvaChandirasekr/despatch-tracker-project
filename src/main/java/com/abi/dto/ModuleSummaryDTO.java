package com.abi.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class ModuleSummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int total;

    private FlowSummaryDTO inbound = new FlowSummaryDTO();

    private FlowSummaryDTO outbound = new FlowSummaryDTO();

    private QueryReplySummaryDTO queryReply = new QueryReplySummaryDTO();

    private Map<String, Integer> requestTypeCounts = new LinkedHashMap<>();

    private RetrySummaryDTO retry = new RetrySummaryDTO();

    // Across every action type in the module (SUBMIT, QUERY_REPLY, PORTAL_STATUS, etc.)
    private List<FailureReasonDTO> topFailureReasons = new ArrayList<>();

    // Same top-5 ranking, scoped to a single flow - kept separate from topFailureReasons
    // since mixing submission/query-reply/inbound failures together buries which failure
    // reasons are actually blocking each specific flow.
    private List<FailureReasonDTO> topSubmissionFailureReasons = new ArrayList<>();

    private List<FailureReasonDTO> topQueryReplyFailureReasons = new ArrayList<>();

    private List<FailureReasonDTO> topInboundFailureReasons = new ArrayList<>();

}

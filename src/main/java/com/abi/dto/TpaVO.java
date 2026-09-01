package com.abi.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class TpaVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer tpaId;

    private String tpaName;

}

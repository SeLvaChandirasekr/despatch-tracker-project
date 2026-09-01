package com.abi.entity;

import java.io.Serializable;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/*-----------------------------------------------------------------------
 * Read-only mapping over the existing master_payor_tb table - resolves
 * ExternalApiTraceLog.tpaId to a display name for the dashboard.
 *---------------------------------------------------------------------*/
@Entity
@Table(name = "master_payor_tb")
@Immutable
@Data
public class MasterPayor implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "master_payor_id")
    private Integer masterPayorId;

    @Column(name = "tpa_name", columnDefinition = "character varying")
    private String tpaName;

}

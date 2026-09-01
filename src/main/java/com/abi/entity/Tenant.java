package com.abi.entity;

import java.io.Serializable;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

/*-----------------------------------------------------------------------
 * Read-only mapping over the existing tenant table - powers the dashboard's tenant list
 * (e.g. the tenant switcher).
 *---------------------------------------------------------------------*/
@Entity
@Table(name = "tenant")
@Immutable
@Data
public class Tenant implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "tenant_id")
    private Integer tenantId;

    @Column(name = "tenant_name", length = 100)
    private String tenantName;

    @Column(name = "is_tenant")
    private boolean tenant;

    @Column(name = "is_group_tenant")
    private boolean groupTenant;

    @Column(name = "group_tenant_id")
    private Integer groupTenantId;

    @Column(name = "group_name", columnDefinition = "character varying")
    private String groupName;

}

package com.abi.repository;

import java.util.List;

import org.springframework.data.repository.Repository;

import com.abi.entity.Tenant;


public interface TenantRepository extends Repository<Tenant, Integer> {

    List<Tenant> findAll();

}

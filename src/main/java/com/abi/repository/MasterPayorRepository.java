package com.abi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.Repository;

import com.abi.entity.MasterPayor;


public interface MasterPayorRepository extends Repository<MasterPayor, Integer> {

    List<MasterPayor> findAll();

    Optional<MasterPayor> findByTpaNameIgnoreCase(String tpaName);

}

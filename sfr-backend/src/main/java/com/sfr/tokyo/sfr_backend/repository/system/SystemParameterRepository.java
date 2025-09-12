package com.sfr.tokyo.sfr_backend.repository.system;

import com.sfr.tokyo.sfr_backend.entity.system.SystemParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemParameterRepository extends JpaRepository<SystemParameter, String> {
}

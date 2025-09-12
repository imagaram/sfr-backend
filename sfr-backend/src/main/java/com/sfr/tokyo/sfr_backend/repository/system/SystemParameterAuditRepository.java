package com.sfr.tokyo.sfr_backend.repository.system;

import com.sfr.tokyo.sfr_backend.entity.system.SystemParameterAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemParameterAuditRepository extends JpaRepository<SystemParameterAudit, Long> {
}

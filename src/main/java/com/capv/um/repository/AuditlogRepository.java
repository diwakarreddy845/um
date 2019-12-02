package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capv.um.model.AuditLog;

@Repository
public interface AuditlogRepository extends JpaRepository<AuditLog, Long> {
}

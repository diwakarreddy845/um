package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capv.um.model.TimeZone;

@Repository
public interface TimeZoneRepository extends JpaRepository<TimeZone, Long> {
}

package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.model.MeetingConfig;


public interface MeetingConfigRepository extends JpaRepository<MeetingConfig, Integer> {
}

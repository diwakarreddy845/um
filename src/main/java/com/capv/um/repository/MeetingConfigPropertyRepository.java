package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capv.um.model.MeetingConfigProperty;

@Repository
public interface MeetingConfigPropertyRepository extends JpaRepository<MeetingConfigProperty, Integer> {
}

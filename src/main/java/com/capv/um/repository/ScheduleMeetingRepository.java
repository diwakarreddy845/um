package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capv.um.model.ScheduleMeeting;

@Repository
public interface ScheduleMeetingRepository extends JpaRepository<ScheduleMeeting, Long> {
}

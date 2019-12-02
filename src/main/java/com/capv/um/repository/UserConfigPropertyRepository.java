package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.model.UserConfigProperty;


public interface UserConfigPropertyRepository extends JpaRepository<UserConfigProperty, Long> {
}

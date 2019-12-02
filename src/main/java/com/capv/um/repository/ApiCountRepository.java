package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capv.um.model.ApiCount;
@Repository
public interface ApiCountRepository extends JpaRepository<ApiCount, Long>{
}

package com.capv.um.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.capv.um.model.ClientNetworkDetails;

@Repository
public interface ClientNetworkRepository extends JpaRepository<ClientNetworkDetails, Long> {
}

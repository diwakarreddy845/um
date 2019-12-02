package com.capv.um.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.ClientNetworkDetails;
import com.capv.um.repository.ClientNetworkRepository;

@Service("clientNetworkService")
@Transactional("transactionManager")
public class ClientNetworkServiceImpl implements ClientNetworkService {

	@Autowired
	ClientNetworkRepository clientNetworkRepository;

	@Override
	public void save(ClientNetworkDetails details) {
		clientNetworkRepository.save(details);
	}
}

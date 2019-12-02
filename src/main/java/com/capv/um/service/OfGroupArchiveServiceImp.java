package com.capv.um.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.OfGroupArchive;
import com.capv.um.repository.OfGroupArchiveRepository;

@Service("ofGroupArchiveService")
@Transactional("transactionManager")
public class OfGroupArchiveServiceImp implements OfGroupArchiveService {

	@Autowired
	OfGroupArchiveRepository ofGroupArchiveRepository;

	@Override
	public List<OfGroupArchive> getArchiveGroupHistory(String groupJid, String startDate, String endDate, int recordsLimit) {
		return ofGroupArchiveRepository.getArchiveGroupHistory(groupJid, startDate, endDate, recordsLimit);
	}

	@Override
	public Long save(OfGroupArchive groupArchiveMessage) {
		return ofGroupArchiveRepository.save(groupArchiveMessage).getId();
	}

	@Override
	public void update(OfGroupArchive groupArchiveMessage) {
		ofGroupArchiveRepository.save(groupArchiveMessage);
	}

	@Override
	public OfGroupArchive fetchById(Long id) {
		Optional<OfGroupArchive> oga = ofGroupArchiveRepository.findById(id);
		if (oga.isPresent())
			return oga.get();
		return null;
	}

	@Override
	public void delete(OfGroupArchive ofGroupArchive) {
		ofGroupArchiveRepository.delete(ofGroupArchive);
	}

	public List<OfGroupArchive> getOneOneHistory(String toJid, String fromJID, String startDate, String endDate, int recordsLimit) {
		return ofGroupArchiveRepository.getOneOneHistory(toJid, fromJID, startDate, endDate, recordsLimit);
	}

	public List<OfGroupArchive> getOneOneSearch(String toJid, String fromJID, String searchParam) {
		return ofGroupArchiveRepository.getOneOneSearch(toJid, fromJID, searchParam);
	}

	public List<OfGroupArchive> getGroupSearch(String groupJid, String searchParam) {
		return ofGroupArchiveRepository.getGroupSearch(groupJid, searchParam);
	}

	@Override
	public Long getGroupMessageByRoomAndDate() {
		return ofGroupArchiveRepository.getGroupMessageByRoomAndDate();
	}
}

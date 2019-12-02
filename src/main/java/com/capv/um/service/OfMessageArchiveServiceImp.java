package com.capv.um.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.chat.model.OfMessageArchive;
import com.capv.um.repository.OfMessageArchiveRepository;

@Service("ofMessageArchiveService")
@Transactional("transactionManagerForOpenFire")
public class OfMessageArchiveServiceImp implements OfMessageArchiveService {

	@Autowired
	OfMessageArchiveRepository ofMessageArchiveRepository; 

	@Override
	public List<OfMessageArchive> getAllOfMessageArchives() {
		return ofMessageArchiveRepository.findAll();
	}

	@Override
	public void save(OfMessageArchive ofMessageArchive) {
		ofMessageArchiveRepository.save(ofMessageArchive);
	}

	@Override
	public void update(OfMessageArchive ofMessageArchive) {
		ofMessageArchiveRepository.save(ofMessageArchive);
	}

	@Override
	public void delete(OfMessageArchive ofMessageArchive) {
		ofMessageArchiveRepository.delete(ofMessageArchive);
	}

	@Override
	public OfMessageArchive getById(Long ofMessageID) {
		return (OfMessageArchive) ofMessageArchiveRepository.getOfMessageArchiveByMessageID(ofMessageID);
	}

	@Override
	public List<OfMessageArchive> getArchiveGroupHistory(String groupJid, String startDate, String endDate, int recordsLimit) {
		return ofMessageArchiveRepository.getArchiveGroupHistory(groupJid, startDate, endDate, recordsLimit);
	}

	@Override
	public OfMessageArchive getByConversationId(Long conversationID) {
		return (OfMessageArchive) ofMessageArchiveRepository.getOfMessageArchiveByConversationID(conversationID);
	}

	@Override
	public List<OfMessageArchive> getByMessageIDAndConversationID(Long messageID, Long conversationID) {
		return ofMessageArchiveRepository.getOfMessageArchiveByMessageIDAndConversationID(messageID, conversationID);
	}

	@Override
	public List<OfMessageArchive> getChatHistory(String toJID, String fromJID) {
		return ofMessageArchiveRepository.getChatHistory(toJID, fromJID);
	}

	@Override
	public List<OfMessageArchive> getArchiveHistory(String ownerJid, String withJid, String startDate, String endDate, int recordsLimit) {
		return ofMessageArchiveRepository.getArchiveHistory(ownerJid, withJid, startDate, endDate, recordsLimit);
	}

	@Override
	public List<OfMessageArchive> getArchiveHistoryLastMessage(String ownerJid, String groups) {
		return ofMessageArchiveRepository.getArchiveHistoryLastMessage(ownerJid, groups);
	}
}

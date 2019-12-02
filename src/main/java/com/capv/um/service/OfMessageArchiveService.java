package com.capv.um.service;

import java.util.List;

import com.capv.um.chat.model.OfMessageArchive;

public interface OfMessageArchiveService {
	
	
	public List<OfMessageArchive> getAllOfMessageArchives();

	
	public void save(OfMessageArchive ofMessageArchive) ;
	
	
	public void update(OfMessageArchive ofMessageArchive);
	

	public void delete(OfMessageArchive ofMessageArchive) ;
	
	
	public OfMessageArchive getById(Long ofMessageID);
	
	public OfMessageArchive getByConversationId(Long conversationID);
	
	public List<OfMessageArchive> getByMessageIDAndConversationID(Long messageID, Long conversationID);


	List<OfMessageArchive> getChatHistory(String toJID, String fromJID);


	public List<OfMessageArchive> getArchiveHistory(String ownerJid, String withJid,
													String startDate, String endDate, 
													int recordsLimit);


	public List<OfMessageArchive> getArchiveGroupHistory(String groupJid, String startDate,
															String endDate, int recordsLimit);


	public List<OfMessageArchive> getArchiveHistoryLastMessage(String ownerJid, String groups);

}

package com.capv.um.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.chat.model.OfMessageArchive;


public interface OfMessageArchiveRepository extends JpaRepository<OfMessageArchive, Long> {

	OfMessageArchive getOfMessageArchiveByMessageID(Long ofMessageID);

	OfMessageArchive getOfMessageArchiveByConversationID(Long conversationID);

	List<OfMessageArchive> getArchiveGroupHistory(String groupJid, String startDate, String endDate, int recordsLimit);

	List<OfMessageArchive> getOfMessageArchiveByMessageIDAndConversationID(Long messageID, Long conversationID);

	List<OfMessageArchive> getChatHistory(String toJID, String fromJID);

	List<OfMessageArchive> getArchiveHistory(String ownerJid, String withJid, String startDate, String endDate, int recordsLimit);

	List<OfMessageArchive> getArchiveHistoryLastMessage(String ownerJid, String groups);
}

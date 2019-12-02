package com.capv.um.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.capv.um.model.OfGroupArchive;


public interface OfGroupArchiveRepository extends JpaRepository<OfGroupArchive, Long> {

	List<OfGroupArchive> getArchiveGroupHistory(String groupJid, String startDate, String endDate, int recordsLimit);

	List<OfGroupArchive> getOneOneHistory(String toJid, String fromJID, String startDate, String endDate, int recordsLimit);

	List<OfGroupArchive> getOneOneSearch(String toJid, String fromJID, String searchParam);

	Long getGroupMessageByRoomAndDate();

	List<OfGroupArchive> getGroupSearch(String groupJid, String searchParam);
}

package com.capv.um.service;

import java.util.List;
import com.capv.um.model.OfGroupArchive;

public interface OfGroupArchiveService {
	
	
	public List<OfGroupArchive> getArchiveGroupHistory(String groupJid, String startDate,
															String endDate, int recordsLimit);
	List<OfGroupArchive> getOneOneHistory(String toJid,String fromJID, String startDate, 
														String endDate, int recordsLimit);
	public OfGroupArchive fetchById(Long id);
	
    public void update(OfGroupArchive ofGroupArchive);
    
    public void delete(OfGroupArchive ofGroupArchive);

	public Long save(OfGroupArchive groupArchiveMessage);
	
	List<OfGroupArchive> getOneOneSearch(String toJid,String fromJID,String searchParam);

	List<OfGroupArchive> getGroupSearch(String groupJid, String searchParam);

	Long getGroupMessageByRoomAndDate();
}

package com.capv.um.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import com.capv.um.model.CallDataStats;
import com.capv.um.model.UserCallState;

@Repository
public interface CallStateRepository extends JpaRepository<UserCallState, Long> {
	
	@Query("SELECT a FROM user_call_state a WHERE schedule_start_date = ?1 AND status = ?2")
	List<UserCallState> oneToOneCallsInProgressByCallers(String callee1, String callee2, Long clientId);
	
	@Query("SELECT a FROM user_call_state a WHERE client_id = ?1 AND call_Type = 'group' and (call_status = 1 or call_status = 3)")
	List<UserCallState> getActiveGroupCallListByClientId(Long clientId);

	UserCallState callStateByRoom(String roomid);

	UserCallState getLastActiveGroupCallByJid(String jid);

	List<UserCallState> callStatesInprogress(String userName, Long clientId);

	List<UserCallState> userCallLog(String userName, int resultOffset, int maxResults, String requestType);

	List<UserCallState> getMissedCallLog(String userName, int resultOffset, int maxResults);

	String getCalleeList(String room);

	UserCallState getCallLogRoomList(String room);

	List<CallDataStats> getAllClientsCallStatsData(String startDate, String endDate1, Integer start, Integer length);

	List<CallDataStats> getAllClientsCallStatsReportUsingSearch(String clientId, String startDate, String endDate1, int startIndex, int countIndex,
			String searchParam);

	List<CallDataStats> getByClientCallStatsData(String clientId, String startDate, String endDate1, Integer start, Integer length);

	List<CallDataStats> getByUserCallStatsData(String clientId, String userName, String startDate, String endDate1, Integer start, Integer length);

	List<CallDataStats> getAllUserCallStatsData(String startDate, String endDate, int start, int length);

	List<CallDataStats> getByClientCallStateData(String clientId, String state, int start, int length);

	Integer getListOfCallStats(String clientId, String startDate, String endDate1);

	Integer getListOfCallStatsForSearch(String clientId, String startDate, String endDate1, String searchParam);

	Integer getListOfCallStatsUsingClientIdAndUserName(String clientId, String startDate, String endDate1, String userName);

	Integer getListOfAllClientsCallStats(String startDate, String endDate1);

	List<UserCallState> userRecodedVideoLog(String userName, int resultOffset, int maxResults, String requestType);

	

	List<CallDataStats> getAllClientsCallStatsReportUsingSearch(String clientId, String searchParam, String startDate, String endDate1,
			int startIndex, int countIndex);
}

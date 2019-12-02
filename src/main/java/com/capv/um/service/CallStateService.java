package com.capv.um.service;

import java.text.ParseException;
import java.util.List;

import com.capv.um.model.CallDataStats;
import com.capv.um.model.UserCallState;

/**
 * <h1> CallStateService </h1>
 * this interface is used to perform video call crud operations
 * @author caprus it
 * @version 1.0
 */
public interface CallStateService {
	
	/**
	 * this method is used save the user call state
	 * @param user the user name
	 */
	void save(UserCallState user);
	
	/**
	 * this method is used update the user call state
	 * @param user the user name
	 */
	void update(UserCallState user);
	
	/**
	 * this method is used delete the user call state
	 * @param user the user name
	 */
	void delete(UserCallState user);
	

	/**
	 * this method is used to get the list of one to one call in progress list
	 * @param callee1 the caller name
	 * @param callee2 the receiver name
	 * @param client_id the user client id
	 * @return the list of call in progress records
	 */
	List<UserCallState> oneToOneCallsInProgressByCallers(String callee1, String callee2, Long client_id);
	
	/**
	 * this method is used to the call state by room
	 * @param roomid the room jid
	 * @return call state of room
	 */
	UserCallState callStateByRoom(String roomid);
	
	/**
	 * this method is used to the last active group calls 
	 * @param jid the room jid
	 * @return last active group call
	 */
	UserCallState getLastActiveGroupCallByJid(String jid);
	

	/**
	 * this method is used to get the list of user call in progress records
	 * @param callee the caller name
	 * @param client_id the user client id
	 * @return the list of call in progress records
	 */
	public List<UserCallState> callStatesInprogress(String userName, Long clientId);
	

	/**
	 * this method is used to get the call log of user.
	 * @param userName the user name
	 * @param resultOffset first result of the result set
	 * @param maxResults maximum number of results to be return from result set
	 * @param requestType based on requestType results to be return from result set
	 * @return list of call log
	 */
	public List<UserCallState> userCallLog(String userName, int resultOffset, int maxResults , String requestType);
	
	List<UserCallState> getMissedCallLog(String userName, int resultOffset, int maxResults );
	/**
	 * this method is used to get call list of room
	 * @param room the room jid
	 *  @return list of calls
	 */
	public String getCalleeList(String room);
	
	public UserCallState getCallLogRoomList(String room);
	
	/**
	 *  This method is used to get All the Clients consolidated call statistics grouped by clientId, startDate, endDate, start and length.
	 *  @param clientId             - it specifies an id of CallDataStats model.
	 *  @param startDate 	  	   - it specifies the start date to fetch data.
	 *  @param endDate 	 	   - it specifies the end date to fetch data.
	 *  @param length				   - it indicate that number of records data table can display in the current draw.
	 *  @param start				   - it indicate the starting point in the current data set.
	 * @return callDataStats contains an array of All Clients consolidated reports to be displayed in data table.
	 */
	List<CallDataStats> getAllClientsCallStats(String startDate, String endDate, Integer start, Integer length);
	
	/**
	 *  This method is used to get All the Clients consolidated call statistics grouped by clientId, startDate, endDate, startIndex, countIndex and searchParam.
	 *  @param clientId             - it specifies an id of CallDataStats model.
	 *  @param startDate 	  	   - it specifies the start date to fetch data.
	 *  @param endDate 	 	   - it specifies the end date to fetch data.
	 *  @param draw				   - it is used by DataTables to ensure that data returns from server-side processing requests are drawn in sequence by DataTables.
	 *  @param countIndex	   - it indicate that number of records data table can display in the current draw.
	 *  @param startIndex		   - it indicate the starting point in the current data set.
	 *  @param searchParam  -  it indicate the records to be search based on specific column.
	 * @return callDataStats contains an array of All Clients consolidated reports to be displayed in data table.
	 */
	List<CallDataStats> getAllClientsCallStatsReportUsingSearch(String clientId, String startDate, String endDate, int startIndex, int countIndex, String searchParam);
	
	/**
	 *  This method is used to get a particular client's call statistics by user's supplied clientId, startDate and endDate.
	 *  @param clientId             - it specifies an id of CallDataStats model.
	 *  @param startDate 	  	   - it specifies the start date to fetch data.
	 *  @param endDate 	 	   - it specifies the end date to fetch data.
	 *  @param length				   - it indicate that number of records data table can display in the current draw.
	 *  @param start				   - it indicate the starting point in the current data set.
	 *  @return callDataStats contains an array of client wise reports to be displayed in data table.
	 * @throws ParseException 
	 */
	List<CallDataStats> getByClientCallStats(String clientId, String startDate, String endDate,
																					Integer start, Integer length) throws ParseException;
	
	/**
	 *  This method is used to  get the call statistics of the user's using clientId, userName, startDate and endDate.
	 *  @param clientId             - it specifies an id of CallDataStats model.
	 *  @param userName		   - it specifies the userName.
	 *  @param startDate 	  	   - it specifies the start date to fetch data.
	 *  @param endDate 	 	   - it specifies the end date to fetch data.
	 *  @param length				   - it indicate that number of records data table can display in the current draw.
	 *  @param start				   - it indicate the starting point in the current data set.
	 *  @return callDataStats contains an array of client wise reports to be displayed in data table.
	 * @throws ParseException 
	 */
	List<CallDataStats> getByUserCallStatsReport(String clientId,String userName, String startDate, String endDate,
																								Integer start, Integer length) throws ParseException;

	/**
	 *  This method is used to get All the Users consolidated call statistics grouped by clientId and caller_name
	 *  @param startDate 	  	   - it specifies the start date to fetch data.
	 *  @param endDate 	 	   - it specifies the end date to fetch data.
	 *  @param length				   - it indicate that number of records data table can display in the current draw.
	 *  @param start				   - it indicate the starting point in the current data set.
	 *  @return callDataStats contains an array of All the Users consolidated call statistics to be displayed in data table.
	 */
	List<CallDataStats> getAllUserCallStats(String startDate, String endDate, int start, int length);

	
	/**
	 *  This method is used to get the call statistics of the user's supplied clientId and call status.
	 *  @param clientId             - it specifies an id of CallDataStats model.
	 *  @param state		   		   - it specifies the userName.
	 *  @param length				   - it indicate that number of records data table can display in the current draw.
	 *  @param start				   - it indicate the starting point in the current data set.
	 *  @return callDataStats contains an array of  call statistics of the user's to be displayed in data table.
	 */
	List<CallDataStats> getByClientCallStateReport(String clientId, String state, int start, int length);

	/**
	 *  This method is used to  get the call statistics of the user's using clientId, searchParam, startDate, endDate, startIndex and countIndex.
	 *  @param clientId             - it specifies an id of CallDataStats model.
	 *  @param searchParam   - it indicate the records to be search based on specific column.
	 *  @param startDate 	  	   - it specifies the start date to fetch data.
	 *  @param endDate 	 	   - it specifies the end date to fetch data.
	 *  @param length				   - it indicate that number of records data table can display in the current draw.
	 *  @param start				   - it indicate the starting point in the current data set.
	 *  @return callDataStats contains an array of client wise reports to be displayed in data table.
	 */
	List<CallDataStats> getByUserCallStatsReportUsingSearch(String clientId, String searchParam, String startDate, String endDate,
																														int startIndex, int countIndex);
	
	Integer getListOfCallStats(String clientId, String startDate, String endDate);

	Integer getListOfCallStatsForSearch(String clientId, String startDate, String endDate, String searchParam);

	Integer getListOfCallStatsUsingClientIdAndUserName(String clientId, String startDate, String endDate, String userName);

	Integer getListOfCallStatsUsingClientIdAndSearchParam(String clientId, String startDate, String endDate, String searchParam);

	Integer getListOfAllClientsCallStats(String startDate, String endDate);
	List<UserCallState> userRecodedVideoLog(String userName, int resultOffset, int maxResults ,String requestType);

	List<UserCallState> getActiveGroupCallListByClientId(Long clientId);

}

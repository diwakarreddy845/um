package com.capv.um.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.um.model.CallDataStats;
import com.capv.um.model.UserCallState;
import com.capv.um.repository.CallStateRepository;

/**
 * <h1>CallStateServiceImpl</h1> this class is used to perform video call crud operations
 * 
 * @author caprus it
 * @version 1.0
 */
@Service("callStateService")
@Transactional("transactionManager")
public class CallStateServiceImpl implements CallStateService {

	@Autowired
	private CallStateRepository callStateRepository;

	private static final Logger log = LoggerFactory.getLogger(CallStateServiceImpl.class);

	@Override
	public void save(UserCallState callStates) {
		callStateRepository.save(callStates);
	}

	@Override
	public void update(UserCallState callStates) {
		callStateRepository.save(callStates);
	}

	@Override
	public void delete(UserCallState callStates) {
		callStateRepository.delete(callStates);
	}

	@Override
	public List<UserCallState> oneToOneCallsInProgressByCallers(String callee1, String callee2, Long clientId) {
		return callStateRepository.oneToOneCallsInProgressByCallers(callee1, callee2, clientId);
	}

	@Override
	public UserCallState callStateByRoom(String roomid) {
		return callStateRepository.callStateByRoom(roomid);
	}

	@Override
	public UserCallState getLastActiveGroupCallByJid(String jid) {
		return callStateRepository.getLastActiveGroupCallByJid(jid);
	}

	@Override
	public List<UserCallState> callStatesInprogress(String userName, Long clientId) {
		return callStateRepository.callStatesInprogress(userName, clientId);
	}

	public List<UserCallState> userCallLog(String userName, int resultOffset, int maxResults, String requestType) {
		return callStateRepository.userCallLog(userName, resultOffset, maxResults, requestType);
	}

	public List<UserCallState> getMissedCallLog(String userName, int resultOffset, int maxResults) {
		return callStateRepository.getMissedCallLog(userName, resultOffset, maxResults);
	}

	public String getCalleeList(String room) {
		return callStateRepository.getCalleeList(room);
	}

	@Override
	public UserCallState getCallLogRoomList(String room) {
		return callStateRepository.getCallLogRoomList(room);
	}

	@Override
	public List<CallDataStats> getAllClientsCallStats(String startDate, String endDate, Integer start, Integer length) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getAllClientsCallStatsData(startDate, endDate1, start, length);
	}

	@Override
	public List<CallDataStats> getAllClientsCallStatsReportUsingSearch(String clientId, String startDate, String endDate, int startIndex,
			int countIndex, String searchParam) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getAllClientsCallStatsReportUsingSearch(clientId, startDate, endDate1, startIndex, countIndex, searchParam);
	}

	@Override
	public List<CallDataStats> getByClientCallStats(String clientId, String startDate, String endDate, Integer start, Integer length)
			throws ParseException {
		return callStateRepository.getByClientCallStatsData(clientId, startDate, getNextDate(endDate), start, length);
	}

	@Override
	public List<CallDataStats> getByUserCallStatsReport(String clientId, String userName, String startDate, String endDate, Integer start,
			Integer length) throws ParseException {
		String endDate1 = null;
		endDate1 = getNextDate(endDate);
		return callStateRepository.getByUserCallStatsData(clientId, userName, startDate, endDate1, start, length);
	}

	@Override
	public List<CallDataStats> getAllUserCallStats(String startDate, String endDate, int start, int length) {
		return callStateRepository.getAllUserCallStatsData(startDate, endDate, start, length);
	}

	@Override
	public List<CallDataStats> getByClientCallStateReport(String clientId, String state, int start, int length) {
		return callStateRepository.getByClientCallStateData(clientId, state, start, length);
	}

	@Override
	public Integer getListOfCallStats(String clientId, String startDate, String endDate) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getListOfCallStats(clientId, startDate, endDate1);
	}

	@Override
	public Integer getListOfCallStatsForSearch(String clientId, String startDate, String endDate, String searchParam) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getListOfCallStatsForSearch(clientId, startDate, endDate1, searchParam);
	}

	@Override
	public Integer getListOfCallStatsUsingClientIdAndUserName(String clientId, String startDate, String endDate, String userName) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getListOfCallStatsUsingClientIdAndUserName(clientId, startDate, endDate1, userName);
	}

	@Override
	public Integer getListOfCallStatsUsingClientIdAndSearchParam(String clientId, String startDate, String endDate, String searchParam) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getListOfCallStatsUsingClientIdAndUserName(clientId, startDate, endDate1, searchParam);
	}

	@Override
	public List<CallDataStats> getByUserCallStatsReportUsingSearch(String clientId, String searchParam, String startDate, String endDate,
			int startIndex, int countIndex) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getAllClientsCallStatsReportUsingSearch(clientId, searchParam, startDate, endDate1, startIndex, countIndex);
	}

	public String getNextDate(String endDate) throws ParseException {
		SimpleDateFormat df = null;
		Date date = null;
		df = new SimpleDateFormat("yyyy-MM-dd");
		date = df.parse(endDate);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, 1);
		date = c.getTime();
		return df.format(date);
	}

	@Override
	public Integer getListOfAllClientsCallStats(String startDate, String endDate) {
		String endDate1 = null;
		try {
			endDate1 = getNextDate(endDate);
		} catch (ParseException e) {
			log.error(e.getMessage());
		}
		return callStateRepository.getListOfAllClientsCallStats(startDate, endDate1);
	}

	public List<UserCallState> userRecodedVideoLog(String userName, int resultOffset, int maxResults, String requestType) {
		return callStateRepository.userRecodedVideoLog(userName, resultOffset, maxResults, requestType);
	}

	@Override
	public List<UserCallState> getActiveGroupCallListByClientId(Long clientId) {
		return callStateRepository.getActiveGroupCallListByClientId(clientId);
	}
}

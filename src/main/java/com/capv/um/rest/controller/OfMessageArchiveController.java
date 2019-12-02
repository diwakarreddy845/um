package com.capv.um.rest.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.client.user.websocket.UserWebSocketMessage;
import com.capv.um.chat.model.OfMessageArchive;
import com.capv.um.model.OfGroupArchive;
import com.capv.um.service.OfGroupArchiveService;
/*
 * import com.capv.service.OfMessageArchiveService; import com.capv.util.CustomComparator; import com.capv.util.ServiceStatus;
 */
import com.capv.um.service.OfMessageArchiveService;
import com.capv.um.util.CustomComparator;
import com.capv.um.util.CustomGroupComparator;
import com.capv.um.util.ServiceStatus;
import com.google.gson.JsonObject;

@RestController
public class OfMessageArchiveController {

	@Autowired
	OfMessageArchiveService ofMessageArchiveService;

	@Autowired
	private OfGroupArchiveService ofGroupArchiveService;

	private static final Logger log = LoggerFactory.getLogger(OfMessageArchiveController.class);

	@RequestMapping(value = "/client/getChatHistory/{toJID}/{fromJID}", method = RequestMethod.GET, produces = { "application/json" })
	public List<OfMessageArchive> getChatHistory(@PathVariable("toJID") String toJID, @PathVariable("fromJID") String fromJID) {
		log.debug("Entered into /client/getChatHistory/{toJID}/{fromJID} method");
		log.info("Getting chat history with  toJID  :{}     fromJID    :{}", toJID, fromJID);
		List<OfMessageArchive> allChatHistory = new ArrayList<OfMessageArchive>();
		List<OfMessageArchive> chatHistory1 = ofMessageArchiveService.getChatHistory(toJID, fromJID);
		List<OfMessageArchive> chatHistory2 = ofMessageArchiveService.getChatHistory(fromJID, toJID);
		// chatHistory2.subList(fromIndex, toIndex)
		allChatHistory.addAll(chatHistory1);
		allChatHistory.addAll(chatHistory2);
		Collections.sort(allChatHistory, new CustomComparator());
		log.debug("Exit from /client/getChatHistory/{toJID}/{fromJID} method");
		return allChatHistory;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/client/getArchiveHistory/{ownerJid:.+}/{withJid:.+}/{clientId}", method = RequestMethod.GET, produces = {
			"application/json" })
	public JSONObject getArchiveHistory(@PathVariable("ownerJid") String ownerJid, @PathVariable("withJid") String withJid,
			@PathVariable("clientId") Long clientId, HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into /client/getArchiveHistory/{ownerJid:.+}/{withJid:.+}/{clientId} method");
		log.info("Getting archive history with ownerJid   :{}    withJid   :{}    clientId   :{}", ownerJid, withJid, clientId);
		JSONObject one_oneChatHistory = new JSONObject();
		try {
			String toJID = withJid;
			String fromJID = ownerJid;
			String historyEndDate = request.getParameter("fromDate") != null ? request.getParameter("fromDate") : null;
			String maxHistoryDaysString = request.getParameter("maxHistoryDays");
			String noOfRecordsConfig = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_HISTORY_MAX_RECORDS);
			int noOfRecords = 25;
			if (noOfRecordsConfig != null) {
				try {
					noOfRecords = Integer.parseInt(noOfRecordsConfig);
				} catch (NumberFormatException nfe) {
					log.error("NumberFormatException occured while getting archive history  :", nfe);
				}
			}
			String historyStartDate = "";
			if (historyEndDate == null)
				historyEndDate = Long.toString(System.currentTimeMillis());
			else
				// subtract 1 millisecond from last fetched time to get the previous records from last fetched date
				historyEndDate = Long.toString((Long.parseLong(historyEndDate) - 1));
			int maxHistoryDays = 60;
			String maxHistoryConfig = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_MAX_HISTORY);
			if (maxHistoryDaysString != null) {
				try {
					maxHistoryDays = Integer.parseInt(maxHistoryDaysString);
				} catch (NumberFormatException nfe) {
					log.error("NumberFormatException occured while getting archive history", nfe);
					if (maxHistoryConfig != null) {
						try {
							maxHistoryDays = Integer.parseInt(maxHistoryConfig);
						} catch (NumberFormatException ne) {
							log.error("NumberFormatException occured while getting archive history", nfe);
						}
					}
				}
			} else {
				if (maxHistoryConfig != null) {
					try {
						maxHistoryDays = Integer.parseInt(maxHistoryConfig);
					} catch (NumberFormatException nfe) {
						log.error("NumberFormatException occured while getting archive history", nfe);
					}
				}
			}
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -maxHistoryDays);
			historyStartDate = Long.toString(calendar.getTime().getTime());
			String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
			/*List<OfMessageArchive> chatHistoryList = ofMessageArchiveService.getArchiveHistory(toJID+"@"+chatServerServiceName, 
																							fromJID+"@"+chatServerServiceName,
																							historyStartDate, historyEndDate, noOfRecords);*/
			List<OfGroupArchive> allChatHistory = new ArrayList<OfGroupArchive>();
			allChatHistory = ofGroupArchiveService.getOneOneHistory(toJID + "@" + chatServerServiceName, fromJID + "@" + chatServerServiceName,
					historyStartDate, historyEndDate, noOfRecords);
			Collections.sort(allChatHistory, new CustomGroupComparator());
			boolean maxFetch = false;
			long lastFetched = 0l;
			if (!allChatHistory.isEmpty()) {
				lastFetched = allChatHistory.get(0).getSentDate();
				if (allChatHistory.size() < noOfRecords)
					maxFetch = true;
			} else
				maxFetch = true;
			one_oneChatHistory.put("lastFetched", lastFetched);
			one_oneChatHistory.put("maxFetch", maxFetch);
			one_oneChatHistory.put("history", allChatHistory);
		} catch (Exception e) {
			log.error("Exception occured while getting archive history :", e);
			System.out.println(e);
		}
		log.debug("Exit from /client/getArchiveHistory/{ownerJid:.+}/{withJid:.+}/{clientId} method");
		return one_oneChatHistory;
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/client/getArchiveGroupHistory/{groupJid:.+}/{clientId}", method = RequestMethod.GET, produces = { "application/json" })
	public JSONObject getArchiveGroupHistory(@PathVariable("groupJid") String groupJid, @PathVariable("clientId") Long clientId,
			HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into /client/getArchiveGroupHistory/{groupJid:.+}/{clientId} method");
		log.info("Getting archive group history with groupJid   :{}    clientId   :{} ", groupJid, clientId);
		List<OfGroupArchive> groupChatHistoryList = new ArrayList<OfGroupArchive>();
		JSONObject groupChatHistory = new JSONObject();
		try {
			String historyEndDate = request.getParameter("fromDate") != null ? request.getParameter("fromDate") : null;
			String maxHistoryDaysString = request.getParameter("maxHistoryDays");
			String noOfRecordsConfig = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_HISTORY_MAX_RECORDS);
			int noOfRecords = 25;
			if (noOfRecordsConfig != null) {
				try {
					noOfRecords = Integer.parseInt(noOfRecordsConfig);
				} catch (NumberFormatException nfe) {
					log.error("NumberFormatException occured while getting archive group history", nfe);
				}
			}
			String historyStartDate = "";
			if (historyEndDate == null)
				historyEndDate = Long.toString(System.currentTimeMillis());
			else
				// subtract 1 millisecond from last fetched time to get the previous records from last fetched date
				historyEndDate = Long.toString((Long.parseLong(historyEndDate) - 1));
			int maxHistoryDays = 60;
			String maxHistoryConfig = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_MAX_HISTORY);
			if (maxHistoryDaysString != null) {
				try {
					maxHistoryDays = Integer.parseInt(maxHistoryDaysString);
				} catch (NumberFormatException nfe) {
					log.error("NumberFormatException occured while getting archive group history", nfe);
					if (maxHistoryConfig != null) {
						try {
							maxHistoryDays = Integer.parseInt(maxHistoryConfig);
						} catch (NumberFormatException ne) {
							log.error("NumberFormatException occured while getting archive group history", nfe);
						}
					}
				}
			} else {
				if (maxHistoryConfig != null) {
					try {
						maxHistoryDays = Integer.parseInt(maxHistoryConfig);
					} catch (NumberFormatException nfe) {
						log.error("NumberFormatException occured while getting archive group history", nfe);
					}
				}
			}
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, -maxHistoryDays);
			historyStartDate = Long.toString(calendar.getTime().getTime());
			String chatServerServiceName = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
			groupChatHistoryList = ofGroupArchiveService.getArchiveGroupHistory(groupJid + "@conference." + chatServerServiceName, historyStartDate,
					historyEndDate, noOfRecords);
			Collections.sort(groupChatHistoryList, new CustomGroupComparator());
			boolean maxFetch = false;
			long lastFetched = 0l;
			if (!groupChatHistoryList.isEmpty()) {
				lastFetched = groupChatHistoryList.get(0).getSentDate();
				if (groupChatHistoryList.size() < noOfRecords)
					maxFetch = true;
			} else
				maxFetch = true;
			groupChatHistory.put("lastFetched", lastFetched);
			groupChatHistory.put("maxFetch", maxFetch);
			groupChatHistory.put("history", CapvClientUserUtil.convertToJsonString(groupChatHistoryList));
		} catch (Exception e) {
			log.error("Exception occured while getting archive group history :", e);
			e.printStackTrace();
		}
		log.error("Exit from /client/getArchiveGroupHistory/{groupJid:.+}/{clientId} method");
		return groupChatHistory;
	}

	@RequestMapping(value = "/client/getLastMessage/{ownerJid}/{groups:.+}", method = RequestMethod.GET, produces = { "application/json" })
	public List<OfMessageArchive> getLastMessage(@PathVariable("ownerJid") String ownerJid, @PathVariable("groups") String groups) {
		log.debug("Entered into /client/getLastMessage/{ownerJid}/{groups:.+} method");
		log.info("Getting last message with ownerJid   :{}    groups   :{}", ownerJid, groups);
		List<OfMessageArchive> chatHistory = null;
		List<OfMessageArchive> chatHistoryAll = new ArrayList<>();
		chatHistory = ofMessageArchiveService.getArchiveHistoryLastMessage(ownerJid, groups);
		for (int i = 0; i < chatHistory.size() - 1; i++) {
			if (chatHistory.get(i).getToJID().equals(chatHistory.get(i + 1).getToJID())) {
				if (chatHistory.get(i).getSentDate() > chatHistory.get(i + 1).getSentDate())
					chatHistoryAll.add(chatHistory.get(i));
				else
					chatHistoryAll.add(chatHistory.get(i + 1));
			}
			if (chatHistory.get(i).getToJID().equals(chatHistory.get(i + 1).getFromJID())) {
				if (chatHistory.get(i).getSentDate() > chatHistory.get(i + 1).getSentDate())
					chatHistoryAll.add(chatHistory.get(i));
				else
					chatHistoryAll.add(chatHistory.get(i + 1));
			}
		}
		Collections.sort(chatHistoryAll, new CustomComparator());
		log.debug("Exit from /client/getLastMessage/{ownerJid}/{groups:.+} method");
		return chatHistoryAll;
	}

	public static Set<String> findDuplicates(List<String> listContainingDuplicates) {
		final Set<String> setToReturn = new HashSet<String>();
		final Set<String> set1 = new HashSet<String>();
		for (String yourInt : listContainingDuplicates) {
			if (!set1.add(yourInt)) {
				setToReturn.add(yourInt);
			}
		}
		return setToReturn;
	}

	@RequestMapping(value = "/client/registerOfMessageArchive", method = RequestMethod.POST, produces = { "application/json" }, consumes = {
			"application/json" })
	public ServiceStatus<Object> registerOfMessageArchive(@RequestBody OfMessageArchive ofMessageArchive, HttpServletRequest request,
			HttpServletResponse response) {
		log.debug("Entered into /client/registerOfMessageArchive method");
		log.info("registerOfMessageArchive  with ofMessageArchive object  :{}", ofMessageArchive);
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		serviceStatus.setStatus("success");
		serviceStatus.setMessage("Registration successful");
		if (ofMessageArchive != null) {
			OfMessageArchive ofMessageArchivefromDB = ofMessageArchiveService.getById(ofMessageArchive.getId());
			if (ofMessageArchivefromDB != null) {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Client already registered");
				return serviceStatus;
			} else
				ofMessageArchiveService.save(ofMessageArchive);
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Client already registered");
			return serviceStatus;
		}
		log.debug("Exit from /client/registerOfMessageArchive method");
		return serviceStatus;
	}

	@RequestMapping(value = "/client/getOfMessageArchive/{messageID}", method = RequestMethod.GET, produces = { "application/json" })
	public OfMessageArchive getOfMessageArchive(@PathVariable("messageID") Long messageID) {
		log.debug("Entered into /client/getOfMessageArchive/{messageID} method");
		log.info("getOfMessageArchive with messageID   :{}", messageID);
		return ofMessageArchiveService.getById(messageID);
	}

	@RequestMapping(value = "/client/getOfMessageArchive/{messageID}/{conversationID}", method = RequestMethod.GET, produces = { "application/json" })
	public List<OfMessageArchive> getOfMessageArchiveByMessageIDAndConversationID(@PathVariable("messageID") Long messageID,
			@PathVariable("conversationID") Long conversationID) {
		log.debug("Entered into /client/getOfMessageArchive/{messageID}/{conversationID} method");
		log.info("getOfMessageArchiveByMessageIDAndConversationID with messageID   :{}", messageID);
		return ofMessageArchiveService.getByMessageIDAndConversationID(messageID, conversationID);
	}

	@RequestMapping(value = "/client/getOfMessageArchive", method = RequestMethod.GET, produces = { "application/json" })
	public List<OfMessageArchive> getOfMessageArchiveByMessageIDAndConversationID() {
		log.debug("Entered into /client/getOfMessageArchive method");
		return ofMessageArchiveService.getAllOfMessageArchives();
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/client/getSearchHistory/{ownerJid:.+}/{withJid:.+}/{searchParam:.+}/{type:.+}/{clientID:.+}", method = RequestMethod.GET, produces = {
			"application/json" })
	public JSONObject getSearchHistory(@PathVariable("ownerJid") String userName, @PathVariable("withJid") String room,
			@PathVariable("searchParam") String searchParam, @PathVariable("type") String type, @PathVariable("clientID") Long clientID,
			HttpServletRequest request, HttpServletResponse response) {
		JSONObject responseSearchResult = new JSONObject();
		String service = CapvClientUserUtil.getClientConfigProperty(clientID, CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
		if (type.equals("group") && searchParam.length() >= 2 && room != null) {
			List<OfGroupArchive> searchResult = ofGroupArchiveService.getGroupSearch(room + "@conference." + service, searchParam);
			Collections.sort(searchResult, new CustomGroupComparator());
			responseSearchResult.put("searchResult", searchResult);
			return responseSearchResult;
		} else if (type.equals("one-one") && searchParam.length() >= 2 && room != null) {
			List<OfGroupArchive> searchResult = ofGroupArchiveService.getOneOneSearch(room + "@" + service, userName + "@" + service, searchParam);
			Collections.sort(searchResult, new CustomGroupComparator());
			responseSearchResult.put("searchResult", searchResult);
			return responseSearchResult;
		} else {
			JSONObject messageToUser = new JSONObject();
			messageToUser.put(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_SEARCH);
			messageToUser.put("Invalid", "Invalid Params");
			return messageToUser;
		}
	}
}

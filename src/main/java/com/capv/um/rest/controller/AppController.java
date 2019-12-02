package com.capv.um.rest.controller;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.model.ContactUs;
import com.capv.um.model.MeetingUpdatePojo;
import com.capv.um.model.TryIt;
import com.capv.um.model.TryItMeet;
import com.capv.um.model.TryItRoom;
import com.capv.um.service.EmailService;
import com.capv.um.service.TryItService;
import com.capv.um.util.CapvUtil;
import com.capv.um.util.ServiceStatus;

@RestController
public class AppController {

	private static final Logger log = LoggerFactory.getLogger(AppController.class);

	@Autowired
	EmailService emailService;

	@Autowired
	private TryItService tryit;

	@RequestMapping(value = "/timeout", method = RequestMethod.GET)
	public void getSelectedClient(HttpServletRequest request, HttpServletResponse response) {
		log.debug("Entered into timeout method ");
		try {
			response.sendRedirect("request_timeout.html");
			return;
		} catch (Exception e) {
			log.error("Exception occured due to time out :", e);
		}
		log.debug("Exit from timeout method");
	}

	@RequestMapping(value = "/ping", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public String ping() {
		return "pong";
	}

	@RequestMapping(value = "/sendContactUsMail", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> sendMessage(@RequestBody ContactUs contactUs) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		if (contactUs.getEmail() != null && contactUs.getMessage() != null) {
			try {
				emailService.sendEmail(new String[] { "info@capv.live" }, "Information request from " + contactUs.getEmail(), contactUs.getMessage());
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("Contact us mail sent successfully");
				return serviceStatus;
			} catch (Exception exception) {
				exception.printStackTrace();
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Sending mail failed due to server error ");
				return serviceStatus;
			}
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid details.");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/sendEmailToAdmin", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> sendEmailToAdmin(@RequestBody TryIt tryIt) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		if (tryIt.getEmail() != null && tryIt.getUserName() != null) {
			try {
				String emailId = tryIt.getEmail();
				String userName = tryIt.getUserName();
				String clientIdStr = CapvClientUserUtil.getConfigProperty("client.id");
				Long clientId = Long.parseLong(clientIdStr);
				String validity = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.TRYIT_ROOM_VALIDITY_KEY);
				String roomNo = UUID.randomUUID().toString();
				String[] to = emailId.split(",");
				TryItRoom tryit_room = new TryItRoom(roomNo, Integer.parseInt(validity), 0);
				tryit.save(tryit_room);
				emailService.sendEmail(to, "Capv Video Calling Invite for Admin ",
						"Please Click on the link to Join Call \t "
								+ CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_C2_UI_URL) + "tryit/" + roomNo
								+ "/" + userName + "?role=Admin");
				serviceStatus.setStatus("success");
				serviceStatus.setMessage(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_C2_UI_URL) + "tryit/"
						+ roomNo + "?role=Participant");
				serviceStatus.setAdminUrl(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_C2_UI_URL) + "tryit/"
						+ roomNo + "/" + userName + "?role=Admin");
				return serviceStatus;
			} catch (Exception exception) {
				exception.printStackTrace();
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Sending mail failed due to server error ");
				return serviceStatus;
			}
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid details.");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/sendInvite", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> sendInvite(@RequestBody TryIt tryIt) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		if (tryIt.getEmail() != null && tryIt.getUserName() != null) {
			try {
				String emailId = tryIt.getEmail();
				String inviteURL = tryIt.getInviteURL();
				String inviteMsg = tryIt.getMessageToSend();
				String room_id = tryIt.getRoom();
				TryItRoom meetingRooms = tryit.fetchUniqueRoomRecord(room_id);
				String[] to = emailId.split(",");
				StringBuilder description = new StringBuilder("<html><body>");
				description.append("Dear User," + "<br>" + "You have been invited to join the call by " + "<b>" + tryIt.getSenderName() + "</b>"
						+ "<br>" + "<br>" + "<br>" + "<b>To join the Meeting:</b>" + "<br><br>" + "&nbsp;&nbsp;<b>Logging from Web :</b>" + "<br>"
						+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please open the below link on Chrome, Firefox or Opera"
						+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + inviteURL + "\">Click to Launch</a>" + "&nbsp;&nbsp;<br>"
						+ "&nbsp;&nbsp;<br>");
				if (CapvClientUserUtil
						.getClientConfigProperty(Long.parseLong(meetingRooms.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_ANDROID)
						.equalsIgnoreCase("enable")) {
					description.append("&nbsp;&nbsp;<b>Logging From Mobile :</b>" + "<br>" + "&nbsp;&nbsp;&nbsp;<b>Android :</b>" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please open the below link on Default Browser or Chrome" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + inviteURL + "\">Click to Launch</a>" + "<br>");
				}
				if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(meetingRooms.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_IOS)
						.equalsIgnoreCase("enable")) {
					description.append("&nbsp;&nbsp;&nbsp;<b>iOS:</b>" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;If application is not installed , you will be requested to install the app ."
							+ "<br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please click the below link which will launch the app ." + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + inviteURL + "\">Click to Launch</a>" + "<br>");
				}
				description.append("<br>" + "Note:&nbsp;This is an auto generated e-mail. Please do not reply to this mail" + "<br>"
						+ "For any support on issues, please send a mail to " + CapvClientUserUtil.getClientConfigProperty(
								Long.parseLong(meetingRooms.getClientId()), CapvClientUserConstants.CAPV_CLIENT_SUPPORT_EMAIL)
						+ "</body></html>");
				emailService.sendInviteEmail1(to, "Invitation ", description.toString(), meetingRooms.getClientId());
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("Email sent sucessfully.");
				return serviceStatus;
			} catch (Exception exception) {
				exception.printStackTrace();
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Sending mail failed due to server error ");
				return serviceStatus;
			}
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid details.");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/createTryItRoom", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> createTryItRoom(@RequestBody TryItMeet tryItmeet) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		TryIt tryIt = new TryIt();
		try {
			if (tryItmeet.getClientId() != null && tryItmeet.getRoomName() != null && tryItmeet.getUserName() != null
					&& tryItmeet.getIsRecoding() != null && tryItmeet.getScheduleMeetingRecurringFlag() != null && tryItmeet.getAttendees() != null
					&& tryItmeet.getAttendees().trim().length() > 0) {
				if (tryItmeet.getScheduleMeetingRecurringFlag() == 1 || tryItmeet.getScheduleMeetingRecurringFlag() == 2) {
					if (tryItmeet.getEndDate() == null) {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("For recurring event end date required");
						return serviceStatus;
					}
				}
				tryIt.setId(CapvClientUserConstants.TRY_IT_ROOM);
				tryIt.setRoom(UUID.randomUUID().toString());
				String clientIdStr = tryItmeet.getClientId();// CapvClientUserUtil.getConfigProperty("client.id");
				Long clientId = Long.parseLong(clientIdStr);
				String validity = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.TRYIT_ROOM_VALIDITY_KEY);
				/*	    		    		this.sch_meeting_recurring_flag=sch_meeting_recurring_flag;
					    		    		this.meeting_duration=meeting_duration;
					    		    		this.time_zone_id=time_zone_id;
					    		    		this.meeting_date=meeting_date;*/
				TryItRoom tryit_room = new TryItRoom(tryIt.getRoom(), Integer.parseInt(validity), tryItmeet.getClientId(), tryItmeet.getRoomName(),
						tryItmeet.getUserName(), tryItmeet.getIsRecoding(), 0, tryItmeet.getScheduleMeetingRecurringFlag(),
						tryItmeet.getMeetingDuration(), tryItmeet.getTimeZoneId(), tryItmeet.getMeetingDate(), tryItmeet.getLocation(),
						tryItmeet.getAgenda(), tryItmeet.getEndDate(), tryItmeet.getAttendees());
				Date utcFormat = new Date(CapvUtil.getUTCTimeStamp());
				tryit_room.setCreatedTimestamp(utcFormat);
				tryit.save(tryit_room);
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("RoomId sent sucessfully.");
				serviceStatus.setResult(tryIt);
				// List<TryItRoom> meetingRooms=tryit.listRoomsByMatchingParameterVlue(tryIt.getRoom());
				if (tryit_room != null) {
					try {
						tryit.sendMeetingICS(tryit_room);
						serviceStatus.setStatus("success");
						serviceStatus.setMessage("successfully sent the invitation");
					} catch (Exception e) {
						e.printStackTrace();
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("failure");
					}
				} else {
					serviceStatus.setStatus("failure");
					serviceStatus.setMessage("room number not found ");
				}
			} else {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Parameters.");
			}
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("RoomId send failed due to server error ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/getTryItRoom/{clientId}/{user_name}", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus<List<TryItRoom>> getTryItRoom(@PathVariable("clientId") String clientId, @PathVariable("user_name") String user_name) {
		ServiceStatus<List<TryItRoom>> serviceStatus = new ServiceStatus<List<TryItRoom>>();
		List<TryItRoom> meetingRooms = new ArrayList<TryItRoom>();
		try {
			meetingRooms = tryit.listRoomsByMatchingUserName(user_name, clientId);
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("RoomId sent sucessfully.");
			serviceStatus.setResult(meetingRooms);
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/getMeetingRooms/{clientId}/{user_name}", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus<List<TryItRoom>> getMeetingRooms(@PathVariable("clientId") String clientId, @PathVariable("user_name") String user_name) {
		ServiceStatus<List<TryItRoom>> serviceStatus = new ServiceStatus<List<TryItRoom>>();
		List<TryItRoom> meetingRooms = new ArrayList<TryItRoom>();
		try {
			meetingRooms = tryit.listRoomsByMatchingUserName(user_name, clientId);
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("RoomId sent sucessfully.");
			serviceStatus.setResult(meetingRooms);
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/getTryItRoomDetails/{room_id}", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus<List<TryItRoom>> getTryItRoomDetails(@PathVariable("room_id") String roomId) {
		ServiceStatus<List<TryItRoom>> serviceStatus = new ServiceStatus<>();
		try {
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("Fetch sucessfully.");
			TryItRoom tr = tryit.fetchUniqueRoomRecord(roomId);
			List<TryItRoom> list = new ArrayList<>();
			list.add(tr);
			serviceStatus.setResult(list);
			return serviceStatus;
		} catch (Exception exception) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/deleteMeetingRoom/{room_id}", method = RequestMethod.DELETE, produces = { "application/json" })
	public ServiceStatus<Object> deleteTryItRoom(@PathVariable("room_id") String room_id) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		TryItRoom meetingRooms = new TryItRoom();
		try {
			meetingRooms = tryit.fetchUniqueRoomRecord(room_id);
			tryit.delete(meetingRooms);
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("Deleted Room sucessfully.");
			return serviceStatus;
		} catch (Exception exception) {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/getValidTryItRooms/{clientId}/{user_name}", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus<List<TryItRoom>> getValidTryItRooms(@PathVariable("clientId") String clientId, @PathVariable("user_name") String user_name) {
		ServiceStatus<List<TryItRoom>> serviceStatus = new ServiceStatus<List<TryItRoom>>();
		List<TryItRoom> meetingRooms = new ArrayList<TryItRoom>();
		try {
			meetingRooms = tryit.listRoomsByMatchingUserName(user_name, clientId);
			for (int i = 0; i < meetingRooms.size(); i++) {
				if (CapvUtil.getValidityCheck(meetingRooms.get(i).getValidity(), meetingRooms.get(i).getCreatedTimestamp())) {
					meetingRooms.remove(meetingRooms.get(i));
				}
			}
			serviceStatus.setStatus("success");
			serviceStatus.setMessage("RoomId sent sucessfully.");
			serviceStatus.setResult(meetingRooms);
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/validateTryItRoomSession", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus validateTryItRoomSession(@RequestParam("room_no") String room_no) {
		ServiceStatus serviceStatus = new ServiceStatus();
		if (room_no != null && !room_no.isEmpty()) {
			try {
				TryItRoom tryItRoom = tryit.fetchUniqueRoomRecord(room_no);
				if (tryItRoom != null) {
					Date startTime = null;
					Date endDate = tryItRoom.getEndDate();
					Date endTime = null;
					Integer duration = Integer.parseInt(tryItRoom.getMeetingDuration());
					if (tryItRoom.getScheduleMeetingRecurringFlag().equals(new Byte("1")) && tryItRoom.getEndDate().after(new Date())) {
						Calendar cal = Calendar.getInstance();
						cal.setTime(tryItRoom.getMeetingDate());
						Calendar cal2 = Calendar.getInstance();
						cal2.set(cal2.get(Calendar.YEAR), cal2.get(Calendar.MONTH), cal2.get(Calendar.DATE), cal.get(Calendar.HOUR_OF_DAY),
								cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));
						startTime = cal2.getTime();
						cal2.add(Calendar.HOUR_OF_DAY, duration);
						endTime = cal2.getTime();
					} else if (tryItRoom.getScheduleMeetingRecurringFlag().equals(new Byte("2")) && tryItRoom.getEndDate().after(new Date())) {
						Calendar cal = getStartDate(tryItRoom.getMeetingDate(), 0);
						Calendar caltime = Calendar.getInstance();
						caltime.setTime(tryItRoom.getMeetingDate());
						cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DATE), caltime.get(Calendar.HOUR_OF_DAY),
								caltime.get(Calendar.MINUTE), caltime.get(Calendar.SECOND));
						startTime = cal.getTime();
						cal.add(Calendar.HOUR_OF_DAY, duration);
						endTime = cal.getTime();
					} else {
						startTime = tryItRoom.getMeetingDate();
						endTime = Date.from(startTime.toInstant().plus(Duration.ofHours(duration)));
					}
					Date currentTime = new Timestamp(System.currentTimeMillis());
					String meetingType = tryItRoom.getMeetingType();
					String roomName = tryItRoom.getRoomName();
					if (currentTime.after(startTime) && currentTime.before(endTime)) {
						serviceStatus.setStatus("success");
						serviceStatus.setMessage("Valid Meeting Room");
						serviceStatus.setResult(true);
						serviceStatus.setMeetingType(meetingType);
						serviceStatus.setRoomName(roomName);
					} else if (currentTime.after(startTime)) {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("Meeting has ended ");
						serviceStatus.setRoomName(roomName);
						serviceStatus.setResult(false);
					} else {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("Meeting will start at : " + startTime);
						serviceStatus.setRoomName(roomName);
						serviceStatus.setResult(false);
					}
				} else {
					serviceStatus.setStatus("failure");
					serviceStatus.setMessage("Invalid Room Id ");
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Room Id " + exception.getMessage());
			}
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
		}
		return serviceStatus;
	}

	@RequestMapping(value = "/validateRoomSession", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus validateRoomSession(@RequestParam("room_no") String room_no) {
		ServiceStatus serviceStatus = new ServiceStatus();
		if (room_no != null && !room_no.isEmpty()) {
			try {
				TryItRoom tryItRoom = tryit.fetchUniqueRoomRecord(room_no);
				if (tryItRoom != null) {
					Date startTime = tryItRoom.getMeetingDate();
					Integer duration = Integer.parseInt(tryItRoom.getMeetingDuration());
					Date endTime = Date.from(startTime.toInstant().plus(Duration.ofHours(duration)));
					Date currentTime = new Timestamp(System.currentTimeMillis());
					String meetingType = tryItRoom.getMeetingType();
					String roomName = tryItRoom.getRoomName();
					long startTimeString = startTime.getTime();
					if (currentTime.after(startTime) && currentTime.before(endTime)) {
						serviceStatus.setStatus("success");
						serviceStatus.setMessage("Valid Meeting Room");
						serviceStatus.setMeetingType(meetingType);
						serviceStatus.setRoomName(roomName);
						String occupants = CapvClientUserUtil.getValidWebCastUser(room_no, tryItRoom.getClientId(), tryItRoom.getUserName());
						if (occupants != null) {
							serviceStatus.setResult(occupants);
						} else {
							serviceStatus.setResult("");
						}
					} else if (currentTime.after(endTime)) {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("Meeting has ended");
						serviceStatus.setRoomName(roomName);
						serviceStatus.setResult(false);
					} else if (currentTime.before(startTime)) {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("Meeting will start at : " + startTime);
						serviceStatus.setRoomName(roomName);
						serviceStatus.setStartTime(Long.toString(startTimeString));
						serviceStatus.setResult(false);
					} else {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("Invalid Room Id");
						serviceStatus.setRoomName(roomName);
						serviceStatus.setResult(false);
					}
				} else {
					serviceStatus.setStatus("failure");
					serviceStatus.setMessage("Invalid Room Id ");
				}
			} catch (Exception exception) {
				exception.printStackTrace();
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Room Id " + exception.getMessage());
			}
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("Invalid Room Id ");
		}
		return serviceStatus;
	}

	@RequestMapping(value = "/updateMeetingRoom", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> updateMeetingRoom(@RequestBody MeetingUpdatePojo tryItmeet) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		try {
			if (tryItmeet.getRoom_id() != null && tryItmeet.getRoom_name() != null && tryItmeet.getUser_name() != null
					&& tryItmeet.getIs_recoding() != null && tryItmeet.getSch_meeting_recurring_flag() != null
					&& tryItmeet.getMeeting_duration() != null && tryItmeet.getTime_zone_id() != null && tryItmeet.getMeeting_date() != null
					&& tryItmeet.getLocation() != null && tryItmeet.getAgenda() != null && tryItmeet.getAttendees() != null
					&& tryItmeet.getAttendees().trim().length() > 0) {
				if (tryItmeet.getSch_meeting_recurring_flag() == 1 || tryItmeet.getSch_meeting_recurring_flag() == 2) {
					if (tryItmeet.getEndDate() == null) {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("For recurring event end date required");
						return serviceStatus;
					}
				}
				TryItRoom tryItRoom = tryit.fetchUniqueRoomRecord(tryItmeet.getRoom_id());
				if (tryItRoom != null) {
					tryItRoom.setAttendees(tryItmeet.getAttendees());
					tryItRoom.setEndDate(tryItmeet.getEndDate());
					tryItRoom.setAgenda(tryItmeet.getAgenda());
					tryItRoom.setLocation(tryItmeet.getLocation());
					tryItRoom.setMeetingDate(tryItmeet.getMeeting_date());
					tryItRoom.setRoomName(tryItmeet.getRoom_name());
					tryItRoom.setTimeZoneId(tryItmeet.getTime_zone_id());
					tryItRoom.setMeetingDuration(tryItmeet.getMeeting_duration());
					tryItRoom.setScheduleMeetingRecurringFlag(tryItmeet.getSch_meeting_recurring_flag());
					tryItRoom.setIsRecoding(tryItmeet.getIs_recoding());
					Date utcFormat = new Date(CapvUtil.getUTCTimeStamp());
					tryItRoom.setCreatedTimestamp(utcFormat);
					tryit.update(tryItRoom);
					tryit.sendMeetingICS(tryItRoom);
				}
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("RoomId updated sucessfully.");
			} else {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Parameters.");
			}
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("RoomId updated failed due to server error ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/sendMeetingICS", method = RequestMethod.GET, produces = { "application/json" })
	public ServiceStatus<Object> sendMeetingICS(@RequestParam("roomNo") String roomNo) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		if (roomNo != null && roomNo.trim().length() > 0) {
			TryItRoom tryItRoom = tryit.fetchUniqueRoomRecord(roomNo);
			if (tryItRoom != null) {
				try {
					tryit.sendMeetingICS(tryItRoom);
					serviceStatus.setStatus("success");
					serviceStatus.setMessage("successfully sent the invitation");
				} catch (Exception e) {
					e.printStackTrace();
					serviceStatus.setStatus("failure");
					serviceStatus.setMessage("failure");
				}
			} else {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("room number not found ");
			}
		} else {
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("invalid room number");
		}
		return serviceStatus;
	}

	@RequestMapping(value = "/createTryItRoom1", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> createTryItRoom1(@RequestBody TryItMeet tryItmeet) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		TryIt tryIt = new TryIt();
		try {
			if (tryItmeet.getClientId() != null && tryItmeet.getRoomName() != null && tryItmeet.getUserName() != null
					&& tryItmeet.getIsRecoding() != null) {
				tryIt.setId(CapvClientUserConstants.TRY_IT_ROOM);
				tryIt.setRoom(UUID.randomUUID().toString());
				String clientIdStr = CapvClientUserUtil.getConfigProperty("client.id");
				Long clientId = Long.parseLong(clientIdStr);
				String validity = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.TRYIT_ROOM_VALIDITY_KEY);
				TryItRoom tryit_room = new TryItRoom(tryIt.getRoom(), Integer.parseInt(validity), tryItmeet.getClientId(), tryItmeet.getRoomName(),
						tryItmeet.getUserName(), tryItmeet.getIsRecoding(), 0);
				Date utcFormat = new Date(CapvUtil.getUTCTimeStamp());
				tryit_room.setCreatedTimestamp(utcFormat);
				tryit.save(tryit_room);
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("RoomId sent sucessfully.");
				serviceStatus.setResult(tryIt);
			} else {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Parameters.");
			}
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("RoomId send failed due to server error ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/createQuickMeeting", method = RequestMethod.POST, produces = { "application/json" }, consumes = { "application/json" })
	public ServiceStatus<Object> createQuickMeeting(@RequestBody TryItMeet tryItmeet) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		TryIt tryIt = new TryIt();
		try {
			if (tryItmeet.getClientId() != null && tryItmeet.getRoomName() != null && tryItmeet.getUserName() != null
					&& tryItmeet.getIsRecoding() != null) {
				tryIt.setId(CapvClientUserConstants.TRY_IT_ROOM);
				tryIt.setRoom(UUID.randomUUID().toString());
				String clientIdStr = CapvClientUserUtil.getConfigProperty("client.id");
				Long clientId = Long.parseLong(clientIdStr);
				String validity = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.TRYIT_ROOM_VALIDITY_KEY);
				int duration = Integer.parseInt(validity) * 60;
				TryItRoom tryit_room = new TryItRoom(tryIt.getRoom(), Integer.parseInt(validity), tryItmeet.getClientId(), tryItmeet.getRoomName(),
						tryItmeet.getUserName(), tryItmeet.getIsRecoding(), 0);
				Date utcFormat = new Date(CapvUtil.getUTCTimeStamp());
				tryit_room.setCreatedTimestamp(utcFormat);
				tryit_room.setMeetingDate(utcFormat);
				tryit_room.setMeetingDuration(Integer.toString(duration));
				tryit_room.setMeetingType(tryItmeet.getMeetingType());
				tryit.save(tryit_room);
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("RoomId sent sucessfully.");
				serviceStatus.setResult(tryIt);
			} else {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Parameters.");
			}
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("RoomId send failed due to server error ");
			return serviceStatus;
		}
	}

	@RequestMapping(value = "/createScheduleMeeting", method = RequestMethod.POST, produces = { "application/json" }, consumes = {
			"application/json" })
	public ServiceStatus<Object> createScheduleMeeting(@RequestBody TryItMeet tryItmeet) {
		ServiceStatus<Object> serviceStatus = new ServiceStatus<Object>();
		TryIt tryIt = new TryIt();
		try {
			if (tryItmeet.getClientId() != null && tryItmeet.getRoomName() != null && tryItmeet.getUserName() != null
					&& tryItmeet.getIsRecoding() != null && tryItmeet.getScheduleMeetingRecurringFlag() != null && tryItmeet.getAttendees() != null
					&& tryItmeet.getAttendees().trim().length() > 0) {
				if (tryItmeet.getScheduleMeetingRecurringFlag() == 1 || tryItmeet.getScheduleMeetingRecurringFlag() == 2) {
					if (tryItmeet.getEndDate() == null) {
						serviceStatus.setStatus("failure");
						serviceStatus.setMessage("For recurring event end date required");
						return serviceStatus;
					}
				}
				tryIt.setId(CapvClientUserConstants.TRY_IT_ROOM);
				tryIt.setRoom(UUID.randomUUID().toString());
				String clientIdStr = tryItmeet.getClientId();// CapvClientUserUtil.getConfigProperty("client.id");
				Long clientId = Long.parseLong(clientIdStr);
				String validity = CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.TRYIT_ROOM_VALIDITY_KEY);
				/*	    		    		this.sch_meeting_recurring_flag=sch_meeting_recurring_flag;
					    		    		this.meeting_duration=meeting_duration;
					    		    		this.time_zone_id=time_zone_id;
					    		    		this.meeting_date=meeting_date;*/
				TryItRoom tryit_room = new TryItRoom(tryIt.getRoom(), Integer.parseInt(validity), tryItmeet.getClientId(), tryItmeet.getRoomName(),
						tryItmeet.getUserName(), tryItmeet.getIsRecoding(), 0, tryItmeet.getScheduleMeetingRecurringFlag(),
						tryItmeet.getMeetingDuration(), tryItmeet.getTimeZoneId(), tryItmeet.getMeetingDate(), tryItmeet.getLocation(),
						tryItmeet.getAgenda(), tryItmeet.getEndDate(), tryItmeet.getAttendees());
				Date utcFormat = new Date(CapvUtil.getUTCTimeStamp());
				tryit_room.setCreatedTimestamp(utcFormat);
				tryit_room.setMeetingType(tryItmeet.getMeetingType());
				tryit.save(tryit_room);
				serviceStatus.setStatus("success");
				serviceStatus.setMessage("RoomId sent sucessfully.");
				serviceStatus.setResult(tryIt);
				// List<TryItRoom> meetingRooms=tryit.listRoomsByMatchingParameterVlue(tryIt.getRoom());
				if (tryit_room != null) {
					try {
						tryit.sendMeetingICS(tryit_room);
						serviceStatus.setStatus("success");
						serviceStatus.setMessage("successfully sent the invitation");
					} catch (Exception e) {
						e.printStackTrace();
						// serviceStatus.setStatus("failure");
						// serviceStatus.setMessage("failure");
					}
				} else {
					serviceStatus.setStatus("failure");
					serviceStatus.setMessage("room number not found ");
				}
			} else {
				serviceStatus.setStatus("failure");
				serviceStatus.setMessage("Invalid Parameters.");
			}
			return serviceStatus;
		} catch (Exception exception) {
			exception.printStackTrace();
			serviceStatus.setStatus("failure");
			serviceStatus.setMessage("RoomId send failed due to server error ");
			return serviceStatus;
		}
	}

	private static Calendar getStartDate(Date meeting_start_date, int i) {
		Calendar startCal = Calendar.getInstance();
		if (i == 0) {
			startCal.setTime(meeting_start_date);
		}
		String startdate = new SimpleDateFormat("MM/dd/yyyy").format(meeting_start_date);
		String currentdate = new SimpleDateFormat("MM/dd/yyyy").format(Calendar.getInstance().getTime());
		Date date1 = new Date(startdate);
		Date date2 = new Date(currentdate);
		Calendar cal = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal.setTime(date1);
		cal2.setTime(date2);
		Calendar returnDate = null;
		if (cal.compareTo(cal2) == 0) {
			returnDate = cal;
		} else if (cal.compareTo(cal2) > 0) {
			if (cal.getTime().equals(startCal.getTime()))
				returnDate = startCal;
			else {
				returnDate = cal;
			}
		} else {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
			returnDate = getStartDate(cal.getTime(), i + 1);
		}
		return returnDate;
	}
}

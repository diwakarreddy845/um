package com.capv.um.service;

import java.io.UnsupportedEncodingException;
import javax.activation.MailcapCommandMap;
import javax.activation.MimetypesFileTypeMap;
import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.constants.CapvConstants;
import com.capv.um.model.TryItRoom;
import com.capv.um.model.User;
import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VAlarm;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationLevel;
import biweekly.parameter.ParticipationStatus;
import biweekly.parameter.Related;
import biweekly.parameter.Role;
import biweekly.property.Action;
import biweekly.property.Attendee;
import biweekly.property.RawProperty;
import biweekly.property.RecurrenceRule;
import biweekly.property.Status;
import biweekly.property.Trigger;
import biweekly.util.DayOfWeek;
import biweekly.util.Frequency;
import biweekly.util.Recurrence;

/**
 * <h1>EmailService</h1> this class is used to send mails to users
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
@Service("emailService")
public class EmailService {

	@Autowired
	MailSenderService mailSenderService;

	@Autowired
	UserService userService;

	@Autowired
	Environment environment;

	public void sendInviteEmail(String[] to, String sub, String msgBody) {
		Long clientId = Long.parseLong(environment.getRequiredProperty("email.client"));
		JavaMailSender javaMailSender = mailSenderService.javaMailSender(clientId);
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			msg.setFrom(new InternetAddress(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CLIENT_MAIL_SENDERNAME),
					"Do-Not-Reply"));
			msg.setSubject(sub);
			for (int i = 0; i < to.length; i++) {
				msg.addRecipients(Message.RecipientType.TO, to[i]);
			}
			msg.setContent(msgBody, "text/html; charset=utf-8");
			javaMailSender.send(msg);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void sendInviteEmail1(String[] to, String sub, String msgBody, String client_id) {
		Long clientId = Long.parseLong(client_id);
		JavaMailSender javaMailSender = mailSenderService.javaMailSender(clientId);
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			msg.setFrom(new InternetAddress(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CLIENT_MAIL_SENDERNAME),
					"Do-Not-Reply"));
			msg.setSubject(sub);
			for (int i = 0; i < to.length; i++) {
				msg.addRecipients(Message.RecipientType.TO, to[i]);
			}
			msg.setContent(msgBody, "text/html; charset=utf-8");
			javaMailSender.send(msg);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * this method is used to send mails to friends
	 * 
	 * @param to      -- friend name
	 * @param sub     -- subject of mail
	 * @param msgBody -- body of message
	 */
	public void sendEmail(String[] to, String sub, String msgBody) {
		Long clientId = Long.parseLong(environment.getRequiredProperty("email.client"));
		JavaMailSender javaMailSender = mailSenderService.javaMailSender(clientId);
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(CapvClientUserUtil.getClientConfigProperty(clientId, CapvClientUserConstants.CAPV_CLIENT_MAIL_SENDERNAME));
		message.setTo(to);
		message.setSubject(sub);
		message.setText(msgBody);
		javaMailSender.send(message);
	}

	public void sendMeetingICS(TryItRoom tryItRoom) throws Exception {
		JavaMailSender javaMailSender = mailSenderService.javaMailSender(Long.parseLong(tryItRoom.getClientId()));
		System.out.println(Long.parseLong(tryItRoom.getClientId()) + "client id*************");
		MimetypesFileTypeMap mimetypes = (MimetypesFileTypeMap) MimetypesFileTypeMap.getDefaultFileTypeMap();
		mimetypes.addMimeTypes("text/calendar ics ICS");
		MailcapCommandMap mailcap = (MailcapCommandMap) MailcapCommandMap.getDefaultCommandMap();
		mailcap.addMailcap("text/calendar;; x-java-content-handler=com.sun.mail.handlers.text_plain");
		MimeMessage message = javaMailSender.createMimeMessage();
		User fromUser = userService.getByUserName(tryItRoom.getUserName(), false);
		String email_fromUser = CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), "email.from");
		message.setFrom(new InternetAddress(email_fromUser));
		message.setSubject(
				CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_NAME)
						+ " Invitation");
		String[] attendees = tryItRoom.getAttendees().split(",");
		Address[] list = new InternetAddress[attendees.length + 1];
		for (int i = 0; i < attendees.length; i++) {
			list[i] = new InternetAddress(attendees[i]);
		}
		list[attendees.length] = new InternetAddress(fromUser.getEmail());
		/* Address[] list=new InternetAddress[]{new InternetAddress("popjhalla@gmail.com"),
		 		new InternetAddress("amit.patel@caprusit.com")}; */
		message.addRecipients(Message.RecipientType.TO, list);
		Multipart multipart = new MimeMultipart("alternative");
		BodyPart messageBodyPart = buildMessageBody(tryItRoom);
		BodyPart calenderBodyPart = buildCalendarPart1(tryItRoom, fromUser, attendees);
		multipart.addBodyPart(messageBodyPart);
		multipart.addBodyPart(calenderBodyPart);
		message.setContent(multipart);
		javaMailSender.send(message);
	}

	private BodyPart buildCalendarPart1(TryItRoom tryItRoom, User fromUser, String[] attendees) throws Exception {
		BodyPart calendarPart = new MimeBodyPart();
		ICalendar ical = new ICalendar();
		VEvent event = new VEvent();
		StringBuilder description = new StringBuilder("<html><body>" + tryItRoom.getAgenda());
		description.append("<br><br>Dear User," + "<br>" + "You have been invited to join the call by " + "<b>"
				+ tryItRoom.getUserName().substring(0, tryItRoom.getUserName().indexOf("_")) + "</b>" + "<br>" + "<br>" + "<br>"
				+ "<b>To join the Meeting:</b>" + "<br><br>" + "&nbsp;&nbsp;<b>Logging from Web :</b>" + "<br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please open the below link on Chrome, Firefox or Opera"
				+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\""
				+ CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_MEETING_URL)
				+ tryItRoom.getRoomNo() + "\">Click to Launch</a>" + "&nbsp;&nbsp;<br>" + "&nbsp;&nbsp;<br>");
		if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_ANDROID)
				.equalsIgnoreCase("enable")
				|| CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_IOS)
						.equalsIgnoreCase("enable")) {
			description.append("&nbsp;&nbsp;<b>Logging From Mobile :</b>" + "<br>");
		}
		if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_ANDROID)
				.equalsIgnoreCase("enable")) {
			description
					.append("&nbsp;&nbsp;&nbsp;<b>Android :</b>" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please open the below link on Default Browser or Chrome" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + CapvClientUserUtil
									.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_MEETING_URL)
							+ tryItRoom.getRoomNo() + "\">Click to Launch</a>" + "<br>");
		}
		if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_IOS)
				.equalsIgnoreCase("enable")) {
			description
					.append("&nbsp;&nbsp;&nbsp;<b>iOS:</b>" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;If application is not installed , you will be requested to install the app ."
							+ "<br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please click the below link which will launch the app ." + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + CapvClientUserUtil
									.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_MEETING_URL)
							+ tryItRoom.getRoomNo() + "\">Click to Launch</a>" + "<br>");
		}
		description.append("<br>" + "Note:&nbsp;This is an auto generated e-mail. Please do not reply to this mail" + "<br>"
				+ "For any support on issues, please send a mail to &nbsp;" + CapvClientUserUtil.getClientConfigProperty(
						Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_CLIENT_SUPPORT_EMAIL)
				+ "</body></html>");
		event.setDescription(description.toString());
		RawProperty property = event.setExperimentalProperty("X-ALT-DESC", description.toString());
		property.setParameter("FMTTYPE", "text/html");
		event.setProperty(RawProperty.class, property);
		event.setPriority(5);
		event.setStatus(Status.confirmed());
		Attendee attendee = null;
		for (int i = 0; i < attendees.length; i++) {
			attendee = new Attendee(" ", attendees[i]);
			attendee.setRsvp(true);
			attendee.setRole(Role.ATTENDEE);
			attendee.setParticipationStatus(ParticipationStatus.NEEDS_ACTION);
			attendee.setParticipationLevel(ParticipationLevel.REQUIRED);
			event.addAttendee(attendee);
		}
		attendee = new Attendee(" ", fromUser.getEmail());
		attendee.setRsvp(true);
		attendee.setRole(Role.ATTENDEE);
		attendee.setParticipationStatus(ParticipationStatus.NEEDS_ACTION);
		attendee.setParticipationLevel(ParticipationLevel.REQUIRED);
		event.addAttendee(attendee);
		event.setSummary(tryItRoom.getRoomName());
		event.setSequence(0);
		/*  DateStart thisStart = new DateStart(new Date(2018, 9, 25, 18, 30, 30), true);*/
		// DateEnd dateEnd = new DateEnd(new Date(2018, 9, 24, 17, 30, 30), true);
		event.setDateStart(tryItRoom.getMeetingDate());
		biweekly.util.Duration duration = new biweekly.util.Duration.Builder().hours(Integer.parseInt(tryItRoom.getMeetingDuration())).build();
		event.setDuration(duration);
		biweekly.util.Duration reminder = new biweekly.util.Duration.Builder().minutes(5).build();
		Trigger trigger = new Trigger(reminder, Related.START);
		Action action = new Action("DISPLAY");
		VAlarm valarm = new VAlarm(action, trigger);
		valarm.setDescription("REMINDER");
		event.addAlarm(valarm);
		event.setUid(tryItRoom.getRoomNo());
		event.setOrganizer(fromUser.getEmail());
		event.setLocation(tryItRoom.getLocation());
		Recurrence recur = null;
		if (tryItRoom.getScheduleMeetingRecurringFlag() == CapvConstants.DAILY) {
			recur = new Recurrence.Builder(Frequency.DAILY).interval(1).until(tryItRoom.getEndDate()).build();
			RecurrenceRule recurrenceRule = new RecurrenceRule(recur);
			event.setRecurrenceRule(recurrenceRule);
		} else if (tryItRoom.getScheduleMeetingRecurringFlag() == CapvConstants.WEEKLY) {
			DayOfWeek[] dayOfWeeks = new DayOfWeek[] { DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
					DayOfWeek.FRIDAY, DayOfWeek.SATURDAY };
			recur = new Recurrence.Builder(Frequency.WEEKLY).byDay(dayOfWeeks[tryItRoom.getMeetingDate().getDay()]).interval(1)
					.until(tryItRoom.getEndDate()).build();
			RecurrenceRule recurrenceRule = new RecurrenceRule(recur);
			event.setRecurrenceRule(recurrenceRule);
		}
		ical.addEvent(event);
		ical.setMethod("REQUEST");
		String calendarContent = Biweekly.write(ical).go();
		calendarPart.addHeader("Content-Class", "urn:content-classes:calendarmessage");
		calendarPart.setContent(calendarContent, "text/calendar;method=CANCEL");
		return calendarPart;
	}

	private static BodyPart buildMessageBody(TryItRoom tryItRoom) {
		BodyPart messagePart = new MimeBodyPart();
		StringBuilder messageBody = new StringBuilder("<html><body>" + tryItRoom.getAgenda());
		messageBody.append("<br><br>Dear User," + "<br>" + "You have been invited to join the call by " + "<b>"
				+ tryItRoom.getUserName().substring(0, tryItRoom.getUserName().indexOf("_")) + "</b>" + "<br>" + "<br>" + "<br>"
				+ "<b>To join the Meeting:</b>" + "<br><br>" + "&nbsp;&nbsp;<b>Logging from Web :</b>" + "<br>"
				+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please open the below link on Chrome, Firefox or Opera"
				+ "<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\""
				+ CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_MEETING_URL)
				+ tryItRoom.getRoomNo() + "\">Click to Launch</a>" + "&nbsp;&nbsp;<br>" + "&nbsp;&nbsp;<br>");
		if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_ANDROID)
				.equalsIgnoreCase("enable")
				|| CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_IOS)
						.equalsIgnoreCase("enable")) {
			messageBody.append("&nbsp;&nbsp;<b>Logging From Mobile :</b>" + "<br>");
		}
		if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_ANDROID)
				.equalsIgnoreCase("enable")) {
			messageBody
					.append("&nbsp;&nbsp;&nbsp;<b>Android :</b>" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please open the below link on Default Browser or Chrome" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + CapvClientUserUtil
									.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_MEETING_URL)
							+ tryItRoom.getRoomNo() + "\">Click to Launch</a>" + "<br>");
		}
		if (CapvClientUserUtil.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_UI_CLIENT_IOS)
				.equalsIgnoreCase("enable")) {
			messageBody
					.append("&nbsp;&nbsp;&nbsp;<b>iOS:</b>" + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;If application is not installed , you will be requested to install the app ."
							+ "<br>" + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Please click the below link which will launch the app ." + "<br>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;" + "<a href=\"" + CapvClientUserUtil
									.getClientConfigProperty(Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_MEETING_URL)
							+ tryItRoom.getRoomNo() + "\">Click to Launch</a>" + "<br>");
		}
		messageBody.append("<br>" + "Note:&nbsp;This is an auto generated e-mail. Please do not reply to this mail" + "<br>"
				+ "For any support on issues, please send a mail to &nbsp;" + CapvClientUserUtil.getClientConfigProperty(
						Long.parseLong(tryItRoom.getClientId()), CapvClientUserConstants.CAPV_CLIENT_SUPPORT_EMAIL)
				+ "</body></html>");
		try {
			messagePart.setText(messageBody.toString());
			messagePart.setHeader("Content-Type", "text/html");
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		return messagePart;
	}
}

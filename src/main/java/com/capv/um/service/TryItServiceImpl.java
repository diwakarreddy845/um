package com.capv.um.service;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.model.TryItRoom;
import com.capv.um.repository.TryItRoomRepository;

@Service("tryItService")
@Transactional("transactionManager")
public class TryItServiceImpl implements TryItService {

	@Autowired
	TryItRoomRepository tryItRoomRepository;

	@Autowired
	EmailService emailService;

	static HttpClient httpClient = HttpClientBuilder.create().build();

	static HttpResponse serverResponse;

	@Override
	public void save(TryItRoom tryitroom) {
		tryItRoomRepository.save(tryitroom);
	}

	@Override
	public void update(TryItRoom tryitroom) {
		tryItRoomRepository.save(tryitroom);
	}

	@Override
	public List<TryItRoom> listRoomsByMatchingUserName(String userName, String clientId) {
		return tryItRoomRepository.listRoomsByMatchingUserName(userName, clientId);
	}

	@Override
	public TryItRoom fetchUniqueRoomRecord(String paramValue) {
		return tryItRoomRepository.findbyRoomNo(paramValue);
	}

	@Override
	public void delete(TryItRoom entity) {
		tryItRoomRepository.delete(entity);
		HttpURLConnection httpConnection = getHttpUrlConnection(
				CapvClientUserUtil.getClientConfigProperty(Long.parseLong(entity.getClientId()), CapvClientUserConstants.CAPV_TRYIT_URL)
						+ "/deleteTryItRoom/" + entity.getRoomNo());
		try {
			httpConnection.setRequestMethod("DELETE");
		} catch (ProtocolException e1) {
			e1.printStackTrace();
		}
		try {
			System.out.println(httpConnection.getResponseCode());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<TryItRoom> fetchAllRooms() {
		return tryItRoomRepository.findAll();
	}

	@Override
	public void sendMeetingICS(TryItRoom tryItRoom) throws Exception {
		emailService.sendMeetingICS(tryItRoom);
	}

	/**
	 * This method is used to initialize HttpURLConnection for a given url
	 * 
	 * @param url The url is used to initialize HttpURLConnection
	 * @return returns the HttpURLConnection for a given url
	 */
	private static HttpURLConnection getHttpUrlConnection(String url) {
		HttpURLConnection httpConnection = null;
		try {
			URL obj = new URL(url);
			httpConnection = (HttpURLConnection) obj.openConnection();
			// add request header
			httpConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return httpConnection;
	}
}

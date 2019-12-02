package com.capv.um.util;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;
import org.joda.time.DateTimeZone;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import com.capv.um.model.UserConfig;
import com.capv.um.security.UserRepositoryUserDetails;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;

public class CapvUtil {

	public static Environment environment;
	private static TokenStore tokenStore;

	public static String encodePassword(String password) {
		String encodedPassword = null;
		if (password != null) {
			encodedPassword = PasswordEncoderFactories.createDelegatingPasswordEncoder().encode(password);
		}
		return encodedPassword;
	}

	public static String get_SHA_512_SecurePassword(String passwordToHash, String salt) {
		String generatedPassword = null;
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-512");
			if (salt != null)
				md.update(salt.getBytes("UTF-8"));
			byte[] bytes = md.digest(passwordToHash.getBytes("UTF-8"));
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < bytes.length; i++) {
				sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
			}
			generatedPassword = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return generatedPassword;
	}

	public static void updateSettingsInUserSession(Long clientId, String userName, List<UserConfig> userSettings) {
		String webClientId = clientId + "@web";
		String mobileClientId = clientId + "@mobile";
		Collection<OAuth2AccessToken> webTokens = tokenStore.findTokensByClientIdAndUserName(webClientId, userName);
		for (OAuth2AccessToken webToken : webTokens) {
			try {
				OAuth2Authentication userAuthentication = tokenStore.readAuthentication(webToken);
				if (userAuthentication != null && userAuthentication.getUserAuthentication() != null
						&& userAuthentication.getUserAuthentication().getPrincipal() != null
						&& userAuthentication.getUserAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
					UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails) userAuthentication.getUserAuthentication().getPrincipal();
					userDetails.setUserConfig(userSettings);
				}
				OAuth2Authentication refreshTokenUserAuthentication = tokenStore.readAuthenticationForRefreshToken(webToken.getRefreshToken());
				if (refreshTokenUserAuthentication != null && refreshTokenUserAuthentication.getUserAuthentication() != null
						&& refreshTokenUserAuthentication.getUserAuthentication().getPrincipal() != null
						&& refreshTokenUserAuthentication.getUserAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
					UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails) refreshTokenUserAuthentication.getUserAuthentication()
							.getPrincipal();
					userDetails.setUserConfig(userSettings);
				}
			} catch (Exception e) {
			}
		}
		Collection<OAuth2AccessToken> mobileTokens = tokenStore.findTokensByClientIdAndUserName(mobileClientId, userName);
		for (OAuth2AccessToken mobileToken : mobileTokens) {
			try {
				OAuth2Authentication userAuthentication = tokenStore.readAuthentication(mobileToken);
				if (userAuthentication != null && userAuthentication.getUserAuthentication() != null
						&& userAuthentication.getUserAuthentication().getPrincipal() != null
						&& userAuthentication.getUserAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
					UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails) userAuthentication.getUserAuthentication().getPrincipal();
					userDetails.setUserConfig(userSettings);
				}
				OAuth2Authentication refreshTokenUserAuthentication = tokenStore.readAuthenticationForRefreshToken(mobileToken.getRefreshToken());
				if (refreshTokenUserAuthentication != null && refreshTokenUserAuthentication.getUserAuthentication() != null
						&& refreshTokenUserAuthentication.getUserAuthentication().getPrincipal() != null
						&& refreshTokenUserAuthentication.getUserAuthentication().getPrincipal() instanceof UserRepositoryUserDetails) {
					UserRepositoryUserDetails userDetails = (UserRepositoryUserDetails) refreshTokenUserAuthentication.getUserAuthentication()
							.getPrincipal();
					userDetails.setUserConfig(userSettings);
				}
			} catch (Exception e) {
			}
		}
	}

	public static void removeUserOAuthTokens(Long clientId, String userName) {
		String webClientId = clientId + "@web";
		String mobileClientId = clientId + "@mobile";
		Collection<OAuth2AccessToken> webTokens = tokenStore.findTokensByClientIdAndUserName(webClientId, userName);
		for (OAuth2AccessToken webToken : webTokens) {
			OAuth2RefreshToken refreshToken = webToken.getRefreshToken();
			tokenStore.removeAccessToken(webToken);
			tokenStore.removeRefreshToken(refreshToken);
		}
		Collection<OAuth2AccessToken> mobileTokens = tokenStore.findTokensByClientIdAndUserName(mobileClientId, userName);
		for (OAuth2AccessToken mobileToken : mobileTokens) {
			OAuth2RefreshToken refreshToken = mobileToken.getRefreshToken();
			tokenStore.removeAccessToken(mobileToken);
			tokenStore.removeRefreshToken(refreshToken);
		}
	}

	public static OAuth2Authentication getOAuth2AuthenticationByUser(Long clientId, String userName) {
		String webClientId = clientId + "@web";
		String mobileClientId = clientId + "@mobile";
		OAuth2Authentication oAuth2Authentication = null;
		OAuth2AccessToken oaAuth2AccessToken = null;
		Collection<OAuth2AccessToken> webTokens = tokenStore.findTokensByClientIdAndUserName(webClientId, userName);
		if (webTokens != null && webTokens.size() > 0) {
			for (OAuth2AccessToken webToken : webTokens) {
				oaAuth2AccessToken = webToken;
				break;
			}
		} else {
			Collection<OAuth2AccessToken> mobileTokens = tokenStore.findTokensByClientIdAndUserName(mobileClientId, userName);
			if (mobileTokens != null && mobileTokens.size() > 0) {
				for (OAuth2AccessToken mobileToken : mobileTokens) {
					oaAuth2AccessToken = mobileToken;
					break;
				}
			}
		}
		if (oaAuth2AccessToken != null) {
			OAuth2RefreshToken oaAuth2RefreshToken = oaAuth2AccessToken.getRefreshToken();
			oAuth2Authentication = tokenStore.readAuthenticationForRefreshToken(oaAuth2RefreshToken);
		}
		return oAuth2Authentication;
	}

	public static void setTokenStore(TokenStore tokenStore) {
		CapvUtil.tokenStore = tokenStore;
	}

	public static String getRandomString(int stringLength) {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < stringLength) {
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;
	}

	public static String encrypt(String key, String initVector, String value) {
		CryptLib cryptLib;
		try {
			cryptLib = new CryptLib();
			String encryptedString = cryptLib.encrypt(value, key, initVector);
			return encryptedString;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	public static String decrypt(String key, String initVector, String encrypted) {
		CryptLib cryptLib;
		try {
			cryptLib = new CryptLib();
			String decrytedString = cryptLib.decrypt(encrypted, key, initVector);
			return decrytedString;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/* public static void main(String[] args) {
	     String key = "Bar12345Bar12345"; // 128 bit key
	     String initVector = "RandomInitVector"; // 16 bytes IV
	
	     System.out.println(decrypt(key, initVector,
	             encrypt(key, initVector, "Hello World")));
	 }*/
	public static void main(String[] args) throws InvalidKeyException, InvalidAlgorithmParameterException {
		String key = "3b3c714572df4c6b91473d6f425a2d87";
		String initVector = "714572df4c6b9147";
		String value = "hmpbUIcIqTtNkWUj6PkKTBDc76IdHOnIm1Dap+wrqE7S0nYM914Ry3FlRCnubuL3882Wx6/Inwsj\r\n"
				+ "+AOACUn/y0qMz7tPHf+f+1d/MxKQ7yt4TqZzouvL3Xca5sEu2Nug";
		CryptLib cryptLib;
		try {
			// cryptLib = new CryptLib();
			// String encryptedString = cryptLib.encrypt(value, key, initVector);
			// System.out.println(encryptedString);
			// String decString = cryptLib.decrypt(value, key, initVector);
			String fire = "sharath_1_10068";;
			System.out.println(fire.substring(0, fire.lastIndexOf("_")));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static long getUTCTimeStamp() {
		long utc = 0;
		try {
			Date local = new Date();
			System.out.println("Local: " + local);
			DateTimeZone zone = DateTimeZone.getDefault();
			utc = zone.convertLocalToUTC(local.getTime(), false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return utc;
	}

	public static boolean getValidityCheck(int inactiveTime, Date toDate) {
		long HOUR = 3600 * 1000;
		Date maxDate = toDate;
		Date now = new Date(maxDate.getTime() + inactiveTime * HOUR);
		long diff = now.getTime() - maxDate.getTime();
		// long diffMinutes = diff / (60 * 60 * 1000); ;
		if (getUTCTimeStamp() > now.getTime())
			return true;
		else
			return false;
	}

	public static void pushCertificate(String token) {
		ApnsService service = APNS.newService().withCert("D:/CapvConfig/capvPushNotifications.p12", "hemanthr45").withSandboxDestination().build();
		// D:/CapvConfig/capvPushNotifications.p12
		// '{"aps":{"alert":{"body":"text"},"category":"capVnotification"}}
		String payload = APNS.newPayload().alertBody("capv").alertAction("capVnotification").category("asdf").build();
		// String token = "eed1304b cf310636 198113a2 dcbe91ee 7bf08678 0f95d22e d33e8b41 19ce0b7b";
		service.push(token, payload);
	}

	public static void setPropertiesEnvironment(Environment environment) {
		CapvUtil.environment = environment;
	}
}

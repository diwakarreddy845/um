package com.capv.client.user.chat.listener;

import com.capv.client.user.constants.CapvClientUserConstants.ChatClientAuthStatus;
/**
 * <h1> Chat client Authentication call back listener</h1>
 * 
 * This is used to listen the authentication call back event with chat server and process the user request after user authentication success.
 * 
 * @author ganesh.maganti
 * @version 1.0
 */

public interface CapvChatClientAuthenticationCallbackListener {
    /**
     * This method is used to process user request upon successful authentication with chat server
     * 
     * @param authStatus The status of user authentication success or fail
     * 
     */
	void processStatus(ChatClientAuthStatus authStatus);
}

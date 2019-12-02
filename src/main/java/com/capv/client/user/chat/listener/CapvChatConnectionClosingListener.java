package com.capv.client.user.chat.listener;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;

public interface CapvChatConnectionClosingListener {

	void processPreConnectionCloseRequest(XMPPTCPConnection chatUserConnection);
}

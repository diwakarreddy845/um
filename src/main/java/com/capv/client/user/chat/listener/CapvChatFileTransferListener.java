package com.capv.client.user.chat.listener;

import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;

import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;

import org.apache.commons.codec.binary.Base64;
import com.capv.client.user.chat.CapvChatUserMessageProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.google.gson.JsonObject;
/**
 * <h1>Listener for file transfer</h1>
 * 
 * File transfers can cause several events to be raised. These events can be
 * monitored through this interface.
 * 
 * @author narendra.muttevi
 * @version 1.0
 */
public class CapvChatFileTransferListener implements FileTransferListener {
	
	private String userName;
	
	/**
	 * this parameterized constructor is used to initialize the CapvChatManagerListener
	 * @param capvChatUserRequestProcessor
	 * 
	 */
	public CapvChatFileTransferListener(String userName){
		this.userName = userName;
	}

	/**
	 * This method used to process file transfer request and deliver file to receiver
	 * @param fileTransReq The request from the other user.
	 */
	@Override
	public void fileTransferRequest(FileTransferRequest fileTransReq) {
		
		IncomingFileTransfer transfer = fileTransReq.accept();
		
		System.out.println("file transfer listener activated");
	 	
	 	String encodedBase64 = null;
	 	
	 	FileInputStream fileInputStreamReader = null;
	 	 
	 	try {
	 			String tempFilePath = CapvClientUserUtil.getConfigProperty(
		    			   								CapvClientUserConstants.CAPV_CHAT_FILE_SEND_TEMP_URL_KEY);
	 			
	    	   	File recive=new File(tempFilePath + "/" + fileTransReq.getFileName());
	           	transfer.recieveFile(recive);
	           	Thread.sleep(4000);
	           	
	           	if(transfer.isDone()) {
	           		fileInputStreamReader = new FileInputStream(recive);
	           		byte[] chunk = new byte[16384];
	           		int chunkLen = 0;
	           		long currentChunk = 1;
	           		double fileSize = recive.length();
	           		long chunkSize = 16384;
	           		long totalChunks = (long) Math.ceil(fileSize/chunkSize);
	           		String file_id = UUID.randomUUID().toString();
	           		while ((chunkLen = fileInputStreamReader.read(chunk)) != -1) {
	           			encodedBase64 = new String(Base64.encodeBase64(chunk));
	           			JsonObject messageToSend = new JsonObject();
	           			messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
	           					CapvClientUserConstants.WS_MESSAGE_FILE_REQUEST);

	           			messageToSend.addProperty("incommingfile", fileTransReq.getRequestor());
	           			messageToSend.addProperty("filename", fileTransReq.getFileName());
	           			messageToSend.addProperty("mimetype", fileTransReq.getMimeType());
	           			messageToSend.addProperty("file", encodedBase64);
	           			messageToSend.addProperty("more", currentChunk!=totalChunks);
	           			messageToSend.addProperty("chunkNum", currentChunk);
	           			messageToSend.addProperty("fileId", file_id);
	           			String userMessage	= CapvClientUserUtil.convertToJsonString(messageToSend);

	           			CapvChatUserMessageProcessor.sendChatClientMessageToUser(userName, userMessage);
	           			currentChunk++;
	           			Thread.sleep(20);

	           		}
	           		System.out.println(fileTransReq.getRequestor()+"file name"+fileTransReq.getFileName()+"file sended successfully");
	           	}
	 	} catch (Exception e) {
	 		System.out.println("Exception"+e);
	 	} finally {
			if(fileInputStreamReader != null){
				try {
					fileInputStreamReader.close();
				} catch(Exception e){}
			}
		}

	}

}

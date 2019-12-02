package com.capv.client.user.chat.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.util.Map;

import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferNegotiator;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.si.packet.StreamInitiation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.codec.binary.Base64;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.chat.listener.CapvChatConnectionClosingListener;
import com.capv.client.user.chat.listener.CapvChatFileTransferListener;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.util.CapvClientUserUtil;
import com.google.gson.JsonObject;
/**
 * <h1>Handler for file sharing requests</h1>
 * 
 * This handler class is used to handle the file transfer requests between sender and receiver
 * @author caprus it
 * @version 1.0
 */
public class CapvFileTransferHandler {
	
	private String userName;
	private FileTransferManager fileTransferManger;
	private CapvChatConnectionClosingListener capvChatConnectionClosingListener;
	
	private static final Logger log = LoggerFactory.getLogger(CapvFileTransferHandler.class);
	/**
	 * This parameterized constructor is used to initialize the CapvFileTransferHandler 
	 * to handle the file transfer requests between sender and receiver
	 * @param capvChatUserRequestProcessor	The reference of CapvChatUserRequestProcessor
	 * 										@see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 * 
	 */
	@SuppressWarnings("unchecked")
	public CapvFileTransferHandler(String userName) {
		
		this.userName = userName;
		
		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		
		fileTransferManger = FileTransferManager.getInstanceFor(chatUserConnection);
		
		FileTransferListener fileTransferlst = new CapvChatFileTransferListener(userName);
        fileTransferManger.addFileTransferListener(fileTransferlst);
        
        capvChatConnectionClosingListener = (XMPPTCPConnection chatConnection) -> {
        										fileTransferManger.removeFileTransferListener(fileTransferlst);
        										chatConnection.unregisterIQRequestHandler(StreamInitiation.ELEMENT, 
        																					StreamInitiation.NAMESPACE, 
        																					IQ.Type.set);
        										
        										
												try {
													final Field ftm_INSTANCES = FileTransferManager.class.getDeclaredField("INSTANCES");
													ftm_INSTANCES.setAccessible(true);
													
													final Field ftn_INSTANCES = FileTransferNegotiator.class.getDeclaredField("INSTANCES");
													ftn_INSTANCES.setAccessible(true);
													
													final Map<XMPPConnection, FileTransferManager> ftm_INSTANCES_Map = 
																					(Map<XMPPConnection, FileTransferManager>) ftm_INSTANCES.get(null);
													ftm_INSTANCES_Map.remove(chatConnection);
													
													final Map<XMPPConnection, FileTransferNegotiator> ftn_INSTANCES_Map = 
																					(Map<XMPPConnection, FileTransferNegotiator>) ftn_INSTANCES.get(null);
													ftn_INSTANCES_Map.remove(chatConnection);
												} catch (Exception e) {
													log.error("Exception occured while CapvFileTransferHandler :",e);
												}
    										    
											};
				
		CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userName)
											.registerChatConnectionClosingListener(
														capvChatConnectionClosingListener);
	}
	
	/**
	 * 
	 * This method is used to send file
	 * 
	 * @param jsonMessageObj The message object with file info and file receiver details
	 * 
	 * @throws NotConnectedException if the connection is not established or broken with the server
	 */
	public void sendFile(JsonObject jsonMessageObj) throws NotConnectedException{
		
		String filename = jsonMessageObj.get("fileName").getAsString();
		String stream = jsonMessageObj.get("stream").getAsString();
		String name = jsonMessageObj.get("to").getAsString();
		boolean more = jsonMessageObj.get("more").getAsBoolean();
		int chunkNum = jsonMessageObj.get("chunkNum").getAsInt();
		OutgoingFileTransfer transfer;
		
		File file;

		XMPPTCPConnection chatUserConnection = CapvChatClientManagerRegistry.getChatUserConnectionByUserName(userName);
		CapvChatUserRequestProcessor capvChatUserRequestProcessor = CapvChatClientManagerRegistry.getCapvChatUserRequestProcessorByUserName(userName);
		
		if(chatUserConnection != null && capvChatUserRequestProcessor != null) {
			try {
				String tempFilePath = CapvClientUserUtil.getConfigProperty(
													CapvClientUserConstants.
													CAPV_CHAT_FILE_RECEIVE_TEMP_URL_KEY);
	   		 
				file = new File(tempFilePath + "/" + filename);
				if(chunkNum==1 && file.exists()){
					file.delete();
				}
				
				byte[] bytes = Base64.decodeBase64(stream);
				long length = bytes.length;
				FileOutputStream out = new FileOutputStream(file,true);
				
				for(int i=0;i<length;i++){
					out.write(bytes[i]);
				}
				out.flush();
				out.close();
				//FileUtils.writeByteArrayToFile( file, bytes );
				
				if(more==false){
					transfer = fileTransferManger.createOutgoingFileTransfer(name+"@"+chatUserConnection.getServiceName()+"/Smack");

					transfer.sendFile(file, "Incomming File");
				}
			} catch (Exception e) {
				log.error("Exception occured while sending file   :",e);
				//e.printStackTrace();
			}
		}
	}

}

package com.capv.um.service;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.apache.commons.codec.digest.DigestUtils;
import com.capv.client.user.UserRegistry;
import com.capv.client.user.UserSession;
import com.capv.client.user.chat.CapvChatClientManager;
import com.capv.client.user.chat.CapvChatClientManagerRegistry;
import com.capv.client.user.chat.CapvChatUserRequestProcessor;
import com.capv.client.user.constants.CapvClientUserConstants;
import com.capv.client.user.constants.CapvClientUserConstants.MessageType;
import com.capv.client.user.util.CapvClientUserUtil;
import com.capv.um.cache.UploadRequest;
import com.capv.um.exception.StorageException;
import com.capv.um.model.OfGroupArchive;
import com.capv.um.model.User;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Service("fileSystemStorageService")
public class FileSystemStorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(FileSystemStorageService.class);

    
    @Autowired
	private Environment environment;
	@Autowired
	private OfGroupArchiveService ofGroupArchiveService;
   
	@Autowired
	private UserService userService;

	@Autowired
	private FCMService fcmService;
	
	@Autowired
	private APNSService apnsService;
	
    @Override
    public void save(UploadRequest ur) {
     Path basePath=Paths.get(environment.getProperty("capv.chat.file.receive.temp.url"));
        if (ur.getFile()==null) {
            throw new StorageException(String.format("File with uuid = [%s] is empty", ur.getUuid().toString()));
        }
       
        ByteArrayInputStream bis = new ByteArrayInputStream(ur.getFile());
        Path targetFile;
        if (ur.getPartIndex() > -1) {
            targetFile = basePath.resolve(ur.getUuid()).resolve(String.format("%s_%05d", ur.getUuid(), ur.getPartIndex()));
        } else {
            targetFile = basePath.resolve(ur.getUuid()).resolve(ur.getFileName());
        }
        try {
            Files.createDirectories(targetFile.getParent());
            Files.copy(bis, targetFile);
        } catch (IOException e) {
            String errorMsg = String.format("Error occurred when saving file with uuid = [%s]", e);
            log.error(errorMsg, e);
            throw new StorageException(errorMsg, e);
        }

    }

  

    @Override
    public void mergeChunks(String uuid, String fileName, int totalParts, long totalFileSize,String occupants,String type,String userName,String checkSum) {
    	 		Path basePath=Paths.get(environment.getProperty("capv.chat.file.receive.temp.url"));
    	   		File targetFile = basePath.resolve(uuid).resolve(fileName).toFile();
    	   
    	   		try (FileChannel dest = new FileOutputStream(targetFile, true).getChannel()) {
    	   			for (int i = 1; i <= totalParts; i++) {
    	   				File sourceFile = basePath.resolve(uuid).resolve(String.format("%s_%05d", uuid, i)).toFile();
    	   				try (FileChannel src = new FileInputStream(sourceFile).getChannel()) {
                    dest.position(dest.size());
                    src.transferTo(0, src.size(), dest);
                }
                sourceFile.delete();
            }
    	   			if (checkSum.equals(checkSumApacheCommons(basePath+"/"+uuid+"/"+fileName))) {
                 shareFileToGroup(fileName,type,occupants,userName,uuid);
    	   	       }else {
     	   		String errorMsg = String.format("CheckSum not matching", uuid);
     	   		throw new StorageException(errorMsg);
         }
        	} catch (IOException e) {
            String errorMsg = String.format("Error occurred when merging chunks for uuid = [%s]", uuid);
            log.error(errorMsg, e);
            
            throw new StorageException(errorMsg, e);
        }	
    }
    public  void shareFileToGroup(String fileName,String type ,String occupants, String userName,String uuid) {

		try {
				JsonObject messageToSend = new JsonObject();
			
				messageToSend.addProperty("incommingfile", fileName);
				messageToSend.addProperty("filename",fileName );
				messageToSend.addProperty("mimetype", userName);
				messageToSend.addProperty("fileSenderUsername", userName);
				messageToSend.addProperty("url", "/getFile?fileName");
				messageToSend.addProperty("fileId", uuid);
				List<UserSession> userSessions = UserRegistry.getUserSessionsByUserName(userName);
				User user_client_id=userService.getByUserName(userName, false);
				String serviceName=CapvClientUserUtil.getClientConfigProperty(user_client_id.getClientId(), CapvClientUserConstants.CHAT_SERVER_SERVICE_KEY);
				 if(userSessions!=null) {
			       if(type.equals("group")) {
			    	   String room = occupants;
			    	   messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
								CapvClientUserConstants.WS_MESSAGE_GROUP_FILE_REQUEST);
			    	   String userMessage	= CapvClientUserUtil.convertToJsonString(messageToSend);
	    				CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
													getCapvChatUserRequestProcessor(userSessions.get(0));
		    			//User user_client_id=userService.getByUserName(userName, false);
		    			String  roomName = room + "@conference." + serviceName;
		    			String  toJid=userName+"@"+serviceName;
			         	long currentTimestamp = System.currentTimeMillis();
			         	OfGroupArchive groupArchiveMessage =new OfGroupArchive();
			         	groupArchiveMessage.setBody(userMessage);
			         	groupArchiveMessage.setFromJID(toJid);
			         	groupArchiveMessage.setFromJIDResource("Smack");
			         	groupArchiveMessage.setToJID(roomName);
			         	groupArchiveMessage.setToJIDResource(userName);
			         	groupArchiveMessage.setSentDate(currentTimestamp);
			         	groupArchiveMessage.setIsEdited(0);
			         	groupArchiveMessage.setIsDeleted(0);
			         	groupArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());;
			         	ofGroupArchiveService.save(groupArchiveMessage);
			         	capvChatUserRequestProcessor.sendGroupMessage(room, userMessage);
			         	String[] grpMembers=occupants.split(",");
			         	
			         	JsonObject fcmToSend = new JsonObject();
	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_RECEIVE);
	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, userName);
	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, userMessage);
	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_FILE_SHARE, CapvClientUserConstants.WS_MESSAGE_FILE_REQUEST);
	   					for(int i=0;i<grpMembers.length;i++) {
	   						com.capv.um.model.User remoteUser = new com.capv.um.model.User();
	   						remoteUser = userService.getByUserName(grpMembers[i], false);
		        				if(remoteUser.getLastSigninOs()!=null&&remoteUser.getLastSigninOs().equals("ios")){
                                apnsService.pushCallNotification(remoteUser.getTokenId(),fcmToSend.toString(),remoteUser.getClientId());
		        				}
							else if(remoteUser.getLastSigninOs()!=null&&remoteUser.getLastSigninOs().equals("android")){
								fcmService.sendMessage(remoteUser.getTokenId(),fcmToSend,remoteUser.getClientId());
							}
			         	}
		        		
			       }else {
			    	   messageToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, 
								CapvClientUserConstants.WS_MESSAGE_FILE_REQUEST);
			    		   CapvChatUserRequestProcessor capvChatUserRequestProcessor = 
								getCapvChatUserRequestProcessor(userSessions.get(0));
			    	   		if(capvChatUserRequestProcessor != null) {
			    	   			String userMessage	= CapvClientUserUtil.convertToJsonString(messageToSend);
			    	   			String receiver = occupants;
			    	   			if(userMessage != null && receiver != null) {
			    	   				long currentTimestamp = System.currentTimeMillis();
						         	OfGroupArchive groupArchiveMessage =new OfGroupArchive();
						         	groupArchiveMessage.setBody(userMessage);
						         	groupArchiveMessage.setFromJID(userName+"@"+serviceName);
						         	groupArchiveMessage.setFromJIDResource("Smack");
						         	groupArchiveMessage.setToJID(receiver+"@"+serviceName);
						         	groupArchiveMessage.setToJIDResource(userName);
						         	groupArchiveMessage.setIsEdited(0);
						         	groupArchiveMessage.setIsDeleted(0);
						         	groupArchiveMessage.setSentDate(currentTimestamp);
						         	groupArchiveMessage.setMessage_type(MessageType.MESSAGE.getTypeId());;
						         	ofGroupArchiveService.save(groupArchiveMessage);
			    	   					capvChatUserRequestProcessor.sendMessage(userMessage, receiver);
			    	   					
			    	   					JsonObject fcmToSend = new JsonObject();
			    	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_ID_KEY, CapvClientUserConstants.WS_MESSAGE_MESSAGE_RECEIVE);
			    	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_SENDER_KEY, userName);
			    	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_CHAT_MESSAGE_KEY, userMessage);
			    	   					fcmToSend.addProperty(CapvClientUserConstants.WS_MESSAGE_FILE_SHARE, CapvClientUserConstants.WS_MESSAGE_FILE_REQUEST);
			    	   					com.capv.um.model.User remoteUser = new com.capv.um.model.User();
			    	   					remoteUser = userService.getByUserName(receiver, false);
			    		        				if(remoteUser.getLastSigninOs()!=null&&remoteUser.getLastSigninOs().equals("ios")){
			                                    apnsService.pushCallNotification(remoteUser.getTokenId(),fcmToSend.toString(),remoteUser.getClientId());
			    		        				}
			    							else if(remoteUser.getLastSigninOs()!=null&&remoteUser.getLastSigninOs().equals("android")){
			    								fcmService.sendMessage(remoteUser.getTokenId(),fcmToSend,remoteUser.getClientId());
			    							}
			    	   					
			    	   			}
			    	   		}
			    	   	 }
			       }	
			}

		catch(Throwable e){
			e.printStackTrace();
		}
		}
	/**
	 * This method used to get CapvChatUserRequestProcessor to process 
	 * user friend requests, group handling and chat messages processing
	 * 
	 * 
	 * @param userSession	This parameter is require to get associated CapvChatUserRequestProcessor instance
	 * @return CapvChatUserRequestProcessor This return CapvChatUserRequestProcessor to process user request
	 * @see com.capv.client.user.chat.CapvChatUserRequestProcessor
	 */
	private CapvChatUserRequestProcessor getCapvChatUserRequestProcessor(UserSession userSession) {
		
		CapvChatUserRequestProcessor capvChatUserRequestProcessor = null;
		
		if(userSession != null && userSession.getUserName() != null && 
				CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userSession.getUserName()) != null) {
			
			CapvChatClientManager capvChatClientManager = CapvChatClientManagerRegistry.getCapvChatClientManagerByUser(userSession.getUserName());
			
			capvChatUserRequestProcessor = (capvChatClientManager.getCapvChatUserRequestProcessor() != null) ? 
														capvChatClientManager.getCapvChatUserRequestProcessor() : null;
		}
			
		return capvChatUserRequestProcessor;
	}
	
	public  String checkSumApacheCommons(String file){
        String checksum = null;
        try {  
            checksum = DigestUtils.md5Hex(new FileInputStream(file));
        } catch (IOException ex) {
        	   ex.printStackTrace();
        }
        return checksum;
    }
}
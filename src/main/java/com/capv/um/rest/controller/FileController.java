package com.capv.um.rest.controller;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.apache.commons.codec.binary.Base64;
import com.capv.um.cache.UploadRequest;
import com.capv.um.cache.UploadResponse;
import com.capv.um.exception.StorageException;
import com.capv.um.service.FileSystemStorageService;


@RestController
public class FileController {

	@Autowired
   FileSystemStorageService FileSystemStorageService;
	
	private TokenStore tokenStore;
   
    @Autowired
   	private Environment environment;
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/uploads",produces = { "application/json"},method = RequestMethod.POST) 
    public ResponseEntity<UploadResponse> upload(
            @RequestParam("qqfile") String file,
            @RequestParam("qquuid") String uuid,
            @RequestParam("qqfilename") String fileName,
            @RequestParam(value = "qqpartindex", required = false, defaultValue = "-1") int partIndex,
            @RequestParam(value = "qqtotalparts", required = false, defaultValue = "-1") int totalParts,
            @RequestParam(value = "qqtotalfilesize", required = false, defaultValue = "-1") long totalFileSize,@RequestParam(value = "occupants") String occupants,
            @RequestParam(value = "type") String type,@RequestParam(value = "userName") String userName,@RequestParam(value = "checkSum") String checkSum) {

    	    byte[] file_path=Base64.decodeBase64(file);
        UploadRequest request = new UploadRequest(uuid, file_path);
        request.setFileName(fileName);
        request.setTotalFileSize(totalFileSize);
        request.setPartIndex(partIndex);
        request.setTotalParts(totalParts);

        FileSystemStorageService.save(request);
        
        if(partIndex==totalParts) {
        		FileSystemStorageService.mergeChunks(uuid, fileName, totalParts,totalFileSize,occupants,type,userName,checkSum);
        		return ResponseEntity.ok().body(new UploadResponse("Successfully Uploaded",true));
        }else {
         	return ResponseEntity.ok().body(new UploadResponse(true));
        }
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<UploadResponse> handleException(StorageException ex) {
        UploadResponse response = new UploadResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
   
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/chunksdone", method = RequestMethod.POST) 
    public ResponseEntity<UploadResponse> chunksDone(
            @RequestParam("qquuid") String uuid,
            @RequestParam("qqfilename") String fileName,
            @RequestParam(value = "qqtotalparts") int totalParts,
            @RequestParam(value = "qqtotalfilesize") long totalFileSize, @RequestParam(value = "occupants") String occupants,@RequestParam(value = "type") String type,@RequestParam(value = "userName") String userName,@RequestParam(value = "checkSum") String checkSum) {

     	FileSystemStorageService.mergeChunks(uuid, fileName, totalParts, totalFileSize,occupants,type,userName,checkSum);

        return ResponseEntity.ok().body(new UploadResponse(true));
    }
    
    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/getFile", produces = { MediaType.APPLICATION_OCTET_STREAM_VALUE}, method = RequestMethod.GET)
    public void getFile(@RequestParam(value="fileName", required=false) String fileName, 
    										HttpServletRequest request,HttpServletResponse response) throws IOException, Exception
    {
		String path=environment.getProperty("capv.chat.file.receive.temp.url");
	        byte[] reportBytes = null;
	        File result=new File(path+"/"+fileName);

	        if(result.exists()){
	            InputStream inputStream = new FileInputStream(path+"/"+fileName); 
	            
	            String type = Files.probeContentType(result.toPath());
	            response.setHeader("Content-Disposition", "attachment; filename=" + result.getName());
	            response.setHeader("Content-Type",type);

	            reportBytes=new byte[100];//New change
	            OutputStream os=response.getOutputStream();//New change
	            int read=0;
	            while((read=inputStream.read(reportBytes))!=-1){
	                os.write(reportBytes,0,read);
	            }
	            
	            os.flush();
	            os.close();
	            inputStream.close();
	        }

    }
    
}
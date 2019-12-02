package com.capv.um.service;

import com.capv.um.cache.UploadRequest;

/**
 * Created by ovaldez on 11/13/16.
 */
public interface StorageService {

    void save(UploadRequest uploadRequest);



    void mergeChunks(String uuid, String fileName, int totalParts, long totalFileSize,String occupents,String type,String userName,String checkSum);

}
package com.capv.um.cache;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UploadResponse {

    @JsonProperty("error")
    private String errorMsg;
    @JsonProperty("status")
    private String status;
    
    public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}

	private boolean success;

    public UploadResponse(boolean success) {
        this.success = success;
    }
    public UploadResponse(String status,boolean success) {
        this.status = status;
        this.success =success;
    }
    public UploadResponse(boolean success, String errorMsg) {
        this.errorMsg = errorMsg;
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
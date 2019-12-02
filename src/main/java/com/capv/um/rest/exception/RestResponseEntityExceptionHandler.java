package com.capv.um.rest.exception;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.capv.um.exception.ClientConfigurationException;
import com.capv.um.exception.UserAlreadyExistException;
import com.capv.um.util.ServiceStatus;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    public RestResponseEntityExceptionHandler() {
        super();
    }

    // API

    // 400
    @Override
    protected ResponseEntity<Object> handleBindException(final BindException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final BindingResult result = ex.getBindingResult();
        final ServiceStatus<String> bodyOfResponse = new ServiceStatus<String>("Bad Request", result.getFieldErrors(), result.getGlobalErrors());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(final MethodArgumentNotValidException ex, final HttpHeaders headers, final HttpStatus status, final WebRequest request) {
        final BindingResult result = ex.getBindingResult();
        final ServiceStatus<String> bodyOfResponse = new ServiceStatus<String>("Bad Request", result.getFieldErrors(), result.getGlobalErrors());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }

    // 400
    @ExceptionHandler({ ClientConfigurationException.class })
    public ResponseEntity<Object> handleClientConfigurationException(final RuntimeException ex, final WebRequest request) {
        final ServiceStatus<Object> bodyOfResponse = new ServiceStatus<Object>("Bad Request", ex.getMessage());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
    
    // 409
    @ExceptionHandler({ UserAlreadyExistException.class })
    public ResponseEntity<Object> handleUserAlreadyExist(final RuntimeException ex, final WebRequest request) {
        final ServiceStatus<Object> bodyOfResponse = new ServiceStatus<Object>("failure", ex.getMessage());
        return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.CONFLICT, request);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> exception(Exception ex, final WebRequest request) {
    	ServiceStatus<Object> bodyOfResponse = new ServiceStatus<Object>("Error", "Server error. Please contact Administrator");
    	return handleExceptionInternal(ex, bodyOfResponse, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

}

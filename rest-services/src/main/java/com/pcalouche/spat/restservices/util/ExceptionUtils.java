package com.pcalouche.spat.restservices.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pcalouche.spat.restservices.api.ClientCode;
import com.pcalouche.spat.restservices.api.exception.RestResourceNotFoundException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class ExceptionUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode buildJsonErrorObject(Exception e, HttpServletRequest request) {
        ObjectNode errorObjectNode = objectMapper.createObjectNode();
        errorObjectNode.put("timestamp", System.currentTimeMillis());
        if (request != null) {
            errorObjectNode.put("path", request.getRequestURI());
        }
        HttpStatus status = getHttpStatusForException(e);
        errorObjectNode.put("status", status.value());
        errorObjectNode.put("error", status.getReasonPhrase());
        errorObjectNode.put("exception", e.getClass().getName());
        if (e instanceof MethodArgumentNotValidException) {
            errorObjectNode.put("message", "See validation messages for more details.");
            // Make the message output more meaningful by providing a map of error codes and messages.
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) e;
            List<ObjectError> objectErrors = methodArgumentNotValidException.getBindingResult().getAllErrors();
            HashMap<String, String> errorMap = new HashMap<>();
            objectErrors.forEach(objectError -> errorMap.put(objectError.getCode(), objectError.getDefaultMessage()));
            errorObjectNode.put("validationMessages", objectMapper.valueToTree(errorMap));
        } else {
            errorObjectNode.put("message", e.getMessage());
        }
        ClientCode clientCode = ClientCode.fromException(e);
        if (clientCode != null) {
            errorObjectNode.put("clientCode", clientCode.name());
        }
        return errorObjectNode;
    }

    public static HttpStatus getHttpStatusForException(Exception e) {
        if (e instanceof AuthenticationException || e instanceof JwtException) {
            return HttpStatus.UNAUTHORIZED;
        } else if (e instanceof AccessDeniedException) {
            return HttpStatus.FORBIDDEN;
        } else if (e instanceof HttpMessageConversionException ||
                e instanceof MethodArgumentNotValidException ||
                e instanceof MethodArgumentTypeMismatchException) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        } else if (e instanceof RestResourceNotFoundException) {
            return HttpStatus.NOT_FOUND;
        } else {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }

    public static void writeExceptionToResponse(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(getHttpStatusForException(e).value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ExceptionUtils.buildJsonErrorObject(e, request));
    }
}
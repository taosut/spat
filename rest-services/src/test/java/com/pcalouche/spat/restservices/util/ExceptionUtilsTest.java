package com.pcalouche.spat.restservices.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pcalouche.spat.restservices.api.dto.TeamDto;
import com.pcalouche.spat.restservices.api.exception.RestResourceNotFoundException;
import com.pcalouche.spat.shared.AbstractUnitTest;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class ExceptionUtilsTest extends AbstractUnitTest {
    @Test
    public void testHttpStatusForAuthenticationException() {
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(new BadCredentialsException("message"));
        assertThat(httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testHttpStatusForJwtException() {
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(new ExpiredJwtException(null, null, "message"));
        assertThat(httpStatus).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testHttpStatusForAccessDeniedException() {
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(new AccessDeniedException("message"));
        assertThat(httpStatus).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void testHttpStatusForMethodHttpMessageConversionException() {
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(new HttpMessageConversionException("message"));
        assertThat(httpStatus).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testHttpStatusForMethodArgumentNotValidException() throws NoSuchMethodException {
        MethodArgumentNotValidException methodArgumentNotValidException = new MethodArgumentNotValidException(
                new MethodParameter(getClass().getDeclaredMethod("testHttpStatusForMethodArgumentNotValidException"), -1),
                new BindException(new TeamDto(), "bad team argument")
        );
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(methodArgumentNotValidException);
        assertThat(httpStatus).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testHttpStatusForMethodMethodArgumentTypeMismatchException() throws NoSuchMethodException {
        MethodArgumentTypeMismatchException methodArgumentTypeMismatchException = new MethodArgumentTypeMismatchException(
                null,
                null,
                "name",
                new MethodParameter(getClass().getDeclaredMethod("testHttpStatusForMethodArgumentNotValidException"), -1),
                new RuntimeException("bad argument"));
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(methodArgumentTypeMismatchException);
        assertThat(httpStatus).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @Test
    public void testHttpStatusForRestResourceNotFoundException() {
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(new RestResourceNotFoundException("message"));
        assertThat(httpStatus).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void testHttpStatusForAllOtherExceptions() {
        HttpStatus httpStatus = ExceptionUtils.getHttpStatusForException(new RuntimeException("message"));
        assertThat(httpStatus).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void testWriteExceptionToResponse() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/request-path");
        AuthenticationException authenticationException = new BadCredentialsException("bad credentials");

        ObjectNode expectedObjectNode = (ObjectNode) objectMapper.readTree(ExceptionUtils.buildJsonErrorObject(authenticationException, request).toString());
        // Remove timestamp for easier comparision
        expectedObjectNode.remove("timestamp");

        ExceptionUtils.writeExceptionToResponse(authenticationException, request, response);

        ObjectNode actualObjectNode = (ObjectNode) objectMapper.readTree(response.getContentAsString());

        // Check timestamp is not null
        assertThat(actualObjectNode.get("timestamp"))
                .isNotNull();

        // Remove timestamp for easier comparision
        actualObjectNode.remove("timestamp");

        assertThat(actualObjectNode).isEqualTo(expectedObjectNode);
    }
}

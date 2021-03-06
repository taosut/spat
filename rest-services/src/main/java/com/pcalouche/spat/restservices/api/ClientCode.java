package com.pcalouche.spat.restservices.api;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public enum ClientCode {
    BAD_CREDENTIALS,
    ACCOUNT_EXPIRED,
    ACCOUNT_CREDENTIALS_EXPIRED,
    ACCOUNT_LOCKED,
    ACCOUNT_DISABLED,
    EXPIRED_TOKEN,
    INVALID_TOKEN;

    public static ClientCode fromException(Exception e) {
        ClientCode clientCode = null;
        // Handle AuthenticationException cases
        if (e instanceof AuthenticationException) {
            if (e instanceof BadCredentialsException || e instanceof UsernameNotFoundException) {
                clientCode = ClientCode.BAD_CREDENTIALS;
            }
            if (e instanceof AccountExpiredException) {
                clientCode = ClientCode.ACCOUNT_EXPIRED;
            }
            if (e instanceof CredentialsExpiredException) {
                clientCode = ClientCode.ACCOUNT_CREDENTIALS_EXPIRED;
            }
            if (e instanceof LockedException) {
                clientCode = ClientCode.ACCOUNT_LOCKED;
            }
            if (e instanceof DisabledException) {
                clientCode = ClientCode.ACCOUNT_DISABLED;
            }
        }
        if (e instanceof JwtException) {
            if (e instanceof ExpiredJwtException) {
                clientCode = ClientCode.EXPIRED_TOKEN;
            } else {
                clientCode = ClientCode.INVALID_TOKEN;
            }
        }
        return clientCode;
    }
}

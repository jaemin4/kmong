package com.kmong.support.exception;

import com.kmong.support.response.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public com.kmong.support.response.APIResponse<Object> handleBindException(BindException e) {
        String message = e.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[BindException] {}", message);
        log.debug("[BindException stack]", e);
        return APIResponse.fail(HttpStatus.BAD_REQUEST.value(), message);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("[HttpMessageNotReadableException] {}", e.getMessage());
        log.debug("[HttpMessageNotReadableException stack]", e);
        return APIResponse.fail(HttpStatus.BAD_REQUEST.value(), "요청 본문을 읽을 수 없습니다.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public APIResponse<Object> handleMethodNotAllowed(HttpRequestMethodNotSupportedException e) {
        log.warn("[HttpRequestMethodNotSupportedException] {}", e.getMethod());
        log.debug("[HttpRequestMethodNotSupportedException stack]", e);
        return APIResponse.fail(HttpStatus.METHOD_NOT_ALLOWED.value(), "지원하지 않는 HTTP 메서드입니다: " + e.getMethod());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public APIResponse<Object> handleUnsupportedMediaType(HttpMediaTypeNotSupportedException e) {
        log.warn("[HttpMediaTypeNotSupportedException] {}", e.getContentType());
        log.debug("[HttpMediaTypeNotSupportedException stack]", e);
        return APIResponse.fail(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(), "지원하지 않는 미디어 타입입니다.");
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public APIResponse<Object> handleAuthenticationException(AuthenticationException e) {
        log.warn("[AuthenticationException] {}", e.getMessage());
        log.debug("[AuthenticationException stack]", e);
        return APIResponse.fail(HttpStatus.UNAUTHORIZED.value(), "인증에 실패했습니다.");
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public APIResponse<Object> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("[AccessDeniedException] {}", e.getMessage());
        log.debug("[AccessDeniedException stack]", e);
        return APIResponse.fail(HttpStatus.FORBIDDEN.value(), "접근이 거부되었습니다.");
    }

/*
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public APIResponse<Object> handleUsernameNotFountException(UsernameNotFoundException e) {
        log.warn("[UsernameNotFoundException] {}", e.getMessage());
        log.debug("[UsernameNotFoundException stack]", e);
        return APIResponse.fail(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }
*/

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public APIResponse<Object> handleNotFound(NoHandlerFoundException e) {
        log.warn("[NoHandlerFoundException] {}", e.getRequestURL());
        log.debug("[NoHandlerFoundException stack]", e);
        return APIResponse.fail(HttpStatus.NOT_FOUND.value(), "요청한 경로를 찾을 수 없습니다.");
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public APIResponse<Object> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String causeMsg = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();
        log.warn("[DataIntegrityViolationException] {}", causeMsg);
        log.debug("[DataIntegrityViolationException stack]", e);
        return APIResponse.fail(HttpStatus.CONFLICT.value(), "데이터 무결성 오류: " + causeMsg);
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public APIResponse<Object> handleRuntimeException(RuntimeException e) {
        log.warn("[RuntimeException] {}", e.getMessage());
        log.debug("[RuntimeException stack]", e);
        return APIResponse.fail(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public APIResponse<Object> handleException(Exception e) {
        log.error("[Exception] {}", e.getMessage());
        log.debug("[Exception stack]", e);
        return APIResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다.");
    }
}

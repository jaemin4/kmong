package com.kmong.support.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
public class APIResponse<T> {
    private final Integer code;
    private final String message;

    @JsonInclude(NON_NULL)
    private final T data;

    private APIResponse(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> APIResponse<T> success(T data) {
        return new APIResponse<>(
                HttpStatus.OK.value(),
                HttpStatus.OK.getReasonPhrase(),
                data
        );
    }

    public static <T> APIResponse<T> message(String message) {
        return new APIResponse<>(
                HttpStatus.OK.value(),
                message,
                null
        );
    }

    public static <T> APIResponse<T> success(String message, T data) {
        return new APIResponse<>(
                HttpStatus.OK.value(),
                message,
                data
        );
    }



    public static <T> APIResponse<T> success() {
        return success(null);
    }
    public static <T> APIResponse<T> fail(Integer code, String message) {
        return new APIResponse<>(code, message, null);
    }


}

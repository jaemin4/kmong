package com.kmong.support.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
public class APIPagingResponse<T, P> {
    private final Integer code;
    private final String message;

    @JsonInclude(NON_NULL)
    private final T data;

    @JsonInclude(NON_NULL)
    private final P paging;

    private APIPagingResponse(Integer code, String message, T data, P paging) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.paging = paging;
    }

    /* ---------- 성공 (데이터 + 페이징) ---------- */
    public static <T, P> APIPagingResponse<T, P> success(String message, T data, P paging) {
        return new APIPagingResponse<>(
                HttpStatus.OK.value(),
                message,
                data,
                paging
        );
    }

    /* ---------- 실패 ---------- */
    public static <T, P> APIPagingResponse<T, P> fail(Integer code, String message) {
        return new APIPagingResponse<>(code, message, null, null);
    }
}
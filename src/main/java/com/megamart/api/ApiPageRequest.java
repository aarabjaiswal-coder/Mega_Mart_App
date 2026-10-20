package com.megamart.api;

import com.megamart.exception.ApiException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;

public final class ApiPageRequest {
    private ApiPageRequest() { }

    public static PageRequest of(int page, int size, Sort sort) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Page must be non-negative and size must be from 1 to 100.");
        }
        return PageRequest.of(page, size, sort);
    }
}

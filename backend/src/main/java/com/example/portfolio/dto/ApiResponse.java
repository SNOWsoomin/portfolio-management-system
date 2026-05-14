package com.example.portfolio.dto;

public record ApiResponse<T>(boolean success, T data, String message, String error) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, "요청이 성공했습니다.", null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static ApiResponse<Void> fail(String error) {
        return new ApiResponse<>(false, null, null, error);
    }
}

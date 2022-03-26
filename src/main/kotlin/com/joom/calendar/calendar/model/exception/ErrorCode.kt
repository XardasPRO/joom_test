package com.joom.calendar.calendar.model.exception

enum class ErrorCode (
    val code: Int
) {
    AUTH_EMPTY_COOKIES (1000),
    AUTH_COOKIE_IS_NOT_EXIST(1001),
    AUTH_TOKEN_IS_EXPIRED(1002),
    AUTH_WRONG_PASSWORD(1003),


    VALIDATION_ERROR_EMPTY_FIELD(2000),
    VALIDATION_ERROR_USED_VALUE(2001),
    VALIDATION_ERROR_INVALID_VALUE(2002),


    USER_IS_NOT_FOUND(3000),
}
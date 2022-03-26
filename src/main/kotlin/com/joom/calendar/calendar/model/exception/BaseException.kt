package com.joom.calendar.calendar.model.exception

abstract class BaseException(
    message: String?,
    val errorCode: ErrorCode
): RuntimeException(message) {
}
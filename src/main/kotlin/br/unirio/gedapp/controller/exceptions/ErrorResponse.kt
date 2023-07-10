package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import java.util.*

class ErrorResponse(
    var timestamp: Date,
    var status: Int,
    var error: HttpStatus,
    var message: String? = null,
    var i18nMsgKey: String? = null
) {
    constructor(
        status: HttpStatus,
        exception: Exception,
        i18nMsgKey: String? = null
    ) : this(
        timestamp = Date(),
        status = status.value(),
        error = status,
        message = exception.message,
        i18nMsgKey = i18nMsgKey
    )
}
package br.unirio.gedapp.controller.exceptions

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {

    @ExceptionHandler
    fun handleCategoryHasChildrenException(ex: CategoryHasChildrenException) =
        ErrorResponse(
            status = HttpStatus.PRECONDITION_FAILED,
            exception = ex,
            i18nMsgKey = "exception.categoryHasChildren"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleCategoryHasDocumentsException(ex: CategoryHasDocumentsException) =
        ErrorResponse(
            status = HttpStatus.PRECONDITION_FAILED,
            exception = ex,
            i18nMsgKey = "exception.categoryHasDocuments"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleLastRemainingDeptManagerException(ex: LastRemainingDeptManagerException) =
        ErrorResponse(
            status = HttpStatus.NOT_ACCEPTABLE,
            exception = ex,
            i18nMsgKey = "exception.lastRemainingDeptManager"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleLastRemainingSystemManagerException(ex: LastRemainingSystemManagerException) =
        ErrorResponse(
            status = HttpStatus.PRECONDITION_FAILED,
            exception = ex,
            i18nMsgKey = "exception.lastRemainingSystemManager"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleResourceNotFoundException(ex: ResourceNotFoundException) =
        ErrorResponse(
            status = HttpStatus.NOT_FOUND,
            exception = ex,
            i18nMsgKey = "exception.resourceNotFound"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleUnauthorizedException(ex: UnauthorizedException) =
        ErrorResponse(
            status = HttpStatus.UNAUTHORIZED,
            exception = ex,
            i18nMsgKey = "exception.unauthorized"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleUnnamedCategoryException(ex: UnnamedCategoryException) =
        ErrorResponse(
            status = HttpStatus.PRECONDITION_FAILED,
            exception = ex,
            i18nMsgKey = "exception.unnamedCategory"
        ).let { ResponseEntity(it, it.error) }

    @ExceptionHandler
    fun handleUserAlreadyInvitedException(ex: UserAlreadyInvitedException) =
        ErrorResponse(
            status = HttpStatus.NOT_ACCEPTABLE,
            exception = ex,
            i18nMsgKey = "exception.userAlreadyInvited"
        ).let { ResponseEntity(it, it.error) }
}
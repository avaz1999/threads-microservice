package uz.avaz.user

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*

sealed class UserServiceException(message: String? = null) : RuntimeException(message) {
    abstract fun errorType(): ErrorCode

    fun getErrorMessage(errorMessageSource: ResourceBundleMessageSource, vararg array: Any?): BaseMessage {
        return BaseMessage(
            errorType().code,
            errorMessageSource.getMessage(
                errorType().toString(),
                array,
                Locale(LocaleContextHolder.getLocale().language)
            )
        )
    }
}
class UserNotFoundException : UserServiceException(){
    override fun errorType() = ErrorCode.USER_NOT_FOUND
}
class PasswordErrorException: UserServiceException(){
    override fun errorType() = ErrorCode.PASSWORD_ERROR
}
class ExistsUsernameException : UserServiceException(){
    override fun errorType() = ErrorCode.EXIST_USERNAME
}
class PhoneNumberException : UserServiceException(){
    override fun errorType() = ErrorCode.PHONE_NUMBER_ERROR
}
class EmailErrorException : UserServiceException(){
    override fun errorType() = ErrorCode.EMAIL_ERROR_EXCEPTION
}
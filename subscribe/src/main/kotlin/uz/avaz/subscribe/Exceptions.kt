package uz.avaz.subscribe

import BaseMessage
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
class FollowerNotFoundException : UserServiceException(){
    override fun errorType() = ErrorCode.FOLLOWER_NOT_FOUND
}
class FollowNotFoundException : UserServiceException(){
    override fun errorType() = ErrorCode.FOLLOW_NOR_FOUND
}
class NotFollowException : UserServiceException(){
    override fun errorType()= ErrorCode.NOT_FOLLOW
}
class NotUnFollowException : UserServiceException(){
    override fun errorType()= ErrorCode.NOT_FOLLOW
}
class SubscribeNotFoundException : UserServiceException(){
    override fun errorType() = ErrorCode.SUBSCRIBE_NOT_FOUND
}
class GeneralApiException(val  msg: String): UserServiceException(){
    override fun errorType() = ErrorCode.GENERAL_API_EXCEPTION
}
class FeignErrorException(val code: Int?, val errorMessage: String?):UserServiceException(){
    override fun errorType() = ErrorCode.GENERAL_API_EXCEPTION
}
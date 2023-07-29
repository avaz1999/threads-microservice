package uz.demo.post

import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.context.support.ResourceBundleMessageSource
import java.util.*


sealed class PostServiceException(message: String? = null) : RuntimeException(message) {
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

class PostNotFoundException : PostServiceException() {
    override fun errorType() = ErrorCode.POST_NOT_FOUND
}

class UserNotFoundException : PostServiceException() {
    override fun errorType() = ErrorCode.USER_NOT_FOUND
}

class GeneralApiException(val msg: String) : PostServiceException() {
    override fun errorType() = ErrorCode.GENERAL_API_EXCEPTION
}

class FeignErrorException(val code: Int?, val errorMessage: String?) : PostServiceException() {
    override fun errorType() = ErrorCode.GENERAL_API_EXCEPTION
}

class DescriptionException : PostServiceException(){
    override fun errorType() = ErrorCode.DESCRIPTION_ERROR
}
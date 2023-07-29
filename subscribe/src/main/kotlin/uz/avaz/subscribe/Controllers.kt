package uz.avaz.subscribe

import BaseMessage
import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource
) {
    @ExceptionHandler(UserServiceException::class)
    fun handleException(exception: UserServiceException): ResponseEntity<*> {
        return when (exception) {
            is FollowerNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is FollowNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource))

            is NotFollowException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )
            is FeignErrorException -> ResponseEntity.badRequest().body(
                BaseMessage(exception.code, exception.errorMessage)
            )

            is GeneralApiException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.msg)
            )

            is NotUnFollowException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is SubscribeNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )
        }
    }
}

@RestController
class SubscribeController(private val service: SubscribeService) {
    @PostMapping("follow/{followerId}/{followId}")
    fun create(@PathVariable followerId: Long, @PathVariable followId: Long) = service.follow(followerId, followId)

    @PutMapping("unfollow/{followerId}/{followId}")
    fun unfollow(@PathVariable followerId: Long,@PathVariable followId: Long) = service.unFollow(followerId, followId)
}

@RestController
@RequestMapping("internal")
class SubscribeInternalController(private val service: SubscribeService){
    @PostMapping("{id}")
    fun createSubscribe(@PathVariable id:Long) = service.create(id)

    @GetMapping("following/{userId}")
    fun getFollowingByUserId(@PathVariable userId:Long) = service.getFollowingByUserId(userId)

}


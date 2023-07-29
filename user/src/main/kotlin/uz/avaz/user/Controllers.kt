package uz.avaz.user

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource
) {
    @ExceptionHandler(UserServiceException::class)
    fun handleException(exception: UserServiceException): ResponseEntity<*> {
        return when (exception) {
            is UserNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is ExistsUsernameException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is PasswordErrorException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is PhoneNumberException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is EmailErrorException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )
        }
    }
}

@RestController
class UserController(private val service: UserService) {
    @PostMapping
    fun create(@RequestBody dto: UserDto) = service.create(dto)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @GetMapping("following/{id}")
    fun getFollowing(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): List<GetOneUserDto> {
        val pageNumber = if (page < 0) 0 else page
        val pageSize = if (size < 0) 0 else size
        return service.getFollowing(id, pageNumber, pageSize).content
    }

    @GetMapping("followers/{id}")
    fun getFollowers(
        @PathVariable id: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int
    ): List<GetOneUserDto> {
        val pageNumber = if (page < 0) 0 else page
        val pageSize = if (size < 0) 0 else size
        return service.getFollowers(id, pageNumber, pageSize).content
    }

    @GetMapping("count-following/{id}")
    fun countFollowing(@PathVariable id: Long) = service.countFollowing(id)

    @GetMapping("count-followers/{id}")
    fun countFollowers(@PathVariable id: Long) = service.countFollowers(id)

    @DeleteMapping("{id}")
    fun delete(@PathVariable id:Long) = service.delete(id)
}


@RestController
@RequestMapping("internal")

class UserInternalController(private val service: UserService) {
    @GetMapping("exists/{id}")
    fun existsById(@PathVariable id: Long) = service.existById(id)

    @GetMapping("subscribes/{userId}")
    fun getSubscribes(@PathVariable userId:Long) = service.getSubscribes(userId)
}

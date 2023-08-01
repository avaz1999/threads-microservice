package uz.avaz.user

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource,
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

    @GetMapping
    fun getById() = service.getById()

    @GetMapping("following")
    fun getFollowing(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): List<GetOneUserDto> {
        val pageNumber = if (page < 0) 0 else page
        val pageSize = if (size < 0) 0 else size
        return service.getFollowing(pageNumber, pageSize).content
    }


    @GetMapping("followers")
    fun getFollowers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): List<GetOneUserDto> {
        val pageNumber = if (page < 0) 0 else page
        val pageSize = if (size < 0) 0 else size
        return service.getFollowers(pageNumber, pageSize).content
    }

    @GetMapping("count-following/")
    fun countFollowing() = service.countFollowing()

    @GetMapping("count-followers/")
    fun countFollowers() = service.countFollowers()

    @DeleteMapping()
    fun delete() = service.delete()

    @GetMapping("find")
    fun findUser(username: String) = service.findByUsername(username)
}


@RestController
@RequestMapping("internal")

class UserInternalController(private val service: UserService) {
    @GetMapping("exists/{followId}")
    fun existsById(@PathVariable followId: Long) = service.existById(followId)

    @GetMapping("subscribes/{userId}")
    fun getSubscribes(@PathVariable userId: Long) = service.getSubscribes()
}

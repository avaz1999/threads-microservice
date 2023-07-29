package uz.demo.post

import org.springframework.context.support.ResourceBundleMessageSource
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@ControllerAdvice
class ExceptionHandlers(
    private val errorMessageSource: ResourceBundleMessageSource,
) {
    @ExceptionHandler(PostServiceException::class)
    fun handleException(exception: PostServiceException): ResponseEntity<*> {
        return when (exception) {
            is PostNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is UserNotFoundException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )

            is FeignErrorException -> ResponseEntity.badRequest().body(
                BaseMessage(exception.code, exception.errorMessage)
            )

            is GeneralApiException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource, exception.msg)
            )

            is DescriptionException -> ResponseEntity.badRequest().body(
                exception.getErrorMessage(errorMessageSource)
            )
        }
    }
}

@RestController
class PostController(
    private val service: PostService,
) {
    @PostMapping
    fun create(@RequestBody dto: PostCreateDto) = service.create(dto)

    @GetMapping("{id}")
    fun getById(@PathVariable id: Long) = service.getById(id)

    @GetMapping("all/post/{userId}")
    fun getAllPostByUserId(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): Page<PostDto> {
        val pageNumber = if (page < 0) 0 else page
        val pageSize = if (size < 0) 0 else size
        return service.getAllPostByUserId(userId, pageNumber, pageSize)
    }

    @GetMapping("users/posts/{userId}")
    fun getUserPosts(
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "5") size: Int,
    ): List<PostDto> {
        val pageNumber = if (page < 0) 0 else page
        val pageSize = if (size < 0) 0 else size
        return service.getMyPosts(userId, pageNumber, pageSize).content
    }

    @PostMapping("post/like/{userId}/{postId}")
    fun postLike(@PathVariable userId: Long, @PathVariable postId: Long) = service.postLike(userId,postId)

    @DeleteMapping("{userId}/{postId}")
    fun deletePost(@PathVariable userId: Long, @PathVariable postId: Long) = service.delete(userId,postId)
}

@RestController
@RequestMapping("internal")
class PostInternalController(
    private val service: PostService,
) {
}
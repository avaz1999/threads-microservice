package uz.demo.post

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import java.util.HashSet
import javax.transaction.Transactional

@FeignClient(name = "user")
interface UserService {
    @GetMapping("internal/exists/{id}")
    fun existById(@PathVariable id: Long): Boolean

    @GetMapping("{id}")
    fun getUserById(@PathVariable id: Long): UserDto

    @GetMapping("internal/subscribes/{userId}")
    fun getSubscribesByUserId(@PathVariable userId: Long): SubscribeDto

    @GetMapping("get/users")
    fun getUsersByIds(@RequestBody followingList: HashSet<Long>?): UserDto
}

interface PostService {
    fun create(dto: PostCreateDto)
    fun getById(id: Long): PostDto
    fun getAllPostByUserId(userId: Long, pageNumber: Int, pageSize: Int): Page<PostDto>
    fun getMyPosts(userId: Long, pageNumber: Int, pageSize: Int): Page<PostDto>
    fun postLike(userId: Long, postId: Long)
    fun delete(userId: Long, postId: Long)

}

@Suppress("ALWAYS_NULL")
@Service
class PostServiceImpl(
    private val postRepository: PostRepository,
    private val userService: UserService,
    private val readPostRepository: PostReadRepository,
    private val postLikeRepository: PostLikeRepository,
) : PostService {
    @Transactional
    override fun create(dto: PostCreateDto) {
        if (!userService.existById(dto.userId)) throw UserNotFoundException()
        postRepository.save(dto.toEntity())
    }

    override fun getById(id: Long): PostDto {
        val post = postRepository.findByIdAndDeletedFalse(id) ?: throw PostNotFoundException()
        val user = userService.getUserById(post.userId)
        return PostDto.toDto(post, user)
    }

    @Transactional
    override fun getAllPostByUserId(userId: Long, pageNumber: Int, pageSize: Int): Page<PostDto> {
        if (!userService.existById(userId)) throw UserNotFoundException()
        val subscribe = userService.getSubscribesByUserId(userId)
        val sharePostForUser = mutableListOf<Post>()
        val user = userService.getUserById(userId)
        val pageRequest: PageRequest = PageRequest.of(pageNumber, pageSize)
        postRepository.findAllFollowersPosts(subscribe.followerList!!, pageRequest).forEach { followerPost ->
            if (!readPostRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, followerPost.id)) {
                val postUser = userService.getUserById(followerPost.userId)
                sharePostForUser.add(followerPost)
                readPostRepository.save(PostRead(userId, followerPost.id!!))
                if (sharePostForUser.size == pageSize) {
                    return sharePostForUser.map { PostDto.toDto(it, postUser) }.toPage(pageRequest)
                }
            }
        }
//        val emptyPage: Page<Post> = PageImpl(emptyList(), PageRequest.of(pageNumber, pageSize), 0)
//        return emptyPage.map { PostDto.toDto(it, user) }
        val allFollowersPosts = postRepository.findAllFollowersPosts(subscribe.followerList, pageRequest)
        return allFollowersPosts.map { PostDto.toDto(it, user) }
    }
//    @Transactional
//    override fun getAllPostByUserId(userId: Long, pageNumber: Int, pageSize: Int): Page<PostDto> {
//        if (!userService.existById(userId)) {
//            throw UserNotFoundException()
//        }
//
//        val sharePostForUser = mutableListOf<Post>()
//        val user = userService.getUserById(userId)
//        val pageRequest = PageRequest.of(pageNumber, pageSize)
//
//        postRepository.findAllFollowersPosts(userService.getSubscribesByUserId(userId).followerList!!, pageRequest)
//            .forEach { followerPost ->
//                val followerUser = userService.getUserById(followerPost.userId)
//                if (!readPostRepository.existsByUserIdAndPostIdAndDeletedFalse(userId, followerPost.id)) {
//                    sharePostForUser.add(followerPost)
//                    readPostRepository.save(PostRead(userId, followerPost.id!!))
//                }
//            }
//
//        return sharePostForUser.map { PostDto.toDto(it, user) }.toPage(pageRequest)
//    }

    override fun getMyPosts(userId: Long, pageNumber: Int, pageSize: Int): Page<PostDto> {
        val of = PageRequest.of(pageNumber, pageSize)
        val user = userService.getUserById(userId)
        return postRepository.findAllByUserIdAndDeletedFalse(userId, of).map { PostDto.toDto(it, user) }
    }

    @Transactional
    override fun postLike(userId: Long, postId: Long) {
        if (!userService.existById(userId)) throw UserNotFoundException()
        if (!postRepository.existsById(postId)) throw PostNotFoundException()
        val postLike = postLikeRepository.findByUserIdAndPostIdAndDeletedFalse(userId, postId)
        if (postLike == null) {
            postLikeRepository.save(PostLike(userId, postId))
        } else if (postLike.postId != postId && postLike.userId != userId)
            postLikeRepository.save(PostLike(userId, postId))
        else {
            postLike.deleted = true
            postLikeRepository.save(postLike)
        }
    }

    @Transactional
    override fun delete(userId: Long, postId: Long) {
        if (!userService.existById(userId)) throw UserNotFoundException()
        if (!postRepository.existsById(postId)) throw PostNotFoundException()
        val post = postRepository.findByIdAndDeletedFalse(postId)
        val allPostLikes = postLikeRepository.findAllByPostIdAndDeletedFalse(postId)
        allPostLikes!!.forEach { postLike ->
            postLike.deleted = true
            postLikeRepository.save(postLike)
        }
        val allReadsPost = readPostRepository.findAllByPostIdAndDeletedFalse(postId)
        allReadsPost!!.forEach { read ->
            read.deleted = true
            readPostRepository.save(read)
        }
        readPostRepository
        post!!.deleted = true
        postRepository.save(post)
    }

    fun <T> List<T>.toPage(pageRequest: PageRequest): Page<T> {
        val start = pageRequest.pageNumber * pageRequest.pageSize
        val end = minOf(start + pageRequest.pageSize, this.size)
        return PageImpl(this.subList(start, end), pageRequest, this.size.toLong())
    }
}

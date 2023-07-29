package uz.demo.post

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseMessage(var code: Int? = null, var message: String? = null)

data class PostCreateDto(
    val userId: Long,
    val description: String
) {
    fun toEntity() = Post( userId,description)
}

data class PostDto(
    val user: UserDto,
    val description: String,
//    val likes: Long,
) {
    companion object {
        fun toDto(post: Post, user: UserDto) = post.run {
            PostDto(user,description)
        }
    }
}
data class GetOnePost(
    val id:Long,
    val user: UserDto,
    val description: String,
    val likes: Long,
) {
    companion object {
        fun toDto(post: Post, user: UserDto, likePosts: Long?) = post.run {
            GetOnePost(id!!,user,description,likePosts!!)
        }
    }
}

data class UserDto(
    val id: Long,
    val username: String
)

data class SubscribeDto(
    val id: Long,
    val userId: Long,
    val followerList: HashSet<Long>?,
    var followingList: HashSet<Long>?
)

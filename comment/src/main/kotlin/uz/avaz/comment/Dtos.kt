package uz.avaz.comment

data class BaseMessage(val code: Int, val message: String?)

data class CommentDto(
    val id: Long,
    val postId: Long,
    val userId: Long
) {


data class PostDto(
    val id:Long?,
    val user: GetUserForPostDto,
    val description:String
)

data class GetUserForPostDto(
    val id: Long?,
    val username: String
){


}}


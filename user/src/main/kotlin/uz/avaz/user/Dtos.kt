package uz.avaz.user

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    val id: Long?,
    val fullName: String,
    val phoneNumber: String,
    val username: String,
    val email: String,
    val password: String,
    val bio: String
) {
    fun toEntity() = User(fullName, username, phoneNumber, email, password, bio)
}

data class GetOneUserDto(
    val id: Long?,
    val fullName: String,
    val username: String,
    val bio: String
){
    companion object {
        fun toDto(user: User) = user.run { GetOneUserDto(id, fullName, username, bio!!) }
    }
}
data class PostDto(
    val id:Long?,
    val user: GetUserForPostDto,
    val description:String
)

data class GetUserForPostDto(
    val id: Long?,
    val username: String
)

data class SubscribeDto(
    val id: Long,
    val userId: Long,
    val followerList: HashSet<Long>?,
    var followingList: HashSet<Long>?
)
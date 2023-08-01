package uz.avaz.user

data class BaseMessage(val code: Int, val message: String?)

data class UserDto(
    val id: Long?,
    val fullName: String,
    val phoneNumber: String,
    val username: String,
    val email: String,
    val password: String,
    val bio: String,
    val roleId: Long,
) {
    fun toEntity(role: Role) = User(username, password, role, fullName, null, "998999701899", email,bio)
}

data class GetOneUserDto(
    val id: Long?,
    val fullName: String,
    val username: String,
    val bio: String,
) {
    companion object {
        fun toDto(user: User) = user.run { GetOneUserDto(id, fullName, username, bio!!) }
    }
}

data class PostDto(
    val id: Long?,
    val user: GetUserForPostDto,
    val description: String,
)

data class GetUserForPostDto(
    val id: Long?,
    val username: String,
)

data class SubscribeDto(
    val id: Long,
    val userId: Long,
    val followerList: HashSet<Long>?,
    var followingList: HashSet<Long>?,
)

data class UserAuthDto(
    var id: Long,
    var username: String,
    var name: String? = null,
    var password: String,
    var role: String,
    var active: Boolean,
    var permissions: List<String>,
) {
    companion object {
        fun toDto(user: User) = user.run {
            UserAuthDto(
                id!!,
                username,
                fullName,
                password,
                role!!.name.name,
                active,
                permissions!!.map { it.name })
        }
    }
}
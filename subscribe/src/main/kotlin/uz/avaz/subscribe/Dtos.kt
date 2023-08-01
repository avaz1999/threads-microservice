import com.fasterxml.jackson.annotation.JsonInclude
import uz.avaz.subscribe.Subscribe

@JsonInclude(JsonInclude.Include.NON_NULL)
data class BaseMessage(var code: Int? = null, var message: String? = null)

data class GetOneSubscribeDto(
    val id: Long,
    var userId: Long,
    val followerList: HashSet<Long>?,
    var followingList: HashSet<Long>?
) {
    companion object {
        fun toDto(subscribe: Subscribe) = subscribe.run { GetOneSubscribeDto(id!!,userId, followerList, followingList) }
    }
}

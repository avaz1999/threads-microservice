package uz.avaz.subscribe

import GetOneSubscribeDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import javax.transaction.Transactional

interface SubscribeService {
    fun follow(followerId: Long, followId: Long)
    fun unFollow(followerId: Long, followId: Long)
    fun create(id: Long)
    fun getFollowingByUserId(userId: Long): GetOneSubscribeDto
}

@FeignClient(name = "user")
interface UserService {
    @GetMapping("internal/exists/{id}")
    fun existsById(@PathVariable id: Long): Boolean
}

@Service
class SubscribeServiceImpl(
    private val subscribeRepository: SubscribeRepository,
    private val userService: UserService
) : SubscribeService {
    @Transactional
    override fun follow(followerId: Long, followId: Long) {
        if (!userService.existsById(followerId)) throw FollowerNotFoundException()
        if (!userService.existsById(followId)) throw FollowNotFoundException()

        val followerExists = subscribeRepository.existsByUserIdAndDeletedFalse(followerId)
        val followExists = subscribeRepository.existsByUserIdAndDeletedFalse(followId)

        if (!followerExists && !followExists) {
            val followingList = hashSetOf(followId)
            val followerList = hashSetOf(followerId)
            subscribeRepository.save(Subscribe(followerId, hashSetOf(), followingList))
            subscribeRepository.save(Subscribe(followId, followerList, hashSetOf()))
        } else if (!followerExists) {
            val followingList = hashSetOf(followId)
            subscribeRepository.save(Subscribe(followerId, hashSetOf(), followingList))

            val following = subscribeRepository.findByUserIdAndDeletedFalse(followId)
            following.followerList?.add(followerId)
            subscribeRepository.save(following)
        } else if (!followExists) {
            val followerList = hashSetOf(followerId)
            subscribeRepository.save(Subscribe(followId, followerList, hashSetOf()))

            val follower = subscribeRepository.findByUserIdAndDeletedFalse(followerId)
            follower.followingList?.add(followId)
            subscribeRepository.save(follower)
        } else {
            val getFollower = subscribeRepository.findByUserIdAndDeletedFalse(followerId)
            val getFollowing = subscribeRepository.findByUserIdAndDeletedFalse(followId)
            if (getFollower.followingList!!.contains(getFollowing.userId) ||
                getFollowing.followerList!!.contains(getFollower.userId) ||
                followerId == followId
            ) throw NotFollowException()
            else {
                getFollower.followingList!!.add(getFollowing.userId)
                getFollowing.followerList!!.add(getFollower.userId)
                subscribeRepository.save(getFollower)
                subscribeRepository.save(getFollowing)
            }
        }
    }

    @Transactional
    override fun unFollow(followerId: Long, followId: Long) {
        if (!userService.existsById(followerId)) throw FollowerNotFoundException()
        if (!userService.existsById(followId)) throw FollowNotFoundException()

        val followerExists = subscribeRepository.existsByUserIdAndDeletedFalse(followerId)
        val followExists = subscribeRepository.existsByUserIdAndDeletedFalse(followId)

        if (followerExists && followExists && followerId != followId){
            val getFollower = subscribeRepository.findByUserIdAndDeletedFalse(followerId)
            val getFollowing = subscribeRepository.findByUserIdAndDeletedFalse(followId)

            getFollower.followingList?.remove(followId)
            getFollowing.followerList?.remove(followerId)

            subscribeRepository.save(getFollower)
            subscribeRepository.save(getFollowing)
        }else throw NotUnFollowException()
    }


    override fun create(id: Long) {
        subscribeRepository.save(Subscribe(id, hashSetOf(), hashSetOf()))
    }

    override fun getFollowingByUserId(userId: Long)=subscribeRepository.findByUserIdAndDeletedFalse(userId).run {
        GetOneSubscribeDto.toDto(this)
    }


}

package uz.avaz.subscribe

import GetOneSubscribeDto
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import javax.transaction.Transactional

interface SubscribeService {
    fun follow( followId: Long)
    fun unFollow( followId: Long)
    fun create(id: Long)
    fun getFollowingByUserId(userId:Long): GetOneSubscribeDto
}

@FeignClient(name = "user", configuration = [Auth2TokenConfiguration::class])
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
    override fun follow(followId: Long) {
        if (!userService.existsById(followId)) throw FollowNotFoundException()


        val followExists = subscribeRepository.existsByUserIdAndDeletedFalse(followId)

        if (!followExists && userId() != followId) {
            val followingList = hashSetOf(followId)
            val followerList = hashSetOf(userId())
            subscribeRepository.save(Subscribe(userId(), hashSetOf(), followingList))
            subscribeRepository.save(Subscribe(followId, followerList, hashSetOf()))
        } else if (!followExists) {
            val followerList = hashSetOf(followId)
            subscribeRepository.save(Subscribe(followId, followerList, hashSetOf()))

            val follower = subscribeRepository.findByUserIdAndDeletedFalse(userId())
            follower.followingList?.add(followId)
            subscribeRepository.save(follower)
        } else {
            val getFollower = subscribeRepository.findByUserIdAndDeletedFalse(userId())
            val getFollowing = subscribeRepository.findByUserIdAndDeletedFalse(followId)
            if (getFollower.followingList!!.contains(getFollowing.userId) ||
                getFollowing.followerList!!.contains(userId()) ||
                userId() == followId
            ) throw NotFollowException()
            else {
                getFollower.followingList!!.add(followId)
                getFollowing.followerList!!.add(userId())
                subscribeRepository.save(getFollower)
                subscribeRepository.save(getFollowing)
            }
        }
    }

    @Transactional
    override fun unFollow(followId: Long) {
        if (!userService.existsById(followId)) throw FollowNotFoundException()


        val followExists = subscribeRepository.existsByUserIdAndDeletedFalse(followId)

        if (  followExists && userId() != followId){
            val getFollower = subscribeRepository.findByUserIdAndDeletedFalse(userId())
            val getFollowing = subscribeRepository.findByUserIdAndDeletedFalse(followId)

            getFollower.followingList?.remove(followId)
            getFollowing.followerList?.remove(userId())

            subscribeRepository.save(getFollower)
            subscribeRepository.save(getFollowing)
        }else throw NotUnFollowException()
    }


    override fun create(id: Long) {
        subscribeRepository.save(Subscribe(id, hashSetOf(), hashSetOf()))
    }

    override fun getFollowingByUserId(userId: Long) =subscribeRepository.findByUserIdAndDeletedFalse(userId).run {
        GetOneSubscribeDto.toDto(this)
    }
}

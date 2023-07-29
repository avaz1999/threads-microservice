package uz.avaz.user

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import javax.transaction.Transactional

interface UserService {
    fun create(dto: UserDto)
    fun getById(id: Long): GetOneUserDto
    fun existById(id: Long): Boolean
    fun isSecurePassword(password: String): Boolean
    fun checkPhoneNumber(phoneNumber: String): Boolean
    fun isValidEmail(email: String): Boolean
    fun getFollowing(id: Long, pageNumber: Int, pageSize: Int): Page<GetOneUserDto>
    fun getFollowers(id: Long, pageNumber: Int, pageSize: Int): Page<GetOneUserDto>
    fun countFollowing(id: Long): Long
    fun countFollowers(id: Long): Long
    fun getSubscribes(id: Long):SubscribeDto
    fun delete(id: Long)
}

@FeignClient(name = "subscribe")
interface SubscribeService {
    @PostMapping("internal/{id}")
    fun createSubscribe(@PathVariable id: Long)

    @GetMapping("internal/following/{userId}")
    fun getByUserId(@PathVariable userId: Long): SubscribeDto
}

@FeignClient(name = "post")
interface PostService {
    @GetMapping("internal/{userId}")
    fun getByUserIdAndFollowerId(@PathVariable userId: Long): List<PostDto>
}
@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val subscribeService: SubscribeService,
    private val postService: PostService
) : UserService {
    @Transactional
    override fun create(dto: UserDto) {
        val username: String = dto.username
        if (userRepository.existsByUsername(username)) throw ExistsUsernameException()
        if (!isSecurePassword(dto.password)) throw PasswordErrorException()
        if (!checkPhoneNumber(dto.phoneNumber)) throw PhoneNumberException()
        if (!isValidEmail(dto.email)) throw EmailErrorException()
        val user = userRepository.save(dto.toEntity())
        subscribeService.createSubscribe(user.id!!)
    }

    override fun getById(id: Long) = userRepository.findByIdAndDeletedFalse(id)?.run { GetOneUserDto.toDto(this) }
        ?: throw UserNotFoundException()

    override fun existById(id: Long): Boolean {
        return userRepository.existsByIdAndDeletedFalse(id)
    }

    override fun getFollowing(id: Long, pageNumber: Int, pageSize: Int): Page<GetOneUserDto> {
        val subscribe = subscribeService.getByUserId(id)
        val followingList = subscribe.followingList ?: emptySet()
        val pageRequest = PageRequest.of(pageNumber, pageSize)
        return userRepository.findUserByFollowingIds(followingList, pageRequest).map { GetOneUserDto.toDto(it) }
    }

    override fun getFollowers(id: Long, pageNumber: Int, pageSize: Int): Page<GetOneUserDto> {
        val subscribe = subscribeService.getByUserId(id)
        val followingList = subscribe.followerList ?: emptySet()
        val pageRequest = PageRequest.of(pageNumber, pageSize)
        return userRepository.findUserByFollowingIds(followingList, pageRequest).map { GetOneUserDto.toDto(it) }
    }

    override fun countFollowing(id: Long): Long {
        val subscribe = subscribeService.getByUserId(id)
        val followingList = subscribe.followingList ?: emptySet()
        return followingList.size.toLong()
    }

    override fun countFollowers(id: Long): Long {
        val subscribe = subscribeService.getByUserId(id)
        val followers = subscribe.followerList ?: emptySet()
        return followers.size.toLong()
    }

    override fun getSubscribes(id: Long): SubscribeDto {
        if (!userRepository.existsByIdAndDeletedFalse(id)) throw UserNotFoundException()
        return subscribeService.getByUserId(id)
    }

    override fun delete(id: Long) {
        if (!userRepository.existsByIdAndDeletedFalse(id)) throw UserNotFoundException()
        val user = userRepository.findByIdAndDeletedFalse(id)
        user!!.deleted = true
        userRepository.save(user)
    }


    override fun isSecurePassword(password: String): Boolean {
        val minLength = 8
        val minUpperCase = 1
        val minLoweCase = 1
        val minDigits = 1
        var upperCaseCount = 0
        var lowerCaseCount = 0
        var digitsCount = 0
        if (password.length < minLength) return false
        for (char in password) {
            when {
                char.isUpperCase() -> upperCaseCount++
                char.isLowerCase() -> lowerCaseCount++
                char.isDigit() -> digitsCount++
            }
        }
        return upperCaseCount >= minUpperCase &&
                lowerCaseCount >= minLoweCase &&
                digitsCount >= minDigits
    }

    override fun checkPhoneNumber(phoneNumber: String): Boolean {
        if (phoneNumber.startsWith("+")) {
            val savePhoneNumber = phoneNumber.substring(1, phoneNumber.length)
            if (savePhoneNumber.length != 12) return false
        }
        return phoneNumber.length == 12
    }

    override fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex(pattern = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}")
        return email.matches(emailRegex)
    }


}
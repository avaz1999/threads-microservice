package uz.avaz.user

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import javax.management.relation.RoleNotFoundException
import javax.transaction.Transactional

interface UserService {
    fun create(dto: UserDto)
    fun getById(): GetOneUserDto
    fun existById(followId:Long): Boolean
    fun isSecurePassword(password: String): Boolean
    fun checkPhoneNumber(phoneNumber: String): Boolean
    fun isValidEmail(email: String): Boolean
    fun getFollowing( pageNumber: Int, pageSize: Int): Page<GetOneUserDto>
    fun getFollowers( pageNumber: Int, pageSize: Int): Page<GetOneUserDto>
    fun countFollowing(): Long
    fun countFollowers(): Long
    fun getSubscribes(): SubscribeDto
    fun delete()
    fun findByUsername(username: String): UserAuthDto
}

@FeignClient(name = "subscribe", configuration = [Auth2TokenConfiguration::class])
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
    private val postService: PostService,
    private val roleRepository: RoleRepository
) : UserService {
    @Transactional
    override fun create(dto: UserDto) {
        val username: String = dto.username
        if (userRepository.existsByUsername(username)) throw ExistsUsernameException()
        val role = roleRepository.findByIdAndDeletedFalse(dto.roleId) ?: throw RoleNotFoundException()
        if (!isSecurePassword(dto.password)) throw PasswordErrorException()
        if (!checkPhoneNumber(dto.phoneNumber)) throw PhoneNumberException()
        if (!isValidEmail(dto.email)) throw EmailErrorException()
        userRepository.save(dto.toEntity(role))
        subscribeService.createSubscribe(userId())
    }

    override fun getById() = userRepository.findByIdAndDeletedFalse(userId())?.run { GetOneUserDto.toDto(this) }
        ?: throw UserNotFoundException()

    override fun existById(followId: Long): Boolean {
        return userRepository.existsByIdAndDeletedFalse(followId)
    }

    override fun findByUsername(username: String): UserAuthDto {
        return userRepository.findByUsernameAndDeletedFalse(username)?.run { UserAuthDto.toDto(this) }
            ?: throw UserNotFoundException()
    }

    override fun getFollowing( pageNumber: Int, pageSize: Int): Page<GetOneUserDto> {
        val subscribe = subscribeService.getByUserId(userId())
        val followingList = subscribe.followingList ?: emptySet()
        val pageRequest = PageRequest.of(pageNumber, pageSize)
        return userRepository.findUserByFollowingIds(followingList, pageRequest).map { GetOneUserDto.toDto(it) }
    }

    override fun getFollowers( pageNumber: Int, pageSize: Int): Page<GetOneUserDto> {
        val subscribe = subscribeService.getByUserId(userId())
        val followingList = subscribe.followerList ?: emptySet()
        val pageRequest = PageRequest.of(pageNumber, pageSize)
        return userRepository.findUserByFollowingIds(followingList, pageRequest).map { GetOneUserDto.toDto(it) }
    }

    override fun countFollowing(): Long {
        val subscribe = subscribeService.getByUserId(userId())
        val followingList = subscribe.followingList ?: emptySet()
        return followingList.size.toLong()
    }

    override fun countFollowers(): Long {
        val subscribe = subscribeService.getByUserId(userId())
        val followers = subscribe.followerList ?: emptySet()
        return followers.size.toLong()
    }

    override fun getSubscribes(): SubscribeDto {
        if (!userRepository.existsByIdAndDeletedFalse(userId())) throw UserNotFoundException()
        return subscribeService.getByUserId(userId())
    }

    override fun delete() {
        if (!userRepository.existsByIdAndDeletedFalse(userId())) throw UserNotFoundException()
        val user = userRepository.findByIdAndDeletedFalse(userId())
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
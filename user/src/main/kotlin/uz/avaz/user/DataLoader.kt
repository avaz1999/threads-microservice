package uz.avaz.user

import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataLoader(
    private val passwordEncoder: BCryptPasswordEncoder,
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
) : CommandLineRunner {
    override fun run(vararg args: String?) {
        val developer = roleRepository.findByNameAndDeletedFalse(UserRole.DEVELOPER)
            ?: run { roleRepository.save(Role(UserRole.DEVELOPER)) }
        val admin = roleRepository.findByNameAndDeletedFalse(UserRole.ADMIN)
            ?: run { roleRepository.save(Role(UserRole.ADMIN)) }
        val moderator = roleRepository.findByNameAndDeletedFalse(UserRole.MODERATOR)
            ?: run { roleRepository.save(Role(UserRole.MODERATOR)) }
        val user = roleRepository.findByNameAndDeletedFalse(UserRole.USER)
            ?: run { roleRepository.save(Role(UserRole.USER)) }


        userRepository.findByUsernameAndDeletedFalse("dev") ?: run {
            userRepository.save(
                User(
                    "dev",
                    passwordEncoder.encode("12345678"),
                    developer,
                    "Developeriddin",
                    mutableSetOf(),
                    "998999701899",
                    "avazabsamtov7@gmail.com",
                    "bio"
                )
            )
        }
    }
}
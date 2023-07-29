package uz.demo.post

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@EnableJpaAuditing
@EnableFeignClients
@SpringBootApplication
@EnableJpaRepositories(repositoryBaseClass = BaseRepositoryImpl::class)
class PostApplication

fun main(args: Array<String>) {
    runApplication<PostApplication>(*args)
}
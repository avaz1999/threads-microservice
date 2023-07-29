package uz.demo.post

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.jpa.repository.support.JpaEntityInformation
import org.springframework.data.jpa.repository.support.SimpleJpaRepository
import org.springframework.data.repository.NoRepositoryBean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.data.repository.query.Param
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager


@NoRepositoryBean
interface BaseRepository<T : BaseEntity> : JpaRepository<T, Long>, JpaSpecificationExecutor<T> {
    fun findByIdAndDeletedFalse(id: Long): T?
    fun trash(id: Long): T?
    fun trashList(ids: List<Long>): List<T?>
    fun findAllNotDeleted(): List<T>
    fun findAllNotDeleted(pageable: Pageable): Page<T>
}

class BaseRepositoryImpl<T : BaseEntity>(
    entityInformation: JpaEntityInformation<T, Long>, entityManager: EntityManager,
) : SimpleJpaRepository<T, Long>(entityInformation, entityManager), BaseRepository<T> {

    val isNotDeletedSpecification = Specification<T> { root, _, cb -> cb.equal(root.get<Boolean>("deleted"), false) }

    override fun findByIdAndDeletedFalse(id: Long) = findByIdOrNull(id)?.run { if (deleted) null else this }

    @Transactional
    override fun trash(id: Long): T? = findByIdOrNull(id)?.run {
        deleted = true
        save(this)
    }

    override fun findAllNotDeleted(): List<T> = findAll(isNotDeletedSpecification)
    override fun findAllNotDeleted(pageable: Pageable): Page<T> = findAll(isNotDeletedSpecification, pageable)

    @Transactional
    override fun trashList(ids: List<Long>): List<T?> = ids.map { trash(it) }
}

interface PostRepository : BaseRepository<Post> {
    @Query(
        nativeQuery = true, value = "" +
                "select p.id, p.description,p.user_id from d_post.post p join d_post.post_read pr on p.id = pr.post_id\n" +
                "and pr.user_id = :userId where pr.post_id = :postId is null "
    )
    fun getAllNotViewUserPosts(@Param("postId") postId: Long, @Param("userId") userId: Long): List<Post>

    @Query(
        nativeQuery = true, value =
        "select * from d_post.post p where p.user_id in :userIds and deleted = false "
    )
    fun findAllFollowersPosts(@Param("userIds") userIds: HashSet<Long>, pageable: Pageable): Page<Post>

    fun findAllByUserIdAndDeletedFalse(userId: Long, pageable: Pageable): Page<Post>
}

interface PostReadRepository : BaseRepository<PostRead> {
    fun existsByUserIdAndPostIdAndDeletedFalse(userId: Long, postId: Long?): Boolean
    fun findAllByPostIdAndDeletedFalse(postId: Long?): List<PostRead>?
}

interface PostLikeRepository : BaseRepository<PostLike> {
    fun findByUserIdAndPostIdAndDeletedFalse(userId: Long, postId: Long): PostLike?
    fun findAllByPostIdAndDeletedFalse(postId: Long): List<PostLike>?
}
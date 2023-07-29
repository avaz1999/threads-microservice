package uz.demo.post

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.math.BigDecimal
import java.util.*
import javax.persistence.*

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
class BaseEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) var id: Long? = null,
    @CreatedDate @Temporal(TemporalType.TIMESTAMP) var createdDate: Date? = null,
    @LastModifiedDate @Temporal(TemporalType.TIMESTAMP) var modifiedDate: Date? = null,
    @Column(nullable = false) @ColumnDefault(value = "false") var deleted: Boolean = false
)
@Entity
class Post(
    @Column(nullable = false) val userId: Long,
    @Column(columnDefinition = "text", nullable = false) val description:String
) : BaseEntity()

@Entity
class PostRead(
    @Column(nullable = false) val userId: Long,
    @Column(nullable = false) val postId: Long
):BaseEntity()

@Entity
class PostLike(
    @Column(nullable = false) val userId: Long,
    @Column(nullable = false) val postId: Long
):BaseEntity()

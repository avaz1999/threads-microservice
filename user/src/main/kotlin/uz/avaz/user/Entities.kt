package uz.avaz.user

import org.hibernate.annotations.ColumnDefault
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.Temporal
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

@Entity(name = "users")
class User(
    @Column(length = 64, nullable = false, unique = true) val username: String,
    @Column(length = 64, nullable = false) val password:String,
    @ManyToOne var role: Role? ,
    @Column(length = 128, nullable = false) val fullName: String,
    @ManyToMany(fetch = FetchType.LAZY) var permissions: MutableSet<Permission>?,
    @Column(length = 12, nullable = false) val phoneNumber: String,
    @Column(length = 128, nullable = false) val email: String,
    @Column(length = 128) val bio: String? = null,
    @Column(columnDefinition = "boolean default true") var active: Boolean = true,
) : BaseEntity()
@Entity(name = "roles")
class Role(
    @Enumerated(value = EnumType.STRING) var name: UserRole
) : BaseEntity()

@Entity(name = "permissions")
class Permission(
    @Column(unique = true) var name: String,
    @ManyToMany var role: MutableSet<Role> = mutableSetOf(),
) : BaseEntity()
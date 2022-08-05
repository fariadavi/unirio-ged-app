package br.unirio.gedapp.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.persistence.*

@Entity
@Table(name = "platform_user", schema = "public")
data class User(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "first_name")
    val firstName: String? = "",

    val surname: String? = "",

    val email: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    val currentDepartment: Department? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "platform_user_department", schema = "public",
        joinColumns = [JoinColumn(name = "platform_user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "department_id", referencedColumnName = "id")]
    )
    val departments: Set<Department>? = null,

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    val userPermission: UserPermission? = null,

    @OneToOne(mappedBy = "user")
    @JsonIgnore
    val userPublicPermission: UserPublicPermission? = null

) : UserDetails {

    @JsonIgnore
    override fun getAuthorities(): List<SimpleGrantedAuthority> {
        val publicAuthorities = userPublicPermission?.permissions?.map { SimpleGrantedAuthority(it.toString()) } ?: emptyList()
        val tenantAuthorities = userPermission?.permissions?.map { SimpleGrantedAuthority(it.toString()) } ?: emptyList()
        return mutableListOf<SimpleGrantedAuthority>().plus(publicAuthorities).plus(tenantAuthorities)
    }

    @JsonIgnore
    override fun getPassword(): String = ""

    @JsonIgnore
    override fun getUsername(): String = email

    @JsonIgnore
    override fun isAccountNonExpired(): Boolean = true

    @JsonIgnore
    override fun isAccountNonLocked(): Boolean = true

    @JsonIgnore
    override fun isCredentialsNonExpired(): Boolean = true

    @JsonIgnore
    override fun isEnabled(): Boolean = userPermission?.permissions?.isNotEmpty() ?: false

    val fullName: String
        get() = "$firstName $surname"

    val permissions: Array<String>
        get() = authorities.map { it.authority }.toTypedArray()
}
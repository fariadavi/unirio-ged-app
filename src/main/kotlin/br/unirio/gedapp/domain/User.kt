package br.unirio.gedapp.domain

import br.unirio.gedapp.domain.converter.EnumSetType
import com.fasterxml.jackson.annotation.JsonIgnore
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "platform_user", schema = "public")
@TypeDef(name = "enum-set", typeClass = EnumSetType::class)
data class User(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    @Column(name = "first_name")
    val firstName: String = "",

    val surname: String = "",

    val email: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    val currentDepartment: Department?,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "platform_user_department", schema = "public",
        joinColumns = [JoinColumn(name = "platform_user_id", referencedColumnName = "id")],
        inverseJoinColumns = [JoinColumn(name = "department_id", referencedColumnName = "id")]
    )
    val departments: Set<Department>?,

    @Type(type = "enum-set", parameters = [Parameter(name = "enumClass", value = "br.unirio.gedapp.domain.Permission")])
    val permissions: EnumSet<Permission>?

) : UserDetails {

    //TODO Granted Authorities will be based on User Permissions
    @JsonIgnore
    override fun getAuthorities(): Collection<GrantedAuthority> = emptyList()

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
    override fun isEnabled(): Boolean = permissions?.isNotEmpty() ?: false

    val fullName: String
        get() = "${this.firstName} ${this.surname}"
}
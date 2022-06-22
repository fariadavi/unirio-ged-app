package br.unirio.gedapp.domain

import br.unirio.gedapp.domain.converter.EnumSetType
import org.hibernate.annotations.Parameter
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.util.*
import javax.persistence.*

@Entity
@Table(name = "user_permission", schema = "public")
@TypeDef(name = "enum-set", typeClass = EnumSetType::class)
data class UserPublicPermission(

    @OneToOne
    @JoinColumn(name = "platform_user_id")
    val user: User,

    @Type(type = "enum-set", parameters = [Parameter(name = "enumClass", value = "br.unirio.gedapp.domain.Permission")])
    val permissions: EnumSet<Permission>,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1
)
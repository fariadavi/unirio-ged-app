package br.unirio.gedapp.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.*

@Entity
@Table(schema = "public")
data class Department(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val name: String = "",

    val acronym: String = "",

    @JsonIgnore
    @ManyToMany(mappedBy = "departments")
    val users: Collection<User>? = null
)
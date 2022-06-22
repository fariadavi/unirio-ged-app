package br.unirio.gedapp.domain

import javax.persistence.*

@Entity
@Table(schema = "public")
data class Department(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val name: String = "",

    val acronym: String = ""
)
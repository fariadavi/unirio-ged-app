package br.unirio.gedapp.domain

data class Department(

    val id: Long,

    val name: String,

    val acronym: String,

    val users: Collection<User>
)
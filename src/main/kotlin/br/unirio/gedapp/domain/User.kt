package br.unirio.gedapp.domain

data class User(

    val id: Long,

    val firstName: String,

    val surname: String,

    val email: String,

    val permissions: Collection<Permission>,

    val departments: Collection<Department>
)
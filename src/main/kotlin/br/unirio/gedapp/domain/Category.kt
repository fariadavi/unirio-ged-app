package br.unirio.gedapp.domain

data class Category(

    val id: Long,

    val name: String,

    val parent: Category?
)
package br.unirio.gedapp.domain

import javax.persistence.*

@Entity
data class Category(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val name: String = "",

    @ManyToOne(fetch = FetchType.EAGER)
    val parent: Category? = null
)
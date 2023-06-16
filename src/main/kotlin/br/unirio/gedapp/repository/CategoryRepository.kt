package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    fun findAllByParentId(parentId: Long): List<Category>
}
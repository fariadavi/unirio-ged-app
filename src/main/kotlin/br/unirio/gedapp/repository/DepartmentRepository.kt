package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.domain.dto.DepartmentDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DepartmentRepository : JpaRepository<Department, Long> {

    @Query(nativeQuery = true)
    fun findAllWithUserCount(): Collection<DepartmentDTO>
}
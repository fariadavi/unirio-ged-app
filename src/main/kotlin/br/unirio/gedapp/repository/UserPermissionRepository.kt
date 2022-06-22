package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.UserPermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserPermissionRepository : JpaRepository<UserPermission, Long> {

    fun existsByUserEmail(email: String): Boolean
}
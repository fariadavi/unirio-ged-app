package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.UserPublicPermission
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserPublicPermissionRepository : JpaRepository<UserPublicPermission, Long> {}
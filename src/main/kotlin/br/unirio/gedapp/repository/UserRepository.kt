package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): Optional<User>

    @Query("SELECT u FROM User u INNER JOIN u.userPermission up")
    fun findAllWithCurrentDepartmentPermission(): List<User>

    @Query("SELECT u FROM User u INNER JOIN u.departments d WHERE d.acronym = :tenant")
    fun findAllByTenant(tenant: String): List<User>

    @Query("SELECT (COUNT(1) > 0) " +
            "FROM User u " +
            "   INNER JOIN u.userPermission up " +
            "WHERE u.id <> :userId " +
            "   AND (u.firstName <> '' OR u.surname <> '') " +
            "   AND CAST(up.permissions as string) like CONCAT('%', :permission, '%')")
    fun findAnyInDeptWithManagePermissionsExceptUser(permission: String, userId: Long): Boolean

    @Query("SELECT (COUNT(1) > 0) " +
            "FROM User u " +
            "   INNER JOIN u.userPublicPermission up " +
            "WHERE u.id <> :userId " +
            "   AND (u.firstName <> '' OR u.surname <> '') " +
            "   AND CAST(up.permissions as string) like CONCAT('%', :permission, '%')")
    fun findAnyWithManagePermissionsExceptUser(permission: String, userId: Long): Boolean
}
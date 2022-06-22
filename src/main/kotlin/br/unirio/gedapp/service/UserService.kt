package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.*
import br.unirio.gedapp.repository.UserPermissionRepository
import br.unirio.gedapp.repository.UserPublicPermissionRepository
import br.unirio.gedapp.repository.UserRepository
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service
import java.util.*

@Service
class UserService(
    val userRepo: UserRepository,
    val userPermissionRepo: UserPermissionRepository,
    val userPublicPermissionRepo: UserPublicPermissionRepository
) : UserDetailsService {

    fun getById(id: Long): User =
        userRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }

    fun getByEmail(email: String): User =
        userRepo
            .findByEmail(email)
            .orElseThrow { ResourceNotFoundException() }

    fun getAllUsers(): List<User> =
        userRepo.findAllWithCurrentDepartmentPermission()

    fun update(userId: Long, newDataUser: User): User =
        update(getById(userId), newDataUser)

    fun update(user: User, newDataUser: User): User {
        var updatedUser = user

        if (newDataUser.firstName.isNotBlank())
            updatedUser = updatedUser.copy(firstName = newDataUser.firstName)

        if (newDataUser.surname.isNotBlank())
            updatedUser = updatedUser.copy(surname = newDataUser.surname)

        if (newDataUser.email.isNotBlank())
            updatedUser = updatedUser.copy(email = newDataUser.email)

        if (newDataUser.currentDepartment != null)
            updatedUser = updatedUser.copy(currentDepartment = newDataUser.currentDepartment)

        if (newDataUser.departments != null)
            updatedUser = updatedUser.copy(departments = newDataUser.departments)

        if (newDataUser.userPermission != null)
            updatedUser = updatedUser.copy(userPermission = newDataUser.userPermission)

        if (newDataUser.userPublicPermission != null)
            updatedUser = updatedUser.copy(userPublicPermission = newDataUser.userPublicPermission)

        return userRepo.save(updatedUser)
    }

    fun updatePermissions(userId: Long, permissions: EnumSet<Permission>): User {
        var user = getById(userId)

        val defaultPermissionList = permissions.filter { it.level == PermissionLevel.DEFAULT }
        val deptPermissionList = permissions.filter { it.level == PermissionLevel.DEPARTMENT }
        if (deptPermissionList.isNotEmpty()) {
            val userPermission = EnumSet.copyOf(deptPermissionList.plus(defaultPermissionList))
            if (user.userPermission?.permissions != userPermission) {
                val permissionsDept = user.userPermission?.copy(permissions = userPermission) ?: UserPermission(user, userPermission)
                userPermissionRepo.save(permissionsDept)

                user = user.copy(userPermission = permissionsDept)
            }
        }

        val systemPermissionList = permissions.filter { it.level == PermissionLevel.SYSTEM }
        if (systemPermissionList.isNotEmpty()) {
            val userPublicPermission = EnumSet.copyOf(systemPermissionList)
            if (user.userPublicPermission?.permissions != userPublicPermission) {
                val permissionsSystem = user.userPublicPermission?.copy(permissions = userPublicPermission) ?: UserPublicPermission(user, userPublicPermission)
                userPublicPermissionRepo.save(permissionsSystem)

                user = user.copy(userPublicPermission = permissionsSystem)
            }
        }

        return user
    }

    fun removeUserDepartmentPermission(userId: Long) {
        val userPermission = getById(userId).userPermission

        if (userPermission != null)
            userPermissionRepo.delete(userPermission)
    }

    override fun loadUserByUsername(email: String): UserDetails = getByEmail(email)

    fun getCurrentUser(): User {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth is AnonymousAuthenticationToken)
            throw ResourceNotFoundException()

        val userEmail = auth.name
        return getByEmail(userEmail)
    }

    fun findOrCreateUser(email: String): User =
        userRepo.findByEmail(email).orElseGet {
            userRepo.save(User(email = email))
        }

    fun inviteUserToCurrentDepartment(email: String): User {
        var user = findOrCreateUser(email)

        val userNewDepartments : MutableSet<Department> = user.departments?.toMutableSet() ?: mutableSetOf()
        getCurrentUser().currentDepartment?.let { userNewDepartments.add(it) }

        user = user.copy(departments = userNewDepartments)

        val userNewPermissions = UserPermission(user = user, permissions = EnumSet.copyOf(Permission.getDefaultPermissions()))
        userPermissionRepo.save(userNewPermissions)

        user = user.copy(userPermission = userNewPermissions)

        return userRepo.save(user)
    }

    fun checkIfUserHasAccessToCurrentDepartment(email: String) =
        userPermissionRepo.existsByUserEmail(email)

    fun batchUpdatePermissions(userPermissionMap: Map<Long, EnumSet<Permission>>): Pair<List<User>, Int> {
        var numErrors = 0
        val modifiedUsers = mutableListOf<User>()

        userPermissionMap.forEach {
            try {
                val modifiedUser = updatePermissions(it.key, it.value)
                modifiedUsers.add(modifiedUser)
            } catch (e : Exception) {
                modifiedUsers.add(getById(it.key))
                numErrors++
            }
        }

        return Pair(modifiedUsers, numErrors)
    }
}
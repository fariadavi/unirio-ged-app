package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.LastRemainingDeptManagerException
import br.unirio.gedapp.controller.exceptions.LastRemainingSystemManagerException
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.controller.exceptions.UnauthorizedException
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
import javax.transaction.Transactional

@Service
class UserService(
    val emailSvc: EmailService,
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

    fun getAllUsers(): List<User> = userRepo.findAll()

    fun getAllUsersInCurrentDepartment(): List<User> =
        userRepo.findAllWithCurrentDepartmentPermission()

    fun getAllUsersInDepartment(dept: Department): List<User> =
        dept.acronym?.let { userRepo.findAllByTenant(it) }
            ?: emptyList()

    fun update(userId: Long, newDataUser: User): User =
        update(getById(userId), newDataUser)

    fun update(user: User, newDataUser: User): User {
        var updatedUser = user

        if (!newDataUser.firstName.isNullOrBlank())
            updatedUser = updatedUser.copy(firstName = newDataUser.firstName)

        if (!newDataUser.surname.isNullOrBlank())
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

    fun updatePermissions(userId: Long, permissions: EnumSet<Permission>, type: String? = null): User {
        var user = getById(userId)

        if (type.isNullOrBlank() || type == "department")
            user = updateDeptPermissions(user, permissions.filter { it.level == PermissionLevel.DEPARTMENT })

        if (type.isNullOrBlank() || type == "system")
            user = updateSystemPermissions(user, permissions.filter { it.level == PermissionLevel.SYSTEM })

        return user
    }

    private fun updateDeptPermissions(user: User, deptPermissionList: List<Permission>): User {
        if (user.currentDepartment != null) {
            val newPermissions = if (deptPermissionList.isNotEmpty()) EnumSet.copyOf(deptPermissionList) else EnumSet.noneOf(Permission::class.java)
            if (user.userPermission?.permissions != newPermissions) {
                validateDeptPermissionsEdit(user, newPermissions)

                val permissionsDept = user.userPermission?.copy(permissions = newPermissions) ?: UserPermission(user, newPermissions)
                userPermissionRepo.save(permissionsDept)

                return user.copy(userPermission = permissionsDept)
            }
        }
        return user
    }

    private fun validateDeptPermissionsEdit(user: User, newPermissions: EnumSet<Permission>) {
        if (user.userPermission != null
            && user.userPermission.permissions.contains(Permission.MANAGE_DEPT_PERM)
            && !newPermissions.contains(Permission.MANAGE_DEPT_PERM)
            && !deptHasOtherManagers(user.id)
        )
            throw LastRemainingDeptManagerException()
    }

    private fun deptHasOtherManagers(userId: Long) =
        userRepo.findAnyInDeptWithManagePermissionsExceptUser(Permission.MANAGE_DEPT_PERM.toString(), userId)

    private fun updateSystemPermissions(user: User, systemPermissionList: List<Permission>): User {
        val newPublicPermissions = if (systemPermissionList.isNotEmpty()) EnumSet.copyOf(systemPermissionList) else EnumSet.noneOf(Permission::class.java)
        if (user.userPublicPermission?.permissions?.toString() != newPublicPermissions.toString()) {
            validateSystemPermissionsEdit(user, newPublicPermissions)

            val permissionsSystem = user.userPublicPermission?.copy(permissions = newPublicPermissions) ?: UserPublicPermission(user, newPublicPermissions)
            userPublicPermissionRepo.save(permissionsSystem)

            return user.copy(userPublicPermission = permissionsSystem)
        }
        return user
    }

    private fun validateSystemPermissionsEdit(user: User, newPermissions: EnumSet<Permission>) {
        if (user.userPublicPermission != null
            && user.userPublicPermission.permissions.contains(Permission.MANAGE_SYSTEM_PERM)
            && !newPermissions.contains(Permission.MANAGE_SYSTEM_PERM)
            && !systemHasOtherManagers(user.id)
        )
            throw LastRemainingSystemManagerException()
    }

    private fun systemHasOtherManagers(userId: Long) =
        userRepo.findAnyWithManagePermissionsExceptUser(Permission.MANAGE_SYSTEM_PERM.toString(), userId)

    override fun loadUserByUsername(email: String): UserDetails = getByEmail(email)

    fun getCurrentUser(): User {
        val auth = SecurityContextHolder.getContext().authentication
        if (auth is AnonymousAuthenticationToken)
            throw UnauthorizedException()

        val userEmail = auth.name
        return getByEmail(userEmail)
    }

    fun findOrCreateUser(email: String): User =
        userRepo.findByEmail(email).orElseGet {
            userRepo.save(User(email = email))
        }

    @Transactional
    fun inviteUserToCurrentDepartment(email: String): User {
        val user = findOrCreateUser(email)
        val currentUser = getCurrentUser()

        val savedUser = addUserToDepartment(user, currentUser.currentDepartment!!)

        updatePermissions(savedUser.id, EnumSet.copyOf(Permission.getDefaultPermissions()))

        emailSvc.sendUserInvitedEmail(email, currentUser.fullName, currentUser.currentDepartment)

        return savedUser
    }

    fun addUserToDepartment(user: User, department: Department, setNewDeptAsCurrent: Boolean = true): User {
        val userNewDepartments : MutableSet<Department> = user.departments?.toMutableSet() ?: mutableSetOf()
        userNewDepartments.add(department)

        return userRepo.save(
            user.copy(
                departments = userNewDepartments,
                currentDepartment = (if (setNewDeptAsCurrent) department else user.currentDepartment)
            )
        )
    }

    @Transactional
    fun removeUserFromCurrentDepartment(userId: Long) {
        val user = getById(userId)
        validateDeptPermissionsEdit(user, EnumSet.noneOf(Permission::class.java))

        val userPermission = user.userPermission
        if (userPermission != null) userPermissionRepo.delete(userPermission)

        val currentDepartment = getCurrentUser().currentDepartment
        if (currentDepartment != null) removeUserFromDepartment(getById(userId), currentDepartment)
    }

    fun removeUserFromDepartment(user: User, department: Department): User {
        val departmentSet = user.departments?.toMutableSet()

        if (departmentSet != null && departmentSet.any { it.id == department.id }) {
            val userNewDepartments = departmentSet.filter { it.id != department.id }.toSet()

            return userRepo.save(
                user.copy(
                    departments = userNewDepartments,
                    currentDepartment = (if (user.currentDepartment?.id == department.id) userNewDepartments.firstOrNull() else user.currentDepartment)
                )
            )
        }

        return user
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
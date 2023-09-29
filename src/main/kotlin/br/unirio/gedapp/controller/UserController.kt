package br.unirio.gedapp.controller

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.controller.exceptions.UnauthorizedException
import br.unirio.gedapp.controller.exceptions.UserAlreadyInvitedException
import br.unirio.gedapp.domain.Permission
import br.unirio.gedapp.domain.User
import br.unirio.gedapp.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/users")
class UserController(@Autowired private val userSvc: UserService) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<User> {
        val user = userSvc.getById(id)
        return ResponseEntity.ok(user)
    }

    @PreAuthorize("hasAuthority('MANAGE_SYSTEM_PERM')")
    @GetMapping
    fun getAllUsers(): ResponseEntity<Iterable<User>> {
        val allUsers = userSvc.getAllUsers()
        return ResponseEntity.ok(allUsers)
    }

    @PreAuthorize("hasAuthority('MANAGE_DEPT_PERM') or hasAuthority('INVITE_USERS')")
    @GetMapping("/currentdept")
    fun getAllUsersFromCurrentDepartment(): ResponseEntity<Iterable<User>> {
        val usersFromCurrentDept = userSvc.getAllUsersInCurrentDepartment()
        return ResponseEntity.ok(usersFromCurrentDept)
    }

    @PreAuthorize("hasAuthority('INVITE_USERS')")
    @PostMapping("/invite")
    fun inviteUser(@RequestParam email: String): ResponseEntity<User> {
        if (userSvc.checkIfUserHasAccessToCurrentDepartment(email))
            throw UserAlreadyInvitedException()

        val user = userSvc.inviteUserToCurrentDepartment(email)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PreAuthorize("hasAuthority('INVITE_USERS')")
    @PatchMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody newDataUser: User): ResponseEntity<User> {
        val modifiedUser = userSvc.update(id, newDataUser)
        return ResponseEntity.ok(modifiedUser)
    }

    @PreAuthorize("hasAuthority('MANAGE_DEPT_PERM') or hasAuthority('MANAGE_SYSTEM_PERM')")
    @PatchMapping("/{id}/permission")
    fun updateUserPermission(
        @PathVariable id: Long,
        @RequestParam(required = false, defaultValue = "") type: String,
        @RequestBody permissions: EnumSet<Permission>) =
        userSvc.updatePermissions(id, permissions, type)
            .let { ResponseEntity.ok(it) }

    @PreAuthorize("hasAuthority('MANAGE_DEPT_PERM') or hasAuthority('MANAGE_SYSTEM_PERM')")
    @PatchMapping("/permission")
    fun updateUsersPermission(
        @RequestParam(required = false, defaultValue = "") type: String,
        @RequestBody userPermissionMap: Map<Long, EnumSet<Permission>>): ResponseEntity<List<User>> {
        val (modifiedUserList, numErrors) = userSvc.batchUpdatePermissions(userPermissionMap, type)

        return when (numErrors) {
            0 -> ResponseEntity.ok(modifiedUserList)
            modifiedUserList.size -> ResponseEntity.status(HttpStatus.CONFLICT).body(modifiedUserList)
            else -> ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).body(modifiedUserList)
        }
    }

    @PreAuthorize("hasAuthority('MANAGE_DEPT_PERM')")
    @DeleteMapping("/{id}")
    fun removeUserAccess(@PathVariable id: Long): ResponseEntity<User> {
        userSvc.removeUserFromCurrentDepartment(id)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/loggedUserInfo")
    fun getLoggedUserInfo(): ResponseEntity<User> =
        try {
            ResponseEntity.ok(userSvc.getCurrentUser())
        } catch (e: ResourceNotFoundException) {
            throw UnauthorizedException()
        }
}
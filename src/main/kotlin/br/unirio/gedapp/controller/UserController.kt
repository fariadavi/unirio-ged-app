package br.unirio.gedapp.controller

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.controller.exceptions.UnauthorizedException
import br.unirio.gedapp.domain.User
import br.unirio.gedapp.repository.UserRepository
import br.unirio.gedapp.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(
    @Autowired private val userSvc: UserService,
    @Autowired private val userRepo: UserRepository
) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): ResponseEntity<User> {
        val user = userSvc.getById(id)
        return ResponseEntity.ok(user)
    }

    @GetMapping
    fun getAllUsers(): ResponseEntity<Iterable<User>> {
        val allUsers = userRepo.findAll()
        return ResponseEntity.ok(allUsers)
    }

    @GetMapping("/department/{id}")
    fun getAllUsersFromDepartment(@PathVariable id: Long): ResponseEntity<Iterable<User>> {
        val usersFromDept = userRepo.findByDepartmentsId(id)
        return ResponseEntity.ok(usersFromDept)
    }

    @PostMapping
    fun addUser(@RequestBody user: User): ResponseEntity<User> {
        val createdUser = userRepo.save(user)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PatchMapping("/{id}")
    fun updateUser(@PathVariable id: Long, @RequestBody newDataUser: User): ResponseEntity<User> {
        val modifiedUser = userSvc.update(id, newDataUser)
        return ResponseEntity.ok(modifiedUser)
    }

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long): ResponseEntity<String> {
        if (!userRepo.existsById(id))
            throw ResourceNotFoundException()

        userRepo.deleteById(id)
        return ResponseEntity.ok("User '$id' have been successfully deleted")
    }

    @GetMapping("/loggedUserInfo")
    fun getLoggedUserInfo(): ResponseEntity<User> =
        try {
            ResponseEntity.ok(userSvc.getCurrentUser())
        } catch (e: ResourceNotFoundException) {
            throw UnauthorizedException()
        }
}
package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.User
import br.unirio.gedapp.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/users")
class UserController(@Autowired private val userRepo: UserRepository) {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: Long): User = userRepo.findById(id).get()

    @GetMapping
    fun getAllUsers(): Iterable<User> = userRepo.findAll()

    @PostMapping
    fun addUser(@RequestBody user: User): User = userRepo.save(user)

    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: Long) = userRepo.deleteById(id)
}
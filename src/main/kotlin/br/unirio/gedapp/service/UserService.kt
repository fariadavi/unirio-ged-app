package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.User
import br.unirio.gedapp.repository.UserRepository
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class UserService(val userRepo: UserRepository) : UserDetailsService {

    fun getById(id: Long): User =
        userRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }

    fun update(userId: Long, newDataUser: User): User {
        var updatedUser = getById(userId)

        if (newDataUser.firstName.isNotBlank())
            updatedUser = updatedUser.copy(firstName = newDataUser.firstName)

        if (newDataUser.surname.isNotBlank())
            updatedUser = updatedUser.copy(surname = newDataUser.surname)

        if (newDataUser.email.isNotBlank())
            updatedUser = updatedUser.copy(email = newDataUser.email)

        if (newDataUser.permissions != null)
            updatedUser = updatedUser.copy(permissions = newDataUser.permissions)

        if (!newDataUser.departments.isNullOrEmpty())
            updatedUser = updatedUser.copy(departments = newDataUser.departments)

        return userRepo.save(updatedUser)
    }

    override fun loadUserByUsername(email: String): UserDetails {
        try {
            return userRepo.findByEmail(email).first()
        } catch (e: NoSuchElementException) {
            throw UsernameNotFoundException("User with the specified email was not found")
        }
    }
}


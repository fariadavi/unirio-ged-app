package br.unirio.gedapp.controller

import br.unirio.gedapp.service.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(@Autowired private val authSvc: AuthenticationService) {

    @PostMapping("/google/login")
    fun loginWithGoogle(@RequestBody accessToken: String): ResponseEntity<String> {
        val jwt = authSvc.loginWithGoogle(accessToken)
        return ResponseEntity.ok(jwt)
    }

    @GetMapping("/refresh")
    fun refreshToken() =
        authSvc.refreshTokenForCurrentUser()
            .let{ ResponseEntity.ok(it) }
}
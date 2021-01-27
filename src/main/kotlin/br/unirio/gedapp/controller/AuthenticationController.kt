package br.unirio.gedapp.controller

import br.unirio.gedapp.service.AuthenticationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthenticationController(@Autowired private val authSvc: AuthenticationService) {

    @PostMapping("/google/login")
    fun loginWithGoogle(@RequestBody accessToken : String): ResponseEntity<String> {
        val jwt = authSvc.loginWithGoogle(accessToken)
        return ResponseEntity.ok(jwt)
    }

    @PostMapping("/logout")
    fun logout(): ResponseEntity<String> = ResponseEntity.ok("")
}
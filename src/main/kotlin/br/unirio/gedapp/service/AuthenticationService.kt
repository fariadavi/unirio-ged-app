package br.unirio.gedapp.service

import org.springframework.stereotype.Service


@Service
class AuthenticationService(val userSvc: UserService) {

    fun loginWithGoogle(accessToken: String): String = ""
}

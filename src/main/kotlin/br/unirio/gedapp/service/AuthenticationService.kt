package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.yml.AuthConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service

@Service
class AuthenticationService(
    @Autowired val authConfig: AuthConfig,
    val jwtProvider: JwtProvider,
    val userSvc: UserService
) {

    fun loginWithGoogle(accessToken: String): String {

        val verifier = GoogleIdTokenVerifier.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory()
        ).setAudience(listOf(authConfig.clientId)).build()

        val idToken: GoogleIdToken = verifier.verify(accessToken) ?: throw ResourceNotFoundException()

        val email: String = idToken.payload.email

        val userDetails: UserDetails = userSvc.loadUserByUsername(email)
        val authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

        return jwtProvider.generateToken(authenticationTokenObj)
    }
}

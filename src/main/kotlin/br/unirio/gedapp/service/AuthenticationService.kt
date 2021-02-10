package br.unirio.gedapp.service

import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuthenticationService(
    val jwtProvider: JwtProvider,
    val userSvc: UserService
) {

    fun loginWithGoogle(accessToken: String): String {

        val verifier = GoogleIdTokenVerifier.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory()
        ).setAudience(listOf("")).build() // TODO get client id from properties file

        val idToken: GoogleIdToken = verifier.verify(accessToken) ?: throw ResourceNotFoundException()

        val email: String = idToken.payload.email

        val userDetails: UserDetails = try { userSvc.loadUserByUsername(email) } catch (e: NoSuchElementException) { throw ResourceNotFoundException() }
        val authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

        return jwtProvider.generateToken(authenticationTokenObj)
    }
}

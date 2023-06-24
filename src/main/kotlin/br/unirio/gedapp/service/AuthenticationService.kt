package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.yml.AuthConfig
import br.unirio.gedapp.configuration.yml.JwtConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.User
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

private val logger = KotlinLogging.logger {}

@Service
class AuthenticationService(
    @Autowired val authConfig: AuthConfig,
    @Autowired val jwtConfig: JwtConfig,
    val jwtProvider: JwtProvider,
    val userSvc: UserService
) {

    fun loginWithGoogle(accessToken: String): String {

        val verifier = GoogleIdTokenVerifier.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory()
        ).setAudience(listOf(authConfig.clientId)).build()

        val idToken = verifier.verify(accessToken) ?: throw ResourceNotFoundException()

        val email = idToken.payload.email

        val user = userSvc.getByEmail(email)

        updateUserName(idToken, user)

        return generateTokenForUser(user)
    }

    fun refreshTokenForCurrentUser() = generateTokenForUser(userSvc.getCurrentUser())

    private fun generateTokenForUser(user: User): String {
        val authenticationTokenObj = UsernamePasswordAuthenticationToken(user as UserDetails, null)
        val now = Instant.now()
        val issuedAt = now.toEpochMilli()
        val expirationTime = now.plus(jwtConfig.expiration, ChronoUnit.SECONDS).toEpochMilli()

        return jwtProvider.generateToken(authenticationTokenObj, issuedAt, expirationTime)
    }

    private fun updateUserName(
        idToken: GoogleIdToken,
        user: User
    ) {
        try {
            val firstName: String = idToken.payload["given_name"] as String
            val surname: String = idToken.payload["family_name"] as String

            var newData = User()
            if (user.firstName != firstName)
                newData = newData.copy(firstName = firstName)

            if (user.surname != surname)
                newData = newData.copy(surname = surname)

            if (user.currentDepartment == null && !user.departments.isNullOrEmpty())
                newData = newData.copy(currentDepartment = user.departments.first())

            if (user.firstName != firstName || user.surname != surname || user.currentDepartment == null)
                userSvc.update(user, newData)
        } catch (e: Exception) {
            logger.error("Unable to update username of user ${user.id}", e)
        }
    }
}

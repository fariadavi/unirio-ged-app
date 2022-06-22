package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.yml.AuthConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.User
import com.google.api.client.auth.openidconnect.IdToken
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

        val user = userSvc.getByEmail(email)

        updateUserName(idToken, user)

        val authenticationTokenObj = UsernamePasswordAuthenticationToken(user as UserDetails, null, user.authorities)

        return jwtProvider.generateToken(authenticationTokenObj)
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

            if (user.currentDepartment == null && user.departments != null && user.departments.isNotEmpty())
                newData = newData.copy(currentDepartment = user.departments.first())

            if (user.firstName != firstName || user.surname != surname || user.currentDepartment == null)
                userSvc.update(user, newData)
        } catch (e: Exception) {
            // TODO log error
        }
    }
}

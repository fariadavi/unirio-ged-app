package br.unirio.gedapp.configuration.web.filter

import br.unirio.gedapp.configuration.yml.JwtConfig
import br.unirio.gedapp.service.JwtProvider
import br.unirio.gedapp.service.UserService
import mu.KotlinLogging
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val kLogger = KotlinLogging.logger {}

class AuthenticationFilter(
    private val jwtConfig: JwtConfig,
    private val jwtProvider: JwtProvider,
    private val userSvc: UserService
) : OncePerRequestFilter() {

    override fun doFilterInternal(req: HttpServletRequest, resp: HttpServletResponse, chain: FilterChain) {
        val authHeader: String? = req.getHeader(jwtConfig.header)

        if (authHeader != null && authHeader.startsWith(jwtConfig.prefix)) {
            val token: String = authHeader.replace(jwtConfig.prefix, "").trim()

            try {
                val claims: Claims = jwtProvider.getClaimsFromJWT(token)
                val email = claims.subject

                // load user info from the public schema and set it in the context
                var userDetails: UserDetails = userSvc.loadUserByUsername(email)
                var authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null)

                SecurityContextHolder.getContext().authentication = authenticationTokenObj

                // load user info again
                // because now we have user's current department in the context,
                // so user data from department schema will be loaded as well
                userDetails = userSvc.loadUserByUsername(email)
                authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authenticationTokenObj.details = WebAuthenticationDetailsSource().buildDetails(req)

                SecurityContextHolder.getContext().authentication = authenticationTokenObj

            } catch (e: ExpiredJwtException) {
                kLogger.error("Rejected attempt to login with expired token $token.", e.message)
                SecurityContextHolder.clearContext()

            } catch (e: Exception) {
                kLogger.error("Error authenticating token $token", e)
                SecurityContextHolder.clearContext()
            }
        }

        chain.doFilter(req, resp)
    }
}
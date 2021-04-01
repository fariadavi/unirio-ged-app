package br.unirio.gedapp.configuration.web.filter

import br.unirio.gedapp.configuration.yml.JwtConfig
import br.unirio.gedapp.service.JwtProvider
import br.unirio.gedapp.service.UserService
import io.jsonwebtoken.Claims
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

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

                val userDetails: UserDetails = try { userSvc.loadUserByUsername(email) } catch (e: NoSuchElementException) { throw ResourceNotFoundException() }
                val authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authenticationTokenObj.details = WebAuthenticationDetailsSource().buildDetails(req)

                SecurityContextHolder.getContext().authentication = authenticationTokenObj

            } catch (e: Exception) {
                SecurityContextHolder.clearContext()
            }
        }

        chain.doFilter(req, resp)
    }
}
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

                var userDetails: UserDetails = userSvc.loadUserByUsername(email)
                var authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)

                SecurityContextHolder.getContext().authentication = authenticationTokenObj

                //TODO this is a gambiarra. needs to be fixed
                userDetails = userSvc.loadUserByUsername(email)
                authenticationTokenObj = UsernamePasswordAuthenticationToken(userDetails, null, userDetails.authorities)
                authenticationTokenObj.details = WebAuthenticationDetailsSource().buildDetails(req)

                SecurityContextHolder.getContext().authentication = authenticationTokenObj

            } catch (e: Exception) {
                logger.error("Erro no filtro de autenticação")
                SecurityContextHolder.clearContext()
            }
        }

        chain.doFilter(req, resp)
    }
}
package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.JwtConfig
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtProvider(@Autowired val jwtConfig: JwtConfig) {

    fun generateToken(authTokenObj: Authentication): String {

        val keyBytes = Decoders.BASE64.decode(jwtConfig.secret)
        val key = Keys.hmacShaKeyFor(keyBytes)

        val currentTime = Date()

        return Jwts.builder()
            .setSubject(authTokenObj.name)
            .claim("authorities", authTokenObj.authorities.map { obj: GrantedAuthority -> obj.authority })
            .setIssuedAt(currentTime)
            .setExpiration(Date(currentTime.time + (jwtConfig.expiration * 1000)))
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getClaimsFromJWT(token: String): Claims =
        Jwts.parserBuilder()
            .setSigningKey(jwtConfig.secret)
            .build()
            .parseClaimsJws(token)
            .body
}
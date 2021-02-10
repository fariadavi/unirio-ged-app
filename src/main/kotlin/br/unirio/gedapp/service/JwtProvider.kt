package br.unirio.gedapp.service

import io.jsonwebtoken.*
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Service
import java.util.*

@Service
class JwtProvider {

    fun generateToken(authTokenObj: Authentication): String {

        val keyBytes = Decoders.BASE64.decode("") // TODO get base64 secret key string from properties file
        val key = Keys.hmacShaKeyFor(keyBytes)

        val currentTime = Date()

        return Jwts.builder()
            .setSubject(authTokenObj.name)
            .claim("authorities", authTokenObj.authorities.map { obj: GrantedAuthority -> obj.authority })
            .setIssuedAt(currentTime)
            .setExpiration(Date(currentTime.time + (10 * 60 * 1000))) // TODO replace fixed 10min with properties file variable
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun getClaimsFromJWT(token: String?): Claims? {

        val keyBytes = Decoders.BASE64.decode("") // TODO get base64 secret key string from properties file
        val key = Keys.hmacShaKeyFor(keyBytes)

        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).body
    }

    fun validateToken(authToken: String?): Boolean {
        try {
            getClaimsFromJWT(authToken)
            return true
        } catch (ex: MalformedJwtException) {
            print("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            print("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            print("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            print("JWT claims string is empty.")
        }

        return false
    }
}
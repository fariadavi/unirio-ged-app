package br.unirio.gedapp.configuration.yml

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("security.jwt")
@ConstructorBinding
data class JwtConfig(
    val header: String,
    val prefix: String,
    val secret: String,
    val expiration: Long
)
package br.unirio.gedapp.configuration.yml

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("security.auth")
@ConstructorBinding
data class AuthConfig(
    val clientId: String
)
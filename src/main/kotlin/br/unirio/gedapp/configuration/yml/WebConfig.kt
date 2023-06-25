package br.unirio.gedapp.configuration.yml

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("security.web")
@ConstructorBinding
data class WebConfig(
    val allowedOrigins: String
)
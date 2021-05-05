package br.unirio.gedapp.configuration.yml

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("application.storage")
@ConstructorBinding
data class StorageConfig(
    val path: String
)
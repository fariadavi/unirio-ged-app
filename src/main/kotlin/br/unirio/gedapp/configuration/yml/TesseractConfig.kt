package br.unirio.gedapp.configuration.yml

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConfigurationProperties("application.tesseract")
@ConstructorBinding
data class TesseractConfig(
    val language: String
)

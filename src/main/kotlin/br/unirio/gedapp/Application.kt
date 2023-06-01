package br.unirio.gedapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRepositoriesAutoConfiguration
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [
	QuartzAutoConfiguration::class,
	ReactiveElasticsearchRepositoriesAutoConfiguration::class
])
@ConfigurationPropertiesScan("br.unirio.gedapp.configuration")
class Application

fun main(args: Array<String>) {
	runApplication<Application>(*args)
}

package br.unirio.gedapp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GedAppApplication

fun main(args: Array<String>) {
	runApplication<GedAppApplication>(*args)
}

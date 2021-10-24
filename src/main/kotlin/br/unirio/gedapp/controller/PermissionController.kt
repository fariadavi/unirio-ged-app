package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Permission
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/permissions")
class PermissionController {

    @GetMapping
    fun getAllPermissions(): ResponseEntity<Array<Permission>> {
        return ResponseEntity.ok(Permission.values())
    }
}
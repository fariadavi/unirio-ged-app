package br.unirio.gedapp.controller

import br.unirio.gedapp.domain.Permission
import br.unirio.gedapp.domain.PermissionLevel
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

    @GetMapping("/default")
    fun getDefaultPermissions(): ResponseEntity<Array<Permission>> {
        return ResponseEntity.ok(Permission.getDefaultPermissions().toTypedArray())
    }

    @GetMapping("/department")
    fun getDepartmentPermissions(): ResponseEntity<Array<Permission>> {
        return ResponseEntity.ok(Permission.getDepartmentPermissions().toTypedArray())
    }

    @GetMapping("/system")
    fun getSystemPermissions(): ResponseEntity<Array<Permission>> {
        return ResponseEntity.ok(Permission.getSystemPermissions().toTypedArray())
    }
}
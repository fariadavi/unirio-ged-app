package br.unirio.gedapp.configuration

import java.security.Principal

import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import java.util.*
import java.util.function.Predicate

@Component
class TenantIdentifierResolver : CurrentTenantIdentifierResolver {

    companion object {
        const val DEFAULT_TENANT = "public"
    }

    override fun resolveCurrentTenantIdentifier(): String {
        return Optional.ofNullable(SecurityContextHolder.getContext().authentication)
            .filter(Predicate.not { authentication -> authentication is AnonymousAuthenticationToken })
            .map { obj: Principal -> obj.name }
            .orElse(DEFAULT_TENANT)
    }

    override fun validateExistingCurrentSessions(): Boolean = true
}
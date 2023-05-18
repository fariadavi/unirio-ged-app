package br.unirio.gedapp.configuration.web.tenant

import br.unirio.gedapp.domain.User
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class TenantIdentifierResolver : CurrentTenantIdentifierResolver {

    companion object {
        const val DEFAULT_TENANT = "public"
    }

    override fun resolveCurrentTenantIdentifier() = (
            SecurityContextHolder.getContext().authentication
                ?.takeUnless { it is AnonymousAuthenticationToken }?.principal
                ?.takeIf { it is User }
                ?.let { it as User }
            )?.currentDepartment?.acronym?.lowercase()
        ?: DEFAULT_TENANT

    override fun validateExistingCurrentSessions(): Boolean = true
}
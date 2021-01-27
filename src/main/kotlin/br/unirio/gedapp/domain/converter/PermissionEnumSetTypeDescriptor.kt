package br.unirio.gedapp.domain.converter

import br.unirio.gedapp.domain.Permission
import java.util.EnumSet

import org.hibernate.type.descriptor.WrapperOptions

import org.hibernate.type.descriptor.java.AbstractTypeDescriptor

class PermissionEnumSetTypeDescriptor : AbstractTypeDescriptor<EnumSet<*>>(EnumSet::class.java) {

    companion object {
        private const val SEPARATOR: String = ";"
    }

    override fun toString(set: EnumSet<*>): String = set.joinToString(SEPARATOR)

    override fun fromString(string: String): EnumSet<*>? {
        val list = string
            .split(SEPARATOR)
            .mapNotNull { x ->
                try {
                    Permission.valueOf(x)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            .toList()

        return if (list.isNotEmpty()) EnumSet.copyOf(list) else null
    }


    override fun <X> wrap(value: X, options: WrapperOptions): EnumSet<*>? =
        when (value) {
            null -> null
            is EnumSet<*> -> value
            is String -> fromString(value)
            else -> throw unknownWrap(value.javaClass)
        }

    override fun <X> unwrap(value: EnumSet<*>, type: Class<X>, options: WrapperOptions?): X? {
        if (value == null)
            return null

        if (EnumSet::class.java.isAssignableFrom(type))
            return value as X

        if (String::class.java.isAssignableFrom(type))
            return toString(value) as X

        throw unknownUnwrap(type)
    }
}
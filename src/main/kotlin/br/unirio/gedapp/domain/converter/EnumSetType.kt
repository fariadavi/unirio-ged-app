package br.unirio.gedapp.domain.converter

import br.unirio.gedapp.domain.Permission
import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor
import org.hibernate.usertype.DynamicParameterizedType
import java.util.*
import java.util.EnumSet

class EnumSetType : AbstractSingleColumnStandardBasicType<EnumSet<*>>(VarcharTypeDescriptor.INSTANCE, null), DynamicParameterizedType {

    override fun getName(): String = "enum-set"

    override fun setParameterValues(parameters: Properties) =
        when (parameters["enumClass"].toString()) {
            Permission::class.qualifiedName -> javaTypeDescriptor = PermissionEnumSetTypeDescriptor()
            else -> throw ClassNotFoundException()
        }

    override fun getRegistrationKeys(): Array<String> = arrayOf(name, "EnumSet", EnumSet::class.java.name)
}
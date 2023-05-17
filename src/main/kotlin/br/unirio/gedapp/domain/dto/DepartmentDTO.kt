package br.unirio.gedapp.domain.dto

import br.unirio.gedapp.domain.Department
import java.math.BigInteger

data class DepartmentDTO(
    var id: BigInteger,
    var name: String?,
    var acronym: String?,
    var numUsers: BigInteger?
) {
    constructor(department: Department) : this(
        department.id.toBigInteger(),
        department.name,
        department.acronym,
        null
    )
}

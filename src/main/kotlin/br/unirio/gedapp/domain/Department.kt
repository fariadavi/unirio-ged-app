package br.unirio.gedapp.domain

import br.unirio.gedapp.domain.dto.DepartmentDTO
import javax.persistence.*

@Entity
@Table(schema = "public")
@NamedNativeQuery(
    name = "Department.findAllWithUserCount",
    query = "SELECT d.id, d.name, d.acronym, count(pud.*) as numUsers " +
            "FROM public.department d " +
            "LEFT JOIN public.platform_user_department pud ON d.id = pud.department_id " +
            "GROUP BY d.id, d.name, d.acronym",
    resultSetMapping = "DepartmentDTO"
)
@SqlResultSetMapping(
    name = "DepartmentDTO",
    classes = [ConstructorResult(
        targetClass = DepartmentDTO::class,
        columns = arrayOf(
            ColumnResult(name = "id"),
            ColumnResult(name = "name"),
            ColumnResult(name = "acronym"),
            ColumnResult(name = "numUsers")
        )
    )]
)
data class Department(

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = -1,

    val name: String? = null,

    val acronym: String? = null
)
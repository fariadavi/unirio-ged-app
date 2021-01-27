package br.unirio.gedapp.service

import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.repository.DepartmentRepository
import br.unirio.gedapp.repository.DocumentRepository
import org.springframework.stereotype.Service
import javax.transaction.Transactional

@Service
class DepartmentService(
    val deptRepo: DepartmentRepository,
    val tenantSvc: TenantService,
    val docRepo: DocumentRepository
) {

    @Transactional
    fun createNewDepartment(dept: Department): Department {
        val saved: Department = deptRepo.save(dept)
        tenantSvc.initDatabase(saved.acronym.toLowerCase())
        return saved
    }
}
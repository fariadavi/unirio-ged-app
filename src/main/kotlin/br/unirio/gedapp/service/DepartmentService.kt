package br.unirio.gedapp.service

import br.unirio.gedapp.configuration.yml.StorageConfig
import br.unirio.gedapp.controller.exceptions.ResourceNotFoundException
import br.unirio.gedapp.domain.Department
import br.unirio.gedapp.repository.DepartmentRepository
import br.unirio.gedapp.util.FileUtils
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.nio.file.NoSuchFileException
import javax.transaction.Transactional

private val logger = KotlinLogging.logger {}

@Service
class DepartmentService(
    @Autowired storageConfig: StorageConfig,
    val docSvc: DocumentService,
    val userSvc: UserService,
    val deptRepo: DepartmentRepository,
    val tenantSvc: TenantService
) {
    val fileUtils: FileUtils = FileUtils(storageConfig)

    fun getById(id: Long): Department =
        deptRepo
            .findById(id)
            .orElseThrow { ResourceNotFoundException() }

    fun findAllDepartments() = deptRepo.findAllWithUserCount()

    @Transactional
    fun createNewDepartment(dept: Department, userId: Long): Department {
        val savedDept: Department = deptRepo.save(dept)
        tenantSvc.initDatabase(savedDept.acronym!!.lowercase(), userId)
        return savedDept
    }

    @Transactional
    fun update(deptId: Long, newDataDept: Department): Department {
        var existingDept = getById(deptId)
        val currentAcronym = existingDept.acronym!!

        if (!newDataDept.name.isNullOrBlank())
            existingDept = existingDept.copy(name = newDataDept.name)

        if (!newDataDept.acronym.isNullOrBlank() && newDataDept.acronym.length <= 5)
            existingDept = existingDept.copy(acronym = newDataDept.acronym)

        val updatedDept = deptRepo.save(existingDept)

        if (currentAcronym.lowercase() != updatedDept.acronym!!.lowercase() && updatedDept.acronym.length <= 5) {
            try {
                fileUtils.renameFolder(currentAcronym.lowercase(), updatedDept.acronym.lowercase())
            } catch (e: NoSuchFileException) {
                logger.error("Folder $currentAcronym not found. Maybe no documents exist for this department?", e)
            }

            docSvc.updateDocsTenantAcronym(currentAcronym.lowercase(), updatedDept.acronym.lowercase())

            tenantSvc.renameSchema(currentAcronym.lowercase(), updatedDept.acronym.lowercase())
        }

        return updatedDept
    }

    fun deleteById(deptId: Long) = delete(getById(deptId))

    @Transactional
    fun delete(dept: Department) {
        val tenant = dept.acronym!!.lowercase()

        //remove all users from dept
        userSvc.getAllUsersInDepartment(dept).forEach { userSvc.removeUserFromDepartment(it, dept) }

        //delete docs on elasticsearch
        docSvc.deleteAllByTenant(tenant)

        //drop tenant schema
        tenantSvc.dropSchema(tenant)

        //delete department entry in public schema
        deptRepo.delete(dept)

        //delete tenant directory and all files it contains
        fileUtils.deleteDirectory(tenant)
    }

    fun batchUpdate(editedDepartments: List<Department>): Pair<List<Department>, Int> {
        var numErrors = 0
        val modifiedDepartments = mutableListOf<Department>()

        editedDepartments.forEach {
            try {
                val modifiedDept = update(it.id, it)
                modifiedDepartments.add(modifiedDept)
            } catch (e: Exception) {
                modifiedDepartments.add(getById(it.id))
                numErrors++
            }
        }

        return Pair(modifiedDepartments, numErrors)
    }
}
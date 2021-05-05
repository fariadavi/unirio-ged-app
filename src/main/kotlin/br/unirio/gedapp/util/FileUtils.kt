package br.unirio.gedapp.util

import br.unirio.gedapp.configuration.yml.StorageConfig
import br.unirio.gedapp.domain.Document
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileUtils(private val storageConfig: StorageConfig) {

    fun getFile(document: Document): File =
        getFile(document.tenant, document.id!!, document.fileName)

    fun getFile(tenant: String, docId: String, fileName: String): File =
        Path.of(storageConfig.path, tenant, "${docId}_${fileName}").toFile()

    fun transferFile(file: MultipartFile, tenant: String, docId: String) =
        transferFile(file, tenant, docId, null)

    fun transferFile(file: MultipartFile, tenant: String, docId: String, fileName: String?) {
        if (Files.notExists(Path.of(storageConfig.path, tenant)))
            Files.createDirectory(Path.of(storageConfig.path, tenant))

        file.transferTo(
            Path.of(storageConfig.path, tenant, "${docId}_${fileName ?: file.originalFilename}")
        )
    }

    fun deleteFile(tenant: String, docId: String, fileName: String) =
        Files.deleteIfExists(Path.of(storageConfig.path, tenant, "${docId}_${fileName}"))
}
package br.unirio.gedapp.util

import br.unirio.gedapp.configuration.yml.StorageConfig
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class FileUtils(private val storageConfig: StorageConfig) {

    fun getFilePath(tenant: String, docId: String, fileName: String): Path {
        if (Files.notExists(Path.of(storageConfig.path, tenant)))
            Files.createDirectory(Path.of(storageConfig.path, tenant))

        return Path.of(storageConfig.path, tenant, "${docId}_${fileName}")
    }

    fun getFile(tenant: String, docId: String, fileName: String): File =
        getFilePath(tenant, docId, fileName).toFile()

    fun transferFile(file: MultipartFile, tenant: String, docId: String) =
        transferFile(file, tenant, docId, null)

    fun transferFile(file: MultipartFile, tenant: String, docId: String, fileName: String?) =
        file.transferTo(getFilePath(tenant, docId, fileName ?: file.originalFilename!!))

    fun deleteFile(tenant: String, fileName: String) =
        Files.deleteIfExists(Path.of(storageConfig.path, tenant, fileName))

    fun deleteFile(tenant: String, docId: String, fileName: String) =
        Files.deleteIfExists(Path.of(storageConfig.path, tenant, "${docId}_${fileName}"))

    fun renameFolder(oldName: String, newName: String) =
        Files.move(
            Path.of(storageConfig.path, oldName),
            Path.of(storageConfig.path, newName)
        )
}
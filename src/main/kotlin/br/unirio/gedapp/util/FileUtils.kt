package br.unirio.gedapp.util

import br.unirio.gedapp.configuration.yml.StorageConfig
import mu.KotlinLogging
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

class FileUtils(private val storageConfig: StorageConfig) {

    fun getFilePath(tenant: String, docId: String, fileName: String): Path {
        val path = Path.of(storageConfig.path, tenant)
        if (Files.notExists(path))
            runCatching { Files.createDirectory(path) }
                .onFailure { logger.warn { "Handled an attempt to create '${path.toAbsolutePath()}' directory. It's possible directory already existed. $it" } }

        return Path.of(storageConfig.path, tenant, "${docId}_${fileName}")
    }

    fun getFile(tenant: String, docId: String, fileName: String): File =
        getFilePath(tenant, docId, fileName).toFile()

    fun transferFile(file: MultipartFile, tenant: String, docId: String) =
        transferFile(file, tenant, docId, null)

    fun transferFile(file: MultipartFile, tenant: String, docId: String, fileName: String?) =
        file.transferTo(getFilePath(tenant, docId, fileName ?: file.originalFilename!!))

    fun deleteDirectory(tenant: String) =
        Files.walk(Path.of(storageConfig.path, tenant))
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete)


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
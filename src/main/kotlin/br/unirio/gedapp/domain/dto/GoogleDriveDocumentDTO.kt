package br.unirio.gedapp.domain.dto

import java.time.LocalDate

data class GoogleDriveDocumentDTO(
    var id: String,
    var name: String,
    var description: String,
    var mimeType: String,
    var lastEditedUtc: LocalDate,
    var type: String,
    var embedUrl: String,
    var iconUrl: String,
    var isShared: Boolean,
    var organizationDisplayName: Long,
    var serviceId: String,
    var sizeBytes: Long,
    var url: String
) { }
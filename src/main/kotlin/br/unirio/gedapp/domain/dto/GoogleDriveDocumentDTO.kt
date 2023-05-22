package br.unirio.gedapp.domain.dto

data class GoogleDriveDocumentDTO(
    var id: String,
    var name: String,
    var description: String,
    var mimeType: String,
    var lastEditedUtc: String,
    var type: String,
    var embedUrl: String,
    var iconUrl: String,
    var isShared: Boolean,
    var organizationDisplayName: String?,
    var serviceId: String,
    var sizeBytes: Long,
    var url: String,
    var category: Long,
    var date: String,
    var email: String,
    var token: String
)
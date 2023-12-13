package br.unirio.gedapp.domain.dto

data class GoogleDriveDocumentDTO(
    var id: String,
    var name: String,
    var description: String?,
    var mimeType: String,
    var type: String,
    var category: Long,
    var date: String?,
    var email: String,
    var token: String
)
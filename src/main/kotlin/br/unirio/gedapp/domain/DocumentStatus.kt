package br.unirio.gedapp.domain

enum class DocumentStatus {
    PROCESSING,
    PENDING,
    PROCESSED,
    EMPTY_CONTENT,
    FAILED_IMPORT,
    FAILED_PROCESSING
}
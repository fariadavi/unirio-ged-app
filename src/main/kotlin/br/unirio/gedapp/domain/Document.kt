package br.unirio.gedapp.domain

import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDate
import java.time.LocalDateTime

@Document(indexName = "documents")
data class Document(

    @Id @Field(type = FieldType.Text)
    val id: String? = null,

    @Field(type = FieldType.Text, name = "department_acronym")
    val tenant: String = "",

    @Field(type = FieldType.Text)
    val fileName: String = "",

    @Field(type = FieldType.Text)
    val title: String = "",

    @Field(type = FieldType.Text)
    val summary: String? = null,

    @Field(type = FieldType.Date, format = DateFormat.date)
    val date: LocalDate? = null,

    @Field(type = FieldType.Text)
    val content: String = "",

    @Field(type = FieldType.Text)
    val status: DocumentStatus = DocumentStatus.NOT_PROCESSED,

    @Field(type = FieldType.Long, name = "category_id")
    val category: Long = -1L,

    @Field(type = FieldType.Long, name = "platform_user_id")
    val registeredBy: Long = -1L,

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    val registeredAt: LocalDateTime? = null
)
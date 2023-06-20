package br.unirio.gedapp.domain

import com.fasterxml.jackson.annotation.JsonAlias
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.DateFormat
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import java.time.LocalDate
import java.time.LocalDateTime

@Document(indexName = "documents")
data class Document(

    @Id @Field(type = FieldType.Keyword)
    val id: String? = null,

    @Field(type = FieldType.Keyword)
    val tenant: String = "",

    @Field(type = FieldType.Keyword)
    val fileName: String = "",

    @Field(type = FieldType.Text, analyzer = "brazilian")
    val title: String = "",

    @Field(type = FieldType.Date, format = [DateFormat.date], pattern = ["yyyy-MM-dd"])
    val date: LocalDate? = null,

    @Field(type = FieldType.Text, analyzer = "brazilian")
    val summary: String? = null,

    @Field(type = FieldType.Keyword)
    val mediaType: String? = null,

    @Field(type = FieldType.Text, analyzer = "brazilian")
    val content: String = "",

    @Field(type = FieldType.Integer)
    val status: Int = DocumentStatus.PENDING.ordinal,

    @JsonAlias("category_id")
    @Field(type = FieldType.Long, name = "category_id")
    val category: Long = -1L,

    @JsonAlias("platform_user_id")
    @Field(type = FieldType.Long, name = "platform_user_id")
    val registeredBy: Long = -1L,

    @Field(type = FieldType.Date, format = [DateFormat.date_hour_minute_second_millis])
    val registeredAt: LocalDateTime? = null
) {
    @Transient
    var searchMatches: List<String> = emptyList()
}
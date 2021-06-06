package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Document
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DocumentRepositoryImpl(@Autowired val mapper: ObjectMapper) : DocumentCustomRepository {

    override fun queryDocuments(
        tenant: String,
        text: String,
        page: Int,
        pageSize: Int,
        category: Long?,
        user: Long?,
        minDate: LocalDate?,
        maxDate: LocalDate?
    ): Pair<Long, Iterable<Document>> {

        val boolQueryBuilder =
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("tenant", tenant))

        if (!text.isNullOrBlank())
            boolQueryBuilder
                .should(QueryBuilders.matchQuery("content", text))
                .should(QueryBuilders.matchQuery("title", text))
                .should(QueryBuilders.matchQuery("summary", text))
                .minimumShouldMatch(1)

        if (category != null)
            boolQueryBuilder.filter(QueryBuilders.termQuery("category_id", category))

        if (user != null)
            boolQueryBuilder.filter(QueryBuilders.termQuery("platform_user_id", user))

        if (minDate != null || maxDate != null) {
            val rangeBuilder = QueryBuilders.rangeQuery("date")

            if (minDate != null)
                rangeBuilder.gte(minDate)
            if (maxDate != null)
                rangeBuilder.lte(maxDate)

            boolQueryBuilder.filter(rangeBuilder)
        }

        val startingIndex = (page - 1) * pageSize
        val searchSourceBuilder = SearchSourceBuilder().query(boolQueryBuilder).from(startingIndex).size(pageSize)
        val searchRequest = SearchRequest().source(searchSourceBuilder)

        val client = RestHighLevelClient(RestClient.builder(HttpHost("localhost", 9200, "http")))
        val searchResponse = client.search(searchRequest, RequestOptions.DEFAULT)

        val docList = searchResponse.hits.sortedBy { hit -> hit.score }.map { hit -> mapper.convertValue(hit.sourceAsMap, Document::class.java) }

        client.close()

        return Pair(searchResponse.hits.totalHits?.value ?: -1, docList)
    }
}
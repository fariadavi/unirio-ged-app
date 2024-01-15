package br.unirio.gedapp.repository

import br.unirio.gedapp.domain.Document
import br.unirio.gedapp.domain.DocumentStatus
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpHost
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.client.RequestOptions
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestHighLevelClient
import org.elasticsearch.index.query.Operator
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.index.reindex.BulkByScrollResponse
import org.elasticsearch.index.reindex.UpdateByQueryRequest
import org.elasticsearch.script.Script
import org.elasticsearch.search.aggregations.AggregationBuilders
import org.elasticsearch.search.aggregations.bucket.terms.Terms
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class DocumentRepositoryImpl(@Autowired val mapper: ObjectMapper) : DocumentCustomRepository {

    @Value("\${spring.elasticsearch.uris}")
    lateinit var elasticSearchURL: String

    private fun getClient() =
        RestHighLevelClient(RestClient.builder(HttpHost.create(elasticSearchURL)))

    private fun performSearch(
        searchRequest: SearchRequest?,
        requestOptions: RequestOptions = RequestOptions.DEFAULT
    ): SearchResponse {
        val searchResponse: SearchResponse
        getClient()
            .also { searchResponse = it.search(searchRequest, requestOptions) }
            .close()
        return searchResponse
    }

    override fun queryDocuments(
        tenant: String,
        text: String,
        page: Int,
        pageSize: Int,
        category: Long?,
        user: Long?,
        minDate: LocalDate?,
        maxDate: LocalDate?,
        status: DocumentStatus?
    ): Pair<Long, Iterable<Document>> {

        val boolQueryBuilder =
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("tenant", tenant))

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

        if (status != null)
            boolQueryBuilder.filter(QueryBuilders.termQuery("status", status.ordinal))

        if (text.isNotBlank())
            boolQueryBuilder
                .must(QueryBuilders
                    .simpleQueryStringQuery(text)
                    .fields(mapOf("content" to 1f, "title" to 1.2f, "summary" to .5f))
                    .defaultOperator(Operator.AND))

        val startingIndex = (page - 1) * pageSize
        val searchSourceBuilder =
            SearchSourceBuilder()
                .query(boolQueryBuilder)
                .from(startingIndex)
                .size(pageSize)
        if (text.isBlank()) searchSourceBuilder.sort("status")
        if (text.isNotBlank())
            searchSourceBuilder.highlighter(
                HighlightBuilder().field("title").preTags("<strong>").postTags("</strong>")
            )
        val searchRequest = SearchRequest().source(searchSourceBuilder)

        val searchResponse = performSearch(searchRequest)

        val docList = searchResponse.hits
            .sortedByDescending { hit -> hit.score }
            .map { hit ->
                mapper.convertValue(hit.sourceAsMap, Document::class.java).copy(id = hit.id)
                    .also { it.searchMatches = hit.highlightFields.values.flatMap{ h -> h.fragments.map { f -> f.string() } } }
            }

        return Pair(searchResponse.hits.totalHits?.value ?: -1, docList)
    }

    override fun getMapOfCategoriesWithDocCount(tenant: String): Map<Long, Long> {
        val boolQueryBuilder =
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("tenant", tenant))

        val searchSourceBuilder =
            SearchSourceBuilder()
                .query(boolQueryBuilder)
                .aggregation(AggregationBuilders.terms("group_by_category").field("category_id"))
                .size(0)
        val searchRequest = SearchRequest("documents").source(searchSourceBuilder)

        val searchResponse = performSearch(searchRequest)

        return searchResponse.aggregations.get<Terms>("group_by_category").buckets.associate { it.key as Long to it.docCount }
    }

    override fun getDocCountByCategory(tenant: String, categoryId: Long): Long {
        val boolQueryBuilder =
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("tenant", tenant))
                .filter(QueryBuilders.termQuery("category_id", categoryId))

        val searchSourceBuilder =
            SearchSourceBuilder()
                .query(boolQueryBuilder)
                .size(0)
        val searchRequest = SearchRequest("documents").source(searchSourceBuilder)

        val searchResponse = performSearch(searchRequest)

        return searchResponse.hits.totalHits?.value ?: 0
    }

    override fun updateDocsTenantAcronym(tenant: String, newTenant: String): Long {
        val boolQueryBuilder =
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("tenant", tenant))

        val updateByQueryRequest =
            UpdateByQueryRequest("documents")
                .setQuery(boolQueryBuilder)
                .setScript(Script("ctx._source.tenant = '$newTenant'"))

        val updateResponse: BulkByScrollResponse
        getClient()
            .also { updateResponse = it.updateByQuery(updateByQueryRequest, RequestOptions.DEFAULT) }
            .close()

        return updateResponse.updated
    }
}
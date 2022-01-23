package com.bits.esclient.elastic;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ElasticComManager {
    private ExecutorService executorService;
    private final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private static RestHighLevelClient client;
    private BulkScheduler bulkScheduler;


    @Inject
    private ElasticComManager(ESConfiguration esConfiguration) {

        executorService = Executors.newFixedThreadPool(1);
        if (esConfiguration.getUserName() != null && !esConfiguration.getUserName().isEmpty()) {
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(esConfiguration.getUserName(), esConfiguration.getPassword()));
        }
        RestClientBuilder builder = RestClient.builder(new HttpHost(esConfiguration.getHostName(), esConfiguration.getPort(), esConfiguration.getProtocolScheme()))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    //httpClientBuilder.disableAuthCaching();
                    return httpClientBuilder
                            .setDefaultCredentialsProvider(credentialsProvider);
                });
        client = new RestHighLevelClient(builder);
        bulkScheduler = new BulkScheduler(client);

    }

    public CompletableFuture<String> index(String indexName, String id, XContentBuilder builder) {
        String indexNameLower = indexName.toLowerCase();
        CompletableFuture<String> completableFuture = new CompletableFuture<>();
        executorService.submit(() -> {
            IndexRequest indexRequest = new IndexRequest(indexNameLower).id(id).source(builder);

            client.indexAsync(indexRequest, RequestOptions.DEFAULT, new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    completableFuture.complete(indexResponse.toString());
                }
                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    completableFuture.completeExceptionally(e);
                }
            });
        });
        return completableFuture;
    }

    void bulkIndex(String index, String id, XContentBuilder builder) {
        IndexRequest indexRequest = new IndexRequest(index).id(id).source(builder);
        bulkScheduler.bulk(indexRequest);

    }


}

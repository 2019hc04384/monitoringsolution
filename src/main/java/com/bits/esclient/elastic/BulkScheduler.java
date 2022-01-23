package com.bits.esclient.elastic;

import org.elasticsearch.action.bulk.BackoffPolicy;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.joda.time.Instant;

class BulkScheduler {
    private static final String BULK_SCHEDULER="BulkScheduler";
    private final BulkProcessor bulkProcessor;
    private int response= 1;
    private int request =1;

    BulkScheduler(RestHighLevelClient client) {
        BulkProcessor.Listener listener = new BulkProcessor.Listener() {
            @Override
            public void beforeBulk(long executionId, BulkRequest bulkRequest) {
                System.out.println(Instant.now() +" Before bulk "+ request++ +" Request size "+ bulkRequest.numberOfActions());
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, BulkResponse bulkResponse) {
                if (bulkResponse.hasFailures()) {
                    System.out.println(Instant.now() +" Failures "+bulkResponse.buildFailureMessage());

                } else{
                    System.out.println(Instant.now() +" Success "+ response++ + " Response "+bulkResponse.getItems().length);
                }
            }

            @Override
            public void afterBulk(long executionId, BulkRequest bulkRequest, Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        BulkProcessor.Builder builder = BulkProcessor.builder(
                (request, bulkListener) ->
                        client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                listener);
        builder.setBulkActions(5000);
        //builder.setBulkSize(new ByteSizeValue(1L, ByteSizeUnit.MB));
        builder.setConcurrentRequests(1);
        builder.setFlushInterval(TimeValue.timeValueMinutes(1L));
        builder.setBackoffPolicy(BackoffPolicy
                .constantBackoff(TimeValue.timeValueSeconds(1L), 3));
        bulkProcessor=builder.build();
    }

    void bulk(IndexRequest indexRequest) {
        bulkProcessor.add(indexRequest);
    }

}

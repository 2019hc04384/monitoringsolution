package com.bits.esclient.elastic;

import org.elasticsearch.common.UUIDs;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class ElasticClient {
    private final ElasticComManager escom;
    private final boolean isBulkMode;
    @Inject
    ElasticClient(ElasticComManager escom, ESConfiguration esConfiguration){
        isBulkMode = esConfiguration.isBulkMode();
        this.escom = escom;
    }
    public void pushToElastic(String indexName, Optional<String> optionalId, Map<String, Object> values) {
        try {
            XContentBuilder builder = jsonBuilder();
            builder.startObject();
            values.forEach((k,v) -> {
                try {
                    builder.field(k, v);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            builder.endObject();
            String docId = optionalId.orElse(UUIDs.base64UUID());
            if(isBulkMode) {
                escom.bulkIndex(indexName, docId, builder);
            } else {
                escom.index(indexName, docId, builder);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


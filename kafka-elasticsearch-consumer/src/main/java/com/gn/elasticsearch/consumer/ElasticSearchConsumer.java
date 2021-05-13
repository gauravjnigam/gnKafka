package com.gn.elasticsearch.consumer;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ElasticSearchConsumer {
    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchConsumer.class);
    private static final JsonParser jsonParser = new JsonParser();

    public static void main(String[] args) throws IOException {
        String topic = "twitter_tweet";
        RestHighLevelClient client = createClient();

        KafkaConsumer<String, String> consumer = createKafkaConsumer(topic);

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            int recordCount = records.count();
            if(recordCount >0) {
                logger.info("Received : {} records", recordCount);
            }

            BulkRequest bulkRequest = new BulkRequest();
            for (ConsumerRecord record : records) {
                // logger.info("Id : " + record.value());
                //String jsonString = "{ \"foo\" : \"bar\" }";
                String id = record.topic() + "_" + record.partition() + "_" + record.offset(); // kafka generate id
                String id1 = extractIdFromTweet(record.value().toString());
                // String val = extractValueFromTweet(record.value().toString());

                String tweet = "{\"text\" : \"" + id + "\"}";

                IndexRequest indexRequest = new IndexRequest("twitter").id(id1)
                        .source(jsonParser.parse(tweet), XContentType.JSON);

                //IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
                bulkRequest.add(indexRequest);
                //  String id2 = indexResponse.getId();
                // logger.info("Id : " + id2);

            }
            if(recordCount > 0) {
                BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);

                logger.info("Committing offset");
                consumer.commitSync();
                logger.info("Offset is committed");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        // client.close();


    }

    private static String extractValueFromTweet(String value) {
        return jsonParser.parse(value).getAsJsonObject().get("text").getAsString();
    }

    private static String extractIdFromTweet(String value) {
        return jsonParser.parse(value).getAsJsonObject().get("id_str").getAsString();
    }

    private static KafkaConsumer<String, String> createKafkaConsumer(String topic) {
        String bootStrapServers = "127.0.0.1:9092";
        String groupId = "gnKafkaElasticSearch";


        logger.info("Logging here");
        // create consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");// always read from beginning, it can be latest to get latest records only

        properties.setProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");// to disable auto commit
        properties.setProperty(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "100");


        // creating consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Arrays.asList(topic));
        return consumer;

    }

    public static RestHighLevelClient createClient() {
        //https://:@
        String hostName = "gnkafkalearning-1532737568.us-east-1.bonsaisearch.net";
        String userName = "yArsqYkwea";
        String password = "zjSN6kLyVewQ42TPUtHaJX";

        // not for local Elastic search
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(userName, password));

        RestClientBuilder builder = RestClient.builder(new HttpHost(hostName, 443, "https"))
                .setHttpClientConfigCallback(httpClientBuilder -> {
                    return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                });

        RestHighLevelClient client = new RestHighLevelClient(builder);

        return client;
    }
}

package com.gn.learnkafka.consumer;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerTestAssignSeek {
    public static Logger logger = LoggerFactory.getLogger(ConsumerTestAssignSeek.class);

    public static void main(String[] args) {
        String bootStrapServers = "127.0.0.1:9092";
        //String groupId = "gnApp04";
        String topic = "gn01";

        logger.info("Logging here");
        // create consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        //properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");// always read from beginning, it can be latest to get latest records only

        // creating consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);

        // assign
        TopicPartition partitionToReadFrom = new TopicPartition(topic, 0);
        long offsetToReadFrom = 15L;
        consumer.assign(Arrays.asList(partitionToReadFrom));

        //seek
        consumer.seek(partitionToReadFrom, offsetToReadFrom);

        int numOfMsgToRead = 5;
        boolean keepOnReading = true;
        int numOfMessageReadSoFar = 0;

        // subscribe to topic
        //consumer.subscribe(Arrays.asList(topic));

        // poll for new data

        while (keepOnReading) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));

            for (ConsumerRecord<String, String> record : records) {
                logger.info("Key : {}\n Value : {}, \n Partition : {}, \n Offset: {}",
                        record.key(), record.value(), record.partition(), record.offset());
                numOfMessageReadSoFar += 1;
                if(numOfMessageReadSoFar >= numOfMsgToRead) {
                    keepOnReading = false;
                    break;
                }

            }
        }

        logger.info("Exiting the consumer!!!");

    }
}

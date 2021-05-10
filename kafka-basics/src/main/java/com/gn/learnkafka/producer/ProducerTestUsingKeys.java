package com.gn.learnkafka.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerTestUsingKeys {
    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(ProducerTestUsingKeys.class);
        String bootStrapServers = "127.0.0.1:9092";
        System.out.println("Starting here");
        logger.info("Logging here");
        // create producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootStrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);


        for (int i = 0; i < 10; i++) {

            String topic = "gn01";
            String value = "Hello-" + Integer.toString(i);
            String key = "id_" + Integer.toString(i);


            // create a record
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
            logger.info("Key : " + key);
            // send data
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    //

                    if (exception == null) {
                        logger.info("Received new metadata. \n Topic: {}, \n Partition: {}, \n Offset: {} \n TimeStamp: {} ",
                                metadata.topic(), metadata.partition(), metadata.offset(), metadata.timestamp());

                    } else {
                        logger.error("Error while producing record", exception);
                    }

                }
            });

        }

        producer.flush();
        producer.close();


    }
}

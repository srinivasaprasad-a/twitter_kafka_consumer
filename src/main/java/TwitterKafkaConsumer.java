import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.Collections;
import java.util.Properties;

public class TwitterKafkaConsumer {
    private final static String topic = "twitter-topic";
    private final static String bootstrap_servers = "kafka:9092, kafka_1:9094";

    private static Consumer<String, String> createConsumer() {
        final Properties prop = new Properties();
        prop.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap_servers);
        prop.put(ConsumerConfig.GROUP_ID_CONFIG, "TwitterKafkaConsumer");
        prop.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        prop.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());

        final Consumer<String, String> consumer = new KafkaConsumer<String, String>(prop);
        consumer.subscribe(Collections.singletonList(topic));

        return consumer;
    }

    public static void run() {
        final Consumer<String, String> consumer = createConsumer();

        final int threshold = 100;
        int numRecords = 0;

        try {
            while (true) {
                final ConsumerRecords<String, String> consumerRecords = consumer.poll(1000);

                if (consumerRecords.count() == 0) {
                    numRecords++;
                    if (numRecords > threshold) break;
                    else continue;
                }

                for (ConsumerRecord<String, String> record : consumerRecords) {
                    System.out.printf("Consumer Record:(%s, %s, %d, %d)\n",
                            record.key(), record.value(),
                            record.partition(), record.offset());
                }

                consumer.commitAsync();
            }
        } finally {
            consumer.close();
        }
    }

    public static void main(String[] args) {
        try {
            run();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

}

package consumer;

import model.Shared;
import model.TransactionRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import serialisation.TransactionJsonDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class TransactionConsumerGroupProKey implements Runnable {

  private final KafkaConsumer<String, TransactionRecord> consumer;
  private final String consumerId;

  public TransactionConsumerGroupProKey(String consumerId) {
    this.consumerId = consumerId;
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Shared.BOOTSTRAP_SERVERS);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, TransactionJsonDeserializer.class.getName());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, Shared.GROUP_ID);
    this.consumer = new KafkaConsumer<>(properties);
  }

  @Override
  public void run() {
    consumer.subscribe(List.of(Shared.TOPIC));
    var oneSecond = Duration.ofSeconds(1);
    try {
      while (!Thread.currentThread().isInterrupted()) {
        var records = consumer.poll(oneSecond);
        if (records.isEmpty()) {
          System.out.println("[Consumer " + consumerId + "] Pas de message");
        } else {
          records.forEach(record ->
                  System.out.println(
                          "[Consumer " + consumerId + "]"
                          + " partition=" + record.partition()
                          + " | clé=" + record.key()
                          + " | valeur=" + record.value()
                  )
          );
        }
      }
    } finally {
      consumer.close();
    }
  }

}

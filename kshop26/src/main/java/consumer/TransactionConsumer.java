package consumer;

import common.Shared;
import model.TransactionRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import serialisation.TransactionJsonDeserializer;
import serialisation.TransactionJsonSerializer;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class TransactionConsumer<K, V> {
  private final KafkaConsumer<K, V> consumer;
  private boolean isRunning = true;

  TransactionConsumer(KafkaConsumer<K, V> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
  }


  private void consume() {
    var oneSecond = Duration.ofSeconds(1);
    while (isRunning) {
      consumer.poll(oneSecond).forEach(record -> System.out.println(record.value()));
    }
    close();
  }

  public void subscribe(List<String> topics){
    Objects.requireNonNull(topics);
    consumer.subscribe(topics);
  }

  private void close(){
    consumer.close();
  }


  public static void main(String[] args) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Shared.BOOTSTRAP_SERVERS);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, TransactionJsonDeserializer.class.getName());
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, Shared.GROUP_ID);
    KafkaConsumer<String, TransactionRecord> consumer = new KafkaConsumer<>(properties);
    var myConsumer = new TransactionConsumer<>(consumer);
    myConsumer.subscribe(List.of(Shared.TOPIC));
    myConsumer.consume();
  }

}

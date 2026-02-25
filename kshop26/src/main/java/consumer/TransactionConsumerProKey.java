package consumer;

import common.Shared;
import model.TransactionRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import serialisation.TransactionJsonDeserializer;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class TransactionConsumerProKey<K, V> {
  private final KafkaConsumer<K, V> consumer;
  private boolean isRunning = true;

  TransactionConsumerProKey(KafkaConsumer<K, V> consumer) {
    this.consumer = Objects.requireNonNull(consumer);
  }


  private void consume() {
    var oneSecond = Duration.ofSeconds(1);
    while (isRunning) {
      var records = consumer.poll(oneSecond);
      if(records.isEmpty()){
        System.out.println("Pas de message");
      }else{
        // On fait trois partitions pour voir où vont les messages
        // ./bin/kafka-topics.sh --bootstrap-server localhost:9092 --create --topic simpleTopic --partitions 3 --replication-factor 1
        records.forEach(record ->
                System.out.println(record.partition() +" | "+record.key() + " = " + record.value()
                ));
      }
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
    var myConsumer = new TransactionConsumerProKey<>(consumer);
    myConsumer.subscribe(List.of(Shared.TOPIC));
    myConsumer.consume();
  }

}

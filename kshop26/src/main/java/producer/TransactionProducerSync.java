package producer;

import common.Shared;
import generator.TransactionRecordGenerator;
import model.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import serialisation.TransactionJsonSerializer;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.IntStream;

public class TransactionProducerSync<K, V> {

  private final KafkaProducer<K, V> producer;
  private final String topic;

  public TransactionProducerSync(KafkaProducer<K, V> producer, String topic) {
    this.producer = Objects.requireNonNull(producer);
    this.topic = Objects.requireNonNull(topic);
  }


  public void sendMessage() {
    var generator = new TransactionRecordGenerator();
    IntStream.range(0, 100).forEach(i -> {
      ProducerRecord<String, TransactionRecord> record = new ProducerRecord<>(topic, generator.generate());
      try {
        producer.send((ProducerRecord<K, V>) record).get(); // synchrone
      } catch (InterruptedException | ExecutionException e) {
        System.out.println(e.getMessage());
      }
    });
    close();
  }

  private void close() {
    producer.close();
  }


  public static void main(String args[]) {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Shared.BOOTSTRAP_SERVERS);
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, Shared.KEY_SERIALIZER);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TransactionJsonSerializer.class.getName());
    properties.put(ProducerConfig.LINGER_MS_CONFIG, "0"); // Le temps entre chaque batch.
    KafkaProducer<String, TransactionRecord> producer = new
            KafkaProducer<>(properties);
    var myProducer = new TransactionProducerSync<>(producer, Shared.TOPIC);
    myProducer.sendMessage();
  }
}

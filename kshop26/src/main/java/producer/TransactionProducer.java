package producer;

import generator.TransactionRecordGenerator;
import model.TransactionRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import serialisation.TransactionJsonSerializer;

import java.util.Objects;
import java.util.Properties;
import java.util.stream.IntStream;

public class TransactionProducer<K, V> {

  private final KafkaProducer<K, V> producer;
  private final String topic;

  public TransactionProducer(KafkaProducer<K, V> producer, String topic) {
    this.producer = Objects.requireNonNull(producer);
    this.topic = Objects.requireNonNull(topic);
  }


  // Faire trois versions : fired-and-forget, synchrone, asynchrone
  public void sendMessage(){
    var generator = new TransactionRecordGenerator();
    var jsonSerializer = new TransactionJsonSerializer();
    IntStream.range(0, 100).forEach(i -> {
      ProducerRecord<String, TransactionRecord> record = new ProducerRecord<>(topic, generator.generate());
      try{
        producer.send((ProducerRecord<K, V>) record); // fired-and-forget

//        producer.send((ProducerRecord<K, V>) record, (metadata, exception) -> { // asynchrone
//          if (exception != null) {
//            System.out.println(exception.getMessage());
//          }
//        });
        Thread.sleep(250);
      }catch (InterruptedException e){
        System.out.println(e.getMessage());
      }
    });
    close();
  }

  private void close(){
    producer.close();
  }


  public static void main(String args[]) {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, TransactionJsonSerializer.class.getName());
    KafkaProducer<String, TransactionRecord> producer = new
            org.apache.kafka.clients.producer.KafkaProducer<>(properties);
    var myProducer = new TransactionProducer<>(producer,"simpleTopic");
    myProducer.sendMessage();
  }
}

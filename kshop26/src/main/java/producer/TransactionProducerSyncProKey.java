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

public class TransactionProducerSyncProKey<K, V> {

  private final KafkaProducer<K, V> producer;
  private final String topic;

  public TransactionProducerSyncProKey(KafkaProducer<K, V> producer, String topic) {
    this.producer = Objects.requireNonNull(producer);
    this.topic = Objects.requireNonNull(topic);
  }


  // Contexte qui tire pleinement parti de la clé :
  // Kafka garantit que tous les messages ayant la même clé sont envoyés dans la même partition.
  // Ainsi, toutes les transactions d'un même produit (même idProduit) arrivent dans le même ordre
  // dans la même partition. Un consumer lisant cette partition verra les achats du produit X
  // dans l'ordre chronologique, ce qui est essentiel pour des cas d'usage comme :
  //   - le suivi du stock en temps réel (incrémenter/décrémenter dans l'ordre)
  //   - l'agrégation des ventes par produit (stream processing avec Kafka Streams)
  //   - la détection de fraude par produit (analyser une séquence d'achats cohérente)
  public void sendMessage() {
    var generator = new TransactionRecordGenerator();
    IntStream.range(0, 100).forEach(i -> {
      var transaction = generator.generate();
      ProducerRecord<String, TransactionRecord> record = new ProducerRecord<>(topic, String.valueOf(transaction.getIdProduit()), transaction);
      try {
        producer.send((ProducerRecord<K, V>) record).get(); // synchrone avec clé
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
    var myProducer = new TransactionProducerSyncProKey<>(producer, Shared.TOPIC);
    myProducer.sendMessage();
  }
}

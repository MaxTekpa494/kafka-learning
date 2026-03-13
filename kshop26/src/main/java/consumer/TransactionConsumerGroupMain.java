package consumer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class TransactionConsumerGroupMain {

  /**
   * Lance un groupe de numConsumers consommateurs en parallèle.
   *
   * Usage : java consumer.TransactionConsumerGroupMain <numConsumers>
   *   ./bin/kafka-topics.sh --bootstrap-server localhost:9092 \
   *     --create --topic simpleTopic --partitions 3 --replication-factor 1
   */
  public static void main(String[] args) {
    var numConsumers = (args.length > 0) ? Integer.parseInt(args[0]) : 3;

    System.out.println("Démarrage du groupe de " + numConsumers + " consommateurs");

    var executor = Executors.newFixedThreadPool(numConsumers);
    IntStream.range(0, numConsumers).forEach(numConsumer -> {
      executor.submit(new TransactionConsumerGroupProKey(String.valueOf(numConsumer)));
    });
    executor.shutdown();
  }

}

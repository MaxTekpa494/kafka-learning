package streams;

import model.TransactionRecord;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;

public class Serde extends Serdes.WrapperSerde<TransactionRecord> {

  public Serde(Serializer<TransactionRecord> serializer, Deserializer<TransactionRecord> deserializer) {
    super(serializer, deserializer);
  }



}

package serialisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.TransactionRecord;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;
import java.util.Objects;

public class TransactionJsonDeserializer implements Deserializer<TransactionRecord> {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
    Deserializer.super.configure(configs, isKey);
  }

  @Override
  public TransactionRecord deserialize(String s, byte[] data) {
    Objects.requireNonNull(s);
    if(data == null || data.length == 0) return null;
    try{
      return MAPPER.readValue(data, TransactionRecord.class);
    }catch (Exception e){
      throw new RuntimeException("Erreur de deserialisation JSON pour la transaction");
    }
  }

  @Override
  public void close() {
    // Nothing to close
  }
}

package serialisation;

import com.fasterxml.jackson.databind.ObjectMapper;
import model.TransactionRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class TransactionJsonSerializer
        implements Serializer<TransactionRecord> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, TransactionRecord data) {
        try {
            return MAPPER.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException(e);
        }
    }
}
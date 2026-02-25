package common;

public final class Shared {

  private Shared(){}
  public static final String BOOTSTRAP_SERVERS = "localhost:9092";
  public static final String TOPIC = "simpleTopic";
  // heartbeats -> Coordinateur utilise le heartbeats pour savoir qui est encore operationnel dans son groupe
  public static final String GROUP_ID = "mygroup";
  public static final String KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
}

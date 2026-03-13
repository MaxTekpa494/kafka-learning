# Apache Kafka & Stream Processing

Cours universitaire — Master 2 | 2026

Repo couvrant l'ensemble de l'écosystème Apache Kafka : théorie, pratique et projet Java end-to-end.

---

## Modules couverts

| # | Module |
|---|--------|
| 1 | Introduction au Stream Processing — Batch vs Stream, CEP, Architecture Lambda |
| 2 | Systèmes de Messaging — Pub/Sub vs Message Queues, comparaison Kafka / Pulsar / Kinesis |
| 3 | Installation & Configuration — Zookeeper, cluster multi-brokers, Kafka CLI |
| 4 | Producer API — fire-and-forget, synchrone, asynchrone, sérialisation, partitionnement |
| 5 | Consumer API — consumer groups, offset commit, rebalancing, thread safety |
| 6 | Kafka Connect — source/sink connectors, mode distribué, SMT, API REST |
| 7 | Kafka Streams — KStream/KTable, jointures, StateStore, gestion du temps, ksqlDB |

---

## Projet Java — `kshop26`

Pipeline de transactions e-commerce simulé en temps réel.

**Stack :** Java, Maven, Apache Kafka 3.9, Jackson

### Structure

```
kshop26/
├── model/
│   ├── TransactionRecord.java       # Modèle : client, produit, prix, magasin
│   └── Shared.java                  # Constantes partagées (bootstrap, topic, group)
├── generator/
│   └── TransactionRecordGenerator.java  # Génération de données avec Faker
├── serialisation/
│   ├── TransactionJsonSerializer.java   # Serializer<T> custom via Jackson
│   └── TransactionJsonDeserializer.java # Deserializer<T> custom via Jackson
├── producer/
│   ├── TransactionProducer.java             # Fire-and-forget
│   ├── TransactionProducerSync.java         # Synchrone (.get())
│   └── TransactionProducerSyncProKey.java   # Synchrone + clé = idProduit (ordre garanti)
├── consumer/
│   ├── TransactionConsumer.java             # Consumer simple
│   ├── TransactionConsumerGroup.java        # Consumer group
│   ├── TransactionConsumerProKey.java       # Consumer avec clé
│   ├── TransactionConsumerGroupProKey.java  # Runnable — un consumer par thread
│   └── TransactionConsumerGroupMain.java    # Lance N consumers en parallèle (ExecutorService)
└── streams/
    └── Serde.java                           # Serde<TransactionRecord> custom pour Kafka Streams
```

### Points clés implémentés

- **3 modes d'envoi** : fire-and-forget / synchrone / asynchrone
- **Partitionnement par clé** : `idProduit` comme clé → tous les messages d'un même produit dans la même partition → ordre garanti (utile pour suivi de stock, agrégation par produit)
- **Consommation multi-threadée** : `ExecutorService` + `Runnable`, chaque thread a sa propre instance `KafkaConsumer`
- **Sérialisation custom** : `Serializer<T>` / `Deserializer<T>` implémentés avec Jackson `ObjectMapper`

---

## TP — Cluster multi-brokers

Simulation d'un cluster de 3 brokers sur une seule machine avec observation des mécanismes de réplication et tolérance aux pannes :

- Configuration de 3 fichiers `server.properties` (ports 9092 / 9093 / 9094)
- Création de topics avec `replication-factor 3` et `partitions 3`
- Observation des ISR (In-Sync Replicas) via `kafka-topics.sh --describe`
- Simulation de panne d'un broker → élection automatique du nouveau leader
- Vérification de la résilience des producers et consumers

---

## Technologies

`Apache Kafka 3.9` `Kafka Streams` `Kafka Connect` `Java` `Maven` `Zookeeper` `Jackson` `RocksDB` `ksqlDB`

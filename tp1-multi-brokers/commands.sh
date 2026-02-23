#!/bin/bash

################################################################################
# TP1 : Multi-Brokers Kafka et Réplication - Commandes
################################################################################

# ==============================================================================
# PRÉPARATION
# ==============================================================================

# Créer les fichiers de configuration
cp config/server.properties config/server2.properties
cp config/server.properties config/server3.properties

# Éditer manuellement :
# server.properties   : broker.id=0, listeners=PLAINTEXT://:9092, log.dirs=/tmp/kafka-logs
# server2.properties  : broker.id=1, listeners=PLAINTEXT://:9093, log.dirs=/tmp/kafka-logs-2
# server3.properties  : broker.id=2, listeners=PLAINTEXT://:9094, log.dirs=/tmp/kafka-logs-3

# ==============================================================================
# DÉMARRAGE
# ==============================================================================

# Terminal 1 - Zookeeper
./bin/zookeeper-server-start.sh config/zookeeper.properties

# Terminal 2 - Broker 0
./bin/kafka-server-start.sh config/server.properties

# Terminal 3 - Broker 1
./bin/kafka-server-start.sh config/server2.properties

# Terminal 4 - Broker 2
./bin/kafka-server-start.sh config/server3.properties

# ==============================================================================
# VÉRIFICATION
# ==============================================================================

# Vérifier les brokers via Zookeeper
./bin/zookeeper-shell.sh localhost:2181
ls /brokers/ids
# Résultat : [0, 1, 2]

# ==============================================================================
# CRÉATION DU TOPIC
# ==============================================================================

# Créer le topic avec 3 réplicas
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --replication-factor 3 \
  --partitions 1 \
  --create \
  --topic topic3Rep

# Décrire le topic
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --describe \
  --topic topic3Rep

# ==============================================================================
# TESTS DE PANNES
# ==============================================================================

# Arrêter un follower (broker 0)
# Dans le terminal du broker 0 : Ctrl+C

# Vérifier l'état
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --describe \
  --topic topic3Rep

# Arrêter le leader (broker 2)
# Dans le terminal du broker 2 : Ctrl+C

# Vérifier l'élection
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --describe \
  --topic topic3Rep

# ==============================================================================
# PRODUCER/CONSUMER
# ==============================================================================

# Terminal A - Consumer
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep

# Terminal B - Producer
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep

# ==============================================================================
# RÉSOLUTION PROBLÈME (BufferUnderflowException)
# ==============================================================================

# Arrêter tout (Ctrl+C sur tous les terminaux)

# Nettoyer les logs
rm -rf /tmp/kafka-logs
rm -rf /tmp/kafka-logs-2
rm -rf /tmp/kafka-logs-3
rm -rf /tmp/zookeeper

# Redémarrer dans l'ordre
./bin/zookeeper-server-start.sh config/zookeeper.properties
./bin/kafka-server-start.sh config/server.properties
./bin/kafka-server-start.sh config/server2.properties
./bin/kafka-server-start.sh config/server3.properties

# Re-créer le topic
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --replication-factor 3 \
  --partitions 1 \
  --create \
  --topic topic3Rep

# ==============================================================================
# COMMANDES UTILES
# ==============================================================================

# Lister les topics
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# Obtenir les offsets
./bin/kafka-get-offsets.sh --bootstrap-server localhost:9092 --topic topic3Rep

# Lire depuis le début
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic topic3Rep \
  --from-beginning

# Supprimer un topic
./bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic topic3Rep

# Arrêt propre d'un broker (recommandé)
./bin/kafka-server-stop.sh

# ==============================================================================
# NETTOYAGE
# ==============================================================================

# Arrêter brokers et Zookeeper (Ctrl+C)

# Nettoyer (optionnel)
rm -rf /tmp/kafka-logs*
rm -rf /tmp/zookeeper

################################################################################
# FIN
################################################################################
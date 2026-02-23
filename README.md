# Apache Kafka et Stream Processing

## Vue d'ensemble du projet

Dans le cadre de mon cursus universitaire, j'ai suivi un cours approfondi sur le **stream processing** et **Apache Kafka**, combinant théorie et pratique à travers des travaux pratiques documentés. L'objectif était double : maîtriser les concepts fondamentaux des systèmes de messaging distribués pour réussir l'examen, et développer des compétences pratiques concrètes pour le marché du travail.

---

## Compétences acquises

### 1. Stream Processing - Concepts fondamentaux

**Distinction Batch vs Stream Processing**
- Compréhension des architectures de traitement de données en temps réel vs par lots
- Maîtrise des concepts de latence, throughput et scalabilité
- Connaissance des use cases (IoT, cybersécurité, finance, smart cities)

**Complex Event Processing (CEP)**
- Différenciation entre CEP et Stream Processing classique
- Compréhension des patterns de corrélation d'événements
- Applications pratiques (détection de fraude, maintenance prédictive)

**Architectures modernes**
- Architecture Lambda (combinaison batch + stream)
- Systèmes de messaging avec stockage (Kafka, Pulsar)
- Moteurs de stream processing (Flink, Kafka Streams)

---

### 2. Systèmes de Messaging - Pub/Sub vs Message Queues

**Message Queues**
- Communication one-to-one
- Garanties de livraison fortes
- Use cases : distribution de tâches, workload processing
- Systèmes : RabbitMQ, ActiveMQ, Amazon SQS

**Publish/Subscribe Systems**
- Communication one-to-many
- Loose coupling entre producteurs et consommateurs
- Rétention des messages après consommation
- Use cases : architectures event-driven, broadcasting
- Systèmes : Kafka, Pulsar, Google Cloud Pub/Sub

---

### 3. Apache Kafka - Architecture et mécanismes

**Architecture distribuée**
- **Topics** : Organisation logique des messages par catégorie
- **Partitions** : Distribution des données pour la scalabilité et le parallélisme
- **Brokers** : Serveurs physiques qui stockent et gèrent les partitions
- **Clusters** : Ensemble de brokers coordonnés par Zookeeper (ou KRaft)

**Réplication et haute disponibilité**
- Mécanisme de réplication au niveau des partitions
- Leaders et Followers : distribution des rôles de lecture/écriture
- **ISR (In-Sync Replicas)** : garantie de synchronisation des données
- Élection automatique de leaders en cas de panne

**Tolérance aux pannes**
- Avec `replication-factor: N`, tolérance à `N-1` pannes simultanées
- Détection automatique des pannes via Zookeeper
- Re-synchronisation automatique des replicas
- Aucune perte de données tant qu'au moins 1 ISR est actif

**Concepts avancés**
- **Offsets** : Position de lecture dans une partition, permettant le replay des messages
- **Consumer Groups** : Parallélisation de la consommation avec répartition automatique des partitions
- **Partitioning Strategy** : Distribution des messages via des clés de partitionnement
- **Bootstrap Servers** : Découverte automatique du cluster et reconnexion en cas de panne

---

### 4. Installation et configuration

**Environnement technique**
- Installation et configuration de Zookeeper (coordination du cluster)
- Déploiement multi-brokers sur une seule machine (simulation de cluster)
- Configuration des paramètres critiques : `broker.id`, `listeners`, `log.dirs`
- Gestion des ports et des répertoires de logs

**Commandes Kafka essentielles**
- Création et gestion de topics (`kafka-topics.sh`)
- Production de messages (`kafka-console-producer.sh`)
- Consommation de messages (`kafka-console-consumer.sh`)
- Inspection et debugging (`--describe`, `--from-beginning`)

---

### 5. Comparaison des technologies

**Kafka vs autres solutions**
- **Apache Pulsar** : Multi-tenancy natif, latence très faible
- **AWS Kinesis** : Solution cloud managée, rétention jusqu'à 365 jours
- **Google Cloud Pub/Sub** : Intégration GCP, haute disponibilité
- **Azure Event Hub** : Écosystème Microsoft

**Critères de comparaison maîtrisés**
- Performance (throughput, latency)
- Rétention des données
- Géoréplication
- Protocoles supportés
- Écosystème et intégrations
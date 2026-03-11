# TP1 : Multi-Brokers Kafka et Réplication

## Objectif

Observer le comportement de Kafka dans un environnement distribué avec **3 brokers** :
- Réplication des données
- Tolérance aux pannes
- Élection automatique de leaders
- Re-synchronisation des replicas

---

## Prérequis

- Apache Kafka >= 3.0 et < 4.0
- Apache Zookeeper
- JDK 11+
- Système : Linux/macOS

---

## Configuration du cluster

### Fichiers de configuration

| Fichier | Broker ID | Port | Logs |
|---------|-----------|------|------|
| `server.properties` | 0 | 9092 | `/tmp/kafka-logs` |
| `server2.properties` | 1 | 9093 | `/tmp/kafka-logs-2` |
| `server3.properties` | 2 | 9094 | `/tmp/kafka-logs-3` |

### Paramètres modifiés

Pour chaque fichier, modifier **3 lignes** :
1. **`broker.id`** (ligne ~21) : 0, 1, ou 2
2. **`listeners`** (ligne ~31) : PLAINTEXT://:9092, 9093, ou 9094
3. **`log.dirs`** (ligne ~61) : /tmp/kafka-logs, -2, ou -3

---

## 🚀 Étapes du TP

### 1. Démarrage de Zookeeper
```bash
./bin/zookeeper-server-start.sh config/zookeeper.properties
```

### 2. Création des fichiers de configuration
```bash
cp config/server.properties config/server2.properties
cp config/server.properties config/server3.properties
# Éditer les 3 paramètres dans chaque fichier
```

### 3. Démarrage des 3 brokers (3 terminaux)
```bash
./bin/kafka-server-start.sh config/server.properties
./bin/kafka-server-start.sh config/server2.properties
./bin/kafka-server-start.sh config/server3.properties
```

### 4. Vérification du cluster
```bash
./bin/zookeeper-shell.sh localhost:2181
ls /brokers/ids
# Résultat : [0, 1, 2]
```

### 5. Création du topic répliqué
```bash
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --replication-factor 3 \
  --partitions 1 \
  --create \
  --topic topic3Rep
```

### 6. Description du topic
```bash
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --describe \
  --topic topic3Rep
```

**Résultat initial :**
```
Partition: 0    Leader: 2    Replicas: 2,0,1    Isr: 2,0,1
```

### 7. Tests de tolérance aux pannes

**Test 1 : Arrêt d'un follower (broker 0)**
- Arrêter le broker 0 avec Ctrl+C
- Observer : `Isr: 2,1` (broker 0 retiré)
- Producer/Consumer fonctionnent normalement

**Test 2 : Arrêt du leader (broker 2)**
- Arrêter le broker 2 avec Ctrl+C
- Observer : `Leader: 1` (élection automatique)
- Observer : `Isr: 1,0` (broker 2 retiré)
- Producer/Consumer fonctionnent normalement

### 8. Producer/Consumer en mode console
```bash
# Consumer
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep

# Producer
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep
```

---

## Résultats observés

| Scénario | Leader | ISR | Fonctionnel ? |
|----------|--------|-----|---------------|
| Cluster normal | 2 | 2,1,0 | Oui |
| Broker 0 (follower) down | 2 | 2,1 | Oui |
| Broker 2 (leader) down | 1 | 1,0 | Oui |

**Conclusion :** Tant qu'il reste au moins 1 broker dans l'ISR, le système fonctionne sans perte de données.

---

## Concepts clés

### Replication Factor
- Nombre de copies de chaque partition
- Avec `replication-factor: 3`, tolérance à **2 pannes simultanées**

### Leader et Followers
- **Leader** : Gère toutes les lectures/écritures
- **Followers** : Répliquent les données (backup)

### ISR (In-Sync Replicas)
- Liste des replicas synchronisés avec le leader
- Seuls les ISR peuvent devenir leader
- Un broker hors ISR = en retard dans la réplication

### Élection automatique
- Détection de panne via Zookeeper
- Nouveau leader choisi parmi les ISR
- Processus transparent et quasi-instantané

---

## Problème rencontré

**Symptôme :** `BufferUnderflowException` au redémarrage d'un broker

**Cause :** Corruption des logs suite à un arrêt brutal (Ctrl+C)

**Solution :**
```bash
# Arrêter tous les composants
# Nettoyer les logs
rm -rf /tmp/kafka-logs*
rm -rf /tmp/zookeeper

./bin/zookeeper-server-start.sh config/zookeeper.properties
./bin/kafka-server-start.sh config/server.properties
./bin/kafka-server-start.sh config/server2.properties
./bin/kafka-server-start.sh config/server3.properties
```

---

## Nettoyage

```bash
# Arrêter les brokers (Ctrl+C)
# Arrêter Zookeeper (Ctrl+C)

# Nettoyer les données (optionnel)
rm -rf /tmp/kafka-logs*
rm -rf /tmp/zookeeper
```

---

## Fichiers

- `README.md` : Ce fichier
- `commands.sh` : Toutes les commandes du TP
- `observations.md` : Observations et déductions détaillées
- `config/` : Fichiers de configuration des brokers

---

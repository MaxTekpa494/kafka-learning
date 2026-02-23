# TP : Multiple brokers Kafka et réplication

Notre objectif dans ce tp est d'observer le comportement de Kafka dans un environnement de 3 brokers.

## ÉTAPE 1 : Démarrage de Zookeeper

La commande à executer pour demarrer `zookeeper`.  

`bin % ./zookeeper-server-start.sh ../config/zookeeper.properties` 

**Message clé** :    
`clientPortAddress is 0.0.0.0:2181`    

Port 2181 = port par défaut pour les connexions clients Kafka.  

On démarre Zookeeper avant les brokers Kafka car il coordonne le cluster et stocke les métadonnées. Si on démarre les brokers avant Zookeeper, les brokers ne pourront pas s'enregistrer auprès de Zookeeper et le cluster ne fonctionnera pas correctement.

## ÉTAPE 2 : Création des fichiers de configuration pour 3 brokers

Il existe déjà un fichier config pour un broker, on va le copier et modifier afin de créer les deux autres configs.    

`cp config/server.properties config/server2.properties`  

`cp config/server.properties config/server3.properties`

----

| Fichier | broker.id | listeners | log.dirs |
|---------|-----------|-----------|----------|
| server.properties | 0 | PLAINTEXT://:9092 | /tmp/kafka-logs |
| server2.properties | 1 | PLAINTEXT://:9093 | /tmp/kafka-logs-2 |
| server3.properties | 2 | PLAINTEXT://:9094 | /tmp/kafka-logs-3 |


On vient de créer  3 fichiers de configuration pour simuler un cluster de 3 brokers sur une seule machine.

Chaque broker a avoir une configuration unique avec 3 paramètres obligatoirement différents :

1. `broker.id` : Identifiant unique du broker dans le cluster (0, 1, 2). 

2. `listeners` : Port d'écoute différent pour éviter les conflits (9092, 9093, 9094)
3. `log.dirs` : Répertoire de stockage séparé pour ne pas écraser les données des autres brokers




## ÉTAPE 3 : Démarrage des 3 brokers

On va démarrer les 3 brokers dans 3 terminaux différents (j'aime bien voir les logs)
```shell
# Terminal 1
./bin/kafka-server-start.sh config/server.properties

# Terminal 2
./bin/kafka-server-start.sh config/server2.properties

# Terminal 3
./bin/kafka-server-start.sh config/server3.properties
```

On vient de démarrer 3 brokers Kafka en mode foreground dans 3 terminaux séparés. 

## ÉTAPE 4 : Vérification que les 3 brokers sont opérationnels
On va vérifier via Zookeeper que les 3 brokers sont bien enregistrés.

```shell
./bin/zookeeper-shell.sh localhost:2181
ls /brokers/ids
```
Zookeeper maintient la liste des brokers actifs dans /brokers/ids.  
On peut voir que les 3 brokers (IDs 0, 1, 2) se sont correctement connectés à Zookeeper.    
Chaque broker s'est enregistré avec son broker.id unique.  

**NB**: Cette commande va également nous permettre de voir si un broker est tombé en panne si c'est le cas, on ID n'apparaitra plus.

## ÉTAPE 5 : Création d'un topic avec réplication

Maintenant qu'on a 3 brokers, on va créer un topic répliqué pour tester la tolérance aux pannes.

```shell
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --replication-factor 3 \
  --partitions 1 \
  --create \
  --topic topic3Rep
```
**Paramètres** :

- `--replication-factor 3` : Chaque partition aura 3 copies (une sur chaque broker). 
- `--partitions 1` : Le topic aura une seule partition
- `--topic topic3Rep` : Nom du topic

**Résultat :**
```
Created topic topic3Rep.
```

Le topic `topic3Rep` a été créé avec succès.    
Il possède 1 partition qui sera répliquée 3 fois (une copie sur chaque broker).  
Cela garantit que même si 2 brokers tombent, les données restent disponibles.   

## ÉTAPE 6 : Description du topic (comprendre Partition, Leader, Replicas, ISR)
Cette commande va nous permettre de voir comment la partition est distribuée.


```sh
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --describe \
  --topic topic3Rep
```

**Résultat obtenu :**
```sh
Topic: topic3Rep        TopicId: IqyV7v8BQiyOne0yGAJUxQ PartitionCount: 1       ReplicationFactor: 3    Configs: 
    Topic: topic3Rep        Partition: 0    Leader: 2       Replicas: 2,0,1 Isr: 2,0,1      Elr: N/A        LastKnownElr: N/A
```

**Interprétation ligne par ligne :**.    
**Ligne 1 : Informations générales du topic**

- `Topic: topic3Rep` -> Nom du topic
- `PartitionCount: 1` -> Le topic a 1 seule partition
- `ReplicationFactor: 3` -> Chaque partition a 3 copies (réplication)

**Ligne 2 - Détails de la partition 0 :***

- `Partition: 0` -> Numéro de la partition (la seule dans notre cas)
- `Leader: 2` -> Le broker 2 est le leader de cette partition.  
Rôle du leader : il gère les lectures et écritures pour cette partition

- `Replicas: 2,0,1` -> La partition est répliquée sur les brokers 2, 0 et 1
    - Broker 2 : leader (copie principale)
    - Broker 0 : follower (copie follower)
    - Broker 1 : follower (copie follower)


- `Isr: 2,0,1` -> In-Sync Replicas = Tous les brokers (2, 0, 1) sont synchronisés
    - Cela signifie que les 3 copies sont parfaitement à jour
    - Si un broker n'est pas dans ISR, il est "en retard" dans la réplication

**NB:** Le leader (broker 2) est choisi automatiquement par kafka en passant par zookeeper lors de la creation du topic, cela se fait aleatoirement.

## ÉTAPE 7 : Test de tolérance aux pannes
Maintenant, on va tester la résilience (fault-tolerent) en arrêtant un broker follower.

On va arrêter le Broker 0 (un follower).   
Ensuite, on re-exécute la commande describe :
```sh
./bin/kafka-topics.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --describe \
  --topic topic3Rep
```

On vient donc d'arrêter le broker 0 afin de tester la tolerence au panne.

**Résultat avant l'arrêt :**.  
```sh
Leader: 2       Replicas: 2,0,1    Isr: 2,0,1
```
**Résultat après l'arrêt du broker 0 :**
```sh
Leader: 2       Replicas: 2,0,1    Isr: 2,1
```

**Ce qui a changé :**.   
- `Isr: 2,0,1` -> Isr: 2,1     
- Le broker 0 a disparu de l'ISR (In-Sync Replicas)


Kafka détecte automatiquement la panne du broker 0, il le retire de l'ISR car il n'est plus synchronisé.

## ÉTAPE 8 : Test Producer/Consumer avec un broker en panne
On va vérifier que le système fonctionne toujours en envoyant et recevant des messages.
Pour faire ça, nous allons avoir un consumer et un producer.

**Terminal 1 -  le consumer :**
```bash
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep
```
![alt text](<Capture d’écran 2026-02-23 à 12.26.57.png>)

**Terminal 2 -  le producer :**
```bash
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep
```

![alt text](<Capture d’écran 2026-02-23 à 12.27.18.png>)


**Résultat :** Les messages sont produits et consommés normalement malgré l'arrêt du broker 0.

Avec `replication-factor: 3`, on peut tolérer 2 pannes simultanées de brokers sans interruption de service.

## ÉTAPE 9 : Redémarrage du broker 0
Maintenant, on va redémarrer le broker 0 et observer la re-synchronisation automatique.

Résultat après redémarrage complet :

```bash
Topic: topic3Rep        Partition: 0    Leader: 2       Replicas: 2,1,0    Isr: 2,1,0
```

## ÉTAPE 11 : Test critique - Arrêt du LEADER
Maintenant, on va tester le scénario le plus critique : arrêter le broker leader (broker 2) et observer l'élection automatique d'un nouveau leader.


**Résultat avant l'arrêt du leader :**
```
Leader: 2       Replicas: 2,1,0    Isr: 2,1,0
```

**Résultat après l'arrêt du broker 2 (leader) :**
```
Leader: 1       Replicas: 2,1,0    Isr: 1,0
```

---

**Ce qui a changé :**
1. **`Leader: 2` → `Leader: 1`**
   - Le broker 1 a été **automatiquement élu nouveau leader**
   - Kafka (via Zookeeper) a détecté la panne et déclenché l'élection

2. **`Isr: 2,1,0` → `Isr: 1,0`**
   - Le broker 2 a été **retiré de l'ISR**
   - Seuls les brokers actifs et synchronisés restent dans l'ISR


### **Points CRITIQUES :**

1. **Haute disponibilité garantie :**
   - Malgré la panne du leader, **le service continue sans interruption**
   - Les clients (producers/consumers) sont **automatiquement redirigés** vers le nouveau leader

2. **Élection automatique :**
   - Kafka choisit un nouveau leader **parmi les ISR** (In-Sync Replicas)
   - Le broker 1 était synchronisé -> il est devenu leader

3. **Aucune perte de données :**
   - Tant qu'il reste au moins **1 réplica dans l'ISR**, les données sont préservées
   - Le nouveau leader (broker 1) a **toutes les données à jour**

---

### **ÉTAPE 12 : Test Producer/Consumer avec le nouveau leader**

Maintenant on va verifier que le système fonctionne avec le nouveau leader (broker 1).

**Terminal 1 - Consumer :**
```bash
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep
```

**Terminal 2 - Producer :**
```bash
./bin/kafka-console-producer.sh --bootstrap-server localhost:9092,localhost:9093,localhost:9094 \
  --topic topic3Rep
```

Test de production et consommation avec le nouveau leader (broker 1) après la panne de l'ancien leader (broker 2).


**Résultat :** Les messages sont produits et consommés normalement avec le broker 1 comme nouveau leader.

## ÉTAPE 13 : Redémarrage de l'ancien leader (broker 2)
Maintenant, on va redémarrer le broker 2 et observer comment il rejoint le cluster en tant que follower.
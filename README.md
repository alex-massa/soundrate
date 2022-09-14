# soundrate
Progetto di Ingegneria del Software (Dipartimento di Informatica @ UNISA, anno accademico 2019/2020).

## Introduzione
Il progetto *soundrate* propone la costruzione di una piattaforma online per la recensione di pubblicazioni musicali.

## Utilizzo
Per eseguire l'applicazione web va eseguito da terminale il comando `docker-compose up` nella directory contenente il file `docker-compose.yml`. \
In alternativa a Docker, sono necessarie installazioni locali di Maven e del modulo [`deezer-api`](https://github.com/alex-massa/deezer-api); va quindi eseguito da terminale il comando `mvn package tomee:run` all'interno della directory `soundrate` contenente il file `pom.xml`.

In caso di avvio riuscito sarà possibile raggiungere la piattaforma collegandosi tramite un browser web all'inidirizzo `localhost:8080/soundrate`.

### Note
- Il contenuto del layer di persistenza viene generato durante l'avvio dell'applicazione e distrutto in seguito al suo spegnimento; tutte le modifiche apportate vengono mantenute unicamente per la durata del dispiegamento dell'applicazione. \
Per usare una base di dati persistente configurare in maniera appropriata i file `resources.xml` e `persistence.xml` nella directory `/soundrate/src/main/resources/META-INF`.

- Se l'applicazione viene dispiegata manualmente mediante Maven, configurare appropriatamente il file `persistence.xml` situato nella directory `/soundrate/src/main/resources/META-INF` affinché venga utilizzato un DBMS disponibile (che può anche essere in memoria, come suggerito dalla porzione disabilitata e pronta all'utilizzo omettendo i commenti).

- Configurare il file `application.properties` nella directory `/soundrate/src/main/resources` per consentire l'invio di e-mail di recupero password.

- Impostare i valori delle proprietà `populateDatabase` nel file `application.properties` contenuto nella directory `/soundrate/src/main/resources` per decidere se far popolare o meno la base di dati a seguito del lancio dell'applicazione. \
Tenere a mente che non sono stati implementati meccanismi per prevenire un ripopolamento della base di dati a seguito di riavvio dell'applicazione, per cui si consiglia di disabilitare l'opzione se si intende utilizzare una base di dati persistente, onde evitare, tra i potenziali problemi, un mancato lancio dell'applicazione dovuto a una potenziale duplicazione dei dati.

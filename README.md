# soundrate
Progetto di Ingegneria del Software (Dipartimento di Informatica @ UNISA, anno accademico 2019/2020).

## Introduzione
Il progetto *soundrate* propone la costruzione di una piattaforma online per la recensione di pubblicazioni musicali.

## Utilizzo
È necessaria un'installazione locale del modulo [`deezer-api`](https://github.com/alex-massa/deezer-api).
Per eseguire l'applicazione web va eseguito il comando Maven `mvn package tomee:run` all'interno della directory `soundrate` contenente il file `pom.xml`; sarà quindi possibile raggiungere la piattaforma collegandosi tramite un browser web all'inidirizzo `localhost:8080/soundrate`.

### Note
- Nel suo stato attuale di snapshot, l'applicazione contiene diverse inefficienze; verrà gradualmente raffinata coi successivi commit.
- Il contenuto del layer di persistenza viene generato durante lo startup dell'applicazione e distrutto in seguito al suo shutdown; tutte le modifiche apportate vengono mantenute per la durata dell'applicazione, e perse al suo spegnimento. Per usare una base di dati persistente configurare in maniera appropriata i file `soundrate/webapp/WEB-INF/resources.xml` e `soundrate/webapp/WEB-INF/persistence.xml`. Impostare il valore della proprietà `generateDataOnStartup` in `soundrate/src/main/resources/application.properties` per decidere se far generare o meno il contenuto della base di dati durante lo startup.

# soundrate
Progetto di Ingegneria del Software (Dipartimento di Informatica @ UNISA, anno accademico 2019/2020).

## Introduzione
Il progetto *soundrate* propone la costruzione di una piattaforma online per la recensione di pubblicazioni musicali.

## Utilizzo
È necessaria un'installazione locale del modulo [`deezer-api`](https://github.com/alex-massa/deezer-api).
Per eseguire l'applicazione web va eseguito il comando Maven `mvn clean package tomee:run` all'interno della directory `soundrate` contenente il file `pom.xml`; sarà quindi possibile raggiungere la piattaforma collegandosi tramite un browser web all'inidirizzo `localhost:8080/soundrate`.

### Note
- Nel suo stato attuale di snapshot, l'applicazione contiene numerose inconsistenze e inefficienze; verrà gradualmente raffinata e ottimizzata (e, più generalmente, riprogettata) coi successivi commit.
- Il layer di persistenza è attualmente simulato utilizzando dei file JSON; tutte le modifiche vengono mantenute in memoria e perse in seguito ad un riavvio dell'applicazione.

package application.producers;

import deezer.client.DeezerClient;

import javax.enterprise.inject.Produces;

public class ApiClientProducer {

    @Produces
    public DeezerClient produceApiClient() {
        return new DeezerClient();
    }

}

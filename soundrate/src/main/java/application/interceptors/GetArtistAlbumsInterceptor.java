package application.interceptors;

import application.cache.ArtistAlbumsHolder;
import application.interceptors.bindings.Cacheable;
import deezer.model.Artist;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Cacheable(type = "artistAlbums")
public class GetArtistAlbumsInterceptor {

    @Inject
    private ArtistAlbumsHolder artistAlbumsHolder;

    @AroundInvoke
    private Object interceptGetArtistAlbumsCalls(InvocationContext invocationContext) throws Exception {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
            if (ArtistAlbumsHolder.class.getName().equals(stackTraceElement.getClassName()))
                return invocationContext.proceed();
        Object[] parameters = invocationContext.getParameters();
        final Artist artist = (Artist) parameters[0];
        if (parameters.length == 1)
            return this.artistAlbumsHolder.getArtistAlbums(artist);
        final Integer index = (Integer) parameters[1];
        final Integer limit = (Integer) parameters[2];
        return this.artistAlbumsHolder.getArtistAlbums(artist, index, limit);
    }

}

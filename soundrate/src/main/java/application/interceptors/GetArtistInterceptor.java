package application.interceptors;

import application.cache.ArtistsHolder;
import application.interceptors.bindings.Cacheable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Cacheable(type = "artist")
public class GetArtistInterceptor {

    @Inject
    private ArtistsHolder artistsHolder;

    @AroundInvoke
    public Object interceptGetTopAlbumsCall(InvocationContext invocationContext) throws Exception {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
            if (ArtistsHolder.class.getName().equals(stackTraceElement.getClassName()))
                return invocationContext.proceed();
        final Long artistId = (Long) invocationContext.getParameters()[0];
        return this.artistsHolder.getArtist(artistId);
    }

}

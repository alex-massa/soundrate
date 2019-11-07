package application.interceptors;

import application.cache.AlbumsHolder;
import application.interceptors.bindings.Cacheable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Cacheable(type = "album")
public class GetAlbumInterceptor {

    @Inject
    private AlbumsHolder albumsHolder;

    @AroundInvoke
    public Object interceptGetTopAlbumsCall(InvocationContext invocationContext) throws Exception {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
            if (AlbumsHolder.class.getName().equals(stackTraceElement.getClassName()))
                return invocationContext.proceed();
        final Long albumId = (Long) invocationContext.getParameters()[0];
        return this.albumsHolder.getAlbum(albumId);
    }

}

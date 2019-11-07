package application.interceptors;

import application.cache.GenresHolder;
import application.interceptors.bindings.Cacheable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Cacheable(type = "genre")
public class GetGenreInterceptor {

    @Inject
    private GenresHolder genresHolder;

    @AroundInvoke
    public Object interceptGetTopAlbumsCall(InvocationContext invocationContext) throws Exception {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
            if (GenresHolder.class.getName().equals(stackTraceElement.getClassName()))
                return invocationContext.proceed();
        final Long genreId = (Long) invocationContext.getParameters()[0];
        return this.genresHolder.getGenre(genreId);
    }

}

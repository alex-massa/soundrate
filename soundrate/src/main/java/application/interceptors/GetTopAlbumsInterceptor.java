package application.interceptors;

import application.cache.TopAlbumsHolder;
import application.interceptors.bindings.Cacheable;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@Cacheable(type = "topAlbums")
public class GetTopAlbumsInterceptor {

    @Inject
    private TopAlbumsHolder topAlbumsHolder;

    @AroundInvoke
    public Object interceptGetTopAlbumsCall(InvocationContext invocationContext) throws Exception {
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace())
            if (TopAlbumsHolder.class.getName().equals(stackTraceElement.getClassName()))
                return invocationContext.proceed();
        Object[] parameters = invocationContext.getParameters();
        if (parameters == null)
            return this.topAlbumsHolder.getTopAlbums();
        final Integer index = (Integer) parameters[0];
        final Integer limit = (Integer) parameters[1];
        return this.topAlbumsHolder.getTopAlbums(index, limit);
    }

}

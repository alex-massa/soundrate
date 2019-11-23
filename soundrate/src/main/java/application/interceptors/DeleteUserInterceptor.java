package application.interceptors;

import application.events.qualifiers.UserUpdated;
import application.interceptors.bindings.UserUpdate;
import application.model.User;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@UserUpdate(type = "delete")
public class DeleteUserInterceptor {

    @Inject
    @UserUpdated(type = "delete")
    private Event<User> userDeletedEvent;

    @AroundInvoke
    public Object interceptDeleteUserCall(InvocationContext invocationContext) throws Exception {
        Object returnValue = invocationContext.proceed();
        User user = (User) invocationContext.getParameters()[0];
        this.userDeletedEvent.fire(user);
        return returnValue;
    }

}

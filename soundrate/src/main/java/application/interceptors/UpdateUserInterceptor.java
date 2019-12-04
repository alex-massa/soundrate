package application.interceptors;

import application.entities.User;
import application.events.qualifiers.UserUpdated;
import application.interceptors.bindings.UserUpdate;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@Interceptor
@UserUpdate(type = "update")
public class UpdateUserInterceptor {

    @Inject
    @UserUpdated(type = "updated")
    private Event<User> userUpdatedEvent;

    @AroundInvoke
    public Object interceptUpdateUserCall(InvocationContext invocationContext) throws Exception {
        Object returnValue = invocationContext.proceed();
        User user = (User) invocationContext.getParameters()[0];
        this.userUpdatedEvent.fire(user);
        return returnValue;
    }

}

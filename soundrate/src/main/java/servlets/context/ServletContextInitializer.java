package servlets.context;

import application.business.DataAgent;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ServletContextInitializer implements ServletContextListener {

    @Inject
    private DataAgent dataAgent;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        event.getServletContext().setAttribute("dataAgent", dataAgent);
    }
    
}

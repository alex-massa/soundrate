package control.dispatchers.fragments;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/sign-in-modal"})
public class SignInFragmentServlet extends HttpServlet {

    private static final long serialVersionUID = 4729363989307223388L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        request.getRequestDispatcher("/WEB-INF/jsp/fragments/sign-in.jsp").forward(request, response);
    }

}

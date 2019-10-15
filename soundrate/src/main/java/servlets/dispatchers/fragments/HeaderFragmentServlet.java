package servlets.dispatchers.fragments;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet({"/header"})
public class HeaderFragmentServlet extends HttpServlet {

    private static final long serialVersionUID = -8973548216641109056L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/jsp/fragments/header.jsp").forward(request, response);
    }

}

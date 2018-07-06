package servlets;

import com.google.gson.Gson;
import constants.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})

public class LoginServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private RoomsManager roomsManager;

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        response.setContentType("text/html;charset=UTF-8");
        String usernameFromSession = Utils.SessionUtils.getUsername(request);
        roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());

        if (request.getParameter("getUserName") != null && usernameFromSession != null) {
            response.getWriter().println("rooms.html");
            response.getWriter().flush();
        } else if (usernameFromSession == null) {
            String usernameFromParameter = request.getParameter(Constants.USERNAME);
            if (usernameFromParameter == null) {
                String errorMessage = "<p>Please enter a username.</p>";
                request.setAttribute(Constants.USER_NAME_ERROR, errorMessage);
                getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
            } else {
                usernameFromParameter = usernameFromParameter.trim();

                if (roomsManager.isPlayerNameExist(usernameFromParameter)) {
                    String errorMessage = " " + usernameFromParameter + " already exists. <p>Please enter a different username.</p>";
                    request.setAttribute(Constants.USER_NAME_ERROR, errorMessage);
                    getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
                } else {
                    roomsManager.addNewOnlinePlayer(usernameFromParameter);
                    handleLoginAttributes(request, response, usernameFromParameter);
                }
            }
        } else {
            //user is already logged in (know from session)
            response.sendRedirect("rooms.html");
        }

    }

    private void handleLoginAttributes(HttpServletRequest request, HttpServletResponse response, String usernameFromParameter) throws IOException {
        //add the new user to the users list
        request.getSession(true).setAttribute(Constants.USERNAME, usernameFromParameter);
        //redirect the request to the rooms - in order to actually change the URL
        response.sendRedirect("rooms.html");
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

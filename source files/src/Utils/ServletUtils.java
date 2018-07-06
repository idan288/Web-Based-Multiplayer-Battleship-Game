package Utils;

import servlets.RoomsManager;

import javax.servlet.ServletContext;

public class ServletUtils {

    private static final String ROOM_MANAGER_ATTRIBUTE_NAME = "RoomsManager";

    public static RoomsManager getRoomsManager(ServletContext servletContext) {
        if (servletContext.getAttribute(ROOM_MANAGER_ATTRIBUTE_NAME) == null) {
            servletContext.setAttribute(ROOM_MANAGER_ATTRIBUTE_NAME, new RoomsManager());
        }
        return (RoomsManager) servletContext.getAttribute(ROOM_MANAGER_ATTRIBUTE_NAME);
    }

}

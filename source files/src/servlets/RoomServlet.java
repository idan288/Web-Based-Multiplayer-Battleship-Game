package servlets;

import GameLogic.*;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
@WebServlet(name = "RoomsServlet", urlPatterns = {"/rooms"})
public class RoomServlet extends HttpServlet {
    private RoomsManager roomsManager;
    private final XMLReader xmlReader = new XMLReader();
    private final Gson gson = new Gson();

    private void processPostRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        String requestType = request.getParameter("requestType");

        if (Objects.equals(requestType, "fileUpload")) {
            handleXMLFile(request, response);
        }

    }

    private void processGetRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String requestType = request.getParameter("requestType");

        switch (requestType) {
            case "userName":
                handleUserName(request, response);
                break;
            case "userList":
                handleUserList(request, response);
                break;
            case "roomList":
                handleRoomList(request, response);
                break;
            case "enterRoom":
                handleEnterRoom(request, response);
                break;
            case "logout":
                handleLogout(request, response);
                break;
            case "deleteRoom":
                handleDeleteRoom(request, response);
                break;
        }
    }

    private void handleDeleteRoom(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("userName");
        String roomName = request.getParameter("roomName");
        ArrayList<Object> res = new ArrayList<>();
        Boolean isDeleted;
        String msg;
        if (roomsManager.isRoomCreatedBy(roomName, username)) {
            if (!roomsManager.hasLoginPlayers(roomName)) {
                roomsManager.deleteRoom(roomName);
                isDeleted = true;
                msg = "The room " + roomName + " has deleted.";
            } else {
                isDeleted = false;
                msg = "The room can't being deleted because he have a login players.";
            }
        } else {
            isDeleted = false;
            msg = "The room can't delete because isn't create by you.";
        }

        res.add(isDeleted);
        res.add(msg);
        response.getWriter().println(gson.toJson(res));
        response.getWriter().flush();
    }

    private void handleEnterRoom(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //user want to enter the room.
        //check if there is less then 2 player in the room.
        // so we can add another player.
        if (roomsManager == null) {
            roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());
        }

        String roomName = request.getParameter("roomName");
        String username = request.getParameter("userName");
        Map<String, String> result = new HashMap<>();

        //user doesn't exist so register them
        if (roomsManager.checkIfPlayerCanIn(roomName, username)) {
            // room isn't full
            roomsManager.addPlayerToRoom(roomName, username);

            result.put("redirect", "game.html");
            Cookie roomNameCookie = new Cookie("roomName", roomName);
            Cookie userNameCookie = new Cookie("userName", username);
            Cookie gameTypeCookie = new Cookie("gameType", roomsManager.getGameType(roomName));
            roomNameCookie.setPath("/");
            userNameCookie.setPath("/");
            gameTypeCookie.setPath("/");
            response.addCookie(roomNameCookie); // so the client side will remember his room id after redirect
            response.addCookie(userNameCookie);
            response.addCookie(gameTypeCookie);
        } else if (roomsManager.isGameRun(roomName)) {
            result.put("error", "Game is already running");
        } else if (roomsManager.isUserAlreadyExist(roomName, username)) {
            result.put("error", "You can't play with yourself.");
        } else {
            // room is full
            result.put("error", "Room is full");
        }
        String json = gson.toJson(result);
        response.getWriter().write(json);
    }

    private void handleRoomList(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        PrintWriter out = response.getWriter();
        if (roomsManager == null) {
            roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());
        }

        String roomsList = gson.toJson(roomsManager.getRoomList());

        out.println(roomsList);
        out.flush();
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (roomsManager == null) {
            roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());
        }
        roomsManager.removePlayer(request.getParameter("userName"));
        Utils.SessionUtils.clearSession(request);

        String json = gson.toJson("index.html");
        response.getWriter().write(json);
    }

    private void handleUserName(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        String userName = gson.toJson(Utils.SessionUtils.getUsername(request));
        out.println(userName);
        out.flush();
    }

    private void handleUserList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        if (roomsManager == null) {
            roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());
        }

        String playerListJson = gson.toJson(roomsManager.getPlayerList());
        out.println(playerListJson);
        out.flush();
    }


    private void handleXMLFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (roomsManager == null) {
            roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());
        }

        Part file = request.getPart("XMLFile");
        String fileName = request.getParameter("fileName");
        String responseMSG;
        String roomName = request.getParameter("roomName");
        if (roomsManager.isRoomNameExist(roomName)) {
            responseMSG = "The room name already exist.";
        } else {
            if (xmlReader.getFileExtension(fileName).equals("xml")) {
                try {
                    ArrayList<ArrayList<Character>> player1Board = new ArrayList<>();
                    ArrayList<ArrayList<Character>> player2Board = new ArrayList<>();
                    ArrayList<BattleShip> battleShipsPlayer1 = new ArrayList<BattleShip>();
                    ArrayList<BattleShip> battleShipsPlayer2 = new ArrayList<BattleShip>();
                    int[] mineAmount = new int[1];
                    String[] gameType = new String[1];
                    xmlReader.loadXML(file.getInputStream(), player1Board, player2Board, gameType, battleShipsPlayer1, battleShipsPlayer2, mineAmount);
                    GameLogic gameLogic = new GameLogic(gameType[0], player1Board, player2Board, battleShipsPlayer1, battleShipsPlayer2, "", "", mineAmount[0]);
                    Room room = new Room(roomName, gameType[0], request.getParameter("userName"), gameLogic, mineAmount[0]);
                    roomsManager.addNewRoom(room);
                    responseMSG = "Room added successfully";

                } catch (Exception e) {
                    responseMSG = e.getMessage();
                }
            } else {
                responseMSG = "The extension of file must be .xml";
            }
        }
        response.getWriter().println(responseMSG);
        response.getWriter().flush();
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processPostRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processGetRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}

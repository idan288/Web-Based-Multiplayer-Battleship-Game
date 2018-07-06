package servlets;

import GameLogic.BattleShip;
import com.google.gson.Gson;
import constants.Constants;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


@WebServlet(name = "GameServlet", urlPatterns = "/game")
public class GameServlet extends HttpServlet {
    private final Gson gson = new Gson();
    private RoomsManager roomsManager;

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String requestType = request.getParameter("requestType");

        switch (requestType) {
            case Constants.CHECK_GAME_START:
                handleCheckGameStart(request, response);
                break;
            case Constants.GET_BOARD:
                handleGetBoard(request, response);
                break;
            case Constants.GET_PLAYER_DETAILS:
                handlePlayerDetails(request, response);
                break;
            case Constants.CLICK_ON_BOARD:
                handelClickOnboard(request, response);
                break;
            case Constants.MINE_PLACE:
                handelMinePlace(request, response);
                break;
            case Constants.GET_GAME_STATUS:
                handleGetGameStatus(request, response);
                break;
            case Constants.GET_PLAYERS_LIST:
                handleGetPlayerList(request, response);
                break;
            case Constants.GET_SHIPS_LIST:
                handleGetShips(request, response);
                break;
            case Constants.MINE_MOVE:
                handleMineMove(request, response);
                break;
            case Constants.GET_STATISTICS:
                handleStatistics(request, response);
                break;
            case Constants.GET_GAMEFINISH:
                handleGameFinish(request, response);
                break;
            case Constants.LEAVE:
                handleLeave(request, response);
                break;
            case Constants.RETURNROOMS:
                handleExitStatisticsPage(request, response);
                break;
        }
    }

    private void handleExitStatisticsPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> result = new HashMap<>();
        result.put("redirect", "rooms.html");
        String json = gson.toJson(result);
        response.getWriter().write(json);
    }

    private void handleLeave(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        String avgAttackTime = request.getParameter("avgTime");

        roomsManager.updateAvgAttack(roomName, userName, avgAttackTime);
        roomsManager.userWantToLeave(userName, roomName);
        handleExitStatisticsPage(request, response);
    }

    private void handleGameFinish(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        String avgAttackTime = request.getParameter("avgTime");

        roomsManager.updateAvgAttack(roomName, userName, avgAttackTime);
        roomsManager.removeUserFromRoom(roomName, userName);
        Map<String, String> result = new HashMap<>();
        result.put("redirect", "statistics.html");
        Cookie roomNameCookie = new Cookie("roomName", roomName);
        Cookie userNameCookie = new Cookie("userName", userName);
        roomNameCookie.setPath("/");
        userNameCookie.setPath("/");
        response.addCookie(roomNameCookie); // so the client side will remember his room id after redirect
        response.addCookie(userNameCookie);

        String json = gson.toJson(result);
        response.getWriter().write(json);
    }

    private void handleStatistics(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");

        ArrayList<ArrayList<Object>> result = roomsManager.getRoomStatistics(roomName);

        response.getWriter().println(gson.toJson(result));
        response.getWriter().flush();
    }

    private void handleMineMove(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));
        Boolean res = roomsManager.makeMineMove(roomName, userName, row, col);

        response.getWriter().println(gson.toJson(res));
        response.getWriter().flush();
    }

    private void handleGetShips(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");

        ArrayList<ArrayList<BattleShip>> result = new ArrayList<>(2);
        roomsManager.getShips(result, roomName, userName);
        response.getWriter().println(gson.toJson(result));
        response.getWriter().flush();
    }

    private void handleGetPlayerList(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String res = gson.toJson(roomsManager.getPlayerLists(roomName));

        response.getWriter().println(res);
        response.getWriter().flush();
    }

    private void handleGetGameStatus(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");

        String res = gson.toJson(roomsManager.getGameStatus(roomName, userName));

        response.getWriter().println(res);
        response.getWriter().flush();
    }

    private void handelMinePlace(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));

        String res = gson.toJson(roomsManager.isMineLegal(roomName, userName, row, col));

        response.getWriter().println(res);
        response.getWriter().flush();
    }

    private void handelClickOnboard(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        int row = Integer.parseInt(request.getParameter("row"));
        int col = Integer.parseInt(request.getParameter("col"));

        String res = gson.toJson(roomsManager.clickOnBoard(roomName, userName, row, col));

        response.getWriter().println(res);
        response.getWriter().flush();
    }

    private void handleGetBoard(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        String boardType = request.getParameter("boardType");
        String board = gson.toJson(roomsManager.getBoard(roomName, userName, boardType));

        response.getWriter().println(board);
        response.getWriter().flush();
    }

    private void handlePlayerDetails(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");
        String playerDetails = gson.toJson(roomsManager.getPlayerDetails(roomName, userName));

        response.getWriter().println(playerDetails);
        response.getWriter().flush();
    }

    private void handleCheckGameStart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (roomsManager == null) {
            roomsManager = Utils.ServletUtils.getRoomsManager(getServletContext());
        }

        String roomName = request.getParameter("roomName");
        String userName = request.getParameter("userName");

        ArrayList<Object> result = new ArrayList<>(3);
        if (roomsManager.startGameRoom(roomName)) {
            // the game can start.
            //    result = new Pair<>(true, roomsManager.getPlayerLists(roomName));
            result.add(true);
            result.add(roomsManager.getPlayerLists(roomName));
            result.add(roomsManager.thisIsPlayerTurn(roomName, userName));
            result.add(roomsManager.getOppPlayerName(roomName, userName));
        } else {
            // missing player.
            result.add(false);
            result.add(roomsManager.getPlayerLists(roomName));
            result.add(roomsManager.thisIsPlayerTurn(roomName, userName));
        }

        response.getWriter().println(gson.toJson(result));
        response.getWriter().flush();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);

    }
}
package servlets;

import GameLogic.BattleShip;
import GameLogic.Player;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.*;

public class RoomsManager implements ServletContextListener {
    private final ArrayList<String> onlinePlayers = new ArrayList<>();
    private final Map<String, Room> roomList = new HashMap<>();

    public Boolean makeMineMove(String roomName, String userName, int row, int col) {
        return roomList.get(roomName).makeMineMove(userName, row, col);
    }

    public void userWantToLeave(String userName, String roomName) {
        roomList.get(roomName).userWantToLeave(userName);
    }

    public void removeUserFromRoom(String roomName, String userName) {
        roomList.get(roomName).removeUser(userName);
    }

    public Boolean isPlayerNameExist(String playerName) {
        for (String pname : onlinePlayers) {
            if (pname.equals(playerName)) {
                return true;
            }
        }
        return false;
    }

    public String getGameType(String roomName) {
        Room room = roomList.get(roomName);
        return room.getGameType();
    }

    public ArrayList<ArrayList<Object>> getRoomStatistics(String roomName) {
        Room room = roomList.get(roomName);
        ArrayList<ArrayList<Object>> res = new ArrayList<>(3);

        // win Player statistics
        ArrayList<Object> winPStatistics = new ArrayList<>(7);
        Player winPlayer = room.getWinPlayer();
        winPStatistics.add(winPlayer.getName());
        winPStatistics.add(winPlayer.getScore());
        winPStatistics.add(winPlayer.getNumOfHitting());
        winPStatistics.add(winPlayer.getNumOfMisses());
        winPStatistics.add(winPlayer.getAverageTime());
        winPStatistics.add(winPlayer.getBattleShipBoard());
        winPStatistics.add(winPlayer.getHitingBoard());
        res.add(winPStatistics);

        // lose Player statistics
        ArrayList<Object> losePStatistics = new ArrayList<>(7);
        Player losePlayer = room.getLosePlayer();
        losePStatistics.add(losePlayer.getName());
        losePStatistics.add(losePlayer.getScore());
        losePStatistics.add(losePlayer.getNumOfHitting());
        losePStatistics.add(losePlayer.getNumOfMisses());
        losePStatistics.add(losePlayer.getAverageTime());
        losePStatistics.add(losePlayer.getBattleShipBoard());
        losePStatistics.add(losePlayer.getHitingBoard());
        res.add(losePStatistics);

        // global Statistics
        ArrayList<Object> global = new ArrayList<>(5);
        global.add(room.getGameLogic().getTotalTurns());
        global.add(room.getGameType());
        global.add(winPlayer.getBattleShips());
        global.add(losePlayer.getBattleShips());
        global.add(room.getGameFinishTime());
        res.add(global);

        return res;
    }

    public void getShips(ArrayList<ArrayList<BattleShip>> ships, String roomName, String userName) {
        roomList.get(roomName).getShips(ships, userName);
    }

    public Boolean isMineLegal(String roomName, String userName, int row, int col) {
        Room room = roomList.get(roomName);
        return room.isMinePlaceLegal(userName, row, col);
    }

    public String getGameStatus(String roomName, String userName) {
        return roomList.get(roomName).getGameStatus(userName);
    }

    public int getMineAmount(String roomName) {
        Room room = roomList.get(roomName);
        return room.getMineAmount();
    }

    public String getOppPlayerName(String roomName, String myName) {
        Room room = roomList.get(roomName);
        int index = room.getIndexOfPlayer(myName);
        PlayerInfo p = room.getPlayerLists().get((index + 1) % 2);
        return p.getName();
    }

    public void addNewOnlinePlayer(String pName) {
        onlinePlayers.add(pName);
    }

    public List<PlayerInfo> getPlayerLists(String roomName) {
        Room room = roomList.get(roomName);
        return room.getPlayerLists();
    }

    public Boolean thisIsPlayerTurn(String roomName, String userName) {
        Room room = roomList.get(roomName);

        return room.getIndexOfPlayer(userName) == room.getCurPlayerTurn();
    }

    public void removePlayer(String name) {
        for (String pname : onlinePlayers) {
            if (pname.equals(name)) {
                onlinePlayers.remove(pname);
                return;
            }
        }
    }

    public boolean checkIfPlayerCanIn(String roomName, String userName) {
        Room room = roomList.get(roomName);

        if (room.numofPlayerInRoom() > 1) {
            return false;
        } else if (room.isUserNameExists(userName)) {
            return false;
        }
        return true;
    }

    public void addPlayerToRoom(String roomName, String playerName) {
        Room room = roomList.get(roomName);
        room.setPlayer(playerName);
    }

    public ArrayList<String> getPlayerDetails(String roomName, String userName) {
        ArrayList<String> pDetails = new ArrayList<>();
        Room room = roomList.get(roomName);
        Player p = room.getPlayer(userName);
        pDetails.add(Integer.toString(p.getMinesAmount()));
        pDetails.add(Integer.toString(room.getGameLogic().getTotalTurns()));
        pDetails.add(Integer.toString(p.getNumOfMisses()));
        pDetails.add(Integer.toString(p.getNumOfHitting()));
        pDetails.add("00:00:00");
        pDetails.add(Integer.toString(room.getIndexOfPlayer(userName)));
        return pDetails;
    }

    public ArrayList<ArrayList<Character>> getBoard(String roomName, String userName, String boardType) {
        Room room = roomList.get(roomName);
        return room.getBoard(userName, boardType);
    }

    public String clickOnBoard(String roomName, String userName, int row, int col) {
        Room room = roomList.get(roomName);
        return room.handleClickOnBoard(userName, row, col);
    }

    public Boolean isRoomNameExist(String name) {
        return roomList.containsKey(name);
    }

    public List<String> getPlayerList() {
        return onlinePlayers;
    }

    public List<Room> getRoomList() {
        return new ArrayList<Room>(roomList.values());
    }

    public void addNewRoom(Room room) {
        roomList.put(room.getRoomName(), room);
    }

    public boolean isGameRun(String roomName) {
        Room room = roomList.get(roomName);
        return room.isGameBegin();
    }

    public boolean startGameRoom(String roomName) {
        Room room = roomList.get(roomName);
        if (room.numofPlayerInRoom() == 2) {
            room.startGame();
            return true;
        }
        return false;
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        servletContextEvent.getServletContext().setAttribute("RoomsManager", this);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
    }

    public boolean isRoomCreatedBy(String roomName, String username) {
        return roomList.get(roomName).getCreatedBy().equals(username);
    }

    public void deleteRoom(String roomName) {
        for (String room : roomList.keySet()) {
            if (room.equals(roomName)) {
                roomList.remove(room);
                break;
            }
        }
    }

    public boolean hasLoginPlayers(String roomName) {
        return roomList.get(roomName).numofPlayerInRoom() > 0;
    }

    public boolean isUserAlreadyExist(String roomName, String userName) {
        return roomList.get(roomName).isUserNameExists(userName);
    }

    public void updateAvgAttack(String roomName, String userName, String avgAttackTime) {
        int ind = roomList.get(roomName).getIndexOfPlayer(userName);
        if (ind != -1) {
            roomList.get(roomName).getPlayer(userName).updateAverageTime(Long.parseLong(avgAttackTime));
        }
    }
}

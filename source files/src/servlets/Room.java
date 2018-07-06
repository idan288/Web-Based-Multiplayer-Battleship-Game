package servlets;

import GameLogic.*;
import jaxb.schema.generated.BattleShipGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Room {

    private String roomName;
    private ArrayList<PlayerInfo> players = new ArrayList<>(2);
    private GameLogic gameLogic;
    private boolean isGameBegin = false;
    private int mineAmount;
    private int numofPlayerInRoom = 0;
    private String createdBy;
    private String gameType;
    private int boardSize;
    private int curPlayerTurn = 0;
    private String gameStatus = "waiting";
    private String gameFinishTime;

    public Room(String roomName, String gameType, String createdBy, GameLogic gameLogic, int mineAmount) {
        this.roomName = roomName;
        this.gameLogic = gameLogic;
        this.mineAmount = mineAmount;
        this.createdBy = createdBy;
        this.gameType = gameType;
        this.boardSize = gameLogic.getBoardSize();
    }

    public void removeUser(String userName) {
        removePlayerByName(userName);
        numofPlayerInRoom--;
    }

    private void removePlayerByName(String userName) {
        for (PlayerInfo p : players) {
            if (p.getName().equals(userName)) {
                players.remove(p);
                break;
            }
        }
    }

    public void setPlayer(String playerName) {
        players.add(new PlayerInfo(playerName, 0, players.size()));
        numofPlayerInRoom++;
    }

    public void userWantToLeave(String userName) {
        removePlayerByName(userName);
        numofPlayerInRoom--;
        gameStatus = "leave";
    }

    public Boolean makeMineMove(String userName, int row, int col) {
        int curUserInd = getIndexOfPlayer(userName);
        BattleShipGame.Boards.Board.Ship.Position pos = new BattleShipGame.Boards.Board.Ship.Position();
        pos.setX(row);
        pos.setY(col);
        if (gameLogic.addMine(pos, curUserInd)) {
            curPlayerTurn = (curPlayerTurn + 1) % 2;
            return true;
        }
        return false;
    }

    public void getShips(ArrayList<ArrayList<BattleShip>> ships, String userName) {
        int oppInd = (getIndexOfPlayer(userName) + 1) % 2;
        Player p = getPlayer(userName);

        ships.add(p.getBattleShips());
        p = gameLogic.getPlayer(oppInd);
        ships.add(p.getBattleShips());
    }

    public ArrayList<ArrayList<Character>> getBoard(String userName, String boardType) {
        Player player = getPlayer(userName);
        ArrayList<ArrayList<Character>> board;

        if (boardType.equals("shipsBoard")) {
            board = player.getBattleShipBoard().getBoard();
        } else {
            board = player.getHitingBoard().getBoard();
        }
        return board;
    }

    public Player getWinPlayer() {
        return gameLogic.getPlayer(curPlayerTurn);
    }

    public Player getLosePlayer() {
        return gameLogic.getPlayer((curPlayerTurn + 1) % 2);
    }

    public Boolean isMinePlaceLegal(String userName, int row, int col) {
        int playerIndex = getIndexOfPlayer(userName);
        return gameLogic.isValidMinePlace(row, col, playerIndex);
    }

    public int getCurPlayerTurn() {
        return curPlayerTurn;
    }

    public String handleClickOnBoard(String userName, int row, int col) {
        int indexCurPlayer = getIndexOfPlayer(userName);
        int oppPlayerIndex = (indexCurPlayer + 1) % 2;
        BattleShipGame.Boards.Board.Ship.Position pos = new BattleShipGame.Boards.Board.Ship.Position();
        pos.setX(row);
        pos.setY(col);

        GameLogic.GameStatus status = gameLogic.UserMove(pos, indexCurPlayer);
        players.get(indexCurPlayer).setScore(gameLogic.getPlayer(indexCurPlayer).getScore());
        players.get(oppPlayerIndex).setScore(gameLogic.getPlayer(oppPlayerIndex).getScore());
        return getStringOfGameStatus(status);
    }

    public String getGameStatus(String userName) {
        String res = gameStatus;

        switch (gameStatus) {
            case "run":
                res = curPlayerTurn == getIndexOfPlayer(userName) ? "myTurn" : "oppTurn";
                break;
        }
        return res;
    }


    private String getStringOfGameStatus(GameLogic.GameStatus status) {
        switch (status) {
            case BadChoose:
                curPlayerTurn = (curPlayerTurn + 1) % 2;
                return "bad";
            case Miss:
                curPlayerTurn = (curPlayerTurn + 1) % 2;
                return "miss";
            case Win:
                gameFinishTime = gameLogic.getElpesedTime();
                gameStatus = "win";
                return "win";
            case Hit:
                return "hit";
            case Mine:
                curPlayerTurn = (curPlayerTurn + 1) % 2;
                return "hitMine";
        }
        curPlayerTurn = (curPlayerTurn + 1) % 2;
        return "bad";
    }

    public String getGameFinishTime() {
        return gameFinishTime;
    }

    public Player getPlayer(String name) {
        return gameLogic.getPlayer(getIndexOfPlayer(name));
    }

    public int getIndexOfPlayer(String name) {
        for (PlayerInfo p : players) {
            if (p.getName().equals(name)) {
                return p.getPlayerIndex();
            }
        }
        return -1;
    }


    public List<PlayerInfo> getPlayerLists() {
        return players;
    }

    public void startGame() {
        gameLogic.resetGame();
        gameLogic.getPlayer(0).setName(players.get(0).getName());
        gameLogic.getPlayer(1).setName(players.get(1).getName());
        curPlayerTurn = 0;
        gameStatus = "run";
    }


    public boolean isGameBegin() {
        return isGameBegin;
    }

    public String getGameType() {
        return gameType;
    }

    public int numofPlayerInRoom() {
        return numofPlayerInRoom;
    }

    public int getMineAmount() {
        return mineAmount;
    }

    public GameLogic getGameLogic() {
        return gameLogic;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public boolean isUserNameExists(String userName) {
        for (PlayerInfo p : players) {
            if (p.getName().equals(userName)) {
                return true;
            }
        }
        return false;
    }
}

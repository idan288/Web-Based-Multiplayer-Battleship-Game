package GameLogic;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jaxb.schema.generated.BattleShipGame;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GameLogic {
    public static final char SHIPSIGN = 'O';
    public static final char HITINGSIGN = 'X';
    public static final char HITMINE = '@';
    public static final char MISSSIGN = '-';
    public static final char MINESIGN = '*';
    private final char EMPTY = ' ';
    private final int NUMOFPLAYERS = 2;
    private Player[] players;
    private String gameType;
    private int boardSize;
    private long startTime;
    private int numOfTurns;
    private IntegerProperty totallTurns = new SimpleIntegerProperty();
    private StringProperty timeElps = new SimpleStringProperty();
    private int mineAmount;
    private BattleShip lastDeadShip = null;
    private int curPlayerIndex = 0;

    public enum GameStatus {
        Win,
        Miss,
        Hit,
        Mine,
        BadChoose,
    }

    public void setCurPlayerIndex(int ind) {
        curPlayerIndex = ind;
    }

    public GameLogic(String gameType, ArrayList<ArrayList<Character>> Player1Board, ArrayList<ArrayList<Character>> Player2Board,
                     ArrayList<BattleShip> battleShipsPlayer1, ArrayList<BattleShip> battleShipsPlayer2, String player1name, String player2name, int mineAmount) {
        players = new Player[NUMOFPLAYERS];
        players[0] = new Player(Player1Board, player1name, battleShipsPlayer1, mineAmount);
        players[1] = new Player(Player2Board, player2name, battleShipsPlayer2, mineAmount);
        this.gameType = gameType;
        this.mineAmount = mineAmount;
        boardSize = Player2Board.size();
        startTime = System.nanoTime();
    }

    public int getMineAmount() {
        return mineAmount;
    }

    public GameStatus UserMove(BattleShipGame.Boards.Board.Ship.Position pos, int playerIndex) {
        GameStatus status;
        Board hitBoard = players[playerIndex].getHitingBoard(); // hitting board of the attacker.
        Board shipBoard = players[(playerIndex + 1) % 2].getBattleShipBoard(); // ship board of the insulter.
        numOfTurns++;
        totallTurns.setValue(numOfTurns);
        // if the attack is on prev attack.
        if (hitBoard.getBoard().get(pos.getX()).get(pos.getY()) != EMPTY) {
            status = GameStatus.BadChoose;
        } else if (shipBoard.getBoard().get(pos.getX()).get(pos.getY()) == SHIPSIGN) {
            status = GameStatus.Hit;
            players[playerIndex].addNumOfHitting(1);
            shipBoard.getBoard().get(pos.getX()).set(pos.getY(), HITINGSIGN);
            hitBoard.getBoard().get(pos.getX()).set(pos.getY(), HITINGSIGN);
            if (checkForWinAndUpdateShips(players[(playerIndex + 1) % 2], players[playerIndex])) {
                status = GameStatus.Win;
            }
        } else if (shipBoard.getBoard().get(pos.getX()).get(pos.getY()) == MINESIGN) {
            status = GameStatus.Mine;
            players[playerIndex].addNumOfHitting(1);
            shipBoard.getBoard().get(pos.getX()).set(pos.getY(), HITMINE);
            hitBoard.getBoard().get(pos.getX()).set(pos.getY(), HITMINE);
            Board shipBoardOfCurPlayer = players[playerIndex].getBattleShipBoard(); //ship board of the attacker.
            Board HittingBoradOfTheAttacker = players[(playerIndex + 1) % 2].getHitingBoard();


            if (shipBoardOfCurPlayer.getBoard().get(pos.getX()).get(pos.getY()) == EMPTY) {
                HittingBoradOfTheAttacker.getBoard().get(pos.getX()).set(pos.getY(), MISSSIGN);
                shipBoardOfCurPlayer.getBoard().get(pos.getX()).set(pos.getY(), MISSSIGN);
            } else if (shipBoardOfCurPlayer.getBoard().get(pos.getX()).get(pos.getY()) == SHIPSIGN) {
                HittingBoradOfTheAttacker.getBoard().get(pos.getX()).set(pos.getY(), HITINGSIGN);
                shipBoardOfCurPlayer.getBoard().get(pos.getX()).set(pos.getY(), HITINGSIGN);
                if (checkForWinAndUpdateShips(players[playerIndex], players[(playerIndex + 1) % 2])) {
                    status = GameStatus.Win;
                }
            } else if (shipBoardOfCurPlayer.getBoard().get(pos.getX()).get(pos.getY()) == MINESIGN) {
                HittingBoradOfTheAttacker.getBoard().get(pos.getX()).set(pos.getY(), MISSSIGN);
                shipBoardOfCurPlayer.getBoard().get(pos.getX()).set(pos.getY(), MISSSIGN);
            }
        } else {
            status = GameStatus.Miss;
            players[playerIndex].addNumOfMisses(1);
            shipBoard.getBoard().get(pos.getX()).set(pos.getY(), MISSSIGN);
            hitBoard.getBoard().get(pos.getX()).set(pos.getY(), MISSSIGN);
        }

        return status;
    }

    private boolean checkForWinAndUpdateShips(Player player, Player curPlayer) {
        int count;
        lastDeadShip = null;

        ArrayList<BattleShip> shipsToDelete = new ArrayList<>();

        for (BattleShip ship : player.getBattleShips()) {
            count = 0;
            for (BattleShipGame.Boards.Board.Ship.Position pos : ship.getShipPosition()) {
                if (player.getBattleShipBoard().getBoard().get(pos.getX()).get(pos.getY()) == HITINGSIGN) {
                    count++;
                }
            }
            if (count == ship.getShipPosition().size()) { // ship is full dead.
                shipsToDelete.add(ship);
            }
        }

        for (BattleShip ship : shipsToDelete) {
            curPlayer.addScore(ship.getScore());
            lastDeadShip = ship;
            player.getBattleShips().remove(ship);
        }

        if (player.getBattleShips().size() == 0) {
            return true;
        }

        return false;
    }

    public BattleShip getLastDeadShip() {
        return lastDeadShip;
    }

    public boolean addMine(BattleShipGame.Boards.Board.Ship.Position userMov, int playerIndex) {
        int x = userMov.getX();
        int y = userMov.getY();
        ArrayList<ArrayList<Character>> board = players[playerIndex].getBattleShipBoard().getBoard();

        if (board.get(x).get(y) != EMPTY) {
            return false;
        }
        if (checkValidPositionOfPoint(x, y, board)) {
            players[playerIndex].getMines().add(new Mine(userMov));
            board.get(x).set(y, MINESIGN);
            numOfTurns++;
            totallTurns.setValue(numOfTurns);
            return true;
        }

        return false;
    }

    public int getTotalTurns() {
        return numOfTurns;
    }

    public boolean isValidMinePlace(int row, int col, int index) {
        Player p1 = getPlayer(index);
        ArrayList<ArrayList<Character>> board = p1.getBattleShipBoard().getBoard();

        return board.get(row).get(col) == EMPTY && checkValidPositionOfPoint(row, col, board);
    }


    public boolean checkValidPositionOfPoint(int x, int y, ArrayList<ArrayList<Character>> board) {
        // check for up
        if (x - 1 > -1 && (board.get(x - 1).get(y) == SHIPSIGN || board.get(x - 1).get(y) == MINESIGN)) {
            return false;
        }

        //down
        if (x + 1 < boardSize && (board.get(x + 1).get(y) == SHIPSIGN || board.get(x + 1).get(y) == MINESIGN)) {
            return false;
        }

        //left
        if (y - 1 > -1 && (board.get(x).get(y - 1) == SHIPSIGN || board.get(x).get(y - 1) == MINESIGN)) {
            return false;
        }

        // right
        if (y + 1 < boardSize && (board.get(x).get(y + 1) == SHIPSIGN || board.get(x).get(y + 1) == MINESIGN)) {
            return false;
        }

        // up left
        if (x - 1 > -1 && y - 1 > -1 && (board.get(x - 1).get(y - 1) == SHIPSIGN || board.get(x - 1).get(y - 1) == MINESIGN)) {
            return false;
        }
        // down left
        if (x + 1 < boardSize && y - 1 > -1 && (board.get(x + 1).get(y - 1) == SHIPSIGN || board.get(x + 1).get(y - 1) == MINESIGN)) {
            return false;
        }

        // up right
        if (x - 1 > -1 && y + 1 < boardSize && (board.get(x - 1).get(y + 1) == SHIPSIGN || board.get(x - 1).get(y + 1) == MINESIGN)) {
            return false;
        }

        // down right
        if (x + 1 < boardSize && y + 1 < boardSize && (board.get(x + 1).get(y + 1) == SHIPSIGN || board.get(x + 1).get(y + 1) == MINESIGN)) {
            return false;
        }

        return true;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public Player[] getPlayers() {
        return players;
    }

    public Player getPlayer(int index) {
        return players[index];
    }

    public long getStartTime() {
        return startTime;
    }

    public String getElpesedTime() {
        long difference = System.nanoTime() - startTime;
        long hours = TimeUnit.NANOSECONDS.toSeconds(difference) / 3600;
        difference = TimeUnit.NANOSECONDS.toSeconds(difference) - (hours * 3600);
        long minutes = difference / 60;
        long seconds = difference - (minutes * 60);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


    public void resetGame() {
        for (int i = 0; i < NUMOFPLAYERS; i++) {
            players[i].reset();
        }

        startTime = System.nanoTime();
        numOfTurns = 0;
        totallTurns.setValue(0);
    }

    public IntegerProperty getNumOfTurns() {
        return totallTurns;
    }

    public final StringProperty timeElps() {
        getElpesedTime();
        return timeElps;
    }

    public void incNumOfTurns() {
        numOfTurns++;
        totallTurns.setValue(numOfTurns);
    }
}

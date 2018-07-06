package GameLogic;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jaxb.schema.generated.BattleShipGame;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Player {
    private Board battleShipBoard;
    private Board hitingBoard;
    private ArrayList<BattleShip> battleShips;
    private ArrayList<BattleShip> saveBattleShips;
    private ArrayList<Mine> mines;
    private int score;
    private int numOfMisses;
    private int numOfHitting;
    private int shipsNAmount;
    private String name;
    private long avarageTime;
    private IntegerProperty mineAmount = new SimpleIntegerProperty();
    private int originalMineAmount;

    private Player() {
    }


    public Player(ArrayList<ArrayList<Character>> battleShipBoard, String name, ArrayList<BattleShip> ships, int mineAmount) {
        this.battleShipBoard = new Board(battleShipBoard);
        hitingBoard = new Board(this.battleShipBoard.getSize());
        mines = new ArrayList<Mine>();
        this.mineAmount.setValue(mineAmount);
        originalMineAmount = mineAmount;
        shipsNAmount = ships.size();
        battleShips = ships;
        saveBattleShips = new ArrayList<BattleShip>(shipsNAmount);
        copy(ships, saveBattleShips);
        score = 0;
        numOfHitting = 0;
        numOfMisses = 0;
        avarageTime = 0;
        this.name = name;
    }

    public IntegerProperty getMineAmount() {
        return mineAmount;
    }

    public int getMinesAmount() {
        return mineAmount.getValue();
    }

    public ObservableList<BattleShip> getBattleShipsTable() {
        ObservableList<BattleShip> shipsTable = FXCollections.observableArrayList();
        for (BattleShip ship : battleShips) {
            shipsTable.add(ship);
        }
        return shipsTable;
    }

    public ArrayList<BattleShip> getBattleShips() {
        return battleShips;
    }

    public int getShipsNAmount() {
        return shipsNAmount;
    }

    public void setShipsNAmount(int shipsNAmount) {
        this.shipsNAmount = shipsNAmount;
    }

    public String getName() {
        return name;
    }

    public int getNumOfMisses() {
        return numOfMisses;
    }

    public void addNumOfHitting(int numOfHitting) {
        this.numOfHitting += numOfHitting;
    }

    private void copy(ArrayList<BattleShip> src, ArrayList<BattleShip> des) {
        des.clear();
        for (BattleShip ship : src) {
            des.add(ship);
        }
    }

    void addNumOfMisses(int numOfMisses) {
        this.numOfMisses += numOfMisses;
    }

    public int getNumOfHitting() {
        return numOfHitting;
    }

    public int getScore() {
        return score;
    }

    void addScore(int score) {
        this.score += score;
    }

    public void updateAverageTime(long time) {
        avarageTime += time;
    }

    void reset() {
        score = 0;
        numOfHitting = 0;
        numOfMisses = 0;
        avarageTime = 0;
        hitingBoard.resetBoard();
        battleShipBoard.resetBoard();
        mineAmount.setValue(originalMineAmount);
        copy(saveBattleShips, battleShips);
    }

    public ArrayList<Mine> getMines() {
        return mines;
    }

    public void removeMine(BattleShipGame.Boards.Board.Ship.Position pos) {
        for (Mine mine : mines) {
            if (mine.getMinePosition().getY() == pos.getY() && mine.getMinePosition().getX() == pos.getX()) {
                mines.remove(mine);
                return;
            }
        }
    }

    public String getAverageTime() {
        long time = 0;
        if (avarageTime != 0) {
            time = avarageTime / (numOfMisses + numOfHitting);

        }
        long hours = time / 3600;
        long minutes = (time / 60) % 60;
        long seconds = time % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public Player deepCopy() {
        Player p = new Player();
        p.setScore(score);
        p.setNumOfHitting(numOfHitting);
        p.setAvarageTime(avarageTime);
        p.setName(name);
        p.setNumOfMisses(numOfMisses);
        p.setShipsNAmount(shipsNAmount);
        p.setMineAmount(mineAmount.getValue());
        p.setOriginalMineAmount(originalMineAmount);
        p.setBattleShipBoard(new Board(battleShipBoard.getSize()));
        copy(p.getBattleShipBoard(), battleShipBoard);
        p.setBattleShips(new ArrayList<>(battleShips.size()));
        copy(battleShips, p.getBattleShips());
        p.setSaveBattleShips(new ArrayList<>(saveBattleShips.size()));
        copy(saveBattleShips, p.getSaveButtleShips());
        p.setHitingBoard(new Board(hitingBoard.getSize()));
        copy(p.getHitingBoard(), hitingBoard);
        return p;
    }

    public ArrayList<BattleShip> getSaveButtleShips() {
        return saveBattleShips;
    }

    private void copy(Board des, Board src) {
        for (int i = 0; i < des.getSize(); i++) {
            for (int j = 0; j < des.getSize(); j++) {
                des.getBoard().get(i).set(j, src.getBoard().get(i).get(j));
            }
        }
    }

    public void subMineAmount(int amount) {
        mineAmount.setValue(mineAmount.getValue() - amount);
    }

    private void setScore(int score) {
        this.score = score;
    }

    public void setNumOfMisses(int numOfMisses) {
        this.numOfMisses = numOfMisses;
    }

    private void setNumOfHitting(int numOfHitting) {
        this.numOfHitting = numOfHitting;
    }

    private void setAvarageTime(long avarageTime) {
        this.avarageTime = avarageTime;
    }

    private void setHitingBoard(Board hitingBoard) {
        this.hitingBoard = hitingBoard;
    }

    private void setBattleShipBoard(Board battleShipBoard) {
        this.battleShipBoard = battleShipBoard;
    }

    private void setBattleShips(ArrayList<BattleShip> battleShips) {
        this.battleShips = battleShips;
    }

    public void setName(String name) {
        this.name = name;
    }

    private void setMineAmount(int amount) {
        mineAmount.setValue(amount);
    }

    private void setOriginalMineAmount(int amount) {
        originalMineAmount = amount;
    }

    private void setSaveBattleShips(ArrayList<BattleShip> saveBattleShips) {
        this.saveBattleShips = saveBattleShips;
    }

    public Board getBattleShipBoard() {
        return battleShipBoard;
    }

    public Board getHitingBoard() {
        return hitingBoard;
    }


}



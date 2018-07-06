package GameLogic;

import jaxb.schema.generated.BattleShipGame;

import java.util.ArrayList;

public class BattleShip {
    private String type;
    private int length;
    private int score;
    private int serialNumber;
    private ArrayList<BattleShipGame.Boards.Board.Ship.Position> shipPosition;

    public BattleShip() {

    }

    public BattleShip(int serialNum, String type, int length, int score, ArrayList<BattleShipGame.Boards.Board.Ship.Position> positions) {
        serialNumber = serialNum;
        this.type = type;
        this.length = length;
        this.score = score;
        shipPosition = positions;
    }

    public int getScore() {
        return score;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public ArrayList<BattleShipGame.Boards.Board.Ship.Position> getShipPosition() {
        return shipPosition;
    }

    public int getLength() {
        return length;
    }

    public ArrayList<BattleShipGame.Boards.Board.Ship.Position> getshipPostion() {
        return shipPosition;
    }

    public String getType() {
        return type;
    }

}



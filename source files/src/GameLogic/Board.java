package GameLogic;

import java.util.ArrayList;

public class Board {
    private ArrayList<ArrayList<Character>> board;
    private String type;
    private static final char SHIPSIGN = 'O';
    private static final char HITINGSIGN = 'X';
    private static final char MISSSIGN = '-';
    private static final char EMPTY = ' ';

    public Board(int size) {
        board = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            board.add(new ArrayList<>(size));
            for (int j = 0; j < size; j++) {
                board.get(i).add(j, EMPTY);
            }
        }
        type = "hit";
    }

    public Board(ArrayList<ArrayList<Character>> inputBoard) {
        board = inputBoard;
        type = "ship";
    }

    void resetBoard() {
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.size(); j++) {
                if (type.equals("ship")) {
                    if (board.get(i).get(j) == HITINGSIGN) {
                        board.get(i).set(j, SHIPSIGN);
                    } else if (board.get(i).get(j) == MISSSIGN) {
                        board.get(i).set(j, EMPTY);
                    }
                } else {
                    board.get(i).set(j, EMPTY);
                }
            }
        }

    }

    public ArrayList<ArrayList<Character>> getBoard() {
        return board;
    }

    public int getSize() {
        return board.size();
    }
}

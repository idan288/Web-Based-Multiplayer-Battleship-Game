package GameLogic;

import javafx.concurrent.Task;
import jaxb.schema.generated.BattleShipGame;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;

public class XMLReader extends Task<Void> {
    private static final String JAXB_XML_GAME_PACKAGE_NAME = "jaxb.schema.generated";
    private final int PLAYERSNUMBER = 2;
    private final int MINBOARDSIZE = 5;
    private final int MAXBOARDSIZE = 20;
    private static final Integer SLEEP_TIME = 500;
    // private final int COLUMNROWSHIPLENGTH = 3;
    private final char SHIPSIGN = 'O';
    private final char EMPTY = ' ';
    private final char TESTEDSHIPSIGN = '@';

    private String pathFile;
    private String[] gameType;
    private ArrayList<ArrayList<Character>> player1Board;
    private ArrayList<ArrayList<Character>> player2Board;
    private ArrayList<BattleShip> battleShipsPlayer1;
    private ArrayList<BattleShip> battleShipsPlayer2;
    private int[] mineAmount;


    private enum BasicDiretionsType {
        ROW,
        COLUMN
    }

    private enum AdvanceDirectionType {
        ROW,
        COLUMN,
        RIGHT_UP,
        RIGHT_DOWN,
        UP_RIGHT,
        DOWN_RIGHT,
    }

    private enum DirectionsPositions {
        LEFTROW,
        RIGHTROW,
        UPCOLUMN,
        DOWNCOLUMN,
    }

    /* public XMLReader(String pathFile, ArrayList<ArrayList<Character>> player1Board,
                      ArrayList<ArrayList<Character>> player2Board, String[] gameType,
                      ArrayList<BattleShip> battleShipsPlayer1, ArrayList<BattleShip> battleShipsPlayer2, int[] mineAmount) {
         this.pathFile = pathFile;
         this.gameType = gameType;
         this.battleShipsPlayer1 = battleShipsPlayer1;
         this.battleShipsPlayer2 = battleShipsPlayer2;
         this.player1Board = player1Board;
         this.player2Board = player2Board;
         this.mineAmount = mineAmount;
     }
 */
    @Override
    protected Void call() throws Exception {
        loadXML(this.pathFile, this.player1Board, this.player2Board, this.gameType, this.battleShipsPlayer1, this.battleShipsPlayer2, this.mineAmount);
        return null;
    }


    public void loadXML(String pathFile, ArrayList<ArrayList<Character>> player1Board,
                        ArrayList<ArrayList<Character>> player2Board, String[] gameType,
                        ArrayList<BattleShip> battleShipsPlayer1, ArrayList<BattleShip> battleShipsPlayer2, int[] mineAmount) throws InterruptedException {
        InputStream inputStream;
        updateMessage("Fetching file");
        updateProgress(0, 7);
        Thread.sleep(SLEEP_TIME);
        updateMessage("Checking if file exist");
        updateProgress(1, 7);
        try {
            inputStream = new FileInputStream(pathFile);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("File does not exist.");
        }
        Thread.sleep(SLEEP_TIME);
        updateMessage("Checking file extension");
        updateProgress(2, 7);
        String extensionOfFile = getFileExtension(pathFile);
        if (!extensionOfFile.equals("xml")) {
            throw new IllegalArgumentException("The extension of file must be .xml");
        }
        try {
            Thread.sleep(SLEEP_TIME);
            updateMessage("Checking board size");
            updateProgress(3, 7);
            BattleShipGame battleShipGame = deserializeFrom(inputStream);
            int boardSize = battleShipGame.getBoardSize();
            if (boardSize < MINBOARDSIZE || boardSize > MAXBOARDSIZE) {
                throw new XMLFileParsingException("board size must be between 5 to 20.");
            }
            Thread.sleep(SLEEP_TIME);
            updateMessage("Checking game type");
            updateProgress(4, 7);
            gameType[0] = battleShipGame.getGameType();
            gameType[0] = gameType[0].toUpperCase();
            if (!gameType[0].equals("BASIC") && !gameType[0].equals("ADVANCE")) {
                throw new XMLFileParsingException("game type must be BASIC or ADVANCE.");
            }

            List<BattleShipGame.ShipTypes.ShipType> shipTypes = battleShipGame.getShipTypes().getShipType();
            Thread.sleep(SLEEP_TIME);
            updateMessage("Checking ship types");
            updateProgress(4, 7);
            checkShipTypes(shipTypes, gameType[0], boardSize);

            List<BattleShipGame.Boards.Board> boards = battleShipGame.getBoards().getBoard();
            checkBoards(boards, shipTypes, boardSize, gameType[0], player1Board, player2Board, battleShipsPlayer1, battleShipsPlayer2);
            if (battleShipGame.getMine() != null) {
                mineAmount[0] = battleShipGame.getMine().getAmount();
            } else {
                mineAmount[0] = 0;
            }
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }


    public void loadXML(InputStream inputStream, ArrayList<ArrayList<Character>> player1Board,
                        ArrayList<ArrayList<Character>> player2Board, String[] gameType,
                        ArrayList<BattleShip> battleShipsPlayer1, ArrayList<BattleShip> battleShipsPlayer2, int[] mineAmount) throws Exception {

        if (inputStream.available() == 0) {
            throw new XMLFileParsingException("file is empty");
        }

        try {
            //  updateMessage("Checking board size");
            //  updateProgress(3, 7);
            BattleShipGame battleShipGame = deserializeFrom(inputStream);
            int boardSize = battleShipGame.getBoardSize();
            if (boardSize < MINBOARDSIZE || boardSize > MAXBOARDSIZE) {
                throw new XMLFileParsingException("board size must be between 5 to 20.");
            }
            //  updateMessage("Checking game type");
            //  updateProgress(4, 7);
            gameType[0] = battleShipGame.getGameType();
            gameType[0] = gameType[0].toUpperCase();
            if (!gameType[0].equals("BASIC") && !gameType[0].equals("ADVANCE")) {
                throw new XMLFileParsingException("game type must be BASIC or ADVANCE.");
            }

            List<BattleShipGame.ShipTypes.ShipType> shipTypes = battleShipGame.getShipTypes().getShipType();
            // updateMessage("Checking ship types");
            //  updateProgress(4, 7);
            checkShipTypes(shipTypes, gameType[0], boardSize);

            List<BattleShipGame.Boards.Board> boards = battleShipGame.getBoards().getBoard();
            checkBoards(boards, shipTypes, boardSize, gameType[0], player1Board, player2Board, battleShipsPlayer1, battleShipsPlayer2);
            if (battleShipGame.getMine() != null) {
                mineAmount[0] = battleShipGame.getMine().getAmount();
            } else {
                mineAmount[0] = 0;
            }
        } catch (JAXBException e) {
            throw new XMLFileParsingException("Bad XML File");
        }
    }

    private void checkBoards(List<BattleShipGame.Boards.Board> boards, List<BattleShipGame.ShipTypes.ShipType> ships, int boadrdsize, String gameType,
                             ArrayList<ArrayList<Character>> player1Board, ArrayList<ArrayList<Character>> player2Board,
                             ArrayList<BattleShip> battleShipsPlayer1, ArrayList<BattleShip> battleShipsPlayer2) throws InterruptedException {
        int curShipAmount;
        List<BattleShipGame.Boards.Board.Ship> boardships;
        if (boards.size() != PLAYERSNUMBER) {
            throw new XMLFileParsingException("need to be exactly tow game boards.");
        }

        // check board ships for the correct amount ships.
        for (BattleShipGame.ShipTypes.ShipType ship : ships) {
            for (BattleShipGame.Boards.Board board : boards) {
                boardships = board.getShip();
                curShipAmount = 0;
                for (BattleShipGame.Boards.Board.Ship boardShip : boardships) {
                    if (boardShip.getShipTypeId().equals(ship.getId())) {
                        curShipAmount++;
                    }
                }
                if (curShipAmount != ship.getAmount()) {
                    throw new XMLFileParsingException(
                            String.format("there is %d amount of %s in shipsTypes and in the board ships there is %d",
                                    ship.getAmount(), ship.getId(), curShipAmount));
                }
            }
        }

        //  Thread.sleep(SLEEP_TIME);
        // updateMessage("Checking player1 board");
        //  updateProgress(5, 7);
        checkShipsPositions(boards.get(0).getShip(), boadrdsize, gameType, 1, player1Board, battleShipsPlayer1, ships);

        //  Thread.sleep(SLEEP_TIME);
        //  updateMessage("Checking player2 board");
        //   updateProgress(6, 7);
        checkShipsPositions(boards.get(1).getShip(), boadrdsize, gameType, 2, player2Board, battleShipsPlayer2, ships);
        //    Thread.sleep(SLEEP_TIME);
        //    updateMessage("XML load successfully");
        //  updateProgress(7, 7);
    }

    private void checkShipsPositions(List<BattleShipGame.Boards.Board.Ship> shipsBoard, int boardSize, String gameType, int boardIndex,
                                     ArrayList<ArrayList<Character>> playerBoard, ArrayList<BattleShip> battleShips,
                                     List<BattleShipGame.ShipTypes.ShipType> ships) {
        // need to check for the board ships.
        // if the this is Basic => direction = ROW , COLUMN.
        // if ADVANCE => direction = RIGHT_DOWN, DOWN_RIGHT, UP_RIGHT,RIGHT_UP, ROW , COLUMN
        int shipIndex = 1;
        for (BattleShipGame.Boards.Board.Ship ship : shipsBoard) {
            if (gameType.equals("BASIC")) {
                try {
                    BasicDiretionsType.valueOf(ship.getDirection());
                } catch (IllegalArgumentException e) {
                    throw new XMLFileParsingException(String.format
                            ("the direction of the ship number %d on board %d is unsupported for this BASIC game type", shipIndex, boardIndex));
                }
            }
            if (gameType.equals("ADVANCE")) {
                try {
                    AdvanceDirectionType.valueOf(ship.getDirection());
                } catch (IllegalArgumentException e) {
                    throw new XMLFileParsingException(String.format
                            ("the direction of the ship number %d on board %d is unsupported for this ADVANCE game type", shipIndex, boardIndex));
                }
            }
            shipIndex++;
        }

        // create empty board.
        char[][] board = new char[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                board[i][j] = EMPTY;
            }
        }

        BattleShipGame.ShipTypes.ShipType shipType;
        shipIndex = 1;
        String dir;
        AdvanceDirectionType type;
        BattleShipGame.Boards.Board.Ship.Position resPos = new BattleShipGame.Boards.Board.Ship.Position();
        ArrayList<BattleShipGame.Boards.Board.Ship.Position> positions;

        // boolean valid = true;
        for (BattleShipGame.Boards.Board.Ship ship : shipsBoard) {
            dir = ship.getDirection().toUpperCase();
            type = AdvanceDirectionType.valueOf(dir);
            shipType = getShipTypeById(ship.getShipTypeId(), ships);
            positions = getShipPossitionArr(type, ship.getPosition(), shipType.getLength());
            switch (type) {
                case ROW:
                    checkValidROWPosition(board, boardSize, ship.getPosition(), boardIndex, shipIndex, shipType.getLength());
                    break;
                case COLUMN:
                    checkValidCOLUMNPosition(board, boardSize, ship.getPosition(), boardIndex, shipIndex, shipType.getLength());
                    break;
                case RIGHT_UP:
                    checkValidRIGHT_UPPosition(board, boardSize, ship.getPosition(), boardIndex, shipIndex, shipType.getLength());
                    break;
                case UP_RIGHT:
                    checkValidUP_RIGHTPosition(board, boardSize, ship.getPosition(), boardIndex, shipIndex, shipType.getLength());
                    break;
                case DOWN_RIGHT:
                    checkValidDOWN_RIGHTTPosition(board, boardSize, ship.getPosition(), boardIndex, shipIndex, shipType.getLength());
                    break;
                case RIGHT_DOWN:
                    checkValidRIGHT_DOWNTPosition(board, boardSize, ship.getPosition(), boardIndex, shipIndex, shipType.getLength());
                    break;
            }
            battleShips.add(new BattleShip(shipIndex, ship.getDirection(), shipType.getLength(), shipType.getScore(), positions));
            shipIndex++;
        }

        copyToPlayerBoard(board, boardSize, playerBoard);
    }

    private ArrayList<BattleShipGame.Boards.Board.Ship.Position> getShipPossitionArr(AdvanceDirectionType type, BattleShipGame.Boards.Board.Ship.Position pos, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        ArrayList<BattleShipGame.Boards.Board.Ship.Position> positions = new ArrayList<>();
        BattleShipGame.Boards.Board.Ship.Position mypos = new BattleShipGame.Boards.Board.Ship.Position();
        mypos.setY(pos.getY());
        mypos.setX(pos.getX());

        switch (type) {
            case ROW:
                getPositionsOneSquence(positions, DirectionsPositions.LEFTROW, mypos, length);
                break;
            case COLUMN:
                getPositionsOneSquence(positions, DirectionsPositions.DOWNCOLUMN, mypos, length);
                break;
            case UP_RIGHT:
                getPositionsOneSquence(positions, DirectionsPositions.LEFTROW, mypos, length);
                mypos.setX(mypos.getX() + 1);
                getPositionsOneSquence(positions, DirectionsPositions.DOWNCOLUMN, mypos, length - 1);
                break;
            case DOWN_RIGHT:
                getPositionsOneSquence(positions, DirectionsPositions.LEFTROW, mypos, length);
                mypos.setX(x);
                getPositionsOneSquence(positions, DirectionsPositions.UPCOLUMN, mypos, length - 1);
                break;
            case RIGHT_DOWN:
                getPositionsOneSquence(positions, DirectionsPositions.DOWNCOLUMN, mypos, length);
                mypos.setY(y);
                getPositionsOneSquence(positions, DirectionsPositions.RIGHTROW, mypos, length - 1);
                break;
            case RIGHT_UP:
                getPositionsOneSquence(positions, DirectionsPositions.UPCOLUMN, mypos, length);
                mypos.setY(y);
                getPositionsOneSquence(positions, DirectionsPositions.RIGHTROW, mypos, length - 1);
                break;
        }
        return positions;
    }

    private void getPositionsOneSquence(ArrayList<BattleShipGame.Boards.Board.Ship.Position> positions
            , DirectionsPositions type, BattleShipGame.Boards.Board.Ship.Position pos, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;

        switch (type) {
            case LEFTROW:
                for (int i = 0; i < length; i++) {
                    BattleShipGame.Boards.Board.Ship.Position p = new BattleShipGame.Boards.Board.Ship.Position();
                    p.setX(x);
                    p.setY(y + i);
                    positions.add(p);
                }
                break;
            case RIGHTROW:
                for (int i = 0; i < length; i++) {
                    BattleShipGame.Boards.Board.Ship.Position p = new BattleShipGame.Boards.Board.Ship.Position();
                    p.setX(x);
                    p.setY(y - i);
                    positions.add(p);
                }
                break;
            case DOWNCOLUMN:
                for (int i = 0; i < length; i++) {
                    BattleShipGame.Boards.Board.Ship.Position p = new BattleShipGame.Boards.Board.Ship.Position();
                    p.setX(x + i);
                    p.setY(y);
                    positions.add(p);
                }
                break;
            case UPCOLUMN:
                for (int i = 0; i < length; i++) {
                    BattleShipGame.Boards.Board.Ship.Position p = new BattleShipGame.Boards.Board.Ship.Position();
                    p.setX(x - i);
                    p.setY(y);
                    positions.add(p);
                }
                break;
        }
    }

    private BattleShipGame.ShipTypes.ShipType getShipTypeById(String shipID, List<BattleShipGame.ShipTypes.ShipType> ships) {
        for (BattleShipGame.ShipTypes.ShipType ship : ships) {
            if (ship.getId().equals(shipID)) {
                return ship;
            }
        }

        return null;
    }

    private void copyToPlayerBoard(char[][] board, int boardSize, ArrayList<ArrayList<Character>> playerBoard) {
        for (int i = 0; i < boardSize; i++) {
            playerBoard.add(new ArrayList<>(boardSize));
            for (int j = 0; j < boardSize; j++) {
                playerBoard.get(i).add(j, board[i][j]);
            }
        }
    }

    private void checkValidRIGHT_UPPosition(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        BattleShipGame.Boards.Board.Ship.Position mypos = new BattleShipGame.Boards.Board.Ship.Position();
        mypos.setY(pos.getY());
        mypos.setX(pos.getX());

        chechCOLUMNFROMDOWN(board, boardSize, mypos, boardIndex, shipIndex, length);
        mypos.setY(y);
        checkROWFROMRIGHT(board, boardSize, mypos, boardIndex, shipIndex, length - 1);

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x - i][y] = SHIPSIGN;
        }

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x][y - i] = SHIPSIGN;
        }
    }

    private void checkValidROWPosition(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        //check ship one each other.
        // check ship for out of bound.
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        checkROW(board, boardSize, pos, boardIndex, shipIndex, length);

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x][y + i] = SHIPSIGN;
        }

    }

    private boolean checkPositionSurrounding(char[][] board, int boardSize, int x, int y, BattleShipGame.Boards.Board.Ship.Position resPos) {

        // check for up
        if (x - 1 > -1 && board[x - 1][y] == SHIPSIGN) {
            resPos.setY(y);
            resPos.setX(x - 1);
            return false;
        }

        //down
        if (x + 1 < boardSize && board[x + 1][y] == SHIPSIGN) {
            resPos.setY(y);
            resPos.setX(x + 1);
            return false;
        }

        //left
        if (y - 1 > -1 && board[x][y - 1] == SHIPSIGN) {
            resPos.setY(y - 1);
            resPos.setX(x);
            return false;
        }

        // right
        if (y + 1 < boardSize && board[x][y + 1] == SHIPSIGN) {
            resPos.setY(y + 1);
            resPos.setX(x);
            return false;
        }

        // up left
        if (x - 1 > -1 && y - 1 > -1 && board[x - 1][y - 1] == SHIPSIGN) {
            resPos.setY(y - 1);
            resPos.setX(x - 1);
            return false;
        }
        // down left
        if (x + 1 < boardSize && y - 1 > -1 && board[x + 1][y - 1] == SHIPSIGN) {
            resPos.setY(y - 1);
            resPos.setX(x + 1);
            return false;
        }

        // up right
        if (x - 1 > -1 && y + 1 < boardSize && board[x - 1][y + 1] == SHIPSIGN) {
            resPos.setY(y + 1);
            resPos.setX(x - 1);
            return false;
        }

        // down right
        if (x + 1 < boardSize && y + 1 < boardSize && board[x + 1][y + 1] == SHIPSIGN) {
            resPos.setY(y + 1);
            resPos.setX(x + 1);
            return false;
        }

        return true;
    }

    private void checkROWFROMRIGHT(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;

        //check th Row part of the ship
        for (int i = 0; i < length; i++) {
            if (x < 0 || x >= boardSize || y - i < 0 || y - i >= boardSize) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d beyond the scope of the board the first problematic position in row = %d col = %d",
                                boardIndex, shipIndex, pos.getX(), pos.getY() - i));
            }
            if (board[x][y - i] != EMPTY) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d has conflict position in row = %d col = %d with another ship",
                                boardIndex, shipIndex, pos.getX(), pos.getY() - i));
            } else {
                board[x][y - i] = TESTEDSHIPSIGN;
            }
        }

        boolean valid;
        BattleShipGame.Boards.Board.Ship.Position resPos = new BattleShipGame.Boards.Board.Ship.Position();
        // check of none one space between ships.
        for (int i = 0; i < length; i++) {
            valid = checkPositionSurrounding(board, boardSize, x, y - i, resPos);
            if (!valid) {
                throw new XMLFileParsingException(String.format(
                        "on board %d the ship number %d has linking with other ship on position row = %d col = %d",
                        boardIndex, shipIndex, resPos.getX() + 1, resPos.getY() + 1));
            }
        }
    }

    private void chechCOLUMNFROMDOWN(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        for (int i = 0; i < length; i++) {
            if (x - i < 0 || x - i > boardSize || y < 0 || y > boardSize) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d beyond the scope of the board the first problematic position in row = %d col = %d",
                                boardIndex, shipIndex, pos.getX() - i, pos.getY()));
            }
            if (board[x - i][y] != EMPTY) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d has conflict position in row = %d col = %d with another ship",
                                boardIndex, shipIndex, pos.getX() - i, pos.getY()));
            } else {
                board[x - i][y] = TESTEDSHIPSIGN;
            }
        }

        boolean valid;
        BattleShipGame.Boards.Board.Ship.Position resPos = new BattleShipGame.Boards.Board.Ship.Position();
        // check of none one space between ships.
        for (int i = 0; i < length; i++) {
            valid = checkPositionSurrounding(board, boardSize, x - i, y, resPos);
            if (!valid) {
                throw new XMLFileParsingException(String.format(
                        "on board %d the ship number %d has linking with other ship on position row = %d col = %d",
                        boardIndex, shipIndex, resPos.getX() + 1, resPos.getY() + 1));
            }
        }
    }

    private void checkValidDOWN_RIGHTTPosition(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        BattleShipGame.Boards.Board.Ship.Position mypos = new BattleShipGame.Boards.Board.Ship.Position();
        mypos.setY(pos.getY());
        mypos.setX(pos.getX());

        checkROW(board, boardSize, mypos, boardIndex, shipIndex, length);
        mypos.setX(x);
        chechCOLUMNFROMDOWN(board, boardSize, mypos, boardIndex, shipIndex, length - 1);

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x - i][y] = SHIPSIGN;
        }

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x][y + i] = SHIPSIGN;
        }
    }

    private void checkValidRIGHT_DOWNTPosition(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        BattleShipGame.Boards.Board.Ship.Position mypos = new BattleShipGame.Boards.Board.Ship.Position();
        mypos.setY(pos.getY());
        mypos.setX(pos.getX());

        //check th Column part of the ship
        checkCOLUMN(board, boardSize, mypos, boardIndex, shipIndex, length);
        mypos.setY(y);
        checkROWFROMRIGHT(board, boardSize, mypos, boardIndex, shipIndex, length - 1);

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x + i][y] = SHIPSIGN;
        }

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x][y - i] = SHIPSIGN;
        }
    }

    private void checkValidUP_RIGHTPosition(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        BattleShipGame.Boards.Board.Ship.Position mypos = new BattleShipGame.Boards.Board.Ship.Position();
        mypos.setY(pos.getY());
        mypos.setX(pos.getX());

        //check th Column part of the ship
        checkCOLUMN(board, boardSize, mypos, boardIndex, shipIndex, length);
        mypos.setY(y + 2);
        //check th Row part of the ship
        checkROW(board, boardSize, mypos, boardIndex, shipIndex, length - 1);

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x + i][y] = SHIPSIGN;
        }

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x][y + i] = SHIPSIGN;
        }
    }

    private void checkROW(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;

        for (int i = 0; i < length; i++) {
            if (x < 0 || x >= boardSize || y + i < 0 || y + i >= boardSize) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d beyond the scope of the board the first problematic position in row = %d col = %d",
                                boardIndex, shipIndex, pos.getX(), pos.getY() + i));
            }
            if (board[x][y + i] != EMPTY) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d has conflict position in row = %d col = %d with another ship",
                                boardIndex, shipIndex, pos.getX(), pos.getY() + i));
            } else {
                board[x][y + i] = TESTEDSHIPSIGN;
            }
        }

        boolean valid;
        BattleShipGame.Boards.Board.Ship.Position resPos = new BattleShipGame.Boards.Board.Ship.Position();
        // check of none one space between ships.
        for (int i = 0; i < length; i++) {
            valid = checkPositionSurrounding(board, boardSize, x, y + i, resPos);
            if (!valid) {
                throw new XMLFileParsingException(String.format(
                        "on board %d the ship number %d has linking with other ship on position row = %d col = %d",
                        boardIndex, shipIndex, resPos.getX() + 1, resPos.getY() + 1));
            }
        }

    }

    private void checkCOLUMN(char[][] board, int boardSize, BattleShipGame.Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;

        // check first column part of the ship
        for (int i = 0; i < length; i++) {
            if (x + i < 0 || x + i > boardSize || y < 0 || y > boardSize) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d beyond the scope of the board the first problematic position in row = %d col = %d",
                                boardIndex, shipIndex, pos.getX() + i, pos.getY()));
            }
            if (board[x + i][y] != EMPTY) {
                throw new XMLFileParsingException(String.format
                        ("on board %d the ship number %d has conflict position in row = %d col = %d with another ship",
                                boardIndex, shipIndex, pos.getX() + i, pos.getY()));
            } else {
                board[x + i][y] = TESTEDSHIPSIGN;
            }
        }

        boolean valid;
        BattleShipGame.Boards.Board.Ship.Position resPos = new BattleShipGame.Boards.Board.Ship.Position();
        // check of none one space between ships.
        for (int i = 0; i < length; i++) {
            valid = checkPositionSurrounding(board, boardSize, x + i, y, resPos);
            if (!valid) {
                throw new XMLFileParsingException(String.format(
                        "on board %d the ship number %d has linking with other ship on position row = %d col = %d",
                        boardIndex, shipIndex, resPos.getX() + 1, resPos.getY() + 1));
            }
        }
    }

    private void checkValidCOLUMNPosition(char[][] board, int boardSize, BattleShipGame.
            Boards.Board.Ship.Position pos, int boardIndex, int shipIndex, int length) {
        //check ship one each other.
        int x = pos.getX() - 1;
        int y = pos.getY() - 1;
        checkCOLUMN(board, boardSize, pos, boardIndex, shipIndex, length);

        // all good so change the TESTEDSIGN to SHIPSIGN.
        for (int i = 0; i < length; i++) {
            board[x + i][y] = SHIPSIGN;
        }
    }

    private void checkShipTypes(List<BattleShipGame.ShipTypes.ShipType> ships, String gameType, int boardSize) {
        if (ships.isEmpty()) {
            throw new XMLFileParsingException("there is no ships, game must contains at least one ship.");
        }

        for (BattleShipGame.ShipTypes.ShipType ship : ships) {
            if (ship.getAmount() == 0) {
                throw new XMLFileParsingException("there is ship with Amount = 0");
            }
            if (gameType.equals("BASIC") && !ship.getCategory().toUpperCase().equals("REGULAR")) {
                throw new XMLFileParsingException("there is unmatched ship category to the game type, category should be REGULAR");
            }

            if (gameType.equals("ADVANCE") &&
                    !ship.getCategory().toUpperCase().equals("REGULAR") &&
                    !ship.getCategory().toUpperCase().equals("L_SHAPE")) {
                throw new XMLFileParsingException("there is unmatched ship category to the game type, category should be REGULAR or L_SHAPE");
            }
        }

        // check if there is just one of ShipId each ship in shipsType.
        String shipId;
        int amoutOfShipId;
        for (BattleShipGame.ShipTypes.ShipType checkedShip : ships) {
            shipId = checkedShip.getId();
            amoutOfShipId = 0;
            for (BattleShipGame.ShipTypes.ShipType ship : ships) {
                if (shipId.equals(ship.getId())) {
                    amoutOfShipId++;
                }
            }

            if (amoutOfShipId != 1) {
                throw new XMLFileParsingException(String.format("there is duplicates ships type id on the type id: %s", shipId));
            }
        }

        // check if the all the length is bigger then 0 and less then BoardSize.
        for (BattleShipGame.ShipTypes.ShipType ship : ships) {
            if (ship.getLength() < 1 || ship.getLength() > boardSize) {
                throw new XMLFileParsingException(String.format
                        ("the ship id %s has wrong length, the length need to be 1 up to %d, and the ship length is: %d", ship.getId(), boardSize));
            }
        }
    }

    public String getFileExtension(String pathFile) {
        if (pathFile.lastIndexOf(".") != -1 && pathFile.lastIndexOf(".") != 0)
            return pathFile.substring(pathFile.lastIndexOf(".") + 1);
        else return "";
    }

    private BattleShipGame deserializeFrom(InputStream in) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(JAXB_XML_GAME_PACKAGE_NAME);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (BattleShipGame) unmarshaller.unmarshal(in);
    }

}

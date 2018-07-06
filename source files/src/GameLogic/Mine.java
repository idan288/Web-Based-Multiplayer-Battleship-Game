package GameLogic;

import jaxb.schema.generated.BattleShipGame;

public class Mine {
    private BattleShipGame.Boards.Board.Ship.Position minePosition;

    public Mine(BattleShipGame.Boards.Board.Ship.Position minPos) {
        minePosition = minPos;
    }

    public BattleShipGame.Boards.Board.Ship.Position getMinePosition() {
        return minePosition;
    }
    
}


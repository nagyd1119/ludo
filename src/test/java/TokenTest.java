import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import hu.unideb.table.model.*;

import java.util.List;
import java.awt.Point;
import java.util.List;

class TokenTest {

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model(2,List.of("p1","p2"));
        model.currentPlayer = Player.PLAYER_1;
    }

    @RepeatedTest(100)
    void tokenMovesAccordingToRoll(){
        var path = model.playerPaths.get(model.currentPlayer);
        Token token = model.playerTokens.get(model.currentPlayer)[0];
        Point start = path.get(0);
        token.current = new Point(start);
        int roll = (int) (Math.random()*6+1);
        Point potential = path.get(roll);
        token.potential = new Point(potential);

        model.move(token.current.x, token.current.y);
        assertEquals(potential, token.current);
    }

    @Test
    void invalidMoveRejected(){
        var path = model.playerPaths.get(model.currentPlayer);
        Token token = model.playerTokens.get(model.currentPlayer)[0];
        Point start = path.get(0);
        token.current = new Point(start);
        token.potential = null;

        assertFalse(model.isAllowedMove(start.x,start.y));

        model.move(start.x, start.y);
        assertEquals(start, token.current);
    }


    @Test
    void collisionSendsEnemyToOrigin() {
        model.currentPlayer = Player.PLAYER_1;
        var P1_path = model.playerPaths.get(Player.PLAYER_1);
        Token P1 = model.playerTokens.get(Player.PLAYER_1)[0];
        Token P2 = model.playerTokens.get(Player.PLAYER_2)[0];
        Point oldPos = P1_path.get(3);
        P1.current = new Point(oldPos);
        Point newPos = P1_path.get(5);
        assertFalse(model.safeZones.contains(newPos));

        P1.potential = new Point(newPos);
        Point P2_Origin = new Point(P2.origin);
        P1.current = new Point(newPos);
        model.move(P1.current.x,P1.current.y);

        assertEquals(P2_Origin, P2.current);
        assertEquals(newPos, P1.current);
        assertEquals(GameStatus.IN_PROGRESS, model.getGameStatus());
    }


}

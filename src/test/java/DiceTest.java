import hu.unideb.table.model.Model;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.awt.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiceTest{

    private Model model;

    @BeforeEach
    void setUp() {
        model = new Model(4, List.of("p1","p2","p3","p4"));
    }

    @Test
    void firstRollIsSix_whenNoTokenOnBoard(){
        Model freshModel = new Model(4, List.of("p1","p2","p3","p4"));
        int die = freshModel.dieRoll();
        assertEquals(6, die);
    }

    @RepeatedTest(5000)
    void dieBetweenOneandSix_whenTokenOnBoard(){
        putOneTokenOnBoardForCurrentPlayer(model);

        int die = model.dieRoll();
        assertTrue(die <= 6 && die >= 1);
    }

    private static void putOneTokenOnBoardForCurrentPlayer(Model model){
        var path = model.playerPaths.get(model.currentPlayer);
        Point entry = path.get(0);
        model.playerTokens.get(model.currentPlayer)[0].current = new Point(entry);
        model.playerTokens.get(model.currentPlayer)[0].potential = null;
    }

    @Test
    void stateStaysValid() {
        putOneTokenOnBoardForCurrentPlayer(model);

        for (int i = 0; i < 20000; i++){
            int die = model.dieRoll();
            assertTrue(die <= 6 && die >= 1);
            assertTrue(model.numberOfConsecutiveSixes >= 0 && model.numberOfConsecutiveSixes <= 2);
            assertNotNull(model.getGameStatus());
        }
    }

}

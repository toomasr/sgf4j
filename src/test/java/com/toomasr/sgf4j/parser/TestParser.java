package com.toomasr.sgf4j.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;

import org.junit.Test;

import junit.framework.TestCase;

public class TestParser extends TestCase {

  @Test
  public void testSimpleMainLineParsing() throws Exception {
    Path path = Paths.get("./src/main/resources/simple-12-move-game.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    assertEquals(12, game.getNoMoves());

    GameNode node = game.getFirstMove();

    for (int i = 1; i < 13; i++) {
      assertEquals(i, node.getMoveNo());
      node = node.getNextNode();
    }
    assertEquals(null, node);
  }

  @Test
  public void testMoveNumbers() throws Exception {
    Path path = Paths.get("./src/main/resources/game-branching-complex.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    assertEquals(6, game.getNoMoves());

    GameNode node = game.getFirstMove();
    assertEquals(1, node.getMoveNo());

    Iterator<GameNode> ite = node.getChildren().iterator();
    GameNode firstChild = ite.next();
    assertEquals(2, firstChild.getMoveNo());
    assertEquals(3, firstChild.getNextNode().getMoveNo());
    assertEquals(4, firstChild.getNextNode().getNextNode().getMoveNo());

    assertEquals(3, firstChild.getChildren().iterator().next().getMoveNo());
    assertEquals(4, firstChild.getChildren().iterator().next().getNextNode().getMoveNo());

    GameNode secondChild = ite.next();
    assertEquals(2, secondChild.getMoveNo());
    assertEquals(3, secondChild.getNextNode().getMoveNo());
    assertEquals(4, secondChild.getNextNode().getNextNode().getMoveNo());

    assertEquals(3, secondChild.getChildren().iterator().next().getMoveNo());
    assertEquals(4, secondChild.getChildren().iterator().next().getNextNode().getMoveNo());

  }

  @Test
  public void testSimpleParsing() throws Exception {
    Path path = Paths.get("./src/main/resources/game-branching-simple.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    assertEquals(4, game.getNoMoves());

    GameNode node = game.getFirstMove();
    assertEquals(2, node.getChildren().size());
  }

  @Test
  public void testParsingWithTime() throws Exception {
    Game game = Sgf.createFromPath(Paths.get("./src/test/resources/game-with-times.sgf"));
    GameNode node = game.getRootNode();
    do {
      if (node.isMove() && node.getProperty("TimeSpentOnMove") == null) {
        // if we find a move and it does not have a time associated we fail the test
        assertEquals(true, false);
      }
    } while ((node = node.getNextNode()) != null);

  }
}

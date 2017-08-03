package com.toomasr.sgf4j.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.toomasr.sgf4j.Parser;

import junit.framework.TestCase;

public class TestProblematicShinkaya extends TestCase {
  @Test
  public void testParseProblematic7() throws Exception {
    Path path = Paths.get("./src/test/resources/problematic-007.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    assertTrue(game.getProperty("AW").length() > 0);
  }

  @Test
  public void testParseProblematic8() throws Exception {
    Path path = Paths.get("./src/test/resources/problematic-008.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    String[] moves = game.getProperty("AB").split(",");
    int count = 0;
    for (int i = 0; i < moves.length; i++) {
      if (moves[i].contains(":")) {
        count = count + Util.coordSequencesToSingle(moves[i]).length;
      }
      else {
        count++;
      }
    }
    assertEquals(7, count);
  }
}

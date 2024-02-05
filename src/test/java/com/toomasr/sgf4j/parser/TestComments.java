package com.toomasr.sgf4j.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import junit.framework.TestCase;

public class TestComments extends TestCase {

  @Test
  public void testSimpleMainLineParsing() throws Exception {
//    Path path = Paths.get("./src/test/resources/one-move.sgf");
//    String gameAsString = new String(Files.readAllBytes(path));
//    Parser parser = new Parser(gameAsString);
//    Game game = parser.parse();
//    game.postProcess();
//
//    assertEquals("Game Comment", game.getProperty("C"));
//    assertEquals("First move comment", game.getRootNode().getNextNode().getProperty("C"));
//    
//    System.out.println(game.getOriginalSgf());
//    System.out.println("XXX");
//    System.out.println(game.getGeneratedSgf());
//    
//    parser = new Parser(game.getGeneratedSgf());
//    Game newGame = parser.parse();
//    newGame.postProcess();
//    
//    assertEquals(game.getNoNodes(), newGame.getNoNodes());
  }
}

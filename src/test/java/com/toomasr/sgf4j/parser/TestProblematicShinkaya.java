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
}

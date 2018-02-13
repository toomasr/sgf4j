package com.toomasr.sgf4j.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.toomasr.sgf4j.Parser;

import junit.framework.TestCase;

public class TestProblematicRoadToDan extends TestCase {
  @Test
  public void testParseProblematic010() throws Exception {
    Path path = Paths.get("./src/test/resources/problematic-010.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    // once the AW props are concatenated it becomes longer
    assertEquals(23, game.getProperty("AW").length());
  }
}

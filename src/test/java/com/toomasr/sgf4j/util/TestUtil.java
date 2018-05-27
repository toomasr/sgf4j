package com.toomasr.sgf4j.util;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;

import junit.framework.TestCase;

public class TestUtil extends TestCase {
  public void setUp() {

  }

  @Test
  public void testGamesEqual() {
    Path gameSgf1 = Paths.get("src/test/resources/util-compare-game1a.sgf");
    Path gameSgf2 = Paths.get("src/test/resources/util-compare-game1b.sgf");

    Game game1 = Sgf.createFromPath(gameSgf1);
    Game game2 = Sgf.createFromPath(gameSgf2);
    assertFalse(game1.isSameGame(game2));
  }

}

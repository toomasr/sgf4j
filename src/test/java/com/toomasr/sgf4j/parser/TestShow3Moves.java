package com.toomasr.sgf4j.parser;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.toomasr.sgf4j.Sgf;


public class TestShow3Moves {
  @Test
  public void testNumberOfMoves() {
    Game game = Sgf.createFromPath(Paths.get("src/test/resources/show-3-moves.sgf"));
    
    Assert.assertEquals(3, game.getNoMoves());
    Assert.assertEquals(4, game.getNoNodes());
  }
}

package com.toomasr.sgf4j.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

public class TestSaveSgf {
  @Test
  public void testSimple() throws Exception {
    Path sgfPath = Paths.get("./src/main/resources/simple-12-move-game.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic001() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-001.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic002() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-002.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic003() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-003.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic004() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-004.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic011() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-011.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic012() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-012.sgf");
    verifyGame(sgfPath, false);
  }

  private void verifyGame(Path sgfPath) throws Exception {
    Game game = Sgf.createFromPath(sgfPath);
    TestSaveSgf.verifyGame(game, false);
  }

  private void verifyGame(Path sgfPath, boolean verbose) throws Exception {
    Game game = Sgf.createFromPath(sgfPath);
    TestSaveSgf.verifyGame(game, verbose);
  }

  public static void verifyGame(Game game) {
    verifyGame(game, false);
  }

  public static void verifyGame(Game game, boolean verbose) {
    File file = null;
    try {
      file = File.createTempFile("sgf4j-test-", ".sgf");
      file.deleteOnExit();
      game.saveToFile(file.toPath());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    Game reReadGame = Sgf.createFromPath(file.toPath());
    boolean result = game.isSameGame(reReadGame);
    if (!result || verbose) {
      File tmpFile = Sgf.writeToFile(game.getOriginalSgf());
      System.out.println("Parsing information:");
      game.isSameGame(reReadGame, true);
      System.out.println();
      System.out.println("ORIGINAL");
      System.out.println(game.getOriginalSgf());
      System.out.println("/ORIGINAL");
      System.out.println();
      System.out.println("NEW");
      System.out.println(reReadGame.getOriginalSgf());
      System.out.println("/NEW");
      if (!result)
        Assert.assertTrue("Problem with game. SGF written to " + tmpFile.getAbsolutePath(), result);
    }
  }
}

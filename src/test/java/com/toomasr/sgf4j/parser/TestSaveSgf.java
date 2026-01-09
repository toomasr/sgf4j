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
    verifyGame(sgfPath, false);
  }

  @Test
  public void testProblematic012() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-012.sgf");
    verifyGame(sgfPath, false);
  }

  @Test
  public void testSaveProblematic001WithVariations() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/save-problematic-001.sgf");
    Game game = Sgf.createFromPath(sgfPath);

    // Find a node to add variations to (move 10)
    GameNode node = game.getRootNode();
    for (int i = 0; i < 10 && node != null; i++) {
      node = node.getNextNode();
    }

    // Add a few variation branches
    GameNode variation1 = new GameNode(node);
    variation1.addProperty("B", "aa");
    node.addChild(variation1);

    GameNode variation1Child = new GameNode(variation1);
    variation1Child.addProperty("W", "bb");
    variation1.addChild(variation1Child);

    GameNode variation2 = new GameNode(node);
    variation2.addProperty("B", "cc");
    node.addChild(variation2);

    // Add another variation deeper in the game (move 20)
    GameNode node2 = game.getRootNode();
    for (int i = 0; i < 20 && node2 != null; i++) {
      node2 = node2.getNextNode();
    }

    GameNode variation3 = new GameNode(node2);
    variation3.addProperty("W", "dd");
    node2.addChild(variation3);

    // Save to temp file
    File tempFile = File.createTempFile("sgf4j-variation-test-", ".sgf");
    tempFile.deleteOnExit();
    game.saveToFile(tempFile.toPath());

    // Reload the saved game
    Game reloadedGame = Sgf.createFromPath(tempFile.toPath());

    // Re-parse the original SGF with variations to get proper node numbering
    // (manually added nodes don't have moveNo/visualDepth set)
    Game originalReparsed = Sgf.createFromString(game.getGeneratedSgf());

    // Print debug info if there's a mismatch
    boolean isSame = originalReparsed.isSameGame(reloadedGame, false);
    if (!isSame) {
      System.out.println("=== ORIGINAL GENERATED SGF ===");
      System.out.println(game.getGeneratedSgf());
      System.out.println("=== RELOADED SGF ===");
      System.out.println(reloadedGame.getOriginalSgf());
      System.out.println("=== DETAILED COMPARISON ===");
      originalReparsed.isSameGame(reloadedGame, true);
    }

    // Verify the variations are preserved by checking node counts
    Assert.assertEquals("Node count should match", originalReparsed.getNoNodes(), reloadedGame.getNoNodes());
    Assert.assertEquals("Move count should match", originalReparsed.getNoMoves(), reloadedGame.getNoMoves());

    // Verify the games are the same
    Assert.assertTrue("Games should be identical after save/load", isSame);
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
    boolean result = game.isSameGame(reReadGame, verbose);
    if (!result || verbose) {
      File tmpFile = Sgf.writeToFile(game.getOriginalSgf());
      System.out.println("Parsing information:");
      game.isSameGame(reReadGame, verbose);
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

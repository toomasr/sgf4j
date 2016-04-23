package com.toomasr.sgf4j.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.toomasr.sgf4j.Sgf;

public class TestSaveSgf {
  @Test
  public void testSimple() throws Exception {
    Path sgfPath = Paths.get("./src/main/resources/simple-12-move-game.sgf");
    verifyGame(sgfPath);
  }

  @Test
  public void testProblematic() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/problematic-001.sgf");
    verifyGame(sgfPath);

    sgfPath = Paths.get("./src/test/resources/problematic-002.sgf");
    verifyGame(sgfPath);

    sgfPath = Paths.get("./src/test/resources/problematic-003.sgf");
    verifyGame(sgfPath);

    sgfPath = Paths.get("./src/test/resources/problematic-004.sgf");
    verifyGame(sgfPath);
  }

  /*
   * Create a Game object. Save to file. Create a new game object.
   * Compare the first and last and see if our writing is working.
   */
  private void verifyGame(Path sgfPath) throws Exception {
    Game game = Sgf.createFromPath(sgfPath);
    TestSaveSgf.verifyGame(game);
  }

  public static void verifyGame(Game game) {
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

    Assert.assertTrue("Problem with game " + file.getName(), game.isSameGame(reReadGame));
  }
}

package com.toomasr.sgf4j.parser;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;

import org.junit.Assert;
import org.junit.Test;
import org.zeroturnaround.zip.ZipEntryCallback;
import org.zeroturnaround.zip.ZipUtil;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.SgfParseException;

public class TestSaveSgfSlow {
  @Test
  public void testAllGamesFromBadukMoviesArchive() {
    Path path = Paths.get("src/test/resources/badukmovies-pro-collection.zip");
    testAllGamesInZipArchive(path);
  }

  @Test
  public void testAllGamesFromAebArchive() {
    Path path = Paths.get("src/test/resources/games-aeb-cwi-nl.zip");
    testAllGamesInZipArchive(path);
  }

  private void testAllGamesInZipArchive(Path path) {
    ZipUtil.iterate(path.toFile(), new ZipEntryCallback() {

      @Override
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        if (zipEntry.toString().endsWith("sgf")) {

          try {
            Game game = Sgf.createFromInputStream(in);
            TestSaveSgf.verifyGame(game);
          }
          catch (SgfParseException e) {
            System.out.println("Problem with " + zipEntry.getName());
            e.printStackTrace();
            Assert.fail();
          }
          catch (AssertionError e) {
            System.out.println("Problem with " + zipEntry.getName());
            throw e;
          }
        }
      }
    });
    Assert.assertTrue(true);
  }
}

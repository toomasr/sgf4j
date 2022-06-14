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

public class TestAebSet {
  @Test
  public void testAllGamesFromArchive() {
    Path path = Paths.get("src/test/resources/games-aeb-cwi-nl.zip");
    ZipUtil.iterate(path.toFile(), new ZipEntryCallback() {

      @Override
      public void process(InputStream in, ZipEntry zipEntry) throws IOException {
        if (zipEntry.toString().endsWith("sgf")) {

          try {
            Sgf.createFromInputStream(in);
          }
          catch (SgfParseException e) {
            System.out.println("Problem with " + zipEntry.getName());
            e.printStackTrace();
            Assert.fail();
          }
        }
      }
    });
    Assert.assertTrue(true);
  }
}

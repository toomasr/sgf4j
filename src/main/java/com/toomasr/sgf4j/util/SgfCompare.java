package com.toomasr.sgf4j.util;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import com.toomasr.sgf4j.Sgf;
import com.toomasr.sgf4j.parser.Game;

public class SgfCompare {
  public static void main(String[] args) throws Exception {
    Path file1 = Paths.get("src/test/resources/long-game.sgf");
    Path file2 = Paths.get("src/test/resources/long-game.sgf");

    if (!Files.exists(file1, LinkOption.NOFOLLOW_LINKS)) {
      throw new FileNotFoundException(file1.toString());
    }

    boolean bCompare = binaryCompare(file1, file2);
    System.out.println(bCompare);
    boolean gCompare = compareSgf(file1, file2);
    System.out.println(gCompare);
  }

  public static boolean binaryCompare(Path file1, Path file2) throws Exception {
    try (BufferedInputStream bis1 = new BufferedInputStream(Files.newInputStream(file1));
        BufferedInputStream bis2 = new BufferedInputStream(Files.newInputStream(file2));) {
      byte[] read1 = new byte[8096];
      byte[] read2 = new byte[8096];
      int r1;
      int r2;
      do {
        r1 = bis1.read(read1);
        r2 = bis2.read(read2);
        if (r1 == -1 && r2 == -1) {
          break;
        }
        if (r1 != r2) {
          return false;
        }
        if (!Arrays.equals(read1, read2)) {
          return false;
        }
      }
      while (true);
      return true;
    }
  }

  public static boolean compareSgf(Path file1, Path file2) {
    Game game1 = Sgf.createFromPath(file1);
    Game game2 = Sgf.createFromPath(file2);
    return game1.isSameGame(game2);
  }
}

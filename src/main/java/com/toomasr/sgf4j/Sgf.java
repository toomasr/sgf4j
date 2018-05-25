package com.toomasr.sgf4j;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.toomasr.sgf4j.parser.Game;

public class Sgf {
  private Parser parser;
  private Game game;

  private Sgf(String sgf) {
    parser = new Parser(sgf);
    game = parser.parse();

    game.postProcess();
  }

  public static Game createFromPath(Path path, String charSet) {
    try {
      String gameAsString = new String(Files.readAllBytes(path), charSet);
      return createFromString(gameAsString);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Game createFromPath(Path path) {
    try {
      String gameAsString = new String(Files.readAllBytes(path), "UTF-8");
      return createFromString(gameAsString);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static Game createFromString(String gameAsString) {
    Sgf rtrn = new Sgf(gameAsString);
    return rtrn.getGame();
  }

  public static Game createFromInputStream(InputStream in) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8").newDecoder()))) {
      StringBuilder out = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        out.append(line);
      }
      Sgf rtrn = new Sgf(out.toString());
      return rtrn.getGame();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  public static void writeToFile(Game game, Path destination) {
    writeToFile(game, destination, "UTF-8");
  }

  public static void writeToFile(Game game, Path destination, String encoding, boolean keepOriginal) {
    if (keepOriginal) {
      Path copyOfOriginal = Paths.get(destination.toString() + ".orig." + System.currentTimeMillis());
      try {
        Files.copy(destination, copyOfOriginal);
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    writeToFile(game, destination, encoding);
  }

  public static void writeToFile(Game game, Path destination, String encoding) {
    try (
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destination.toFile()), Charset.forName(encoding).newEncoder())) {
      osw.write(game.getGeneratedSgf());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static File writeToFile(String sgf) {
    BufferedOutputStream bos = null;
    try {
      File tmpFile = File.createTempFile("sgf4j-test-", ".sgf");
      bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
      bos.write(sgf.getBytes());
      return tmpFile;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
    finally {
      if (bos != null) {
        try {
          bos.close();
        }
        catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private Game getGame() {
    return game;
  }
}

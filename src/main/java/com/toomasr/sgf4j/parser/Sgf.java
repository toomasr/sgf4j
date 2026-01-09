package com.toomasr.sgf4j.parser;

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

/**
 * Main entry point for parsing and writing SGF (Smart Game Format) files.
 * Provides static factory methods to create {@link Game} objects from various sources
 * and methods to write games back to SGF format.
 *
 * <p>Example usage:</p>
 * <pre>
 * // Parse from file
 * Game game = Sgf.createFromPath(Paths.get("game.sgf"));
 *
 * // Parse from string
 * Game game = Sgf.createFromString("(;GM[1]FF[4];B[pd];W[dp])");
 *
 * // Write to file
 * Sgf.writeToFile(game, Paths.get("output.sgf"));
 * </pre>
 *
 * @see Game
 * @see GameNode
 */
public class Sgf {
  private Parser parser;
  private Game game;

  private Sgf(String sgf) {
    parser = new Parser(sgf);
    game = parser.parse();

    game.postProcess();
  }

  /**
   * Creates a Game from an SGF file using the specified character encoding.
   *
   * @param path the path to the SGF file
   * @param charSet the character encoding to use (e.g., "UTF-8", "ISO-8859-1")
   * @return a parsed Game object
   * @throws RuntimeException if the file cannot be read
   */
  public static Game createFromPath(Path path, String charSet) {
    try {
      String gameAsString = new String(Files.readAllBytes(path), charSet);
      return createFromString(gameAsString);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a Game from an SGF file using UTF-8 encoding.
   *
   * @param path the path to the SGF file
   * @return a parsed Game object
   * @throws RuntimeException if the file cannot be read
   */
  public static Game createFromPath(Path path) {
    try {
      String gameAsString = new String(Files.readAllBytes(path), "UTF-8");
      return createFromString(gameAsString);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Creates a Game by parsing an SGF string.
   *
   * @param gameAsString the SGF content as a string
   * @return a parsed Game object
   */
  public static Game createFromString(String gameAsString) {
    Sgf rtrn = new Sgf(gameAsString);
    return rtrn.getGame();
  }

  /**
   * Creates a Game by parsing SGF content from an InputStream using UTF-8 encoding.
   *
   * @param in the input stream containing SGF content
   * @return a parsed Game object
   * @throws RuntimeException if the stream cannot be read
   */
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

  /**
   * Writes a Game to an SGF file using UTF-8 encoding.
   *
   * @param game the game to write
   * @param destination the path where the SGF file will be written
   * @throws RuntimeException if the file cannot be written
   */
  public static void writeToFile(Game game, Path destination) {
    writeToFile(game, destination, "UTF-8");
  }

  /**
   * Writes a Game to an SGF file with the specified encoding, optionally keeping a backup.
   *
   * @param game the game to write
   * @param destination the path where the SGF file will be written
   * @param encoding the character encoding to use
   * @param keepOriginal if true, creates a backup of the original file with a timestamp suffix
   * @throws RuntimeException if the file cannot be written
   */
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

  /**
   * Writes a Game to an SGF file with the specified encoding.
   *
   * @param game the game to write
   * @param destination the path where the SGF file will be written
   * @param encoding the character encoding to use
   * @throws RuntimeException if the file cannot be written
   */
  public static void writeToFile(Game game, Path destination, String encoding) {
    try (
        OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(destination.toFile()), Charset.forName(encoding).newEncoder())) {
      osw.write(game.getGeneratedSgf());
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Writes an SGF string to a temporary file. Useful for testing.
   *
   * @param sgf the SGF content as a string
   * @return a temporary File containing the SGF content
   * @throws RuntimeException if the file cannot be written
   */
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

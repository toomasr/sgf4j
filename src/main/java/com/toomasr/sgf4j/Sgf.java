package com.toomasr.sgf4j;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;

import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.GameNode;

public class Sgf {
  private Parser parser;
  private Game game;

  private Sgf(String sgf) {
    parser = new Parser(sgf);
    game = parser.parse();
    game.postProcess();
  }

  public static Game createFromPath(Path path) {
    try {
      String gameAsString = new String(Files.readAllBytes(path));
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
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
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
    try (
        FileWriter fw = new FileWriter(destination.toFile())) {

      fw.write("(");

      // lets write all the root node properties
      Map<String, String> props = game.getProperties();
      if (props.size() > 0) {
        fw.write(";");
      }

      for (Iterator<Map.Entry<String, String>> ite = props.entrySet().iterator(); ite.hasNext();) {
        Map.Entry<String, String> entry = ite.next();
        fw.write(entry.getKey() + "[" + entry.getValue() + "]");
      }
      GameNode node = game.getRootNode();
      do {
        fw.write(";");
        for (Iterator<Map.Entry<String, String>> ite = node.getProperties().entrySet().iterator(); ite.hasNext();) {
          Map.Entry<String, String> entry = ite.next();
          fw.write(entry.getKey() + "[" + entry.getValue() + "]");
        }
        fw.write("\n");
        // System.out.println(node);
      }
      while ((node = node.getNextNode()) != null);
      fw.write(")");

      fw.close();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Game getGame() {
    return game;
  }
}

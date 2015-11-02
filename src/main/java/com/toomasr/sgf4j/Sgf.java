package com.toomasr.sgf4j;

import java.io.FileWriter;
import java.io.IOException;
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
    Sgf rtrn = null;
    rtrn = new Sgf(gameAsString);
    return rtrn.getGame();
  }

  public static void writeToFile(Game game, Path destination) throws Exception {
    FileWriter fw = new FileWriter(destination.toFile());

    fw.write("(");
    GameNode node = game.getRootNode();
    do {
      fw.write(";");
      for (Iterator<Map.Entry<String, String>> ite = node.getProperties().entrySet().iterator(); ite.hasNext();) {
        Map.Entry<String, String> entry = ite.next();
        fw.write(entry.getKey() + "[" + entry.getValue() + "]");
      }
      fw.write("\n");
      System.out.println(node);
    }
    while ((node = node.getNextNode()) != null);
    fw.write(")");

    fw.close();
  }

  private Game getGame() {
    return game;
  }
}

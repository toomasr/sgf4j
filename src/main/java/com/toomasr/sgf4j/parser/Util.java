package com.toomasr.sgf4j.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Util {
  //@formatter:off
  public static final String[] alphabet = new String[] { "A", "B", "C", "D"
      , "E", "F", "G", "H", "J"
      , "K", "L", "M", "N", "O"
      ,"P", "Q", "R", "S", "T" };

  public static final Map<String, Integer> alphaToCoord = new HashMap<String, Integer>(){{
    put("a", 0);
    put("b", 1);
    put("c", 2);
    put("d", 3);
    put("e", 4);
    put("f", 5);
    put("g", 6);
    put("h", 7);
    put("i", 8);
    put("j", 9);
    put("k", 10);
    put("l", 11);
    put("m", 12);
    put("n", 13);
    put("o", 14);
    put("p", 15);
    put("q", 16);
    put("r", 17);
    put("s", 18);
    put("t", 19);
  }};
  //@formatter:on
  private Util() {
  }

  public static void printNodeTree(GameNode rootNode) {
    if (rootNode.hasChildren()) {
      Set<GameNode> children = rootNode.getChildren();
      for (Iterator<GameNode> ite = children.iterator(); ite.hasNext();) {
        GameNode node = ite.next();
        printNodeTree(node);
      }
    }
  }

  public static int[] alphaToCoords(String input) {
    if (input == null || input.length() < 2) {
      throw new RuntimeException("Coordinate cannot be less than 2 characters. Input '" + input + "'");
    }
    return new int[] { alphaToCoord.get(input.charAt(0) + ""), alphaToCoord.get(input.charAt(1) + "") };
  }

  public static Map<String, String> extractLabels(String str) {
    HashMap<String, String> rtrn = new HashMap<String, String>();
    // the LB property comes like 'fb:A][gb:C][jd:B
    if (str == null)
      return rtrn;
    String[] labels = str.split("\\]\\[");
    for (int i = 0; i < labels.length; i++) {
      String[] label = labels[i].split(":");
      rtrn.put(label[0], label[1]);
    }
    return rtrn;
  }
}

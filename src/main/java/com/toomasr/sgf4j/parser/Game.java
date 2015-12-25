package com.toomasr.sgf4j.parser;

import java.util.HashMap;
import java.util.Map;

public class Game {
  private Map<String, String> properties = new HashMap<String, String>();
  private GameNode rootNode;
  private int noMoves = 0;
  private int noNodes = 0;

  public void addProperty(String key, String value) {
    properties.put(key, value);
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public String toString() {
    return properties.toString();
  }

  public void setRootNode(GameNode rootNode) {
    this.rootNode = rootNode;
  }

  public GameNode getRootNode() {
    return rootNode;
  }

  public int getNoMoves() {
    return noMoves;
  }

  public void setNoMoves(int noMoves) {
    this.noMoves = noMoves;
  }

  public void postProcess() {
    // count the moves
    GameNode node = getRootNode();
    do {
      if (node.isMove()) {
        setNoMoves(getNoMoves() + 1);
      }
      noNodes++;
    }
    while (((node = node.getNextNode()) != null));
    
    // calculate the visual depth
    VisualDepthHelper helper = new VisualDepthHelper();
    helper.calculateVisualDepth(getLastMove());
  }
  
  public int getNoNodes() {
    return noNodes;
  }

  public GameNode getFirstMove() {
    GameNode node = getRootNode();

    do {
      if (node.isMove())
        return node;
    }
    while ((node = node.getNextNode()) != null);

    return null;
  }

  public GameNode getLastMove() {
    GameNode node = getRootNode();
    GameNode rtrn = null;
    do {
      if (node.isMove()) {
        rtrn = node;
      }
    }
    while ((node = node.getNextNode()) != null);
    return rtrn;
  }
}

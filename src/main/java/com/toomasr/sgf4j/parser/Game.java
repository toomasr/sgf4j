package com.toomasr.sgf4j.parser;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.Sgf;

/**
 * This class denotes a Go game. It deals with loading the game and saving the game
 * back to disk.
 */
public class Game {
  private static final Logger log = LoggerFactory.getLogger(Game.class);

  private Map<String, String> properties = new HashMap<String, String>();
  private GameNode rootNode;
  private int noMoves = 0;
  private int noNodes = 0;

  // great for debugging
  private String originalSgf = null;

  public Game() {
  }

  public Game(String originalSgf) {
    this.originalSgf = originalSgf;
  }

  public void addProperty(String key, String value) {
    /*
     * Actually properties can be set multiple times and it seems based on
     * other software that the expectation is that everything is appended rather
     * than the last definition wins.
     */
    if (properties.get(key) != null) {
      String current = properties.get(key);
      properties.put(key, current + "," + value);
    }
    else {
      properties.put(key, value);
    }
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public String getProperty(String key, String defaultValue) {
    if (properties.get(key) == null) {
      return defaultValue;
    }
    else {
      return properties.get(key);
    }
  }

  public Map<String, String> getProperties() {
    return new HashMap<String, String>(this.properties);
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
    // make sure we have a empty first node
    if (getRootNode().isMove()) {
      GameNode oldRoot = getRootNode();
      GameNode newRoot = new GameNode(null);

      newRoot.addChild(oldRoot);
      setRootNode(newRoot);
    }

    GameNode node = getRootNode();
    heuristicalBranchReorder(node);

    // count the moves & nodes
    node = getRootNode();
    do {
      if (node.isMove()) {
        noMoves++;
      }
      noNodes++;
    }
    while (((node = node.getNextNode()) != null));

    // number all the moves
    numberTheMoves(getRootNode(), 1, 0);

    // calculate the visual depth
    VisualDepthHelper helper = new VisualDepthHelper();
    helper.calculateVisualDepth(getLastMove(), 1);
  }

  /*
   * This is a funny logic that I added because my teacher would
   * send me SGF files where very often a variation that should have
   * been the mainline actually ended up being a branch.
   *
   * So I'm looking for the string "answer" in the comment of the child
   * nodes and if I find it I swap this with the main line.
   */
  private void heuristicalBranchReorder(GameNode node) {
  	do {
  		GameNode tmpNode = node.getNextNode();
  		Set<GameNode> children = node.getChildren();

  		if (node.isMove() && tmpNode != null) {
  			GameNode newMainLine = null;
      	for (Iterator<GameNode> ite = children.iterator(); ite.hasNext();) {
					GameNode gameNode = ite.next();
					if (gameNode.getSgfComment().toLowerCase().contains("answer")) {
						newMainLine = gameNode;
					}
				}
      	if (newMainLine != null) {
      		children.remove(newMainLine);
      		children.add(node.getNextNode());
      		node.getNextNode().setPrevNode(null);
      		node.setNextNode(newMainLine);
      		newMainLine.setPrevNode(node);
      	}
      }
    }
    while (((node = node.getNextNode()) != null));
	}

	private void numberTheMoves(GameNode startNode, int moveNo, int nodeNo) {
    GameNode node = startNode;
    int nextMoveNo = moveNo;
    int nextNodeNo = nodeNo;

    if (node.isMove()) {
      startNode.setMoveNo(moveNo);
      nextMoveNo++;
    }

    startNode.setNodeNo(nodeNo);
    nextNodeNo++;

    if (node.getNextNode() != null) {
      numberTheMoves(node.getNextNode(), nextMoveNo, nextNodeNo);
    }

    if (node.hasChildren()) {
      for (Iterator<GameNode> ite = node.getChildren().iterator(); ite.hasNext();) {
        GameNode childNode = ite.next();
        numberTheMoves(childNode, nextMoveNo, nextNodeNo);
      }
    }
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

  public void saveToFile(Path path) {
    Sgf.writeToFile(this, path);
  }

  public boolean isSameGame(Game otherGame) {
    return isSameGame(otherGame, false);
  }

  public boolean isSameGame(Game otherGame, boolean verbose) {
    if (this.equals(otherGame)) {
      if (verbose) {
        System.out.println("The very same game object - returning true");
      }
      return true;
    }

    // all root level properties have to match
    Map<String, String> reReadProps = otherGame.getProperties();
    if (properties.size() != reReadProps.size()) {
      log.trace("Properties mismatch {} {}", properties.size(), otherGame.getProperties().size());
      if (verbose) {
        System.out.printf("Properties mismatch %s %s\n", properties.size(), otherGame.getProperties().size());
      }
      return false;
    }

    for (Iterator<Map.Entry<String, String>> ite = properties.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      if (!entry.getValue().equals(reReadProps.get(entry.getKey()))) {
        log.trace("Property mismatch {}={} {}", entry.getKey(), entry.getValue(), reReadProps.get(entry.getKey()));
        if (verbose) {
          System.out.printf("Property mismatch %s='%s' '%s'", entry.getKey(), entry.getValue(), reReadProps.get(entry.getKey()));
        }
        return false;
      }
    }

    // same number of nodes?
    if (this.getNoNodes() != otherGame.getNoNodes()) {
      log.trace("Games have different no of nodes {} {}", this.getNoNodes(), otherGame.getNoNodes());
      if (verbose) {
        System.out.printf("Games have different no of nodes old=%s new=%s", this.getNoNodes(), otherGame.getNoNodes());
      }
      return false;
    }

    // same number of moves?
    if (this.getNoMoves() != otherGame.getNoMoves()) {
      log.trace("Games have different no of moves {} {}", this.getNoMoves(), otherGame.getNoMoves());
      if (verbose)
        System.out.println("Games have different number of moves " + this.getNoMoves()+" "+otherGame.getNoMoves());
      return false;
    }
    else if (verbose) {
      System.out.println("Games have same number of moves " + this.getNoMoves());
    }

    // alrighty, lets check alllllll the moves
    if (!doAllNodesEqual(this, this.getRootNode(), otherGame, otherGame.getRootNode(), verbose)) {
      if (verbose)
        System.out.println("Some nodes don't equal");
      return false;
    }

    return true;
  }

  private boolean doAllNodesEqual(Game game, GameNode node, Game otherGame, GameNode otherNode, boolean verbose) {
    if (!node.equals(otherNode)) {
      return false;
    }

    // First let's check the nextNode
    GameNode nextNode = node.getNextNode();
    GameNode nextOtherNode = otherNode.getNextNode();

    if (nextNode != null) {
      if (!nextNode.equals(nextOtherNode)) {
        if (verbose) {
          System.out.println("Nodes don't equal");
          System.out.println(nextNode);
          System.out.println(nextOtherNode);
          System.out.println();
        }
        return false;
      }

      if (!doAllNodesEqual(game, nextNode, otherGame, nextOtherNode, verbose)) {
        return false;
      }
    }
    else if (nextNode == null && nextOtherNode != null) {
      if (verbose) {
        System.out.println("Nodes don't equal node="+nextNode+" otherNode="+nextOtherNode);
      }
      return false;
    }

    // Secondly let's check the children nodes
    Set<GameNode> children = node.getChildren();
    Set<GameNode> otherChildren = otherNode.getChildren();

    if (!children.equals(otherChildren)) {
      if (verbose) {
        System.out.println("Children don't equal node="+children+" otherNode="+otherChildren);
      }
      return false;
    }

    Iterator<GameNode> ite = children.iterator();
    Iterator<GameNode> otherIte = otherChildren.iterator();
    for (; ite.hasNext();) {
      GameNode childNode = ite.next();
      GameNode otherChildNode = otherIte.next();
      if (!doAllNodesEqual(game, childNode, otherGame, otherChildNode, verbose)) {
        return false;
      }
    }

    return true;
  }

  public String getOriginalSgf() {
    return originalSgf;
  }

  public void setOriginalSgf(String originalSgf) {
    this.originalSgf = originalSgf;
  }

  public String getGeneratedSgf() {
    StringBuilder rtrn = new StringBuilder();
    rtrn.append("(");

    // lets write all the root node properties
    Map<String, String> props = getProperties();
    if (props.size() > 0) {
      rtrn.append(";");
    }

    for (Iterator<Map.Entry<String, String>> ite = props.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      rtrn.append(entry.getKey() + "[" + entry.getValue() + "]");
    }

    populateSgf(getRootNode(), rtrn);

    rtrn.append(")");
    return rtrn.toString();
  }

  private void populateSgf(GameNode node, StringBuilder sgfString) {
    // print out the node
    sgfString.append(";");
    for (Iterator<Map.Entry<String, String>> ite = node.getProperties().entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      sgfString.append(entry.getKey() + "[" + entry.getValue() + "]");
    }
    sgfString.append("\n");

    // if we have children then first print out the
    // getNextNode() and then the rest of the children
    if (node.hasChildren()) {
      sgfString.append("(");
      populateSgf(node.getNextNode(), sgfString);
      sgfString.append(")");
      sgfString.append("\n");

      for (GameNode childNode : node.getChildren()) {
        sgfString.append("(");
        populateSgf(childNode, sgfString);
        sgfString.append(")");
        sgfString.append("\n");
      }
    }
    // we can just continue with the next elem
    else if (node.getNextNode() != null){
      populateSgf(node.getNextNode(), sgfString);
    }
  }
}

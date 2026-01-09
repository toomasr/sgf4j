package com.toomasr.sgf4j.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.parser.board.Square;
import com.toomasr.sgf4j.parser.board.StoneState;
import com.toomasr.sgf4j.parser.board.VirtualBoard;

/**
 * Represents a complete Go game parsed from SGF format.
 *
 * <p>A Game contains:</p>
 * <ul>
 *   <li>Game-level properties (player names, date, result, etc.)</li>
 *   <li>A tree of {@link GameNode}s representing moves and variations</li>
 *   <li>Timing information for both players (if available in the SGF)</li>
 * </ul>
 *
 * <p>Use {@link Sgf} factory methods to create Game instances:</p>
 * <pre>
 * Game game = Sgf.createFromPath(Paths.get("game.sgf"));
 * GameNode node = game.getRootNode();
 * while (node != null) {
 *     // process node
 *     node = node.getNextNode();
 * }
 * </pre>
 *
 * @see Sgf
 * @see GameNode
 */
public class Game {
  private static final Logger log = LoggerFactory.getLogger(Game.class);

  private Map<String, String> properties = new LinkedHashMap<String, String>();
  private GameNode rootNode;
  private int noMoves = 0;
  private int noNodes = 0;

  // great for debugging
  private String originalSgf = null;

  private MoveTimingInfo wTimings = new MoveTimingInfo(0, 0, 0, 0);

  private MoveTimingInfo bTimings = new MoveTimingInfo(0, 0, 0, 0);

  private boolean timingInfoFound = false;

  public Game() {
  }

  public Game(String originalSgf) {
    this.originalSgf = originalSgf;
  }

  /**
   * Adds a game-level property. If the property already exists, values are concatenated
   * with a comma separator (per SGF spec behavior).
   *
   * @param key the SGF property key (e.g., "PB" for black player name)
   * @param value the property value
   */
  public void addProperty(String key, String value) {
    if (properties.get(key) != null) {
      String current = properties.get(key);
      properties.put(key, current + "," + value);
    } else {
      properties.put(key, value);
    }
  }

  /**
   * Sets the property by overwriting the previous value.
   *
   * @param key the SGF property key
   * @param value the property value
   */
  public void setProperty(String key, String value) {
    properties.put(key, value);
  }

  /**
   * Gets a game-level property value.
   *
   * @param key the SGF property key (e.g., "PB", "PW", "DT", "RE")
   * @return the property value, or null if not set
   */
  public String getProperty(String key) {
    return properties.get(key);
  }

  /**
   * Gets a game-level property value with a default fallback.
   *
   * @param key the SGF property key
   * @param defaultValue the value to return if the property is not set
   * @return the property value, or defaultValue if not set
   */
  public String getProperty(String key, String defaultValue) {
    if (properties.get(key) == null) {
      return defaultValue;
    } else {
      return properties.get(key);
    }
  }

  /**
   * Returns a copy of all game-level properties.
   *
   * @return a new Map containing all properties
   */
  public Map<String, String> getProperties() {
    return this.properties;
  }

  public String toString() {
    return properties.toString();
  }

  /**
   * Sets the root node of the game tree.
   *
   * @param rootNode the root node
   */
  public void setRootNode(GameNode rootNode) {
    this.rootNode = rootNode;
  }

  /**
   * Returns the root node of the game tree. The root node typically contains
   * setup information but not a move. Use {@link #getFirstMove()} to get the
   * first actual move.
   *
   * @return the root node
   */
  public GameNode getRootNode() {
    return rootNode;
  }

  /**
   * Returns the total number of moves in the main line of the game.
   * Does not count setup nodes or nodes in variations.
   *
   * @return the number of moves
   */
  public int getNoMoves() {
    return noMoves;
  }

  public void setNoMoves(int noMoves) {
    this.noMoves = noMoves;
  }

  public void calculateTimingMetrics() {
    GameNode node = getRootNode();
    // count the moves & nodes
    // also calculate the times if possible
    double prevBL = -1;
    int prevBlTime = 0;
    double prevWL = -1;
    int prevWlTime = 0;

    List<Integer> blackTimings = new ArrayList<>();
    List<Integer> whiteTimings = new ArrayList<>();

    node = getRootNode();
    do {
      // TIME RELATED LOGIC
      String bl = node.getProperty("BL");
      String wl = node.getProperty("WL");
      if (bl != null) {
        try {
          double curDouble = Double.parseDouble(bl);
          if (prevBL != -1) {
            prevBlTime = (int) Math.round(prevBL - curDouble);
          }
          if (prevBlTime < 0) {
            prevBlTime = (int) Math.round(prevBL);
          }
          prevBL = curDouble;
          node.addProperty("TimeSpentOnMove", prevBlTime + "");
          blackTimings.add(prevBlTime);
        } catch (NumberFormatException e) {
          // can ignore this, property won't be set
        }
      }

      if (wl != null) {
        try {
          double curDouble = Double.parseDouble(wl);
          if (prevWL != -1) {
            prevWlTime = (int) Math.round(prevWL - curDouble);
          }
          if (prevWlTime < 0) {
            prevWlTime = (int) Math.round(prevWL);
          }
          prevWL = curDouble;
          node.addProperty("TimeSpentOnMove", prevWlTime + "");
          whiteTimings.add(prevWlTime);
        } catch (NumberFormatException e) {
          // can ignore this, property won't be set
        }
      }
      // END OF TIME RELATED LOGIC
    } while (((node = node.getNextNode()) != null));

    if (whiteTimings.size() > 0 || blackTimings.size() > 0) {
      this.wTimings = calculateTimings(whiteTimings);
      this.bTimings = calculateTimings(blackTimings);
    }
  }

  private MoveTimingInfo calculateTimings(List<Integer> timings) {
    if (timings.size() == 0) {
      return new MoveTimingInfo(0, 0, 0, 0);
    }

    int max = 0;
    int min = Integer.MAX_VALUE;
    long sum = 0;
    for (Iterator<Integer> ite = timings.iterator(); ite.hasNext();) {
      int timing = ite.next();
      if (timing > max) {
        max = timing;
      }
      if (timing < min) {
        min = timing;
      }
      sum += timing;
    }

    Collections.sort(timings);
    int median = 0;

    if (timings.size() == 1) {
      median = timings.get(0);
    } else if (timings.size() > 1) {
      median = timings.get(timings.size() / 2);
    }

    if (timings.size() % 2 == 0) {
      median = (timings.size() / 2 + (timings.size() / 2 - 1)) / 2;
    }

    MoveTimingInfo rtrn = new MoveTimingInfo(min, max, (int) sum / timings.size(), median);
    return rtrn;
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
    // I'll need to figure out if and how to add the heuristical
    // reorder. This can be bad as right now it will have side-effects
    // when saving the game.
    // heuristicalBranchReorder(node);

    node = getRootNode();
    do {
      if (node.isMove()) {
        noMoves++;
      }
      noNodes++;
      if (node.getProperty("BL") != null || node.getProperty("WL") != null) {
        timingInfoFound = true;
      }
    } while (((node = node.getNextNode()) != null));

    // number all the moves
    numberTheMoves(getRootNode(), 1, 0);

    // calculate the visual depth
    VisualDepthHelper helper = new VisualDepthHelper();
    helper.calculateVisualDepth(getLastMove(), 1);

    if (timingInfoFound) {
      calculateTimingMetrics();
    }
  }

  /*
   * This is a funny logic that I added because my teacher would send me SGF files
   * where very often a variation that should have been the mainline actually
   * ended up being a branch.
   *
   * So I'm looking for the string "answer" in the comment of the child nodes and
   * if I find it I swap this with the main line.
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
    } while (((node = node.getNextNode()) != null));
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

  /**
   * Returns the total number of nodes in the main line of the game.
   * Includes all nodes (moves, setup, comments, etc.) but not variations.
   *
   * @return the number of nodes
   */
  public int getNoNodes() {
    return noNodes;
  }

  /**
   * Returns the first move node in the game (skipping any setup nodes).
   *
   * @return the first move node, or null if the game has no moves
   */
  public GameNode getFirstMove() {
    GameNode node = getRootNode();

    do {
      if (node.isMove())
        return node;
    } while ((node = node.getNextNode()) != null);

    return null;
  }

  /**
   * Returns the last move node in the main line of the game.
   *
   * @return the last move node, or null if the game has no moves
   */
  public GameNode getLastMove() {
    GameNode node = getRootNode();
    GameNode rtrn = null;
    do {
      if (node.isMove()) {
        rtrn = node;
      }
    } while ((node = node.getNextNode()) != null);
    return rtrn;
  }

  /**
   * Saves the game to an SGF file.
   *
   * @param path the destination file path
   */
  public void saveToFile(Path path) {
    Sgf.writeToFile(this, path);
  }

  /**
   * Compares this game with another for equality. Useful for verifying that
   * parsing and re-serialization preserves game content.
   *
   * @param otherGame the game to compare with
   * @return true if games have identical properties, nodes, and structure
   */
  public boolean isSameGame(Game otherGame) {
    return isSameGame(otherGame, false);
  }

  /**
   * Compares this game with another for equality with optional verbose output.
   *
   * @param otherGame the game to compare with
   * @param verbose if true, prints differences to stdout
   * @return true if games have identical properties, nodes, and structure
   */
  public boolean isSameGame(Game otherGame, final boolean verbose) {
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
      if (!entry.getValue().trim().equals(reReadProps.get(entry.getKey()).trim())) {
        log.trace("Property mismatch {}={} {}", entry.getKey(), entry.getValue(), reReadProps.get(entry.getKey()));
        if (verbose) {
          System.out.printf("Property mismatch %s='%s' '%s'", entry.getKey(), entry.getValue(),
              reReadProps.get(entry.getKey()));
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
        System.out.println("Games have different number of moves " + this.getNoMoves() + " " + otherGame.getNoMoves());
      return false;
    } else if (verbose) {
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

  private boolean doAllNodesEqual(Game game, GameNode node, Game otherGame, GameNode otherNode, final boolean verbose) {
    if (!node.isSameNode(otherNode)) {
      if (verbose) {
        System.out.println("Nodes don't equal \na=" + node + "\nb=" + otherGame);
      }
      return false;
    }

    // First let's check the nextNode
    GameNode nextNode = node.getNextNode();
    GameNode nextOtherNode = otherNode.getNextNode();

    if (nextNode != null) {
      if (!nextNode.isSameNode(nextOtherNode)) {
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
    } else if (nextNode == null && nextOtherNode != null) {
      if (verbose) {
        System.out.println("Nodes don't equal node=" + nextNode + " otherNode=" + nextOtherNode);
      }
      return false;
    }

    // Secondly let's check the children nodes
    Set<GameNode> children = node.getChildren();
    Set<GameNode> otherChildren = otherNode.getChildren();

    if (children.size() != otherChildren.size()) {
      if (verbose) {
        System.out.println("Size of children don't equal node=" + children + " otherNode=" + otherChildren);
      }
      return false;
    }

    for (Iterator<GameNode> ite = children.iterator(); ite.hasNext();) {
      GameNode gameNode = ite.next();
      boolean found = false;
      for (Iterator<GameNode> ite2 = otherChildren.iterator(); ite2.hasNext();) {
        GameNode gameNode2 = ite2.next();
        if (gameNode.isSameNode(gameNode2))
          found = true;
      }
      if (!found) {
        if (verbose) {
          System.out.println("Children don't equal node=" + children + " otherNode=" + otherChildren);
        }
        return false;
      }
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

  /**
   * Returns the original SGF string that was parsed to create this game.
   *
   * @return the original SGF string, or null if not available
   */
  public String getOriginalSgf() {
    return originalSgf;
  }

  /**
   * Sets the original SGF string for reference.
   *
   * @param originalSgf the original SGF content
   */
  public void setOriginalSgf(String originalSgf) {
    this.originalSgf = originalSgf;
  }

  /**
   * Generates an SGF string representation of this game. The output can be
   * saved to a file and re-parsed to recreate the game.
   *
   * @return the game as an SGF-formatted string
   */
  public String getGeneratedSgf() {
    StringBuilder rtrn = new StringBuilder();
    rtrn.append("(");

    // lets write all the root node properties
    Map<String, String> props = getProperties();

    rtrn.append(";");

    for (Iterator<Map.Entry<String, String>> ite = props.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();

      String[] toSplit = new String[] { "ab", "aw" };
      
      if (Arrays.asList(toSplit).contains(entry.getKey().toLowerCase()) && entry.getValue().indexOf(",") != -1) {
        String entries[] = entry.getValue().split(",");
        for (int i = 0; i < entries.length; i++) {
          rtrn.append(entry.getKey() + "[" + entries[i] + "]");
        }
      } else {
        rtrn.append(entry.getKey() + "[" + entry.getValue() + "]");
      }
    }

    populateSgf(getRootNode(), rtrn);

    rtrn.append(")");
    return rtrn.toString();
  }

  /**
   * Generates an SGF string representing the board position at a specific node.
   * The output includes the current stone positions (similar to FEN in chess)
   * plus any remaining moves from that point.
   *
   * @param node the game node representing the current position
   * @param vBoard the virtual board with the current stone positions
   * @return an SGF string with the position setup and remaining moves
   */
  public String getPositionSgf(GameNode node, VirtualBoard vBoard) {
    StringBuilder rtrn = new StringBuilder();
    rtrn.append("(");

    // lets write all the root node properties
    Map<String, String> props = getProperties();

    rtrn.append(";");

    for (Iterator<Map.Entry<String, String>> ite = props.entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      // we skip adding black or white stones because we will overwrite
      // these two anyways
      if ("AB".equals(entry.getKey()) || "AW".equals(entry.getKey())) {
        continue;
      }
      // we won't honour the original value of who's move it is
      // we'll overwrite this with a new one
      if ("PL".equals(entry.getKey())) {
        continue;
      }
      rtrn.append(entry.getKey() + "[" + entry.getValue().trim() + "]");
    }

    StringBuffer AB = new StringBuffer();
    StringBuffer AW = new StringBuffer();
    Square[][] board = vBoard.getBoard();
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        if (board[i][j].isOfColor(StoneState.BLACK)) {
          AB.append("[" + Util.coodToAlpha(i, j) + "]");
        } else if (board[i][j].isOfColor(StoneState.WHITE)) {
          AW.append("[" + Util.coodToAlpha(i, j) + "]");
        }
      }
    }

    if (AB.length() > 1) {
      rtrn.append("AB" + AB.toString());
    }

    if (AW.length() > 1) {
      rtrn.append("AW" + AW.toString());
    }

    if (node != null && node.isBlack()) {
      rtrn.append("PL[W]\n");
    }

    if (node.getNextNode() != null) {
      populateSgf(node.getNextNode(), rtrn);
    }

    rtrn.append(")");
    return rtrn.toString();
  }

  private void populateSgf(GameNode node, StringBuilder sgfString) {
    // print out the node (skip empty nodes that have no properties)
    boolean hasProperties = false;
    for (Map.Entry<String, String> entry : node.getProperties().entrySet()) {
      if (!"TimeSpentOnMove".equals(entry.getKey())) {
        hasProperties = true;
        break;
      }
    }

    if (hasProperties) {
      sgfString.append(";");
      for (Iterator<Map.Entry<String, String>> ite = node.getProperties().entrySet().iterator(); ite.hasNext();) {
        Map.Entry<String, String> entry = ite.next();
        if ("TimeSpentOnMove".equals(entry.getKey()))
          continue;
        sgfString.append(entry.getKey() + "[" + entry.getValue() + "]");
      }
      sgfString.append("\n");
    }

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
    else if (node.getNextNode() != null) {
      populateSgf(node.getNextNode(), sgfString);
    }
  }

  /**
   * Returns whether timing information (BL/WL properties) was found in the SGF.
   *
   * @return true if timing information is available
   */
  public boolean getTimingInfoFound() {
    return this.timingInfoFound;
  }

  /**
   * Returns timing statistics for White's moves.
   *
   * @return timing info with min, max, avg, and median move times in seconds
   */
  public MoveTimingInfo getWTimings() {
    return wTimings;
  }

  /**
   * Returns timing statistics for Black's moves.
   *
   * @return timing info with min, max, avg, and median move times in seconds
   */
  public MoveTimingInfo getBTimings() {
    return bTimings;
  }
}

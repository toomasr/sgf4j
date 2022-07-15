package com.toomasr.sgf4j.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.toomasr.sgf4j.parser.board.Square;
import com.toomasr.sgf4j.parser.board.StoneState;
import com.toomasr.sgf4j.parser.board.VirtualBoard;

/**
 * This class denotes a Go game. It deals with loading the game and saving the
 * game back to disk.
 */
public class Game {
  private static final Logger log = LoggerFactory.getLogger(Game.class);

  private Map<String, String> properties = new HashMap<String, String>();
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

  public void addProperty(String key, String value) {
    /*
     * Actually properties can be set multiple times and it seems based on other
     * software that the expectation is that everything is appended rather than the
     * last definition wins.
     */
    if (properties.get(key) != null) {
      String current = properties.get(key);
      properties.put(key, current + "," + value);
    } else {
      properties.put(key, value);
    }
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public String getProperty(String key, String defaultValue) {
    if (properties.get(key) == null) {
      return defaultValue;
    } else {
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
    }
    else if (timings.size() > 1) {
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

  public int getNoNodes() {
    return noNodes;
  }

  public GameNode getFirstMove() {
    GameNode node = getRootNode();

    do {
      if (node.isMove())
        return node;
    } while ((node = node.getNextNode()) != null);

    return null;
  }

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

  public void saveToFile(Path path) {
    Sgf.writeToFile(this, path);
  }

  public boolean isSameGame(Game otherGame) {
    return isSameGame(otherGame, false);
  }

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
      if (!entry.getValue().trim().equals( reReadProps.get(entry.getKey()).trim()) ) {
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
  
  public String getPositionSgf(GameNode node, VirtualBoard vBoard) {
    StringBuilder rtrn = new StringBuilder();
    rtrn.append("(");

    // lets write all the root node properties
    Map<String, String> props = getProperties();
    if (props.size() > 0) {
      rtrn.append(";");
    }

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
         AB.append("["+Util.coodToAlpha(i, j)+"]"); 
        }
        else if (board[i][j].isOfColor(StoneState.WHITE)) {
          AW.append("["+Util.coodToAlpha(i, j)+"]"); 
         }
      }
    }
    
    if (AB.length() > 1 ) {
      rtrn.append("AB"+AB.toString());
    }
    
    if (AW.length() > 1 ) {
      rtrn.append("AW"+AW.toString());
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
    // print out the node
    sgfString.append(";");
    for (Iterator<Map.Entry<String, String>> ite = node.getProperties().entrySet().iterator(); ite.hasNext();) {
      Map.Entry<String, String> entry = ite.next();
      if("TimeSpentOnMove".equals(entry.getKey()))
        continue;
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
    else if (node.getNextNode() != null) {
      populateSgf(node.getNextNode(), sgfString);
    }
  }

  public boolean getTimingInfoFound() {
    return this.timingInfoFound;
  }

  public MoveTimingInfo getWTimings() {
    return wTimings;
  }

  public MoveTimingInfo getBTimings() {
    return bTimings;
  }
}

package com.toomasr.sgf4j.parser;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.toomasr.sgf4j.board.StoneState;

/**
 * GameNode is any node of a game. This can be any SGF node, whether it is an actual
 * move, markers, placement of stones etc. More information at https://www.red-bean.com/sgf/sgf4.html
 */
public class GameNode implements Comparable<GameNode>, Cloneable {
  private final Set<GameNode> children = new TreeSet<>();
  private final Map<String, String> properties = new HashMap<>();
  private final Map<String, String> updatedProperties = new HashMap<>();

  private int moveNo = -1;
  private int nodeNo = -1;
  private int visualDepth = -1;

  private GameNode parentNode;
  private GameNode nextNode = null;
  private GameNode prevNode = null;
  private int id;

  /**
   * Constructs a new node with the argument as the parent node. Besides a
   * parent node each node also has possibly a previous and next node. Branching
   * is achieved by also having children nodes. See the following for a short
   * overview.
   *
   * getNextNode is the node next on the same line of play. If this is null
   * then the line does not have any more moves.
   *
   * getPrevNode is the previous node on the same line of play. If this is null
   * then this line does not have any previous moves.
   *
   * If node hasChildren() is true then this node has child nodes and not just
   * a nextNode. In that case the getNextNode will be part of the getChildren().
   *
   * @param parentNode node to be the parent of the just created node.
   */
  public GameNode(GameNode parentNode) {
    this.parentNode = parentNode;
  }

  public void addChild(GameNode node) {
    if (nextNode == null) {
      nextNode = node;
      nextNode.setVisualDepth(0);
      node.setPrevNode(this);
      return;
    }

    if (children.contains(node)) {
      throw new RuntimeException("Node '" + node + "' already exists for " + this);
    }

    children.add(node);
  }

  public void setNextNode(GameNode nextNode) {
    this.nextNode = nextNode;
  }

  public GameNode getNextNode() {
    return nextNode;
  }

  public void setPrevNode(GameNode node) {
    this.prevNode = node;
  }

  public GameNode getPrevNode() {
    return prevNode;
  }

  public GameNode getParentNode() {
    return parentNode;
  }

  public void setParentNode(GameNode node) {
    parentNode = node;
  }

  public void addProperty(String key, String value) {
    properties.put(key, value);
  }

  public String getUpdatedProperty(String key) {
    return updatedProperties.get(key);
  }

  public void addUpdatedProperty(String key, String value) {
    updatedProperties.put(key, value);
  }

  public String getProperty(String key) {
    return properties.get(key);
  }

  public String getProperty(String key, String defaultValue) {
    if (properties.get(key) == null)
      return defaultValue;
    else
      return properties.get(key);
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  public boolean isMove() {
    return properties.get("W") != null || properties.get("B") != null;
  }

  public String getMoveString() {
    if (properties.get("W") != null) {
      return properties.get("W");
    }
    else if (properties.get("B") != null) {
      return properties.get("B");
    }
    else {
      return null;
    }
  }

  public int[] getCoords() {
    String moveStr = getMoveString();
    int[] moveCoords = Util.alphaToCoords(moveStr);
    return moveCoords;
  }

  public boolean isWhite() {
    return properties.get("W") != null;
  }

  public boolean isBlack() {
    return properties.get("B") != null;
  }

  public String getColor() {
    if (properties.get("W") != null)
      return "W";
    return "B";
  }

  public StoneState getColorAsEnum() {
    if (properties.get("W") != null)
      return StoneState.WHITE;
    return StoneState.BLACK;
  }

  public boolean hasChildren() {
    return children.size() > 0;
  }

  public Set<GameNode> getChildren() {
    return children;
  }

  public void setMoveNo(int i) {
    this.moveNo = i;
  }

  public int getMoveNo() {
    return moveNo;
  }

  public boolean isEmpty() {
    if (properties.isEmpty() && children.size() == 0)
      return true;
    return false;
  }

  @Override
  protected Object clone() throws CloneNotSupportedException {
    return super.clone();
  }

  /**
   * Similar to equals but doesn't include the ID fiel which is auto-assigned
   * during parsing and can be system and time dependent. Method is meant
   * to compare nodes for all the properties worth while equals method
   * is good to compare objects.
   *
   * @param otherNode
   * @return
   */
  public boolean isSameNode(GameNode otherNode) {
    if (this == otherNode)
      return true;
    if (otherNode == null)
      return false;
    if (getClass() != otherNode.getClass())
      return false;
    GameNode other = (GameNode) otherNode;

    if (children == null) {
      if (other.children != null)
        return false;
    }
    else {
      for (Iterator<GameNode> ite = children.iterator(); ite.hasNext();) {
        GameNode gameNode = (GameNode) ite.next();
        boolean found = false;
        for (Iterator<GameNode> ite2 = children.iterator(); ite2.hasNext();) {
          GameNode gameNode2 = (GameNode) ite2.next();
          if (gameNode.isSameNode(gameNode2)) {
            found = true;
            break;
          }
        }
        if (!found)
          return false;
      }
    }

    if (moveNo != other.moveNo)
      return false;
    if (parentNode == null) {
      if (other.parentNode != null)
        return false;
    }
    else if (!parentNode.isSameNode(other.parentNode))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    }
    else if (!properties.equals(other.properties))
      return false;
    if (visualDepth != other.visualDepth)
      return false;

    return true;
  }

  @Override
  public int compareTo(GameNode o) {
    if (this.visualDepth < o.visualDepth)
      return -1;

    if (this.visualDepth > o.visualDepth)
      return 1;

    if (this.moveNo < o.moveNo)
      return -1;

    if (this.moveNo > o.moveNo)
      return 1;

    // so the move no is the same and the depth is the same
    if (this.hashCode() < o.hashCode())
      return -1;
    else if (this.hashCode() > o.hashCode())
      return 1;

    return 0;
  }

  public void setVisualDepth(int visualDepth) {
    this.visualDepth = visualDepth;
  }

  public int getVisualDepth() {
    return visualDepth;
  }

  public boolean isPass() {
    // tt means a pass and actually an empty [] also
    // but right now not handling that because I don't know
    // how exactly it looks like in a SGF
    if (!isPlacementMove() && "tt".equals(getMoveString())) {
      return true;
    }
    return false;
  }

  /**
   * There are moves that actually don't place a stone of a
   * move but rather a new added position. I call this a placementMove
   *
   * @return true if this is a placement move and not a game move
   */
  public boolean isPlacementMove() {
    return properties.get("W") == null && properties.get("B") == null
        && (properties.get("AB") != null || properties.get("AW") != null);
  }

  public void setNodeNo(int nodeNo) {
    this.nodeNo = nodeNo;
  }

  public int getNodeNo() {
    return this.nodeNo;
  }

  public String getSgfComment() {
    return properties.getOrDefault("C", "");
  }

  public void setId(int id) {
    this.id = id;
  }

  public int getId() {
    return this.id;
  }

  public String toString() {
    return "Props: keys=" + properties.keySet().toString() + " all=" + properties.toString() + " moveNo: " + moveNo + " children: " + children.size() + " vdepth: " + visualDepth+ " parentNode: "+getParentNode().hashCode();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((children == null) ? 0 : children.hashCode());
    result = prime * result + moveNo;
    result = prime * result + id;
    result = prime * result + ((parentNode == null) ? 0 : parentNode.properties.hashCode());
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
    result = prime * result + visualDepth;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    GameNode other = (GameNode) obj;
    if (children == null) {
      if (other.children != null)
        return false;
    }
    else if (!children.equals(other.children))
      return false;
    if (moveNo != other.moveNo)
      return false;
    if (parentNode == null) {
      if (other.parentNode != null)
        return false;
    }
    else if (!parentNode.equals(other.parentNode))
      return false;
    if (properties == null) {
      if (other.properties != null)
        return false;
    }
    else if (!properties.equals(other.properties))
      return false;
    if (visualDepth != other.visualDepth)
      return false;
    if (id != other.id)
      return false;
    return true;
  }
}

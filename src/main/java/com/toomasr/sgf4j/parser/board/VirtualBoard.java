package com.toomasr.sgf4j.parser.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Util;

/**
 * Represents a virtual Go board for replaying games and tracking stone positions.
 *
 * <p>The board handles:</p>
 * <ul>
 *   <li>Stone placement and removal</li>
 *   <li>Capture detection and removal of dead groups</li>
 *   <li>Move undo with restoration of captured stones</li>
 *   <li>Board listener notifications for UI updates</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>
 * VirtualBoard board = new VirtualBoard();
 * GameNode node = game.getRootNode();
 * GameNode prev = null;
 * while (node != null) {
 *     board.makeMove(node, prev);
 *     prev = node;
 *     node = node.getNextNode();
 * }
 * </pre>
 */
public class VirtualBoard {
  private int size = 19;
  private Square[][] vBoard = new Square[size][size];
  private List<BoardListener> boardListeners = new ArrayList<>();
  private Map<GameNode, Set<Group>> moveToRemovedGroups = new HashMap<>();

  /**
   * Creates a new empty 19x19 board.
   */
  public VirtualBoard() {
    initEmptyBoard();
  }

  private void initEmptyBoard() {
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard.length; j++) {
        vBoard[i][j] = new Square(StoneState.EMPTY, i, j);
      }
    }
  }

  /**
   * Makes a move on the board, handling captures and notifying listeners.
   * Records captured groups for potential undo.
   *
   * @param move the move to make
   * @param prevMove the previous move (for listener context)
   */
  public void makeMove(GameNode move, GameNode prevMove) {
    // only if the move is a visible move
    if (move.getMoveString() != null && !move.isPass() && !move.isPlacementMove()) {
      int x = move.getCoords()[0];
      int y = move.getCoords()[1];
      
      this.vBoard[x][y] = new Square(move.getColorAsEnum(), x, y);
      Set<Group> removedGroups = removeDeadGroupsForOppColor(move.getColorAsEnum());
      moveToRemovedGroups.put(move, removedGroups);
      // place the stone on the board
      placeStone(move);
    }

    // play the move fully out with all the bells and whistles
    playMove(move, prevMove);
  }

  /**
   * Undoes a move, removing the stone and restoring any captured stones.
   *
   * @param moveNode the move to undo
   * @param prevMove the move that was before this one
   */
  public void undoMove(GameNode moveNode, GameNode prevMove) {
    if (!moveNode.isPass() && !moveNode.isPlacementMove() && moveNode.getMoveString() != null) {
      String currMoveStr = moveNode.getMoveString();
      int[] moveCoords = Util.alphaToCoords(currMoveStr);
      removeStone(moveCoords[0], moveCoords[1]);
    }

    // if the move that we are taking back happened to remove
    // stones on the board and now the move is undone we need
    // to put those stones back
    Set<Group> removedGroups = moveToRemovedGroups.get(moveNode);
    if (removedGroups != null) {
      for (Iterator<Group> ite = removedGroups.iterator(); ite.hasNext();) {
        Group group = ite.next();
        for (Iterator<Square> ite2 = group.stones.iterator(); ite2.hasNext();) {
          Square square = ite2.next();
          placeStone(square);
        }
      }
    }

    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.undoMove(moveNode, prevMove);
    }
  }

  /**
   * Places a stone on the board and notifies listeners.
   * Does not handle captures - use {@link #makeMove} for game moves.
   *
   * @param color the stone color
   * @param x the x coordinate (0-18)
   * @param y the y coordinate (0-18)
   */
  public void placeStone(StoneState color, int x, int y) {
    this.vBoard[x][y] = new Square(color, x, y);
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.placeStone(x, y, color);
    }
  }

  public void playMove(GameNode move, GameNode prevMove) {
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.playMove(move, prevMove);
    }
  }

  public void placeStone(Square sq) {
    placeStone(sq.getColor(), sq.x, sq.y);
  }

  /**
   * Place a stone on the board. No dead group handling or nothing. This is good
   * to put stones on the board for any starting position.
   *
   * @param gameNode the node to place
   */
  public void placeStone(GameNode gameNode) {
    placeStone(gameNode.getColorAsEnum(), gameNode.getCoords()[0], gameNode.getCoords()[1]);
  }

  /**
   * Places a white stone at the specified coordinates.
   *
   * @param i the x coordinate
   * @param j the y coordinate
   */
  public void placeWhiteStone(int i, int j) {
    placeStone(new Square(StoneState.WHITE, i, j));
  }

  /**
   * Places a black stone at the specified coordinates.
   *
   * @param i the x coordinate
   * @param j the y coordinate
   */
  public void placeBlackStone(int i, int j) {
    placeStone(new Square(StoneState.BLACK, i, j));
  }

  /**
   * Removes dead groups of the opposite color (used after making a move).
   *
   * @param color the color that just played
   * @return the set of groups that were removed
   */
  public Set<Group> removeDeadGroupsForOppColor(StoneState color) {
    return removeDeadGroups(oppColor(color));
  }

  /**
   * Finds and removes all dead groups of the specified color.
   *
   * @param color the color to check for dead groups
   * @return the set of groups that were removed
   */
  public Set<Group> removeDeadGroups(StoneState color) {
    Set<Group> groups = findDistinctGroups(color);
    Set<Group> rtrn = new HashSet<>();
    for (Iterator<Group> ite = groups.iterator(); ite.hasNext();) {
      Group group = ite.next();
      if (group.isDead(vBoard)) {
        removeStones(group);
        rtrn.add(group);
      }
    }
    return rtrn;
  }

  /**
   * Removes a stone from the board and notifies listeners.
   *
   * @param x the x coordinate
   * @param y the y coordinate
   */
  public void removeStone(int x, int y) {
    vBoard[x][y] = new Square(x, y);
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.removeStone(x, y);
    }
  }

  /**
   * Removes all stones in a group from the board.
   *
   * @param group the group to remove
   */
  public void removeStones(Group group) {
    for (Iterator<Square> ite = group.stones.iterator(); ite.hasNext();) {
      Square square = ite.next();
      removeStone(square.x, square.y);
    }
  }

  /**
   * Finds all distinct connected groups of stones of the specified color.
   *
   * @param color the stone color to find groups for
   * @return set of distinct groups
   */
  public Set<Group> findDistinctGroups(StoneState color) {
    Set<Square> alreadyChecked = new HashSet<>();

    Set<Group> groups = new HashSet<>();
    Group activeGroup = new Group();
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard[i].length; j++) {
        // we found a group, lets expand this
        if (vBoard[i][j].isOfColor(color) && !alreadyChecked.contains(vBoard[i][j])) {
          populateGroup(i, j, color, activeGroup);
          alreadyChecked.addAll(activeGroup.stones);

          groups.add(activeGroup);
          activeGroup = new Group();
        }
        // alreadyChecked.add(new Square());
      }
    }
    return groups;
  }

  /*
   * Starts from a node and then finds all the connected stones with this group.
   * Basically populates by starting from a single node.
   */
  private void populateGroup(int i, int j, StoneState color, Group activeGroup) {
    if (vBoard[i][j].isOfColor(color) && !activeGroup.contains(vBoard[i][j])) {
      activeGroup.addStone(vBoard[i][j]);
      if (i - 1 > -1)
        populateGroup(i - 1, j, color, activeGroup);
      if (i + 1 < 19)
        populateGroup(i + 1, j, color, activeGroup);
      if (j - 1 > -1)
        populateGroup(i, j - 1, color, activeGroup);
      if (j + 1 < 19)
        populateGroup(i, j + 1, color, activeGroup);
    } else {
      return;
    }
  }

  /**
   * Returns the opposite color.
   *
   * @param color the input color (must not be EMPTY)
   * @return BLACK if input is WHITE, WHITE if input is BLACK
   * @throws RuntimeException if color is EMPTY
   */
  public StoneState oppColor(StoneState color) {
    if (color.equals(StoneState.EMPTY))
      throw new RuntimeException("Wrong argument for oppColor");
    if (color.equals(StoneState.WHITE))
      return StoneState.BLACK;
    else
      return StoneState.WHITE;
  }

  public void printBoard() {
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard[i].length; j++) {
        System.out.print(vBoard[i][j]);
      }
      System.out.println();
    }
  }

  /**
   * Returns the square at the specified coordinates.
   *
   * @param x the x coordinate (0-18)
   * @param y the y coordinate (0-18)
   * @return the square at that position
   */
  public Square getCoord(int x, int y) {
    return vBoard[x][y];
  }

  /**
   * Creates a board from a string representation. Each character represents
   * a square: 'B' for black, 'W' for white, '.' for empty. Lines are separated
   * by newlines.
   *
   * @param board the string board representation
   * @return a new VirtualBoard with stones placed
   */
  public static VirtualBoard setUpFromStringBoard(String board) {
    VirtualBoard rtrn = new VirtualBoard();
    String[] lines = board.split("\\n");
    for (int i = 0; i < lines.length; i++) {
      for (int j = 0; j < lines[i].length(); j++) {
        Square sq = new Square(lines[i].charAt(j), i, j);
        rtrn.placeStone(sq);
      }
    }
    return rtrn;
  }

  /**
   * Fast-forwards the board to a specific node by replaying all moves from
   * the root. Clears the board first and then plays all moves leading to
   * the specified node.
   *
   * @param fwdTo the target node to fast-forward to
   */
  public void fastForwardTo(GameNode fwdTo) {
    // the fwdTo could be an element in one of the child nodes
    // it is really difficult to find if we start from the rootNode
    // so lets start from the node itself, go backwards until we
    // find the root node and later on play all the moves until that point
    List<GameNode> movesToPlay = new ArrayList<>();
    GameNode node = fwdTo;
    do {
      // if (node.isMove()) {
      movesToPlay.add(node);
      // }
    } while ((node = node.getParentNode()) != null);

    initEmptyBoard();

    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.initInitialPosition();
    }

    GameNode prevMove = null;
    // now lets re-play the moves
    for (int i = movesToPlay.size() - 1; i > -1; i--) {
      node = movesToPlay.get(i);
      makeMove(node, prevMove);

      prevMove = node;
    }
  }

  /**
   * Returns the underlying board array.
   *
   * @return 19x19 array of Squares
   */
  public Square[][] getBoard() {
    return vBoard;
  }

  /**
   * Adds a listener to be notified of board changes.
   *
   * @param listener the listener to add
   */
  public void addBoardListener(BoardListener listener) {
    this.boardListeners.add(listener);
  }
  
  public String toString() {
    StringBuffer rtrn = new StringBuffer();
    
    for (int i = 0; i < vBoard.length; i++) {
      for (int j = 0; j < vBoard.length; j++) {
        rtrn.append(vBoard[j][i]);
      }
      rtrn.append("\n");
    }
    
    return rtrn.toString();
  }
}

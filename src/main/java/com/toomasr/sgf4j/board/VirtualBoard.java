package com.toomasr.sgf4j.board;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.toomasr.sgf4j.parser.GameNode;
import com.toomasr.sgf4j.parser.Game;
import com.toomasr.sgf4j.parser.Util;

public class VirtualBoard {
  private int size = 19;
  private Square[][] board = new Square[size][size];
  private List<BoardListener> boardListeners = new ArrayList<>();
  private Map<GameNode, Set<Group>> moveToRemovedGroups = new HashMap<>();

  public VirtualBoard() {
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board.length; j++) {
        board[i][j] = new Square(StoneState.EMPTY, i, j);
      }
    }
  }

  public void placeStone(StoneState color, int x, int y) {
    this.board[x][y] = new Square(color, x, y);
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.placeStone(x, y, color);
    }
  }

  public void placeStone(Square sq) {
    placeStone(sq.getColor(), sq.x, sq.y);
  }

  public void placeStone(GameNode node) {
    placeStone(node.getColorAsEnum(), node.getCoords()[0], node.getCoords()[1]);
  }

  public void placeWhiteStone(int i, int j) {
    placeStone(new Square(StoneState.WHITE, i, j));
  }

  public void placeBlackStone(int i, int j) {
    placeStone(new Square(StoneState.BLACK, i, j));
  }

  public Set<Group> removeDeadGroupsForOppColor(StoneState color) {
    return removeDeadGroups(oppColor(color));
  }

  public Set<Group> removeDeadGroups(StoneState color) {
    Set<Group> groups = findDistinctGroups(color);
    Set<Group> rtrn = new HashSet<Group>();
    for (Iterator<Group> ite = groups.iterator(); ite.hasNext();) {
      Group group = ite.next();
      if (group.isDead(board)) {
        removeStones(group);
        rtrn.add(group);
      }
    }
    return rtrn;
  }

  public void removeStone(int x, int y) {
    board[x][y] = new Square(x, y);
    for (Iterator<BoardListener> ite = boardListeners.iterator(); ite.hasNext();) {
      BoardListener boardListener = ite.next();
      boardListener.removeStone(x, y);
    }
  }

  public void removeStones(Group group) {
    for (Iterator<Square> ite = group.stones.iterator(); ite.hasNext();) {
      Square square = ite.next();
      removeStone(square.x, square.y);
    }
  }

  protected Set<Group> findDistinctGroups(StoneState color) {
    Set<Square> alreadyChecked = new HashSet<Square>();

    Set<Group> groups = new HashSet<Group>();
    Group activeGroup = new Group();
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        // we found a group, lets expand this
        if (board[i][j].isOfColor(color) && !alreadyChecked.contains(board[i][j])) {
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

  private void populateGroup(int i, int j, StoneState color, Group activeGroup) {
    if (board[i][j].isOfColor(color) && !activeGroup.contains(board[i][j])) {
      activeGroup.addStone(board[i][j]);
      if (i - 1 > -1)
        populateGroup(i - 1, j, color, activeGroup);
      if (i + 1 < 19)
        populateGroup(i + 1, j, color, activeGroup);
      if (j - 1 > -1)
        populateGroup(i, j - 1, color, activeGroup);
      if (j + 1 < 19)
        populateGroup(i, j + 1, color, activeGroup);
    }
    else {
      return;
    }
  }

  private boolean isDead(int i, int j, StoneState color) {
    if (color.equals(StoneState.EMPTY)) {
      return false;
    }

    if ((j + 1 >= size) || board[i][j + 1].isOfColor(oppColor(color))) {
      if ((i + 1 >= size) || board[i + 1][j].isOfColor(oppColor(color))) {
        return true;
      }
    }

    return false;
  }

  public StoneState oppColor(StoneState color) {
    if (color.equals(StoneState.EMPTY))
      throw new RuntimeException("Wrong argument for oppColor");
    if (color.equals(StoneState.WHITE))
      return StoneState.BLACK;
    else
      return StoneState.WHITE;
  }

  public void printBoard() {
    for (int i = 0; i < board.length; i++) {
      for (int j = 0; j < board[i].length; j++) {
        System.out.print(board[i][j]);
      }
      System.out.println();
    }
  }

  public Square getCoord(int x, int y) {
    return board[x][y];
  }

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

  public void fastForwardTo(Game game, GameNode move) {
    GameNode node = game.getRootNode();

    while ((node = game.getNextNode()) != null) {
      if (!node.isMove()) {
        continue;
      }
      placeStone(node);
      Set<Group> removedGroups = removeDeadGroupsForOppColor(move.getColorAsEnum());
      moveToRemovedGroups.put(move, removedGroups);
      if (move.equals(node))
        break;
    }
  }

  public Square[][] getBoard() {
    return board;
  }

  public void addBoardListener(BoardListener listener) {
    this.boardListeners.add(listener);
  }

  public void makeMove(GameNode move) {
    String moveStr = move.getMoveString();
    int[] moveCoords = Util.alphaToCoords(moveStr);
    
    placeStone(move.getColorAsEnum(), moveCoords[0], moveCoords[1]);
    Set<Group> removedGroups = removeDeadGroupsForOppColor(move.getColorAsEnum());
    moveToRemovedGroups.put(move, removedGroups);
  }

  public void undoMove(GameNode currentMove) {
    String currMoveStr = currentMove.getMoveString();
    int[] moveCoords = Util.alphaToCoords(currMoveStr);
    removeStone(moveCoords[0], moveCoords[1]);
    
    // if the move that we are taking back happened to remove
    // stones on the board and now the move is undone we need
    // to put those stones back
    Set<Group> removedGroups = moveToRemovedGroups.get(currentMove);
    if (removedGroups != null) {
      for (Iterator<Group> ite = removedGroups.iterator(); ite.hasNext();) {
        Group group = ite.next();
        for (Iterator<Square> ite2 = group.stones.iterator(); ite2.hasNext();) {
          Square square = ite2.next();
          placeStone(square);
        }
      }
    }
  }
}

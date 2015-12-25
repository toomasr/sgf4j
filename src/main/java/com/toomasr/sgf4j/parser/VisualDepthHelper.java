package com.toomasr.sgf4j.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Computes the visual depth for the game nodes. The visual
 * depth designates as how deep in the tree should the line be
 * shown if a GUI is being used. I should move this helper to
 * sgf4j-gui project but haven't done it yet.
 */
public class VisualDepthHelper {

  public void calculateVisualDepth(GameNode lastNode) {
    // if there are no moves, for example we are just
    // looking at a problem then we can skip calculating
    // the visual depth
    if (lastNode == null) {
      return;
    }

    // a XyZ matrix that we'll fill with 1s and 0s based
    // on whether a square is occupied or not
    List<List<Integer>> depthMatrix = new ArrayList<>();

    initializeMainLine(lastNode, depthMatrix);

    GameNode activeNode = lastNode;
    do {
      if (activeNode.hasChildren()) {
        for (Iterator<GameNode> ite = activeNode.getChildren().iterator(); ite.hasNext();) {
          // the do/while iterates over the main line that has depth 1
          // all other branches have to be at least depth 2
          calculateVisualDepthFor(ite.next(), depthMatrix, 2);
        }
      }
    }
    while ((activeNode = activeNode.getPrevNode()) != null);
  }

  private void initializeMainLine(GameNode lastNode, List<List<Integer>> depthMatrix) {
    // initialize the first line with 0s
    List<Integer> firstLine = new ArrayList<>();
    for (int i = 0; i <= lastNode.getMoveNo(); i++) {
      firstLine.add(i, 0);
    }
    depthMatrix.add(0, firstLine);

    // initialize the first line actual moves with 1s
    GameNode node = lastNode;
    do {
      if (node.isMove()) {
        firstLine.set(node.getMoveNo(), 1);
        // main line will be at depth 0
        node.setVisualDepth(0);
      }
    }
    while ((node = node.getPrevNode()) != null);
  }

  private void calculateVisualDepthFor(GameNode node, List<List<Integer>> depthMatrix, int minDepth) {
    int visualDepth = findVisualDepthForNode(node, depthMatrix, minDepth);
    setVisualDepthForLine(node, visualDepth);

    GameNode activeNode = node;
    do {
      if (activeNode.hasChildren()) {
        for (Iterator<GameNode> ite = activeNode.getChildren().iterator(); ite.hasNext();) {
          calculateVisualDepthFor(ite.next(), depthMatrix, minDepth);
        }
      }
    } while ((activeNode = activeNode.getNextNode()) != null);
  }

  protected void setVisualDepthForLine(GameNode child, int depth) {
    GameNode node = child;
    do {
      node.setVisualDepth(depth);
    }
    while ((node = node.getNextNode()) != null);
  }

  protected int findVisualDepthForNode(GameNode node, List<List<Integer>> depthMatrix, int minDepth) {
    int length = findLengthOfLine(node);

    int depthDelta = minDepth;

    do {
      // init the matrix at this depth if not yet done
      if (depthMatrix.size() <= depthDelta) {
        for (int i = depthMatrix.size(); i <= depthDelta; i++) {
          depthMatrix.add(i, new ArrayList<Integer>());
        }
      }

      List<Integer> levelList = depthMatrix.get(depthDelta);

      boolean available = isAvailableForLineOfPlay(node, length, levelList);

      if (available) {
        bookForLineOfPlay(node, length, levelList);
        break;
      }
      else {
        isAvailableForLineOfPlay(node, length, levelList);
      }
      depthDelta++;
    }
    while (true);

    return depthDelta;
  }

  protected void bookForLineOfPlay(GameNode node, int length, List<Integer> levelList) {
    int start = 0;
    if (node.getMoveNo() > 0) {
      start = node.getMoveNo() - 1;
    }
    for (int i = start; i < (node.getMoveNo() + length); i++) {
      levelList.set(i, 1);
    }
  }

  protected boolean isAvailableForLineOfPlay(GameNode node, int length, List<Integer> levelList) {
    // if the row is not initialized yet then lets fill with 0s
    if (levelList.size() <= node.getMoveNo()) {
      for (int i = levelList.size(); i <= node.getMoveNo() + length; i++) {
        levelList.add(i, 0);
      }
    }

    // we'll start the search one move earlier as we also
    // want to show to "glue stone"
    Integer marker = 0;
    if (node.getMoveNo() != 0) {
      marker = levelList.get(node.getMoveNo() - 1);
    }

    // marker exists, now lets see if available for the whole length
    if (marker == 0) {
      for (int i = marker; i < node.getMoveNo() + length; i++) {
        Integer localMarker = levelList.get(i);
        if (localMarker == 1) {
          return false;
        }
      }
      return true;
    }
    else {
      return false;
    }
  }

  /**
   * Returns the length of this game line. This is the length with
   * no branch taken into account except the main line for this branch.
   *
   * @param node
   * @return
   */
  protected int findLengthOfLine(final GameNode node) {
    GameNode tmpNode = node;
    int i = 0;
    do {
      i++;
    }
    while ((tmpNode = tmpNode.getNextNode()) != null);
    return i;
  }
}

package com.toomasr.sgf4j.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Precomputes the visual depth for the game nodes. The visual
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
    
    calculateVisualDepthFor(lastNode, depthMatrix);
  }

  private void initializeMainLine(GameNode lastNode, List<List<Integer>> depthMatrix) {
    List<Integer> firstLine = new ArrayList<>();
    for (int i = 0; i <= lastNode.getMoveNo(); i++) {
      firstLine.add(i, 0);
    }
    depthMatrix.add(0, firstLine);

    GameNode node = lastNode;
    do {
      if (node.isMove()) {
        firstLine.set(node.getMoveNo(), 1);
      }
    }
    while ((node = node.getPrevNode()) != null);
  }

  private void calculateVisualDepthFor(GameNode node, List<List<Integer>> depthMatrix) {

    do {
      if (node.hasChildren()) {
        for (Iterator<GameNode> ite = node.getChildren().iterator(); ite.hasNext();) {
          GameNode child = ite.next();

          int visualDepth = findVisualDepthForNode(child, depthMatrix);
          setVisualDepthForLine(child, visualDepth + 1);
          calculateVisualDepthFor(child, depthMatrix);
        }
      }
    }
    while ((node = node.getPrevNode()) != null);
  }

  protected void setVisualDepthForLine(GameNode child, int depth) {
    GameNode node = child;
    do {
      node.setVisualDepth(depth);
    }
    while ((node = node.getNextNode()) != null);
  }

  protected int findVisualDepthForNode(GameNode node, List<List<Integer>> depthMatrix) {
    int length = findLengthAtDepth(node);
    // little point in searching on the 0th row
    // which is taken up by the main line
    int depthDelta = 1;
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
    for (int i = node.getMoveNo()-1; i < (node.getMoveNo() + length); i++) {
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
    Integer marker = levelList.get(node.getMoveNo()-1);

    // marker exists, now lets see if available for the whole length
    if (marker == 0) {
      for (int i = node.getMoveNo()-1; i < node.getMoveNo() + length; i++) {
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

  protected int findLengthAtDepth(GameNode rootNode) {
    GameNode node = rootNode;
    int i = 0;
    do {
      i++;
    }
    while ((node = node.getNextNode()) != null);
    return i;
  }
}

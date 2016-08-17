package com.toomasr.sgf4j.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Test;

import com.toomasr.sgf4j.Parser;

import junit.framework.TestCase;

public class TestVisualDepthHelper extends TestCase {

  private Game simpleBranchingGame;
  private VisualDepthHelper helper;
  private ArrayList<Integer> nodeMarkerList;
  private GameNode firstChild;
  private GameNode secondChild;
  private Game complexBranchingGame;

  public void setUp() throws Exception {
    Path path = Paths.get("./src/main/resources/game-branching-simple.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    simpleBranchingGame = parser.parse();

    helper = new VisualDepthHelper();

    nodeMarkerList = new ArrayList<Integer>();
    for (int i = 0; i < 10; i++) {
      nodeMarkerList.add(i, 0);
    }

    Iterator<GameNode> ite = simpleBranchingGame.getFirstMove().getChildren().iterator();
    firstChild = ite.next();
    secondChild = ite.next();

    path = Paths.get("./src/main/resources/game-branching-complex.sgf");
    gameAsString = new String(Files.readAllBytes(path));
    Parser myParser = new Parser(gameAsString);
    complexBranchingGame = myParser.parse();
  }

  @Test
  public void testBookForLineOfPlay() throws Exception {
    helper.bookForLineOfPlay(firstChild, 2, nodeMarkerList);

    Assert.assertEquals(0, (int) nodeMarkerList.get(0));
    Assert.assertEquals(1, (int) nodeMarkerList.get(1));
    Assert.assertEquals(1, (int) nodeMarkerList.get(2));
    Assert.assertEquals(1, (int) nodeMarkerList.get(3));
    Assert.assertEquals(0, (int) nodeMarkerList.get(4));
  }

  @Test
  public void testIsAvailableForlineOfPlayFail() throws Exception {

    nodeMarkerList.set(1, 1);
    nodeMarkerList.set(2, 1);
    nodeMarkerList.set(3, 1);

    boolean result = helper.isAvailableForLineOfPlay(firstChild, 2, nodeMarkerList);
    assertFalse(result);
  }

  @Test
  public void testIsAvailableForlineOfPlaySuccess() throws Exception {

    boolean result = helper.isAvailableForLineOfPlay(firstChild, 2, nodeMarkerList);
    assertTrue(result);
  }

  @Test
  public void testIsAvailableForlineOfPlaySuccessComplex() throws Exception {
    nodeMarkerList.set(4, 1);
    nodeMarkerList.set(5, 1);
    nodeMarkerList.set(6, 1);
    boolean result = helper.isAvailableForLineOfPlay(firstChild, 2, nodeMarkerList);
    assertTrue(result);
  }

  @Test
  public void testCalculateVisualDepth() {
    helper.calculateVisualDepth(simpleBranchingGame.getLastMove());

    assertEquals(1, firstChild.getVisualDepth());
    assertEquals(2, secondChild.getVisualDepth());
  }

  @Test
  public void testCalculateVisualDepthComplex1() {
    helper.calculateVisualDepth(complexBranchingGame.getLastMove());

    GameNode rootNode = complexBranchingGame.getFirstMove();

    Iterator<GameNode> ite = rootNode.getChildren().iterator();
    GameNode firstChild = ite.next();
    GameNode secondChild = ite.next();

    assertEquals(1, firstChild.getVisualDepth());
    assertEquals(2, firstChild.getChildren().iterator().next().getVisualDepth());
    assertEquals(2, firstChild.getChildren().iterator().next().getNextNode().getVisualDepth());

    assertEquals(3, secondChild.getVisualDepth());
    assertEquals(4, secondChild.getChildren().iterator().next().getVisualDepth());
  }

  public void testProblematic005() throws Exception {
    Path path = Paths.get("./src/test/resources/problematic-005.sgf");
    String gameAsString = new String(Files.readAllBytes(path));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();

    GameNode rootNode = game.getRootNode();
    GameNode node = rootNode.getChildren().iterator().next();
    assertEquals(1, node.getVisualDepth());
    GameNode node2 = node.getNextNode();
    assertEquals(1, node2.getVisualDepth());
    assertEquals(2, node2.getChildren().iterator().next().getVisualDepth());
  }
}

package com.toomasr.sgf4j.board;

import java.util.Set;

import org.junit.Test;

import junit.framework.TestCase;

public class TestBoard extends TestCase {
  public void setUp() {

  }

  @Test
  public void testDeadGroupsSingleStoneInCorner() {
    VirtualBoard brd = new VirtualBoard();
    brd.placeBlackStone(0, 0);
    brd.placeWhiteStone(0, 1);
    brd.placeWhiteStone(1, 0);
    brd.removeDeadGroups(StoneState.BLACK);
    assertTrue(brd.getCoord(0, 0).isEmpty());
  }

  @Test
  public void testDeadGroupsSingleStoneInCenter() {
    VirtualBoard brd = new VirtualBoard();
    brd.placeBlackStone(9, 9);
    brd.placeWhiteStone(9, 10);
    brd.placeWhiteStone(9, 8);
    brd.placeWhiteStone(10, 9);
    brd.placeWhiteStone(8, 9);
    brd.removeDeadGroups(StoneState.BLACK);
    assertTrue(brd.getCoord(9, 9).isEmpty());
  }

  @Test
  public void testDeadGroupsMultipleStones1() {
    VirtualBoard brd = new VirtualBoard();
    brd.placeBlackStone(9, 9);
    brd.placeBlackStone(9, 10);

    brd.placeWhiteStone(9, 8);
    brd.placeWhiteStone(9, 11);

    brd.placeWhiteStone(8, 9);
    brd.placeWhiteStone(8, 10);

    brd.placeWhiteStone(10, 9);
    brd.placeWhiteStone(10, 10);

    brd.removeDeadGroups(StoneState.BLACK);
  }

  @Test
  public void testFindGroupHuge() {
    // @formatter:off
    VirtualBoard brd = VirtualBoard.setUpFromStringBoard(
              "xx--xx-----x-------\n"
            + "-x--x---xxxx-------\n"
            + "-xxxx---x--x-------\n"
            + "--xx---xx----------\n"
            + "---xxxxx-----------\n"
            + "-------------------\n"
            + "-------------------");
    // @formatter:on
    Set<Group> groups = brd.findDistinctGroups(StoneState.BLACK);
    assertEquals(1, groups.size());
  }

  @Test
  public void testGroupIsDead1() {
    // @formatter:off
    VirtualBoard brd = VirtualBoard.setUpFromStringBoard(
              "xo-----------------\n"
            + "o------------------");
    // @formatter:on
    Set<Group> groups = brd.findDistinctGroups(StoneState.BLACK);
    assertEquals(1, groups.size());
    boolean isDead = groups.iterator().next().isDead(brd);
    assertEquals(true, isDead);
  }

  @Test
  public void testGroupIsDead2() {
    // @formatter:off
    VirtualBoard brd = VirtualBoard.setUpFromStringBoard(
              "xxxxxxxxxxxxxxxxxxx\n"
            + "ooooooooooooooooooo");
    // @formatter:on
    Set<Group> groups = brd.findDistinctGroups(StoneState.BLACK);
    assertEquals(1, groups.size());
    boolean isDead = groups.iterator().next().isDead(brd);
    assertEquals(true, isDead);
  }
}

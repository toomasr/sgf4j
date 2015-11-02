package com.toomasr.sgf4j.board;

public interface BoardListener {
  void placeStone(int x, int y, StoneState color);
  void removeStone(int x, int y);
}

package com.toomasr.sgf4j.parser.board;

public enum StoneState {
  EMPTY(1), BLACK(2), WHITE(3);

  private int value;

  private StoneState(int value) {
    this.value = value;
  }

  public boolean isEmpty() {
    return this.value == 1;
  }
}
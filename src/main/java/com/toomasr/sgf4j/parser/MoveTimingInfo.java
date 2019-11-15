package com.toomasr.sgf4j.parser;

public class MoveTimingInfo {
  final public int min;
  final public int max;
  final public int avg;
  final public int median;

  public MoveTimingInfo(int min, int max, int avg, int median) {
    super();
    this.min = min;
    this.max = max;
    this.avg = avg;
    this.median = median;
  }
}

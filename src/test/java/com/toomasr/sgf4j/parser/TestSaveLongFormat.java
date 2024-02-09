package com.toomasr.sgf4j.parser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * To my sad surprise I discovered the shortform of a property, for example with
 * AW the case of AW[qc,dd,qd,qe,rf,dp] that you find in sgf files on the
 * internet is not supported by very many programs. CGoban and GoWrite for
 * example. Not sure why I ever even supported that.
 * 
 * Anyways, I'm now making sure that even if the file uses that format this
 * library will save that as AW[qc]AW[dd] etc.
 */
public class TestSaveLongFormat {
  @Test
  public void testSimple() throws Exception {
    Path sgfPath = Paths.get("./src/test/resources/long-format-aw.sgf");
    String gameAsString = new String(Files.readAllBytes(sgfPath));
    Parser parser = new Parser(gameAsString);
    Game game = parser.parse();
    game.postProcess();
    
    String sgf = game.getGeneratedSgf();
    System.out.println(sgf);
  }
}

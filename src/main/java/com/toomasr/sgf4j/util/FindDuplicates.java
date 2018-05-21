package com.toomasr.sgf4j.util;

import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FindDuplicates {
  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.exit(0);
    }
    Path root = Paths.get(args[0]);
    if (!Files.exists(root)) {
      System.out.println(root.toString() + " does not exist");
      System.exit(0);
    }
    System.out.println("Starting searching " + root.toRealPath().toString());
    Path[] sgfFiles = findSgfFiles(root.toRealPath());
    findDuplicateGames(sgfFiles);
  }

  private static void findDuplicateGames(Path[] sgfFiles) throws Exception {
    int bMatches = 0;
    int gMatches = 0;
    for (int i = 0; i < sgfFiles.length; i++) {
      for (int j = i; j < sgfFiles.length; j++) {
        if (i == j)
          continue;

        if (SgfCompare.binaryCompare(sgfFiles[i], sgfFiles[j])) {
          System.out.println("Binary match:");
          System.out.println("\t" + sgfFiles[i]);
          System.out.println("\t" + sgfFiles[j]);
          bMatches++;
        }
        else if (SgfCompare.compareSgf(sgfFiles[i], sgfFiles[j])) {
          System.out.println("Game match:");
          System.out.println("\t" + sgfFiles[i]);
          System.out.println("\t" + sgfFiles[j]);
          gMatches++;
        }
      }
    }
    System.out.println("Found " + bMatches + " binary matches");
    System.out.println("Found " + gMatches + " game matches");
  }

  private static Path[] findSgfFiles(Path root) throws Exception {
    final List<Path> rtrn = new ArrayList<>();
    Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path path,
          BasicFileAttributes attr) {
        if (attr.isRegularFile() && path.getFileName().toString().toLowerCase().endsWith("sgf")) {

          rtrn.add(path);
        }
        return FileVisitResult.CONTINUE;
      }
    });
    return rtrn.toArray(new Path[] {});
  }

}

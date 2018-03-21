SGF4J
=====

I've been doing some serious [yak shaving](https://en.wiktionary.org/wiki/yak_shaving). Instead of solving [Go](https://en.wikipedia.org/wiki/Go_(game)) problems to improve my game I've been writing a [SGF](http://www.red-bean.com/sgf/) viewer to make it easier to go through my problem files.

As a side result of [that viewer](https://github.com/toomasr/sgf4j-gui) I had to do some SGF parsing. Hence this project. You can use this library to parse SGF files and then actually play it out on a virtual board and then write out the SGF if you want to. I test the library with bunch of databases to make sure it is able to parse enough different SGF files and also when writing those out no data is lost.

Include in your project
=======================

The project is synced to Maven Central and to use it just include it in your `pom.xml`.

```
<dependencies>
  <dependency>
    <groupId>com.toomasr</groupId>
    <artifactId>sgf4j-parser</artifactId>
    <version>0.0.1</version>
  </dependency>
<dependencies>
```

Parsing
=======

Simple parsing looks like this. You either have a SGF file in a String or a Path of the location of the file. You create a Game object of it and then start walking the nodes from the root node. There are methods to start from the end too.

```java
    Game game = Sgf.createFromPath(Paths.get("location-of-your.sgf"));

    GameNode node = game.getRootNode();
    do {
      System.out.println(node);
    }
    while ((node = node.getNextNode()) != null);
```

Remember that not every node is an actual move. Also note that a node might have multiple children. I have built it in a way that `node.getNextMove()` returns the next move in the line while getChildren() will return the rest of the possible next moves.

Writing
=======

Once you have parsed a file into a Game object you can also write it back to a SGF file.

```java
Game game = Sgf.createFromPath(sgfPath);
//... add/remove/update nodes
game.saveToFile(Paths.get("/tmp/output.sgf"))
```

And the game has been serialised into a SGF file.
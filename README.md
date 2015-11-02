SGF4J
=====

I've been doing some serious [yak shaving](https://en.wiktionary.org/wiki/yak_shaving). Instead of solving [Go](https://en.wikipedia.org/wiki/Go_(game)) problems to improve my game I've been writing a [SGF](http://www.red-bean.com/sgf/) viewer to make it easier to go through my problem files.

As a side result of that viewer (which I haven't put up yet anywhere) I had to do some SGF parsing. Hence this project. You can use this library to parse SGF files and then actually play it out on a virtual board. The library is not mature, I have tested it with couple of games as I've been writing the viewer.

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

import java.util.ArrayList;
import java.util.Arrays;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

//Represents a Cell for the Game
class Cell {
  Color c;
  Posn loc;
  Cell up;
  Cell down;
  Cell left;
  Cell right;

  Cell(Color c, Posn loc) {
    this.c = c;
    this.loc = loc;
    this.up = null;
    this.down = null;
    this.left = null;
    this.right = null;
  }

  // Renders a cell as a Square
  WorldImage toImage() {
    return new RectangleImage(Utils.CELLSIZE, Utils.CELLSIZE, "solid", this.c);
  }

  // Is given Posn within the Size of a Cell?
  boolean onCell(Posn pos) {
    return (this.loc.x - (Utils.CELLSIZE / 2) < pos.x)
        && (this.loc.x + (Utils.CELLSIZE / 2) > pos.x)
        && (this.loc.y - (Utils.CELLSIZE / 2) < pos.y)
        && (this.loc.y + (Utils.CELLSIZE / 2) > pos.y);
  }
}

//Represents the World 
class BridgIt extends World {
  ArrayList<ArrayList<Cell>> game;
  int n;
  boolean p1;
  Utils u = new Utils();

  BridgIt(int n) {
    this.n = n;
    this.p1 = true;
    if (n >= 3 && n % 2 != 0) {
      this.game = u.generateBoard(n);
    }

    else {
      throw new RuntimeException("Incorrect Game Size");
    }
  }

  // Render the Game
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(n * Utils.MARGIN, n * Utils.MARGIN);
    for (ArrayList<Cell> row : game) {
      for (Cell c : row) {
        scene.placeImageXY(c.toImage(), c.loc.x, c.loc.y);
      }
    }
    return scene;
  }

  // creates the on tick (run) of the game
  public void onTick() {
    checkForWinner();
  }

  // On click of the program
  public void onMouseClicked(Posn pos) {
    for (int row = 1; row < this.game.size() - 1; row++) { // Don't iterate through outer cells
      for (int col = 1; col < this.game.size() - 1; col++) {
        Cell c = this.game.get(row).get(col);
        if (c.onCell(pos) && c.c.equals(Color.white)) { // If Mouse clicks White cell
          if (this.p1) {
            c.c = Color.pink; // Player 1 places Pink
            this.p1 = false; // Changes player
          }
          else {
            c.c = Color.magenta; // Player 2 places Magenta
            this.p1 = true; // Changes player
          }
        }
      }
    }
  }

  // Call this method after each player's move to check for a winner.
  public void checkForWinner() {
    // Initialize visited array for DFS
    boolean[][] visited = new boolean[this.n][this.n];

    // Check for a path for Player 1 (Pink, from left to right)
    for (int row = 0; row < this.n; row++) {
      if (this.game.get(row).get(0).c.equals(Color.pink) && !visited[row][0]) {
        if (dfs(row, 0, Color.pink, visited, true)) {
          this.endOfWorld("Player one has won!");
          return;
        }
      }
    }

    // Reset visited array for Player 2's DFS
    for (int i = 0; i < this.n; i++) {
      Arrays.fill(visited[i], false);
    }

    // Check for a path for Player 2 (Magenta, from top to bottom)
    for (int col = 0; col < this.n; col++) {
      if (this.game.get(0).get(col).c.equals(Color.magenta) && !visited[0][col]) {
        if (dfs(0, col, Color.magenta, visited, false)) {
          this.endOfWorld("Player two has won!");
          return;
        }
      }
    }
  }

  // DFS helper method to find path recursively
  public boolean dfs(int row, int col, Color playerColor, boolean[][] visited,
      boolean horizontal) {
    // Check if current cell is outside the grid or already visited
    if (row < 0 || col < 0 || row >= this.n || col >= this.n || visited[row][col]
        || !this.game.get(row).get(col).c.equals(playerColor)) {
      return false;
    }
    // Mark the current cell as visited
    visited[row][col] = true;
    // If horizontal, check if we've reached the last column
    if (horizontal && col == this.n - 1) {
      return true;
    }
    // If vertical, check if we've reached the last row
    if (!horizontal && row == this.n - 1) {
      return true;
    }
    // Explore all four possible directions
    return dfs(row + 1, col, playerColor, visited, horizontal)
        || dfs(row - 1, col, playerColor, visited, horizontal)
        || dfs(row, col + 1, playerColor, visited, horizontal)
        || dfs(row, col - 1, playerColor, visited, horizontal);
  }

  // End of World Scene to displayer winner
  public WorldScene lastScene(String msg) {
    int sceneSize = n * Utils.MARGIN;
    WorldScene endScene = new WorldScene(sceneSize, sceneSize);
    endScene.placeImageXY(new TextImage(msg, 50, Color.black), sceneSize / 2, sceneSize / 2);
    return endScene;
  }

}

// Utils class 
class Utils {
  public static int CELLSIZE = 35;

  public static int MARGIN = 50;

  // Generates the Game board
  ArrayList<ArrayList<Cell>> generateBoard(int n) {
    ArrayList<ArrayList<Cell>> board = new ArrayList<ArrayList<Cell>>();

    for (int row = 0; row < n; row++) {
      ArrayList<Cell> boardRow = new ArrayList<Cell>();

      for (int col = 0; col < n; col++) {
        Color cellColor;

        // Pink cells in every odd-numbered row and even-numbered column
        if (row % 2 != 0 && col % 2 == 0) {
          cellColor = Color.pink;
        }
        // Magenta cells in every even-numbered row and odd-numbered column
        else if (row % 2 == 0 && col % 2 != 0) {
          cellColor = Color.magenta;
        }
        // All other cells are white
        else {
          cellColor = Color.white;
        }

        // Create the cell and add it to the row
        boardRow.add(new Cell(cellColor,
            new Posn(col * Utils.CELLSIZE + Utils.MARGIN, row * Utils.CELLSIZE + Utils.MARGIN)));
      }
      // Add the row to the board
      board.add(boardRow);
    }
    return board;
  }

  // Initializes Board by linking all Cells
  void initializeBoard(ArrayList<ArrayList<Cell>> board) {
    int n = board.size(); // Assuming a square board
    for (int row = 0; row < n; row++) {
      for (int col = 0; col < n; col++) {
        Cell currentCell = board.get(row).get(col);

        if (row > 0) {
          currentCell.up = board.get(row - 1).get(col);
        }
        if (row < n - 1) {
          currentCell.down = board.get(row + 1).get(col);
        }
        if (col > 0) {
          currentCell.left = board.get(row).get(col - 1);
        }
        if (col < n - 1) {
          currentCell.right = board.get(row).get(col + 1);
        }
      }
    }
  }
}

// Represents Examples
class Examples {
  Utils u = new Utils();

  // test to use bigbang
  void testBigBang(Tester t) {
    BridgIt game = new BridgIt(11);
    game.bigBang(game.n * Utils.MARGIN, game.n * Utils.MARGIN, 0.005);
  }

  // Test toImage method of the Cell class
  void testCellToImage(Tester t) {
    Cell pinkCell = new Cell(Color.pink, new Posn(0, 0));
    t.checkExpect(pinkCell.toImage(),
        new RectangleImage(Utils.CELLSIZE, Utils.CELLSIZE, "solid", Color.pink));
  }

  // Test generateBoard method of the Utils class
  void testGenerateBoard(Tester t) {
    Utils u = new Utils();
    ArrayList<ArrayList<Cell>> board5x5 = u.generateBoard(5);

    // Checking the first cell (should be white)
    t.checkExpect(board5x5.get(0).get(0).c, Color.white);
    // Checking a pink cell
    t.checkExpect(board5x5.get(0).get(1).c, Color.magenta);
    // Checking a cell with expected position
    t.checkExpect(board5x5.get(0).get(1).loc,
        new Posn(Utils.CELLSIZE + Utils.MARGIN, Utils.MARGIN));
  }

  // Test constructor of BridgIt class
  void testBridgItConstructor(Tester t) {
    BridgIt game11 = new BridgIt(11);
    t.checkExpect(game11.n, 11); // Size of Board
    t.checkExpect(game11.game.size(), 11); // # of Arrays in ArrayList
    t.checkExpect(game11.game.get(0).size(), 11); // # of Cells in Arrayss
    t.checkConstructorException(new RuntimeException("Incorrect Game Size"), "BridgIt", 10);
  }

  // Test makeScene method of the BridgIt class
  void testMakeScene(Tester t) {
    BridgIt game5 = new BridgIt(5);
    BridgIt sceneGame = new BridgIt(3);
    WorldScene scene = game5.makeScene();
    WorldScene empty = new WorldScene(150, 150);
    t.checkExpect(scene.width, game5.n * Utils.MARGIN); // Check if the scene has the expected width
    t.checkExpect(scene.height, game5.n * Utils.MARGIN); // Check if the scene has the expected
    // Check makeScene
    Cell cell1 = new Cell(Color.white, new Posn(50, 50));
    empty.placeImageXY(cell1.toImage(), 50, 50);
    Cell cell2 = new Cell(Color.magenta, new Posn(100, 50));
    empty.placeImageXY(cell2.toImage(), 85, 50);
    Cell cell3 = new Cell(Color.white, new Posn(150, 50));
    empty.placeImageXY(cell3.toImage(), 120, 50);
    Cell cell4 = new Cell(Color.pink, new Posn(50, 100));
    empty.placeImageXY(cell4.toImage(), 50, 85);
    Cell cell5 = new Cell(Color.WHITE, new Posn(100, 100));
    empty.placeImageXY(cell5.toImage(), 85, 85);
    Cell cell6 = new Cell(Color.pink, new Posn(150, 100));
    empty.placeImageXY(cell6.toImage(), 120, 85);
    Cell cell7 = new Cell(Color.white, new Posn(50, 150));
    empty.placeImageXY(cell7.toImage(), 50, 120);
    Cell cell8 = new Cell(Color.magenta, new Posn(100, 150));
    empty.placeImageXY(cell8.toImage(), 85, 120);
    Cell cell9 = new Cell(Color.white, new Posn(150, 50));
    empty.placeImageXY(cell9.toImage(), 120, 120);

    t.checkExpect(sceneGame.makeScene(), empty);
  }

  //Test to initialize the game
  void testInitializeGame(Tester t) {
    BridgIt linkGame = new BridgIt(5);
    t.checkExpect(linkGame.game.get(0).get(1).right, null);
    t.checkExpect(linkGame.game.get(0).get(1).left, null);
    t.checkExpect(linkGame.game.get(0).get(1).up, null);
    t.checkExpect(linkGame.game.get(0).get(1).down, null);
    u.initializeBoard(linkGame.game);
    t.checkExpect(linkGame.game.get(0).get(1).right, linkGame.game.get(0).get(2));
    t.checkExpect(linkGame.game.get(0).get(2).right, linkGame.game.get(0).get(3));
    t.checkExpect(linkGame.game.get(0).get(2).down, linkGame.game.get(1).get(2));
    t.checkExpect(linkGame.game.get(0).get(3).left, linkGame.game.get(0).get(2));
    t.checkExpect(linkGame.game.get(3).get(2).up, linkGame.game.get(2).get(2));
    t.checkExpect(linkGame.game.get(1).get(1).left, linkGame.game.get(1).get(0));
  }

  //Test for on cell method
  void testOnCell(Tester t) {
    BridgIt game3 = new BridgIt(3);
    t.checkExpect(game3.game.get(0).get(0).onCell(new Posn(50, 50)), true);
    t.checkExpect(game3.game.get(0).get(0).onCell(new Posn(50, 200)), false);
    t.checkExpect(game3.game.get(0).get(0).onCell(new Posn(200, 50)), false);
    t.checkExpect(game3.game.get(0).get(0).onCell(new Posn(60, 40)), true);
  }

  //Test for mouse on click action
  void testMouseClick(Tester t) {
    BridgIt gameTestClick = new BridgIt(5);
    t.checkExpect(gameTestClick.game.get(1).get(1).c, Color.white); // Cell is White
    gameTestClick.onMouseClicked(new Posn(100, 100));
    t.checkExpect(gameTestClick.game.get(1).get(1).c, Color.pink); //Cell becomes Pink from p1 click
    gameTestClick.onMouseClicked(new Posn(100, 100));
    t.checkExpect(gameTestClick.game.get(1).get(1).c, Color.pink); //Cell can't change color anymore
    t.checkExpect(gameTestClick.game.get(3).get(1).c, Color.white); //Cell can't change color 
    gameTestClick.onMouseClicked(new Posn(85, 155));
    t.checkExpect(gameTestClick.game.get(3).get(1).c, Color.magenta); //P2 turn, white turns magenta
  }

  //Test for dfs case 1: path Found for player 1
  void testDFSPlayer1Path(Tester t) {
    BridgIt game1 = new BridgIt(5);
    // Manually setting up a continuous pink path from left to right
    for (int row = 1; row < game1.n; row += 2) {
      for (int col = 0; col < game1.n; col++) {
        game1.game.get(row).get(col).c = Color.pink;
      }
    }
    // Checking for path
    t.checkExpect(game1.dfs(1, 0, Color.pink, new boolean[game1.n][game1.n], true), true);
  }

  //Test for dfs case 2: path found for player 2
  void testDFSPlayer2Path(Tester t) {
    BridgIt game2 = new BridgIt(5);
    // Manually set up a continuous magenta path from top to bottom
    for (int col = 1; col < game2.n; col += 2) {  // Adjusted to start from column 1
      for (int row = 0; row < game2.n; row++) {
        game2.game.get(row).get(col).c = Color.magenta;
      }
    }
    // Check for path
    t.checkExpect(game2.dfs(0, 1, Color.magenta, new boolean[game2.n][game2.n], false), true);
  }

  //Test Case 3: No Path Found
  void testDFSNoPath(Tester t) {
    BridgIt game3 = new BridgIt(5);
    // making sure there is no complete path for either player on the board (default mode)
    for (ArrayList<Cell> row : game3.game) {
      for (Cell cell : row) {
        cell.c = Color.white;
      }
    }
    // Check for path
    t.checkExpect(game3.dfs(0, 0, Color.pink, new boolean[game3.n][game3.n], true), false);
    t.checkExpect(game3.dfs(0, 0, Color.magenta, new boolean[game3.n][game3.n], false), false);
  }

  //Test for last scene in the game
  void testLastScene(Tester t) {
    BridgIt game = new BridgIt(5);
    String winningMessage = "Player one has won!";
    WorldScene expectedScene = new WorldScene(game.n * Utils.MARGIN, game.n * Utils.MARGIN);
    expectedScene.placeImageXY(new TextImage(winningMessage, 50, Color.black),
        game.n * Utils.MARGIN / 2, game.n * Utils.MARGIN / 2);
    t.checkExpect(game.lastScene(winningMessage), expectedScene);
  }

}


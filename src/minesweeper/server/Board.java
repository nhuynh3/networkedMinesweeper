package minesweeper.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import minesweeper.server.Box.State;

/**
 * Thread safety argument:
 *  This data type uses synchronization when it performs
 *  mutations or accesses to more than one of its variants. 
 *  Mutators that are not synchronized are private methods,
 *  that are only called by synchronized public methods.
 *  In the cases where actions are performed on its variants, 
 *  we guarantee thread safety via synchronization on the 
 *  variant itself. This is described in further detail in 
 *  the Box class
 *  
 * synchronization is used to prevent interleaving of 
 * mutations on a given set of Boxes in a board. 
 * If a box is getting mutated by a call invoked on 
 * this Board, only that method can mutate objects in
 * the board for the duration of that call since it 
 * maintains a lock on that board 
 *  
 * RI: The Board may not contain a box with a bomb count greater than the number of bombs in its neighbors.
 * Additionally, there must be no more boxes in a row than columns in the board, and vice versa
 * @author nathaliehuynh
 *
 */

public class Board {

    private static final String BOOM_MESSAGE = "BOOM!";
    private Box[][] board; 
    private int rowSize;
    private int columnSize;
    private final double BOMB_PROBABILITY = 0.25;
    private final int MAX_NEIGHBORS = 8;

    /**
     * Board is a 2-dimensional array which represents a minesweeper board
     * The first element in Board Board[0][0] represents the top-left corner
     * @param rowSize - the number of columns in the Board
     * @param columnSize - the number of rows in the Board
     */

    public Board(int rowSize, int columnSize){
        this.rowSize = rowSize;
        this.columnSize = columnSize;
        this.board = makeBoard(rowSize, columnSize);

        checkRep();
    }

    /**
     * Board is a 2-dimensional array which represents a minesweeper board
     * The first element in Board Board[0][0] represents the top-left corner
     * @param boardFile - a file which contains information about the Board
     * 					The first line specifies the board size where 
     * 					-the first number corresponds to #columns (i.e. the size of the rows)
     * 					-and the second corresponds to #rows (i.e. the size of the columns)
     * 					-0's indicate no bombs
     * 					-1's indicate bombs
     * 					-rows are delineated by new lines 
     * 					-and spots on the board
     * 					are delineated by a " " (a space)
     * @param debug - whether or not the debug state is activated
     * @throws IOException  if file cannot be read properly
     */

    public Board(File boardFile) throws IOException{
        this.board = makeBoard(boardFile);
        this.rowSize = board.length;
        this.columnSize = board[0].length;
        checkRep();
    }

    /**
     * Creates a board such that every square has a BOMB_PROBABILITY
     * chance of containing a bomb
     * @param rowSize - the number of columns in the board
     * @param columnSize - the number of rows in the board
     * @return a 2D array representing the minesweeper board game
     */
    private Box[][] makeBoard(int rowSize, int columnSize) {
        Box[][] constructedBoard = new Box[rowSize][columnSize];
        Random random = new Random();
        for (int y = 0; y < columnSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                if (random.nextInt((int) (1/BOMB_PROBABILITY)) == 0) { //create a bomb for every 1 in 1/BOMB_PROBABILITY squares
                    constructedBoard[x][y] = new Box(x, y, true);
                } else {
                    constructedBoard[x][y] = new Box(x, y, false);
                }	
            }
        }

        this.board = constructedBoard;
        updateNeighboringBombs();

        return constructedBoard;
    }

    /**
     * Creates a board given an input file
     * @param boardFile a text file used to create a minesweeper board
     * @return a 2D array representing the minesweeper board game
     * @throws IOException
     * @throws RuntimeException if the input file is improperly formatted
     */
    private Box[][] makeBoard(File boardFile) throws IOException {
        ArrayList<String> fileLines = readFile(boardFile);

        String firstLine = fileLines.get(0);
        List<String> boardLines = fileLines.subList(1, fileLines.size());
        firstLine = firstLine.replace("\\n", "");
        if (firstLine.matches("[0-9]+\\s[0-9]+")) {
            String[] dimensions = firstLine.split("\\s");
            rowSize = Integer.parseInt(dimensions[0]);
            columnSize = Integer.parseInt(dimensions[1]);

        } else {
            throw new RuntimeException("The first line of the input file is not properly formatted.");
        }
        Box[][] constructedBoard = new Box[rowSize][columnSize];
        createBoardFromExtractedFileLines(boardLines, constructedBoard);

        this.board = constructedBoard;
        updateNeighboringBombs();
        return constructedBoard;
    }

    /**
     * Attempts to parse a file into lines to be converted to a Minesweeper board
     * @param boardFile a text file used to create a minesweeper board
     * @return a list of strings read out from boardFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    private ArrayList<String> readFile(File boardFile) throws FileNotFoundException, IOException {
        BufferedReader reader = new BufferedReader(new FileReader(boardFile));
        ArrayList<String> boardLines = new ArrayList<String>();

        while (reader.ready()){
            boardLines.add(reader.readLine());
        }
        reader.close();
        return boardLines;
    }

    /**
     * Creates the board given a List of properly formatted lines
     * @param boardLines a list of strings read out from boardFile
     */
    private void createBoardFromExtractedFileLines(List<String> boardLines, Box[][] constructedBoard) {
        int rowLocX = 0;
        int columnLocY = 0;
        for (String boardLine : boardLines) {
            boardLine = boardLine.replace("\\n", "");
            String[] boxes = boardLine.split("\\s");
            for (String box : boxes) {
                if (box.equals("0")) {
                    constructedBoard[rowLocX][columnLocY] = new Box(rowLocX, columnLocY, false);
                } else if (box.equals("1")){
                    constructedBoard[rowLocX][columnLocY] = new Box(rowLocX, columnLocY, true);
                } else {
                    System.out.println(box);
                }
                rowLocX++;
            }
            columnLocY++;
            rowLocX = 0;
        }
    }

    /**
     * updates all the boxes in board to reflect the correct
     * neighbors bomb count 
     * @param inputBoard - a 2D array of boxes
     */
    public synchronized void updateNeighboringBombs() {
        for (int y = 0; y < columnSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                List<Box> neighbors = getNeighborBoxes(x, y);
                int numBombs = 0;
                for (Box neighbor : neighbors) {
                    if (neighbor.bombStatus()) {
                        numBombs++;
                    }
                }
                getBox(x, y).updateNumberOfNeighboringBombs(numBombs);
            }
        }
        checkRep();
    }

    /**
     * 
     * @param xLoc
     * @param yLoc
     * @return the box at this location
     */
    private Box getBox(int xLoc, int yLoc) {
        return this.board[xLoc][yLoc];
    }

    /**
     * checks that the number of neighboring bombs in each Box of the board
     * is a number between 0 and 8
     */
    private void checkRep() {		
        assert(board.length == this.rowSize);
        assert(board[0].length == this.columnSize);
        for (int y = 0; y < columnSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                Box box = board[x][y];

                //CHECK REP ON LOCATION of each box
                assert(box.getXLoc() == x);
                assert(box.getYLoc() == y);

                //CHECK REP ON NEIGHBORING BOMB COUNTS
                Integer numberOfNeighboringBombs = box.getNumberOfNeighboringBombs();

                List<Box> neighbors = getNeighborBoxes(x, y);
                int actualNumBombs = 0;
                for (Box neighbor : neighbors) {
                    if (neighbor.bombStatus()) {
                        actualNumBombs++;
                    }
                }
                assert(numberOfNeighboringBombs==actualNumBombs);

            }
        }
    }

    /**
     * Processes an action at a specified location on the board
     * @param rowLocX - the y location of a box on the board
     * @param columnLocY - the y location of a box on the board
     * @param action an action to be processed
     * @return "BOOM!" if a bomb was detonated as a result of a dig, or 
     *     a string representation of the board otherwise
     */
    public synchronized String processAction(int rowLocX, int columnLocY, Action action){
        String result = "";
        if (rowLocX <rowSize && columnLocY <columnSize) { //process action only if coordinates are in board
            if (action.equals(Action.DIG)) {
                boolean bombDug = processDigAction(rowLocX, columnLocY);
                if (bombDug) {
                    result = BOOM_MESSAGE;
                } else {
                    result = this.toString();   
                }
            } else if (action.equals(Action.FLAG) || action.equals(Action.UNFLAG)) {
                processFlagAction(action, rowLocX, columnLocY); 
                result = this.toString();
            }
        }
        checkRep();
        return result;
    }

    /**
     * change the status of this to DUG and updates the resulting board
     * @param activeBox 
     * @return true if a bomb was dug as a result of this action
     */
    private boolean processDigAction(int rowLocX, int columnLocY) {
        boolean bombDug = false;
        Box activeBox = getBox(rowLocX, columnLocY);

        if (activeBox.bombStatus() && activeBox.getStatus().equals(State.UNTOUCHED)) {
            bombDug = true;
            activeBox.updateStatus(State.DUG);
            activeBox.updateBombStatus(false);
            //update bomb number
            updateNeighboringBombs();

            List<Integer[]> neighbors = getNeighborsCoordinates(rowLocX , columnLocY);
            if (activeBox.getNumberOfNeighboringBombs()==0) {
                for (Integer[] neighborCoord : neighbors) {

                    Integer xLoc = neighborCoord[0];
                    Integer yLoc = neighborCoord[1];
                    Box neighborBox = getBox(xLoc, yLoc);

                    if (!neighborBox.bombStatus()) {
                        processDigAction(xLoc, yLoc);
                    }
                }
            }

        } else if (!activeBox.bombStatus() && activeBox.getStatus().equals(State.UNTOUCHED)){
            bombDug = false;
            activeBox.updateStatus(State.DUG);
            //if (this.numberOfNeighboringBombs!=0)     don't do anything 
            if (activeBox.getNumberOfNeighboringBombs()==0) {
                List<Integer[]> neighbors = getNeighborsCoordinates(rowLocX , columnLocY);
                for (Integer[] neighborCoord : neighbors) {
                    Integer xLoc = neighborCoord[0];
                    Integer yLoc = neighborCoord[1];
                    processDigAction(xLoc, yLoc);
                }
                // invariant: calling this method recursively will not detonate a bomb 
                // because the recursion terminates when numberOfNeighboringBombs>0. 
                // This always occurs before this method can be recursively called 
                // on a box that has a bomb
            }
        }

        checkRep();
        return bombDug;

    }


    /**
     * given an action, determines if the box should
     * be flagged, deflagged, or left unchanged
     * @param action - an action denoting the action to be executed
     * @param columnLocY the y coordinate at which to perform the action
     * @param rowLocX the x coordinate at which to perform the action
     */
    private void processFlagAction(Action action, int rowLocX, int columnLocY) {
        Box activeBox = getBox(rowLocX, columnLocY);

        if (action.equals(Action.FLAG)) {
            if (activeBox.getStatus().equals(State.UNTOUCHED)) {
                activeBox.updateStatus(State.FLAGGED);
            }
        } else if (action.equals(Action.UNFLAG)) {
            if (activeBox.getStatus().equals(State.FLAGGED)) {
                activeBox.updateStatus(State.UNTOUCHED);
            }
        } 
        checkRep();
    }

    /**
     * gets the coordinates of the cells neighboring a cell at roxLocX
     * and columnLocY
     * @param rowLocX
     * @param columnLocY
     * @return the coordinates of the locations neighboring a location 
     * specified by rowLocX and columnLocY
     */
    private List<Integer[]> getNeighborsCoordinates(int rowLocX, int columnLocY) {
        List<Integer[]> neighborCoord = new LinkedList<Integer[]>();
        for (int y = -1; y <=1; y++) {
            for (int x = -1; x <= 1; x++) {
                if (!(x==0 && y==0)) {
                    int neighborX = rowLocX +x;
                    int neighborY = columnLocY + y;
                    if ((neighborX>=0  && neighborX < rowSize) && (neighborY>=0 && neighborY < columnSize)) {
                        Integer[] neighborArray = {neighborX, neighborY};
                        neighborCoord.add(neighborArray);
                    }
                }
            }
        }
        return neighborCoord;       
    }

    /**
     * Gets the neighbors of a Box located at rowLocX, columnLocY
     * @param rowLocX - the x location of a box
     * @param columnLocY - the y location of a box
     * @return a list of at most 8 neighbors surround the box at 
     * location rowLocX, columnLocY. The Box with input location is not
     * included in this list.
     */
    protected synchronized List<Box> getNeighborBoxes(int rowLocX, int columnLocY) {
        List<Box> neighbors = new LinkedList<Box>();
        List<Integer[]> neighborsCoords = getNeighborsCoordinates(rowLocX , columnLocY);

        for (Integer[] neighborCoord : neighborsCoords) {
            Integer xLoc = neighborCoord[0];
            Integer yLoc = neighborCoord[1];
            neighbors.add(getBox(xLoc, yLoc));

        }

        return neighbors;
    }

    @Override 
    public synchronized String toString() {
        String result = "";
        for (int y = 0; y < columnSize; y++) {
            for (int x = 0; x < rowSize; x++) {
                if (x==rowSize-1) {
                    result+=board[x][y].toString() + "\n";
                } else {
                    result+=board[x][y].toString() + " ";
                }
            }

        }
        checkRep();
        return result;
    }

    /**
     * 
     * @return whether or not this board should be run in debug mode
     */
    public boolean debug() {
        return this.debug();
    }

    /**
     * 
     * @return the number of columns in board
     */
    public String getColumns() {
        return Integer.valueOf(rowSize).toString();
    }

    /**
     * 
     * @return the number of rows in board
     */
    public String getRows() {
        return Integer.valueOf(columnSize).toString();
    }

    protected enum Action { 
        UNFLAG,
        FLAG,
        DIG
    }

}

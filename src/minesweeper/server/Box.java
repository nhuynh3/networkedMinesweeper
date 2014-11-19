package minesweeper.server;

import java.util.List;

import minesweeper.server.Board.Action;




/**
 * Thread safety argument:
 * This data type is made thread safe by synchronizing all
 * dig, flag, and status changes using a lock on the object
 * itself.
 */

/**
 * AF: A Box represents a square located at row i and column j of a Minesweeper game board
 * RI: i, j >=0
 * @author nathaliehuynh
 *
 */
public class Box {

    private boolean hasBomb;
    private int row;
    private int numberOfNeighboringBombs;
    private int column;
    private State status;


    /**
     * Creates a square in a Minesweeper game
     * located at row i and column j of a board
     * @param xRow - the x location of the box
     * @param yColumn - the y location of the box
     * @param hasBomb - whether or not this box contains a bomb
     * @param containingBoard - the board which this box is contained in
     */
    public Box(int xRow, int yColumn, boolean hasBomb) {
        this.hasBomb = hasBomb;
        this.row = xRow;
        this.column = yColumn;
        this.numberOfNeighboringBombs = 0;
        this.status = State.UNTOUCHED;
        checkRep();
    }

    private void checkRep() {
        assert(row>=0);
        assert(column>=0);
    }
    
    /**
     * 
     * @return the x location of this box
     */
    public synchronized int getXLoc() {
        return new Integer(this.row);
    }

    /**
     * 
     * @return the y location of this box
     */
    public synchronized int getYLoc() {
        return new Integer(this.column);
    }
    /**
     * 
     * @return the status of whether or not this box is flagged, unflagged or dug
     */
    public synchronized State getStatus() {
        return this.status;
    }

    /**
     * 
     * @return the number of neighboring bombs
     */
    public synchronized Integer getNumberOfNeighboringBombs() {
        return new Integer(this.numberOfNeighboringBombs);
    }
    /**
     * 
     * @return whether or not this contains a bomb
     */
    public synchronized boolean bombStatus() {
        //maintains a lock on this so other players cannot detonate bomb once this method is called
        if (this.hasBomb) {return true;} 
        return false;
    }

    /**
     * updates the number of bombs to be equal to bombs
     * @param bombs - the number of surrounding bombs
     */
    public synchronized void updateNumberOfNeighboringBombs(int bombs) {
        this.numberOfNeighboringBombs = bombs;
        checkRep();
    }
    
    /**
     * updates the status of the box to be state
     * @param bombs - the number of surrounding bombs
     */
    public synchronized void updateStatus(State state) {
        this.status = state;
        checkRep();
    }
    
    /**
     * updates the bomb status of the box to be bombStatus
     * @param bombs - the number of surrounding bombs
     */
    public synchronized void updateBombStatus(boolean bombStatus) {
        this.hasBomb= bombStatus;
        checkRep();
    }

    /**
     * returns the string representation of a box as displayed
     * on a Minesweeper board
     */
    @Override public synchronized String toString() {
        String statusRep = "-";

        if (status.equals(State.DUG) && numberOfNeighboringBombs!=0) {
            statusRep = Integer.toString(numberOfNeighboringBombs);
        } else if(status.equals(State.DUG) && numberOfNeighboringBombs==0) {
            statusRep = " ";
        } else if (status.equals(State.FLAGGED)) {
            statusRep = "F";
        }
        checkRep();
        return statusRep;

    }

    /**
     * states denoting the state of a box
     *
     */
    protected enum State { 
        UNTOUCHED,
        FLAGGED,
        DUG
    }

}

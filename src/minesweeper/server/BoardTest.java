package minesweeper.server;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import minesweeper.server.Board.Action;

import org.junit.Before;
import org.junit.Test;

public class BoardTest {


	/**
	 * Testing Partition:
	 * 	Construct a basic board  X
	 * 	Construct a board from a file X 
	 * 
	 * Digging:
	 * 		where there is no bomb and a neighboring bomb  X
	 * 					   no bomb and no neighboring bombs  
	 * 					   a flag X
	 * 					   a dug place  X
	 *                     outside of board X
	 * Flagging:
	 * 		Flagging an unflagged spot X
	 * 		flagging a flagged spot X
	 * 		Unflagging a flagged spot X
	 * 		Unflagging an unflagged spot X
	 * 
	 * 
	 */

	private File simpleBoardFile;
	private File largeBoardFile;
    private File manyBombBoardFile;
	
	
	@Before
	public void testSetup() {
		//basicBoard = 3x2 board w/ String "- -\n- -\n- -"
		simpleBoardFile = new File("simpleBoard.txt"); // one bomb at row 2, column 1
		largeBoardFile = new File("largeBoard.txt");  //use more advanced board to test recursive dig function
		manyBombBoardFile = new File("manyBombBoardTest.txt");
	}
	
	@Test
	public void createSimpleBoard() {
		Board simpleBoard = new Board(2, 3);
		String rows = simpleBoard.getRows();
		String columns = simpleBoard.getColumns();
		String simpleBoardString = "- -\n- -\n- -\n";
		assertEquals(simpleBoardString, simpleBoard.toString());
		assertEquals("3", rows);
		assertEquals("2", columns);
	}
	
	@Test
	public void createSimpleBoardFromFile() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		String simpleBoardString = "- -\n- -\n- -\n";
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void digSpotTwice() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(0, 2, Action.DIG);
		simpleBoard.processAction(0, 2, Action.DIG);
		String simpleBoardString = "- -\n- -\n1 -\n"; 
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void digSpotWithNeighborBomb() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(0, 2, Action.DIG);
		String simpleBoardString = "- -\n- -\n1 -\n"; 
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void digSpotWithBomb() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(1, 2, Action.DIG);
		String simpleBoardString = "   \n   \n   \n"; 
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	   @Test
	    public void digSpotOutsideBoard() throws IOException {
	        Board simpleBoard = new Board(simpleBoardFile);
	        simpleBoard.processAction(4, 4, Action.DIG);
	        String simpleBoardString = "- -\n- -\n- -\n"; 
	        assertEquals(simpleBoardString, simpleBoard.toString());
	    }
	
	@Test
	public void digSpotWithNoNeighborBomb() throws IOException {
		Board simpleBoard = new Board(largeBoardFile);
		simpleBoard.processAction(0, 0, Action.DIG);
		String simpleBoardString = "       \n1 1 2 1\n- - - -\n"; 
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void flagThenDigSpot() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(0, 2, Action.FLAG);
		simpleBoard.processAction(0, 2, Action.DIG);
		String simpleBoardString = "- -\n- -\nF -\n";
		assertEquals(simpleBoardString, simpleBoard.toString());
	}

	@Test
	public void flagThenFlagSpot() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(0, 2, Action.FLAG);
		simpleBoard.processAction(0, 2, Action.FLAG);
		String simpleBoardString = "- -\n- -\nF -\n"; 
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void flagThenUnflagSpot() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(0, 2, Action.FLAG);
		simpleBoard.processAction(0, 2, Action.UNFLAG);
		String simpleBoardString = "- -\n- -\n- -\n"; //no change
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void unflagThenUntouchedSpot() throws IOException {
		Board simpleBoard = new Board(simpleBoardFile);
		simpleBoard.processAction(0, 2, Action.UNFLAG);
	      simpleBoard.processAction(0, 2, Action.UNFLAG);
		String simpleBoardString = "- -\n- -\n- -\n"; //no change
		assertEquals(simpleBoardString, simpleBoard.toString());
	}
	
	@Test
	public void manyBombBoardFile() throws IOException {
	    Board manyBombBoard = new Board(manyBombBoardFile);
	    String mBBString ="- 1 -\n- - -\n- - -\n";
	    manyBombBoard.processAction(1, 0, Action.DIG);
	    assertEquals(mBBString, manyBombBoard.toString());
	}

}

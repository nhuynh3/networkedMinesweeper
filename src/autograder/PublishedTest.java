/* Copyright (c) 2007-2014 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package autograder;

import static autograder.PublishedTestUtil.connectToMinesweeperServer;
import static autograder.PublishedTestUtil.nextNonEmptyLine;
import static autograder.PublishedTestUtil.startMinesweeperServer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.junit.Test;

/**
 * Tests basic LOOK and DIG commands and X,Y directions.
 */
public class PublishedTest {

    @Test(timeout = 10000)
    public void publishedTest() throws IOException, InterruptedException {

        ThreadWithObituary serverThread = startMinesweeperServer(true, "board_file_5");

        Socket socket = connectToMinesweeperServer(serverThread);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        assertTrue("expected HELLO message", nextNonEmptyLine(in).startsWith("Welcome"));

        // This test ignores extraneous newlines, but *other tests may not*.

        out.println("look");
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));

        out.println("dig 3 1");
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - 1 - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));
        assertEquals("- - - - - - -", nextNonEmptyLine(in));

        out.println("dig 4 1");
        assertEquals("BOOM!", nextNonEmptyLine(in));

        out.println("look"); // debug mode is on
        assertEquals("             ", nextNonEmptyLine(in));
        assertEquals("             ", nextNonEmptyLine(in));
        assertEquals("             ", nextNonEmptyLine(in));
        assertEquals("             ", nextNonEmptyLine(in));
        assertEquals("             ", nextNonEmptyLine(in));
        assertEquals("1 1          ", nextNonEmptyLine(in));
        assertEquals("- 1          ", nextNonEmptyLine(in));

        out.println("bye");
        socket.close();
    }
}

/* Copyright (c) 2007-2014 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package autograder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import minesweeper.server.MinesweeperServer;

/**
 * Testing utilities. Designed for autograder environment, not recommended as a model.
 */
public class PublishedTestUtil {

    /**
     * Minesweeper server port.
     */
    static final int port = 4000 + (int)(Math.random() * 32768);
    static final String portStr = Integer.toString(port);

    /**
     * Return the absolute path of the specified file resource on the classpath.
     * @throws IOException if a valid path to an existing file cannot be returned
     */
    private static String getResourcePath(String fileName) throws IOException {
        URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
        if (url == null) {
            throw new IOException("Failed to locate resource " + fileName);
        }
        File file;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException urise) {
            throw new IOException("Invalid URL: " + urise);
        }
        String path = file.getAbsolutePath();
        if ( ! file.exists()) {
            throw new IOException("File " + path + " does not exist");
        }
        return path;
    }

    /**
     * Start a MinesweeperServer with the given debug mode and board file.
     */
    static ThreadWithObituary startMinesweeperServer(boolean debug, String boardFile) throws IOException {
        String boardPath = getResourcePath("autograder/resources/" + boardFile);
        return startMinesweeperServer(debug ? "--debug" : "--no-debug", "--port", portStr, "--file", boardPath);
    }

    /**
     * Start a MinesweeperServer with the given command-line arguments.
     */
    static ThreadWithObituary startMinesweeperServer(final String... args) {
        return new ThreadWithObituary(new Runnable() {
            public void run() {
                MinesweeperServer.main(args);
            }
        });
    }

    /**
     * Connect to a MinesweeperServer and return the connected socket.
     * @param server if not null, abort connection attempts if that thread dies
     */
    static Socket connectToMinesweeperServer(ThreadWithObituary server) throws IOException {
        Socket socket = null;
        final int MAX_ATTEMPTS = 20;
        int attempts = 0;
        do {
            try {
                socket = new Socket("127.0.0.1", port);
            } catch (ConnectException ce) {
                if (server != null && ! server.thread().isAlive()) {
                    throw new IllegalStateException("Server thread is not running", server.error());
                }
                if (++attempts > MAX_ATTEMPTS) {
                    throw new IOException("Exceeded max connection attempts", ce);
                }
                try { Thread.sleep(attempts * 10); } catch (InterruptedException ie) { };
            }
        } while (socket == null);
        socket.setSoTimeout(3000);
        return socket;
    }

    /**
     * Return the next non-empty line of input from the given stream, or null if
     * the end of the stream has been reached.
     */
    static String nextNonEmptyLine(BufferedReader in) throws IOException {
        while (true) {
            String ret = in.readLine();
            if (ret == null || ! ret.equals(""))
                return ret;
        }
    }
}

/** A thread and possibly the error that terminated it. */
class ThreadWithObituary {
    
    private final Thread thread;
    private Throwable error = null;
    
    /** Create and start a new thread. */
    ThreadWithObituary(Runnable runnable) {
        thread = new Thread(runnable);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            public void uncaughtException(Thread thread, Throwable error) {
                error.printStackTrace();
                ThreadWithObituary.this.error = error;
            }
        });
        thread.start();
    }
    
    /** Return the thread. */
    synchronized Thread thread() { return thread; }
    
    /** Return the error that terminated the thread, if any. */
    synchronized Throwable error() { return error; }
}

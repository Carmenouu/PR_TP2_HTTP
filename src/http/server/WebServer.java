///A Simple Web Server (WebServer.java)

package http.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.TreeMap;

/**
 * Example program from Chapter 1 Programming Spiders, Bots and Aggregators in
 * Java Copyright 2001 by Jeff Heaton
 * 
 * WebServer is a very simple web-server. Any request is responded with a very
 * simple web-page.
 * 
 * @author Jeff Heaton
 * @version 1.0
 */
public class WebServer {
	
	public final static String SERVER_NAME = "Carmen & Nel";
	public final static String SERVER_PUBLIC_ROOT = "doc";
	public final static String SERVER_BIN_ROOT = "bin";
	public final static String SERVER_SRC_ROOT = "src";
	public final static String SERVER_DEFAULT_PAGE = "index.html";

	public final static String HEADER_DELIMITER = ":";
	public final static String HEADER_CONTENT_LENGTH = "Content-Length";
	public final static String HEADER_CONTENT_TYPE = "Content-Type";
	public final static String HEADER_DATE = "Date";
	public final static String HEADER_LAST_MODIFIED = "Last-Modified";
	public final static String HEADER_SERVER = "Server";
	
	public final static int STATUS_OK = 200;
	public final static int STATUS_CREATED = 201;
	public final static int STATUS_UNAUTHORIZED = 401;
	public final static int STATUS_NOT_FOUND = 404;
	public final static int STATUS_INTERNAL_SERVER = 500;
	public final static TreeMap<Integer, String> STATUS = new TreeMap<>() {{
		put(STATUS_OK, "OK");
		put(STATUS_CREATED, "CREATED");
		put(STATUS_UNAUTHORIZED, "UNAUTHORIZED");
		put(STATUS_NOT_FOUND, "NOT FOUND");
		put(STATUS_INTERNAL_SERVER, "INTERNAL SERVER ERROR");
	}};
	
	/**
	 * WebServer constructor.
	 */
	protected static void start(int port) {
		
		ServerSocket listenSocket = null;
		
		try {
			
			listenSocket = new ServerSocket(port);
			System.out.println("Server ready on port " + port); 
			
			while(true) {
				Socket clientSocket = listenSocket.accept();
				new ClientThread(clientSocket).start();
			}
			
		} catch (IOException e) { System.err.println("Error in EchoServer:" + e); }
		
		try { listenSocket.close(); }
		catch(IOException e) { System.err.println("Failed to properly close the ServerSocket."); }
		
	}

	/**
	 * Start the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		
		if(args.length < 1) { System.err.println("Arg1 should be the server's running port."); System.exit(1); }
		
		start(Integer.parseInt(args[0]));
		
	}
}

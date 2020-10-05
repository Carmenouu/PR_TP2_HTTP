///A Simple Web Server (WebServer.java)

package http.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	
	private final static int STATUS_OK = 200;
	private final static int STATUS_UNAUTHORIZED = 401;
	private final static int STATUS_NOT_FOUND = 404;
	private final static int STATUS_INTERNAL_SERVER = 500;
	private final static TreeMap<Integer, String> STATUS = new TreeMap<>() {{
		put(STATUS_OK, "OK");
		put(STATUS_UNAUTHORIZED, "UNAUTHORIZED");
		put(STATUS_NOT_FOUND, "NOT FOUND");
		put(STATUS_INTERNAL_SERVER, "INTERNAL SERVER ERROR");
	}};
	
	protected static void sendResponse(String data, int statusCode, PrintWriter out) {
		
		// Send the response
		// Send the headers
		out.println("HTTP/1.0 " + statusCode + " " + STATUS.get(statusCode));
		out.println("Content-Type: text/html");
		out.println("Server: Bot");
		// this blank line signals the end of the headers
		out.println("");
		// Send the HTML page
		if(data != null) out.println(data);
		out.flush();
		
	}
	
	protected static void processGetRequest(String request, BufferedReader in, PrintWriter out) {
		
		String line = null;
		String data = "";
		File file = getRequestTarget(request);
		BufferedReader reader = null;

		cleanSocketInput(in);
		
		try { reader = new BufferedReader(new FileReader(file)); }
		catch(FileNotFoundException e) {
			System.err.println("File " + file + " does not exists.");
			sendResponse(null, STATUS_NOT_FOUND, out);
			
			return;
		}
		
		do {
			try { line = reader.readLine(); }
			catch(IOException e) { System.err.println("Failed to read next line."); break; }
			if(line != null) data += line;
		} while(line != null);
		
		try { reader.close(); }
		catch(IOException e) { System.err.println("Failed to properly close the BufferedReader."); }
		
		sendResponse(data, STATUS_OK, out);
		
	}
	
	protected static void processHeadRequest(String request, BufferedReader in, PrintWriter out) {
		
		String line = null;
		String data = "";
		File file = getRequestTarget(request);
		BufferedReader reader = null;

		cleanSocketInput(in);
		
		try { reader = new BufferedReader(new FileReader(file)); }
		catch(FileNotFoundException e) {
			System.err.println("File " + file + " does not exists.");
			sendResponse(null, STATUS_NOT_FOUND, out);
			
			return;
		}
		
		// Read until target's head starts
		do {
			try { line = reader.readLine(); }
			catch(IOException e) { System.err.println("Failed to read next line."); break; }
		} while(line != null && line.indexOf("<head>") == -1);

		// Read until target's head stops
		do {
			try { line = reader.readLine(); }
			catch(IOException e) { System.err.println("Failed to read next line."); break; }
			if(line != null && line.indexOf("</head>") == -1) data += line;
		} while(line != null && line.indexOf("</head>") == -1);
		
		try { reader.close(); }
		catch(IOException e) { System.err.println("Failed to properly close the BufferedReader."); }
		
		sendResponse(data, STATUS_OK, out);
		
	}
	
	protected static void cleanSocketInput(BufferedReader in) {

		String line = null;
		
		do { 
			try { line = in.readLine(); }
			catch(IOException e) { System.err.println("Connexion with client lost."); }
		} while(line != null && !line.equals(""));
		
	}
	
	protected static File getRequestTarget(String request) {
		
		String fileName = request.substring(request.indexOf(" ") + 1, request.indexOf(" ", request.indexOf(" ") + 1));
		
		return new File("doc" + fileName + (fileName.equals("/") ? "index.html" : ""));
		
	}
	
	/**
	 * WebServer constructor.
	 */
	protected static void start() {
		
		ServerSocket s;
		
		System.out.println("Webserver starting up on port 80");
		System.out.println("(press ctrl-c to exit)");
		try { s = new ServerSocket(80); }
		catch(Exception e) { System.out.println("Error: " + e); return; }
		
		System.out.println("Waiting for connection");
		while(true) {
			
			try {
				// wait for a connection
				Socket remote = s.accept();
				// remote is now the connected socket
				System.out.println("Connection, sending data.");
				BufferedReader in = new BufferedReader(new InputStreamReader(remote.getInputStream()));
				PrintWriter out = new PrintWriter(remote.getOutputStream());
				
				// read the data sent. We basically ignore it,
				// stop reading once a blank line is hit. This
				// blank line signals the end of the client HTTP
				// headers.
				String line = in.readLine();
				switch(line.substring(0, line.indexOf(" "))) {
				
					case "GET": processGetRequest(line, in, out); break;
//					case "POST": processPostRequest(line, in, out); break;
					case "HEAD": processHeadRequest(line, in, out); break;
//					case "PUT": processPutRequest(line, in, out); break;
//					case "DELETE": processDeleteRequest(line, in, out); break;
					default: System.out.println("Could not read request " + line); break;
					
				}

				remote.close();
			} catch (Exception e) { System.out.println("Error: " + e); }
			
		}
		
	}

	/**
	 * Start the application.
	 * 
	 * @param args
	 *            Command line parameters are not used.
	 */
	public static void main(String args[]) {
		
		WebServer ws = new WebServer();
		
		ws.start();
		
	}
}

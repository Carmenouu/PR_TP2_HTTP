package http.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Nel Bouvier et Carmen Pr�vot
 * @version 1.0
 */
public class ClientThread extends Thread {
	
	private Socket socket;
	
	/**
	 * Creates a new instance of ClientThread.
	 * @param socket The client socket.
	 */
	ClientThread(Socket socket) { this.socket = socket; }
	
	/**
	 * Processes the HEAD request.
	 * @param request The request to process.
	 * @param in The input.
	 * @param out The output.
	 */
	protected static void processHeadRequest(String request, BufferedReader in, OutputStream out) {
		
		File file = getRequestTarget(request);
		
		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		
		sendResponse(out, file, WebServer.STATUS_OK, (Object)null);
		
	}
	
	
	/**
	 * Processes the PUT request.
	 * @param request The request to process.
	 * @param in The input.
	 * @param out The output.
	 */
	protected static void processPutRequest(String request, BufferedReader in, OutputStream out) {
		
		int statusCode = WebServer.STATUS_OK;
		File file = getRequestTarget(request);
		TreeMap<String, String> attributes = getRequestHeader(in);

		if(!file.exists()) {
			if(createFile(file)) { statusCode = WebServer.STATUS_CREATED; }
			else { sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return; }
		}

		if(!cleanFile(file)) { sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return; }		
		if(!writeInFile(file, getRequestBody(in, Integer.parseInt(attributes.get(WebServer.HEADER_CONTENT_LENGTH)))))
		{ sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return; }

		sendResponse(out, file, statusCode, (Object)null);

	}
	
	/**
	 * Processes the GET request.
	 * @param request The request to process.
	 * @param in The input.
	 * @param out The output.
	 */
	protected static void processGetRequest(String request, BufferedReader in, OutputStream out) {

		File file = getRequestTarget(request);
		
		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		
		sendResponse(out, file, WebServer.STATUS_OK);
		
	}
	
	/**
	 * Processes the POST request.
	 * @param request The request to process.
	 * @param in The input.
	 * @param out The output.
	 */
	protected static void processPostRequest(String request, BufferedReader in, OutputStream out) {

		TreeMap<String, Object> fileProperties = getRequestTargetPost(request);
		File file = (File)fileProperties.get("file");

		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		
		sendResponse(out, file, WebServer.STATUS_OK, runJavaRessource(fileProperties, getRequestParameters(request)));
		
	}
	
	/**
	 * Processes the DELETE request.
	 * @param request The request to process.
	 * @param in The input.
	 * @param out The output.
	 */
	protected static void processDeleteRequest(String request, BufferedReader in, OutputStream out) {
		
		File file = getRequestTarget(request);

		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		if(!file.delete()) { sendResponse(out, null, WebServer.STATUS_UNAUTHORIZED); return; }
		
		sendResponse(out, null, WebServer.STATUS_OK);
		
	}
	
	/**
	 * Get the file targeted by the request.
	 * @param request The request to process.
	 * @return The targeted file.
	 */
	protected static File getRequestTarget(String request) {
		
		String fileName = request.substring(request.indexOf(" ") + 1, request.indexOf("?") != -1 ? request.indexOf("?") : request.indexOf(" ", request.indexOf(" ") + 1));
		
		return new File(WebServer.SERVER_PUBLIC_ROOT + fileName + (fileName.equals("/") ? WebServer.SERVER_DEFAULT_PAGE : ""));

	}
	
	/**
	 * Get the file targeted by the POST request.
	 * @param request
	 * @return The file properties.
	 */
	protected static TreeMap<String, Object> getRequestTargetPost(String request) {
		
		TreeMap<String, Object> fileProperties = new TreeMap<>();
		String fileName = request.substring(request.indexOf(" ") + 1, request.indexOf("?") != -1 ? request.indexOf("?") : request.indexOf(" ", request.indexOf(" ") + 1));
		fileProperties.put("file", new File(WebServer.SERVER_SRC_ROOT + "/" + fileName));
		fileProperties.put("classPackage", fileName.substring(1, fileName.lastIndexOf(".")).replace("/", "."));
		
		if(!fileName.substring(fileName.lastIndexOf(".")).equals(".java")) { return null; }

		return fileProperties;
		
	}
	
	/**
	 * Get the parameters listed in the request.
	 * @param request
	 * @return The parameters.
	 */
	protected static TreeMap<String, String> getRequestParameters(String request) {
		
		TreeMap<String, String> parameters = new TreeMap<>();
		String stringParameters = request.indexOf("?") != -1 ? request.substring(request.indexOf("?") + 1, request.indexOf(" ", request.indexOf("?"))) : null;
		String[] param;
		
		if(stringParameters == null) { return null; }
		
		for(String parameter : stringParameters.split("&")) {
			param = parameter.split("=");
			parameters.put(param[0], param[1]);
		}
		
		return parameters;
		
	}
	
	/**
	 * Get the header of the request.
	 * @param in The input.
	 * @return The header.
	 */
	protected static TreeMap<String, String> getRequestHeader(BufferedReader in) {

		String line = null;
		int delimiterPos;
		TreeMap<String, String> results = new TreeMap<>();
		
		do {
			
			try { line = in.readLine(); }
			catch(IOException e) { System.err.println("Failed to read next line."); break; }
			
			if(line == null) { break; }
			
			if((delimiterPos = line.indexOf(":")) == -1) { continue; }
			
			results.put(line.substring(0, delimiterPos), line.substring(delimiterPos + 2));
			
		} while(!line.equals(""));
		
		return results;
		
	}
	
	/**
	 * Get the body of the request.
	 * @param in The input.
	 * @param contentLength The length of the request body.
	 * @return The body of the request, as a String.
	 */
	protected static String getRequestBody(BufferedReader in, int contentLength) {
		
		char[] requestBody = new char[contentLength];
		
		try{ in.read(requestBody, 0, contentLength); }
		catch(Exception e) { System.err.println("Failed to read the request's body."); }
		
		return new String(requestBody);
		
	}
	
	/**
	 * Creates a new file.
	 * @param file The file to create.
	 * @return The outcome of the creation ; if the file has been created, it is true, otherwise it is false.
	 */
	protected static boolean createFile(File file) {
		
		try { file.createNewFile(); }
		catch(IOException e) { System.err.println("Could not created file."); return false; }
		
		return true;
		
	}
	
	/**
	 * Cleans the file, removes all its content.
	 * @param file The file to clean.
	 * @return The outcome.
	 */
	protected static boolean cleanFile(File file) { return writeInFile(file, ""); }
	
	/**
	 * Cleans the file, removes all its content.
	 * @param file The file to clean.
	 * @return The outcome.
	 */
	protected static boolean writeInFile(File file, String data) {
		
		BufferedWriter writer;
		
		try {
			
			writer = new BufferedWriter(new FileWriter(file));
			
			writer.write(data);
			writer.flush();
			
			writer.close();
			
		} catch(IOException e) { System.err.println("Could not write in the file."); return false; }
		
		return true;
		
	}
	
	/**
	 * Reads the input stream content.
	 * @param stream The input stream.
	 * @return The content of the input stream.
	 */
	protected static String readStream(InputStream stream) {
		
		String line;
		String data = "";
		BufferedReader in;
		
		try {
			
			in = new BufferedReader(new InputStreamReader(stream));
			
	        while((line = in.readLine()) != null) { data += line; }
	        
			in.close();
			
		} catch(IOException e) { System.err.println("Could not read the stream."); return null; }
		
		return data;
		
	}
	
	/**
	 * Runs the java file described in the request.
	 * @param fileProperties The properties of the java file to execute.
	 * @param parameters The parameters for the execution.
	 * @return The output stream.
	 */
	protected static String runJavaRessource(TreeMap<String, Object> fileProperties, TreeMap<String, String> parameters) {

		String cmd = "cmd.exe /c java -cp " + WebServer.SERVER_BIN_ROOT + "; " + fileProperties.get("classPackage");
		String stream = null;

		for(Map.Entry<String, String> parameter : parameters.entrySet()) { cmd += " " + parameter.getKey() + " " + parameter.getValue(); }

		try { stream = readStream(Runtime.getRuntime().exec(cmd).getInputStream()); }
		catch(IOException e) { System.err.println("Could not execute the commande."); }
		
		return stream;	
		
	}
	
	/**
	 * Sends the response to the request to the server.
	 * @param out The output.
	 * @param file The file created before.
	 * @param statusCode The status of the web server.
	 * @param optionalArgs Optional arguments to send to the server.
	 */
	protected static void sendResponse(OutputStream out, File file, int statusCode, Object... optionalArgs) {
		
		PrintWriter outPrint = new PrintWriter(out);
		SimpleDateFormat formatter = new SimpleDateFormat("E, dd MM yyyy HH:mm:ss z");
		SimpleDateFormat readerFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
		
		outPrint.println("HTTP/1.0 " + statusCode + " " + WebServer.STATUS.get(statusCode));
		
		if(file != null && (optionalArgs.length == 0 || optionalArgs[0] == null)) {
			outPrint.println(WebServer.HEADER_CONTENT_LENGTH + WebServer.HEADER_DELIMITER + file.length());
			try { outPrint.println(WebServer.HEADER_CONTENT_TYPE + WebServer.HEADER_DELIMITER + " " + Files.probeContentType(file.toPath())); }
			catch(IOException e) { System.err.println("Could not get file's mime type."); }
		}
		
		if(optionalArgs.length != 0 && optionalArgs[0] != null)
		{ outPrint.println(WebServer.HEADER_CONTENT_LENGTH + WebServer.HEADER_DELIMITER + ((String)optionalArgs[0]).length()); }
		
		if(file != null) {
			try { outPrint.println(WebServer.HEADER_LAST_MODIFIED + WebServer.HEADER_DELIMITER + " " + formatter.format(readerFormatter.parse(Files.getAttribute(file.toPath(), "creationTime").toString()))); }
			catch(ParseException e) { System.err.println("Could not parse the file's path."); }
			catch(IOException e) { System.err.println("Could not get file's creation date."); }
		}
		
		outPrint.println(WebServer.HEADER_DATE + WebServer.HEADER_DELIMITER + " " + formatter.format(new Date(System.currentTimeMillis())));
		outPrint.println(WebServer.HEADER_SERVER + WebServer.HEADER_DELIMITER + " " + WebServer.SERVER_NAME);
		
		outPrint.println("");
		outPrint.flush();
		
		if(file != null && (optionalArgs.length == 0 || optionalArgs[0] == null)) {
			try {
				Files.copy(file.toPath(), out);
				out.flush();
			} catch(IOException e) { System.err.println("Couldn't send the response's body."); }
		}
		
		if(optionalArgs.length != 0 && optionalArgs[0] != null) {
			outPrint.println(optionalArgs[0]);
			outPrint.flush();
		}
		
	}
	
	/**
	* Starts the client thread.
	**/
	public void run() {
		
		String line;
		BufferedReader in;
		OutputStream out;
		
		try {
			
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = socket.getOutputStream();
			if((line = in.readLine()) == null) { return; };
			
			switch(line.substring(0, line.indexOf(" "))) {
			
				case "GET": processGetRequest(line, in, out); break;
				case "POST": processPostRequest(line, in, out); break;
				case "HEAD": processHeadRequest(line, in, out); break;
				case "PUT": processPutRequest(line, in, out); break;
				case "DELETE": processDeleteRequest(line, in, out); break;
				default: System.out.println("Could not read request " + line); break;
				
			}
			
		} catch(IOException e) { System.err.println("Internal Server Error"); }
		
	}

}

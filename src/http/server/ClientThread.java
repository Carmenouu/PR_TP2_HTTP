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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class ClientThread extends Thread {
	
	private Socket socket;
	
	ClientThread(Socket socket) { this.socket = socket; }
	
	protected static void processHeadRequest(String request, BufferedReader in, OutputStream out) {
		
		File file = getRequestTarget(request);
		
		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		
		sendResponse(out, file, WebServer.STATUS_OK, (Object)null);
		
	}
	
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
	
	protected static void processGetRequest(String request, BufferedReader in, OutputStream out) {

		File file = getRequestTarget(request);
		String fileExtension = getFileExtension(file);
		String data;
		
		if(!WebServer.RESSOURCES.contains(fileExtension)) { sendResponse(out, null, WebServer.STATUS_NOT_IMPLEMENTED); return; }
		
		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		
		if(WebServer.EXECUTABLES.containsKey(fileExtension)) {
			
			if((data = runRessource(file, getRequestParameters(request), out)) == null) { return; };
			
			sendResponse(out, file, WebServer.STATUS_OK, data);
			
			return;
			
		}
		
		sendResponse(out, file, WebServer.STATUS_OK);
		
	}
	
	protected static void processPostRequest(String request, BufferedReader in, OutputStream out) {

		File file = getRequestTarget(request);
		String fileExtension = getFileExtension(file);
		TreeMap<String, String> attributes = getRequestHeader(in);
		String requestBody = getRequestBody(in, Integer.parseInt(attributes.get(WebServer.HEADER_CONTENT_LENGTH)));
		String data;
		
		if(!WebServer.RESSOURCES.contains(fileExtension)) { sendResponse(out, null, WebServer.STATUS_NOT_IMPLEMENTED); return; }
		
		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		
		if(WebServer.EXECUTABLES.containsKey(fileExtension)) {
			
			if((data = runRessource(file, getRequestParameters(requestBody), out)) == null) { return; };
			
			sendResponse(out, file, WebServer.STATUS_OK, data);
			
			return;
			
		}
		
		writeInFile(file, requestBody);
		sendResponse(out, file, WebServer.STATUS_OK);
		
	}
	
	protected static void processDeleteRequest(String request, BufferedReader in, OutputStream out) {
		
		File file = getRequestTarget(request);

		if(!file.exists()) { sendResponse(out, null, WebServer.STATUS_NOT_FOUND); return; }
		if(!file.delete()) { sendResponse(out, null, WebServer.STATUS_UNAUTHORIZED); return; }
		
		sendResponse(out, null, WebServer.STATUS_OK);
		
	}
	
	protected static File getRequestTarget(String request) {
		
		String fileName = request.substring(request.indexOf(" ") + 1, request.indexOf("?") != -1 ? request.indexOf("?") : request.indexOf(" ", request.indexOf(" ") + 1));

		if(!WebServer.EXECUTABLES.containsKey(getFileExtension(fileName))) { fileName = WebServer.SERVER_PUBLIC_ROOT + fileName + (fileName.equals("/") ? WebServer.SERVER_DEFAULT_PAGE : ""); }
		else { fileName = WebServer.SERVER_SRC_ROOT + fileName; }
	
		return new File(fileName);

	}
	
	protected static TreeMap<String, String> getRequestParameters(String request) {
		
		TreeMap<String, String> parameters = new TreeMap<>();
		String stringParameters = request.indexOf("?") != -1 ? request.substring(request.indexOf("?") + 1, request.indexOf(" ", request.indexOf("?"))) : request.indexOf("=") != -1 ? request : null;
		String[] param;
		
		if(stringParameters == null) { return null; }
		
		for(String parameter : stringParameters.split("&")) {
			param = parameter.split("=");
			parameters.put(param[0], param[1]);
		}
		
		return parameters;
		
	}
	
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
	
	protected static String getRequestBody(BufferedReader in, int contentLength) {
		
		char[] requestBody = new char[contentLength];
		
		try{ in.read(requestBody, 0, contentLength); }
		catch(Exception e) { System.err.println("Failed to read the request's body."); }
		
		return new String(requestBody);
		
	}
	
	protected static boolean createFile(File file) {
		
		try { file.createNewFile(); }
		catch(IOException e) { System.err.println("Could not created file."); return false; }
		
		return true;
		
	}
	
	protected static boolean cleanFile(File file) {
		
		BufferedWriter writer;
		
		try {
			
			writer = new BufferedWriter(new FileWriter(file));
			
			writer.write("");
			writer.flush();
			
			writer.close();
			
		} catch(IOException e) { System.err.println("Could not write in the file."); return false; }
		
		return true;
		
	}
	
	protected static boolean writeInFile(File file, String data) {
		
		BufferedWriter writer;
		
		try {
			
			writer = new BufferedWriter(new FileWriter(file, true));
			
			writer.append(data);
			writer.newLine();
			writer.flush();
			
			writer.close();
			
		} catch(IOException e) { System.err.println("Could not write in the file."); return false; }
		
		return true;
		
	}
	
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
	
	protected static String runRessource(File file, TreeMap<String, String> parameters, OutputStream out) {
		
		Method getRessourceMethod;
		String cmd;
		String stream = null;
		
		try { getRessourceMethod = ClientThread.class.getDeclaredMethod(WebServer.EXECUTABLES.get(getFileExtension(file)), File.class); }
		catch(NoSuchMethodException e) { sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return null; }
		catch(SecurityException e) { sendResponse(out, null, WebServer.STATUS_UNAUTHORIZED); return null; }
		
		try { cmd = (String)getRessourceMethod.invoke(null, file); }
		catch(IllegalAccessException e) { sendResponse(out, null, WebServer.STATUS_UNAUTHORIZED); return null; }
		catch(IllegalArgumentException e) { sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return null; }
		catch(InvocationTargetException e) { sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return null; }

		for(Map.Entry<String, String> parameter : parameters.entrySet()) { cmd += " " + parameter.getKey() + " " + parameter.getValue(); }

		try { stream = readStream(Runtime.getRuntime().exec(cmd).getInputStream()); }
		catch(IOException e) { sendResponse(out, null, WebServer.STATUS_INTERNAL_SERVER); return null; }
		
		return stream;
		
	}
	
	protected static String getJavaRessourceCommand(File file) { return "cmd.exe /c java -cp " + WebServer.SERVER_BIN_ROOT + "; " + getJavaPackageName(file); }
	
	protected static String getJavasciptRessourceCommand(File file) { return "cmd.exe /c node " + file.getAbsolutePath().replace(WebServer.SERVER_SRC_ROOT, WebServer.SERVER_BIN_ROOT); }
	
	protected static String getFileExtension(String path) { return path.substring(path.lastIndexOf(".")); }
	
	protected static String getFileExtension(File file) { return getFileExtension(file.getAbsolutePath()); }
	
	protected static String getJavaPackageName(File file) {
		
		String filePath = file.getAbsolutePath();

		return filePath.substring(0, filePath.lastIndexOf(".")).substring(System.getProperty("user.dir").length() + WebServer.SERVER_BIN_ROOT.length() + 2).replace("\\", ".");
		
	}
	
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
				default: sendResponse(out, null, WebServer.STATUS_METHOD_NOT_ALLOWED); break;
				
			}
			
		} catch(IOException e) { System.err.println("Internal Server Error."); }
		
	}

}

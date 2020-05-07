package br.com.consero.aura.server;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class HttpConnection implements Runnable {

	
	static final File ROOT = new File(".");
	static final String INDEX_FILENAME = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "405.html";
	static final String INTERNAL_SERVER_ERROR = "500.html";
	
	private Socket connect;
	private BufferedReader socketIn = null;
	private PrintWriter socketOut = null;
	private BufferedOutputStream bufferOS = null;

	public HttpConnection(Socket serverConnect) {
		this.connect = serverConnect;
	}

	public void run() {
		
		try
		{
			socketIn = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			socketOut =  new PrintWriter(connect.getOutputStream());
			bufferOS = new BufferedOutputStream(connect.getOutputStream());
			
			String input = socketIn.readLine();
			StringTokenizer parse = new StringTokenizer(input);
			String method = parse.nextToken().toUpperCase();
			
			String fileRequested = 
					parse.nextToken().toLowerCase();
			
			if (method.equals("GET")) {
				doGet(fileRequested);
				
			} else {
				sendFile(METHOD_NOT_SUPPORTED, "405 - Method not supported");
			}
			
		}
		catch (FileNotFoundException fnfe) {
			try {
				sendFile(FILE_NOT_FOUND, "404 File Not Found");
			} catch (IOException ioe) {
				System.err.println("File not found exception : " + ioe.getMessage());
			}
			
		} catch (Exception e) {
			try {
				sendFile(INTERNAL_SERVER_ERROR, "500 Internal Server Error");
			} catch (IOException ioe) {
				System.err.println("Internal Server Error : " + ioe.toString());
				ioe.printStackTrace();
			}
		} finally {
			try {
				socketIn.close();
				socketOut.close();
				bufferOS.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 
			
		}
	}

	private void doGet(String fileRequested) throws IOException {
		if (fileRequested.endsWith("/")) {
			fileRequested += INDEX_FILENAME;
		}
		
		sendFile(fileRequested, "200 - OK");
	}
	
	private void sendFile(String fileRequested, String responseStatus) throws IOException {
		File file = new File(ROOT, fileRequested);
		int fileLength = (int) file.length();
		String contentType = getContentType(fileRequested);
		byte[] fileData = readFileData(file, fileLength);
		
		socketOut.println(getResponseHeader(
				responseStatus, 
				contentType, 
				Integer.toString(fileLength)));

		socketOut.flush();
		
		bufferOS.write(fileData, 0, fileLength);
		bufferOS.flush();
	}
	
	private String getResponseHeader(String httpCode, String contentType, String fileLength )
	{
		StringBuilder response = new StringBuilder();
		response.append("HTTP/1.1 ");
		response.append(httpCode);
		response.append("\n");
		response.append("Server: Aura HTTP Server");
		response.append("\n");
		response.append("Date: " + new Date());
		response.append("\n");
		response.append("Content-type: " + contentType);
		response.append("\n");
		response.append("Content-length: " + fileLength);
		response.append("\n");
		
		return response.toString();
	}
	
	private byte[] readFileData(File file, int fileLength) throws IOException {
		FileInputStream fileIn = null;
		byte[] fileData = new byte[fileLength];
		
		try {
			fileIn = new FileInputStream(file);
			fileIn.read(fileData);
		} finally {
			if (fileIn != null) 
				fileIn.close();
		}
		
		return fileData;
	}
	
	private String getContentType(String fileRequested) {
		if (fileRequested.endsWith(".htm")  ||  fileRequested.endsWith(".html"))
			return "text/html";
		else
			return "text/plain";
	}
}

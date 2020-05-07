package br.com.consero.aura.server;

import java.io.IOException;
import java.net.ServerSocket;

public class HttpServer {

	private static final int PORT = 8080;

	public static void main(String[] args) {
		ServerSocket serverConnect = null;
		try {
			serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			
			while(true) {
				Thread thread = new Thread(new HttpConnection(serverConnect.accept()));
				thread.start();
			}
		} catch (IOException e) {
			System.err.println("Server Connection error : " + e.getMessage());
		} finally {
			if(serverConnect != null) {
				try {
					serverConnect.close();
				} catch (IOException e) {
					System.out.println("Failed to close Connection");
				}
			}
		}

	}
}

package org.betacraft.launcher;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import pl.betacraft.auth.MicrosoftAuth;

public class ListenThread extends Thread {
	private ServerSocket socket;
	public static boolean running = true;

	// Used for sleek transition between browser OAuth and the launcher
	// (without restarts)
	public ListenThread() {
		try {
			this.socket = new ServerSocket(11799);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	public void run() {
		try {
			while (running) {
				Socket sock = this.socket.accept();
				try {
					byte[] bytes = readInputStreamBytes(sock.getInputStream());
					String http = new String(bytes, "UTF-8");
					System.out.println(http);
					// msa
					if (http.startsWith("GET " + MicrosoftAuth.AUTH_URI)) {
						System.out.println("Received Microsoft login response");
						MicrosoftAuth msa = new MicrosoftAuth(null);
						msa.code = http.substring(http.indexOf("=")+1, http.indexOf(" HTTP/"));
						try {
							LoginPanel.continueMSA(msa);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					} else {
						sock.close();
						continue;
					}
					BufferedWriter bos = new BufferedWriter(
							new OutputStreamWriter(
									new BufferedOutputStream(sock.getOutputStream()), "UTF-8"));
					bos.write("HTTP/1.1 200 OK\r\n" +
							"Content-Type: text/html\r\n" +
							"\r\n\r\n");
					bos.write("<html> <head> <title>All done!</title> <script> window.onload = function() { window.close(); } </script> </head> <body> <center><h1>You can now close this tab.</h1></center> </html>");
					bos.flush();
					bos.close();
					sock.close();
				} catch (Throwable t) {}
			}
			// close on exit
			this.socket.close();
		} catch (Throwable t) {
			System.out.println("Couldn't bind to port 11799 for input data - there must be another instance of BC running!");
			t.printStackTrace();
		}
	}

	public static boolean isAvailable() {
		try (ServerSocket s = new ServerSocket(11799)) {
			s.close();
			return true;
		} catch (Throwable t) {
			return false;
		}
	}

	public static byte[] readInputStreamBytes(InputStream in) {
		try {
			byte[] buffer = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (in.available() > 0) {
				in.read(buffer);
				baos.write(buffer);
			}
			byte[] data = baos.toByteArray();
			return data;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}

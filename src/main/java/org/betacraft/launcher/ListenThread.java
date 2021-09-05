package org.betacraft.launcher;

import java.io.DataInputStream;
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
					DataInputStream bais = new DataInputStream(sock.getInputStream());
					String action = bais.readUTF();
					// msa
					if (action.startsWith(MicrosoftAuth.REDIRECT_URI)) {
						System.out.println("Received Microsoft login response");
						MicrosoftAuth msa = new MicrosoftAuth(null);
						msa.code = action.substring(action.indexOf("=")+1);
						try {
							LoginPanel.continueMSA(msa);
						} catch (Throwable t) {
							t.printStackTrace();
						}
					}
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
}

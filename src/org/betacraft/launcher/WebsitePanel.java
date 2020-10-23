package org.betacraft.launcher;

import java.awt.Color;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.MatteBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.betacraft.launcher.Window.Tab;

public class WebsitePanel extends JPanel {

	JScrollPane scrollPane = null;

	public static final HyperlinkListener EXTERNAL_HYPERLINK_LISTENER = new HyperlinkListener() {
		public void hyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
			if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				try {
					openLink(hyperlinkEvent.getURL().toURI());
				}
				catch (Exception ex) {
					ex.printStackTrace();
					Logger.printException(ex);
				}
			}
		}
	};

	public static final HyperlinkListener SERVERS_HYPERLINK_LISTENER = new HyperlinkListener() {
		public void hyperlinkUpdate(final HyperlinkEvent hyperlinkEvent) {
			if (hyperlinkEvent.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
				String u = hyperlinkEvent.getDescription();
				if (u.startsWith("join://")) { // If the URL isn't pointing to any http/s website
					// TODO
					//
					//
					//
					//
					//
					
					// MAKE THIS ACCEPT LOCAL JSONS PROTOCOLS!!!
					String raw = u.substring(7);
					String[] split = raw.split("/");
					String address = split[0];
					String mppass = split[1];
					String protocolVersion = split[2];
					String preferredVersion = split[3];
					ArrayList<Release> matches = new ArrayList<Release>();
					for (Release r : Release.versions) {
						if (r.getInfo().getProtocol().equals(protocolVersion)) {
							matches.add(r);
						}
					}
					new SelectServerVersion(matches, mppass, address, preferredVersion);
					return;
				} else {
					try {
						openLink(hyperlinkEvent.getURL().toURI());
					}
					catch (Exception ex) {
						ex.printStackTrace();
						Logger.printException(ex);
					}
				}
			}
		}
	};

	public WebsitePanel() {}

	public static void openLink(final URI uri) {
		try {
			final Object invoke = Class.forName("java.awt.Desktop").getMethod("getDesktop", (Class<?>[])new Class[0]).invoke(null, new Object[0]);
			invoke.getClass().getMethod("browse", URI.class).invoke(invoke, uri);
		}
		catch (Throwable t) {
			System.out.println("Failed to open link in a web browser: " + uri.toString());
		}
	}

	public JScrollPane getInstances() {

		final JEditorPane textPane = new JEditorPane();
		try {
			textPane.setEditable(false);
			textPane.setBackground(Color.BLACK);
			textPane.setContentType("text/html;charset=UTF-8");
			textPane.addHyperlinkListener(EXTERNAL_HYPERLINK_LISTENER);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		this.scrollPane = new JScrollPane(textPane);
		this.scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
		this.scrollPane.setWheelScrollingEnabled(true);
		//this.scrollPane.setBounds(20, 20, 750, 250);
		return this.scrollPane;
	}

	public JScrollPane getServers(boolean isConnection) {

		final JEditorPane textPane = new JEditorPane();
		try {
			label1:
			{
			textPane.setEditable(false);
			textPane.setBackground(Color.BLACK);
			textPane.setContentType("text/html;charset=UTF-8");
			String loading = Lang.TAB_SRV_LOADING;
			String list1 = "<html><body bgcolor=\"black\"><font color=\"#808080\"><br><br><br><br><br><center><h1>" + loading + "</h1></center></font></body></html>";
			textPane.setText(list1);
			textPane.addHyperlinkListener(SERVERS_HYPERLINK_LISTENER);

			if (!isConnection) {
				textPane.setText("<html><body bgcolor=\"black\"><font color=\"red\"><br><br><br><br><br><center><h1>" + Lang.TAB_SRV_FAILED + "</h1><br>no connection</center></font></body></html>");
				break label1;
			}
			new Thread() {
				public void run() {
					try {
						HttpURLConnection con = (HttpURLConnection) new URL("https://betacraft.pl/server.jsp").openConnection();
						con.setRequestMethod("POST");
						con.setDoInput(true);
						con.setDoOutput(true);
						con.setUseCaches(false);
						con.setConnectTimeout(5000);
						con.connect();

						DataOutputStream output = new DataOutputStream(con.getOutputStream());
						output.writeUTF(Launcher.getNickname());
						output.writeUTF(Launcher.getAuthToken(true));
						output.flush();
						output.close();
						InputStream instream = con.getInputStream();
						Scanner s = new Scanner(instream, "UTF-8");
						StringBuilder bob = new StringBuilder();
						while (s.hasNextLine()) {
							bob.append(s.nextLine());
						}
						s.close();
						instream.close();
						textPane.setText(bob.toString());
					} catch (SocketTimeoutException | UnknownHostException ex) {
						textPane.setContentType("text/html");
						textPane.setText("<html><body bgcolor=\"black\"><font color=\"red\"><br><br><br><br><br><center><h1>" + Lang.TAB_SRV_FAILED + "</h1><br>" + Lang.ERR_NO_CONNECTION + "</center></font></body></html>");
					} catch (Exception ex) {
						ex.printStackTrace();
						Logger.printException(ex);
						textPane.setContentType("text/html");
						textPane.setText("<html><body bgcolor=\"black\"><font color=\"red\"><br><br><br><br><br><center><h1>" + Lang.TAB_SRV_FAILED + "</h1><br>" + ex.toString() + "</center></font></body></html>");
					}
				}
			}.start();
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		this.scrollPane = new JScrollPane(textPane);
		//this.scrollPane.setBorder(new MatteBorder(1, 1, 1, 1, Color.BLACK));
		this.scrollPane.setWheelScrollingEnabled(true);
		//this.scrollPane.setBounds(20, 20, 750, 250);
		return this.scrollPane;
	}

	public JScrollPane getUpdateNews(boolean isConnection) {

		final JEditorPane textPane = new JEditorPane();
		try {
			label1:
			{
			textPane.setEditable(false);
			textPane.setBackground(Color.BLACK);
			textPane.setContentType("text/html;charset=UTF-8");
			String news = Lang.TAB_CL_LOADING;
			String news1 = "<html><body bgcolor=\"black\"><font color=\"#808080\"><br><br><br><br><br><center><h1>" + news + "</h1></center></font></body></html>";
			textPane.setText(news1);
			textPane.addHyperlinkListener(EXTERNAL_HYPERLINK_LISTENER);

			if (!isConnection) {
				textPane.setText("<html><body bgcolor=\"black\"><font color=\"red\"><br><br><br><br><br><center><h1>" + Lang.TAB_CL_FAILED + "</h1><br>no connection</center></font></body></html>");
				break label1;
			}
			new Thread() {
				public void run() {
					try {
						HttpURLConnection con = (HttpURLConnection) new URL("https://betacraft.pl/versions/changelog/" + Util.getProperty(BC.SETTINGS, "language") + ".html").openConnection();
						con.setDoInput(true);
						con.setDoOutput(false);
						con.setConnectTimeout(5000);
						con.connect();
						Scanner s = new Scanner(con.getInputStream(), "UTF-8");
						StringBuilder bob = new StringBuilder();
						while (s.hasNextLine()) {
							bob.append(s.nextLine());
						}
						textPane.setText(bob.toString());
						s.close();
						con.disconnect();
					} catch (SocketTimeoutException | UnknownHostException ex) {
						textPane.setContentType("text/html");
						textPane.setText("<html><body bgcolor=\"black\"><font color=\"red\"><br><br><br><br><br><center><h1>" + Lang.TAB_CL_FAILED + "</h1><br>" + Lang.ERR_NO_CONNECTION + "</center></font></body></html>");
					} catch (Throwable ex) {
						ex.printStackTrace();
						Logger.printException(ex);
						textPane.setContentType("text/html");
						textPane.setText("<html><body bgcolor=\"black\"><font color=\"red\"><br><br><br><br><br><center><h1>" + Lang.TAB_CL_FAILED + "</h1><br>" + ex.toString() + "</center></font></body></html>");
					}
				}
			}.start();
			//this.scrollPane.getViewport().getView().setBackground(Color.BLACK);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		this.scrollPane = new JScrollPane(textPane);
		this.scrollPane.setBorder(null);
		this.scrollPane.setWheelScrollingEnabled(true);
		//this.scrollPane.setBounds(20, 20, 750, 250);
		return this.scrollPane;
	}

	public JScrollPane getEmptyTabFor(Tab tab) {
		final JEditorPane textPane = new JEditorPane();
		try {
			textPane.setEditable(false);
			textPane.setBackground(Color.BLACK);
			textPane.setContentType("text/html;charset=UTF-8");
			String news = tab == Tab.CHANGELOG ? Lang.TAB_CL_LOADING : Lang.TAB_SRV_LOADING;
			String news1 = "<html><body bgcolor=\"black\"><font color=\"#808080\"><br><br><br><br><br><center><h1>" + news + "</h1></center></font></body></html>";
			textPane.setText(news1);
			textPane.addHyperlinkListener(EXTERNAL_HYPERLINK_LISTENER);
			//this.scrollPane.getViewport().getView().setBackground(Color.BLACK);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			Logger.printException(ex);
		}
		this.scrollPane = new JScrollPane(textPane);
		this.scrollPane.setBorder(null);
		this.scrollPane.setWheelScrollingEnabled(true);
		//this.scrollPane.setBounds(20, 20, 750, 250);
		return this.scrollPane;
	}
}

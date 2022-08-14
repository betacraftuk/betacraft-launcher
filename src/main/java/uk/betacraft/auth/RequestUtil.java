package uk.betacraft.auth;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import com.google.gson.Gson;

import uk.betacraft.util.WebData;

public class RequestUtil {
	private static boolean debug = false;

	public static String performPOSTRequest(Request req) {
		WebData data = performRawPOSTRequest(req);
		if (data.getData() != null) {
			try {
				String response = new String(data.getData(), "UTF-8");
				if (debug) System.out.println("INCOMING: " + response);
				return response;
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return null;
	}

	public static WebData performRawPOSTRequest(Request req) {
		HttpURLConnection con = null;
		try {
			URL url = new URL(req.REQUEST_URL);
			con = (HttpURLConnection) url.openConnection();

			for (String key : req.PROPERTIES.keySet()) {
				con.addRequestProperty(key, req.PROPERTIES.get(key));
			}
			con.setRequestMethod("POST");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);

			// Send POST
			DataOutputStream out = new DataOutputStream(con.getOutputStream());
			if (req.POST_DATA == null) {
				Gson gson = new Gson();
				String s = gson.toJson(req);
				if (debug) System.out.println("OUTGOING: " + s);
				out.write(s.getBytes("UTF-8"));
			} else {
				if (debug) System.out.println("OUTGOING: " + req.POST_DATA);
				out.write(req.POST_DATA.getBytes("UTF-8"));
			}
			out.flush();
			out.close();

			// Read response
			int http = con.getResponseCode();
			byte[] data = null;
			if (debug) System.out.println(http);

			if (http >= 400 && http < 600) {
				data = readInputStream(con.getErrorStream());
			} else {
				data = readInputStream(con.getInputStream());
			}

			return new WebData(data, http);
		} catch (Throwable t) {
			t.printStackTrace();
			return new WebData(null, -1);
		}
	}

	public static String performGETRequest(Request req) {
		WebData data = performRawGETRequest(req);
		if (data.getData() != null) {
			try {
				String response = new String(data.getData(), "UTF-8");
				if (debug) System.out.println("INCOMING: " + response);
				return response;
			} catch (Throwable t) {
				t.printStackTrace();
			}
		}
		return null;
	}

	public static WebData performRawGETRequest(Request req) {
		HttpURLConnection con = null;
		try {
			if (debug) System.out.println("OUTCOME TO: " + req.REQUEST_URL);
			URL url = new URL(req.REQUEST_URL);
			con = (HttpURLConnection) url.openConnection();

			con.setRequestMethod("GET");
			con.setReadTimeout(15000);
			con.setConnectTimeout(15000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setUseCaches(false);
			// i'm a browser C:
			con.addRequestProperty("User-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.95 Safari/537.36");
			for (String key : req.PROPERTIES.keySet()) {
				con.addRequestProperty(key, req.PROPERTIES.get(key));
			}

			// Read response
			int http = con.getResponseCode();
			byte[] data = null;
			if (debug) System.out.println(http);

			if (http >= 400 && http < 600) {
				data = readInputStream(con.getErrorStream());
			} else {
				data = readInputStream(con.getInputStream());
			}

			return new WebData(data, http);
		} catch (Throwable t) {
			t.printStackTrace();
			return new WebData(null, -1);
		}
	}

	public static byte[] readInputStream(InputStream in) {
		try {
			byte[] buffer = new byte[4096];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int count = in.available();
			while ((count = in.read(buffer)) > 0) {
				baos.write(buffer, 0, count);
			}
			byte[] data = baos.toByteArray();
			return data;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}

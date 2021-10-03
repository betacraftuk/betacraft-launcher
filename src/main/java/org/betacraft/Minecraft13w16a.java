package org.betacraft;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.betacraft.launcher.BC;

public class Minecraft13w16a extends Wrapper {

	public Minecraft13w16a(String user, String ver_prefix, String version, String sessionid, String mainFolder,
			Integer height, Integer width, Boolean RPC, String launchMethod, String server, String mppass, String USR,
			String VER, Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, null, USR, VER, img,
				addons);
	}

	String jars = "";

	public void loadJars() {
		
	}

	public void play() {
		try {
			ArrayList<String> params = new ArrayList<>();
			params.add("java");
			String nativesPath = BC.get() + "bin/natives";

			//params.add("-Djava.util.Arrays.useLegacyMergeSort=true");
			params.add("-Dorg.lwjgl.librarypath=" + nativesPath);
			params.add("-Dnet.java.games.input.librarypath=" + nativesPath);
			params.add("-Xmx1G");
			params.add("-cp");
			File[] list = new File(BC.get() + "bin/").listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String fileName) {
					return fileName.endsWith(".jar");
				}
			});
			StringBuilder bld = new StringBuilder();
			for (File f : list) {
				bld.append(f.getPath() + ":");
			}
			bld.append(BC.get() + "versions/" + this.version + ".jar");
			params.add(bld.toString());
			//params.add(BC.get() + "bin/lwjgl.jar:" + BC.get() + "bin/lwjgl_util.jar:" + BC.get() + "bin/jinput.jar:" + BC.get() + "versions/" + this.version + ".jar");
			params.add("net.minecraft.client.main.Main");
			params.add("--username");
			params.add(this.params.get("username"));
			params.add("--session");
			params.add(this.params.get("sessionid"));
			if (this.params.get("server") != null) {
				params.add("--server");
				params.add(this.params.get("server"));
				params.add("--port");
				params.add(this.params.get("port"));
			}
			params.add("--proxyHost");
			params.add("78.46.193.103");
			params.add("--proxyPort");
			params.add("80");
			params.add("--version");
			params.add(this.window_name);
			for (String param : params) {
				System.out.print(param + " ");
			}
			System.out.println();
			System.out.println(params);
			ProcessBuilder builder = new ProcessBuilder(params);
			new File(this.mainFolder).mkdirs();
			builder.directory(new File(this.mainFolder));

			Process process = builder.start();
			InputStream output = process.getInputStream(), err = process.getErrorStream();
			InputStreamReader isr_log = new InputStreamReader(output), isr_err = new InputStreamReader(err);
			BufferedReader br_log = new BufferedReader(isr_log), br_err = new BufferedReader(isr_err);
			String line1;
			while ((line1 = br_log.readLine()) != null) {
				System.out.println(line1);
			}
			//System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

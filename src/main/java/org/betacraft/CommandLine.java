package org.betacraft;

import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.betacraft.launcher.BC;

public class CommandLine extends Wrapper {

	public CommandLine(String user, String ver_prefix, String version, String sessionid, String mainFolder,
			Integer height, Integer width, Boolean RPC, String launchMethod, String server, String mppass, String USR,
			String VER, Image img, ArrayList addons) {
		super(user, ver_prefix, version, sessionid, mainFolder, height, width, RPC, launchMethod, server, mppass, null, USR, VER, img,
				addons);
	}

	@Override
	public void play() {
		try {
			ArrayList<String> params = new ArrayList<>();
			params.add("java");
			String nativesPath = BC.get() + "bin/natives";

			params.add("-Djava.util.Arrays.useLegacyMergeSort=true");
			params.add("-Dorg.lwjgl.librarypath=" + nativesPath);
			params.add("-Dnet.java.games.input.librarypath=" + nativesPath);
			params.add("-cp");
			params.add(BC.get() + "bin/lwjgl.jar:" + BC.get() + "bin/lwjgl_util.jar:" + BC.get() + "bin/jinput.jar:" + BC.get() + "versions/" + this.version + ".jar");
			params.add("net.minecraft.client.Minecraft");
			params.add(this.params.get("username"));
			params.add(this.params.get("sessionid"));
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
			System.exit(0);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}

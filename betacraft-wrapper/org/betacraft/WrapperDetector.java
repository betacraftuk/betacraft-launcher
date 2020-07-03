package org.betacraft;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class WrapperDetector {

	protected static String[][] classRules = new String[][] {
		{"rd",
			"com.mojang.rubydung.RubyDung"
		},
		{"mc",
			"com.mojang.minecraft.RubyDung",
			"com.mojang.minecraft.MinecraftApplet"
		},
		{"classic12a",
			"com.mojang.minecraft.MinecraftApplet",
			"com.mojang.minecraft.server.MinecraftServer",
			"!com.mojang.minecraft.User"
		},
		{"classic",
			"com.mojang.minecraft.MinecraftApplet",
			"!com.mojang.minecraft.net.NetworkPlayer"
		},
		{"classic15a",
			"com.mojang.minecraft.MinecraftApplet",
			"com.mojang.minecraft.net.NetworkPlayer",
			"!com.mojang.minecraft.e"
		},
		{"classicmp",
			"com.mojang.minecraft.MinecraftApplet",
			"com.mojang.minecraft.net.NetworkPlayer"
		},
		{"4k",
			"M"
		},
		{"indev",
			"net.minecraft.client.MinecraftApplet"
		},
		{"1.6",
			"net.minecraft.client.main.Main"
		}
	};

	public static String getLaunchMethod(String versionpath) {
		try {
			URLClassLoader loader = new URLClassLoader(new URL[] { new File(versionpath).toURI().toURL() });
			for (String[] arg0 : classRules) {
				String launchmethod = arg0[0];
				boolean passes = true;
				for (int i = 1; i < arg0.length; i++) {
					if (!(passes = testClass(arg0[i], loader))) {
						passes = false;
						break;
					}
				}
				if (passes) {
					System.out.println("Detected launch method: " + launchmethod);
					return launchmethod;
				}
			}
		} catch (Throwable ex) {
			return "custom";
		}
		return "custom";
	}

	public static boolean testClass(String path, ClassLoader loader) {
		boolean pass = true;
		if (path.startsWith("!")) {
			path = path.substring(1);
			pass = !pass;
		}
		try {
			Class.forName(path, false, loader);
		} catch (ClassNotFoundException ex) {
			return !pass;
		} catch (Throwable ex) {}
		return pass;
	}
}

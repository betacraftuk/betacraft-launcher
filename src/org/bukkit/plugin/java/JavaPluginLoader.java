package org.bukkit.plugin.java;

import java.applet.Applet;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import me.kazu.betacraftlaunucher.Window;

/**
 * Stolen from: https://github.com/Bukkit/Bukkit/blob/da29e0aa4dcb08c5c91157c0830851330af8b572/src/main/java/org/bukkit/plugin/java/JavaPluginLoader.java
 */
public class JavaPluginLoader {
    protected final Pattern[] fileFilters = new Pattern[] {
        Pattern.compile("\\.jar$"),
    };
    protected final Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
    protected final Map<String, PluginClassLoader> loaders = new HashMap<String, PluginClassLoader>();

    public JavaPluginLoader() {}

    public Applet loadPlugin(File file) throws FileNotFoundException {
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("%s does not exist", file.getPath()));
        }
        Applet result = null;

        PluginClassLoader loader = null;

        try {
            URL[] urls = new URL[1];

            urls[0] = file.toURI().toURL();
            loader = new PluginClassLoader(this, urls, getClass().getClassLoader());
            Class<?> jarClass = Class.forName("net.minecraft.client.MinecraftApplet", true, loader);
            Class<? extends Applet> plugin = jarClass.asSubclass(Applet.class);

            Constructor<? extends Applet> constructor = plugin.getConstructor();

            result = constructor.newInstance();

            result.init();
        } catch (Throwable ex) {
            System.out.println("NIE DZIA!!!");
            ex.printStackTrace();
        }

        loaders.put(Window.chosen_version, (PluginClassLoader) loader);

        return result;
    }

    public Class<?> getClassByName(final String name) {
        Class<?> cachedClass = classes.get(name);

        if (cachedClass != null) {
            return cachedClass;
        } else {
            for (String current : loaders.keySet()) {
                PluginClassLoader loader = loaders.get(current);

                try {
                    cachedClass = loader.findClass(name, false);
                } catch (ClassNotFoundException cnfe) {}
                if (cachedClass != null) {
                    return cachedClass;
                }
            }
        }
        return null;
    }

    public void setClass(final String name, final Class<?> clazz) {
        if (!classes.containsKey(name)) {
            classes.put(name, clazz);
        }
    }
}
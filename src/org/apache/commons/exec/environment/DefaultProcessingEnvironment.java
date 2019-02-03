/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.exec.environment;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.OS;

/**
 * Helper class to determine the environment variable
 * for the OS. Depending on the JDK the environment
 * variables can be either retrieved directly from the
 * JVM or requires starting a process to get them running
 * an OS command line.
 *
 * @version $Id: DefaultProcessingEnvironment.java 1636056 2014-11-01 21:12:52Z ggregory $
 */
public class DefaultProcessingEnvironment {

    /** the line separator of the system */
//    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /** the environment variables of the process */
    protected Map<String, String> procEnvironment;

    /**
     * Find the list of environment variables for this process.
     *
     * @return a map containing the environment variables
     * @throws IOException obtaining the environment variables failed
     */
    public synchronized Map<String, String> getProcEnvironment() throws IOException {

        if (procEnvironment == null) {
            procEnvironment = this.createProcEnvironment();
        }

        // create a copy of the map just in case that
        // anyone is going to modifiy it, e.g. removing
        // or setting an evironment variable
        final Map<String, String> copy = createEnvironmentMap();
        copy.putAll(procEnvironment);
        return copy;
    }

    /**
     * Find the list of environment variables for this process.
     *
     * @return a amp containing the environment variables
     * @throws IOException the operation failed
     */
    protected Map<String, String> createProcEnvironment() throws IOException {
        if (procEnvironment == null) {
            final Map<String, String> env = System.getenv();
            procEnvironment = createEnvironmentMap();
            procEnvironment.putAll(env);
        }

// No longer needed
//        if (procEnvironment == null) {
//            procEnvironment = createEnvironmentMap();
//            final BufferedReader in = runProcEnvCommand();
//
//            String var = null;
//            String line;
//            while ((line = in.readLine()) != null) {
//                if (line.indexOf('=') == -1) {
//                    // Chunk part of previous env var (UNIX env vars can
//                    // contain embedded new lines).
//                    if (var == null) {
//                        var = LINE_SEPARATOR + line;
//                    } else {
//                        var += LINE_SEPARATOR + line;
//                    }
//                } else {
//                    // New env var...append the previous one if we have it.
//                    if (var != null) {
//                        EnvironmentUtils.addVariableToEnvironment(procEnvironment, var);
//                    }
//                    var = line;
//                }
//            }
//            // Since we "look ahead" before adding, there's one last env var.
//            if (var != null) {
//                EnvironmentUtils.addVariableToEnvironment(procEnvironment, var);
//            }
//        }
        return procEnvironment;
    }

    /**
     * Start a process to list the environment variables.
     *
     * @return a reader containing the output of the process
     * @throws IOException starting the process failed
     * @deprecated No longer needed
     */
    @Deprecated
    protected BufferedReader runProcEnvCommand() throws IOException {
//        final ByteArrayOutputStream out = new ByteArrayOutputStream();
//        final Executor exe = new DefaultExecutor();
//        exe.setStreamHandler(new PumpStreamHandler(out));
//        // ignore the exit value - Just try to use what we got
//        exe.execute(getProcEnvCommand());
//        return new BufferedReader(new StringReader(toString(out)));
        return null;
    }

    /**
     * Determine the OS specific command line to get a list of environment
     * variables.
     *
     * @return the command line
     * @deprecated No longer needed
     */
    @Deprecated
    protected CommandLine getProcEnvCommand() {
//        String executable;
//        String[] arguments = null;
//        if (OS.isFamilyOS2()) {
//            // OS/2 - use same mechanism as Windows 2000
//            executable = "cmd";
//
//            arguments = new String[] {"/c", "set"};
//        } else if (OS.isFamilyWindows()) {
//            // Determine if we're running under XP/2000/NT or 98/95
//            if (OS.isFamilyWin9x()) {
//                executable = "command.com";
//                // Windows 98/95
//            } else {
//                executable = "cmd";
//                // Windows XP/2000/NT/2003
//            }
//            arguments = new String[] {"/c", "set"};
//        } else if (OS.isFamilyZOS() || OS.isFamilyUnix()) {
//            // On most systems one could use: /bin/sh -c env
//
//            // Some systems have /bin/env, others /usr/bin/env, just try
//            if (new File("/bin/env").canRead()) {
//                executable = "/bin/env";
//            } else if (new File("/usr/bin/env").canRead()) {
//                executable = "/usr/bin/env";
//            } else {
//                // rely on PATH
//                executable = "env";
//            }
//        } else if (OS.isFamilyNetware() || OS.isFamilyOS400()) {
//            // rely on PATH
//            executable = "env";
//        } else {
//            // MAC OS 9 and previous
//            // TODO: I have no idea how to get it, someone must fix it
//            executable = null;
//        }
        final CommandLine commandLine = null;
//        if (executable != null) {
//            commandLine = new CommandLine(executable);
//            commandLine.addArguments(arguments);
//        }
        return commandLine;
    }

//    /**
//     * ByteArrayOutputStream#toString doesn't seem to work reliably on OS/390,
//     * at least not the way we use it in the execution context.
//     *
//     * @param bos
//     *            the output stream that one wants to read
//     * @return the output stream as a string, read with special encodings in the
//     *         case of z/os and os/400
//     */
//    private String toString(final ByteArrayOutputStream bos) {
//        if (OS.isFamilyZOS()) {
//            try {
//                return bos.toString("Cp1047");
//            } catch (final java.io.UnsupportedEncodingException e) {
//                // noop default encoding used
//            }
//        } else if (OS.isFamilyOS400()) {
//            try {
//                return bos.toString("Cp500");
//            } catch (final java.io.UnsupportedEncodingException e) {
//                // noop default encoding used
//            }
//        }
//        return bos.toString();
//    }

    /**
     * Creates a map that obeys the casing rules of the current platform for key
     * lookup. E.g. on a Windows platform, the map keys will be
     * case-insensitive.
     *
     * @return The map for storage of environment variables, never
     *         {@code null}.
     */
    private Map<String, String> createEnvironmentMap() {
        if (OS.isFamilyWindows()) {
            return new TreeMap<String, String>(new Comparator<String>() {
                public int compare(final String key0, final String key1) {
                    return key0.compareToIgnoreCase(key1);
                }
            });
        }
        return new HashMap<String, String>();
    }

}

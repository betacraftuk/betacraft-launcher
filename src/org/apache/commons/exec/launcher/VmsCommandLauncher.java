/* 
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.apache.commons.exec.launcher;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.util.StringUtils;

/**
 * A command launcher for VMS that writes the command to a temporary DCL script
 * before launching commands. This is due to limitations of both the DCL
 * interpreter and the Java VM implementation.
 *
 * @version $Id: VmsCommandLauncher.java 1636056 2014-11-01 21:12:52Z ggregory $
 */
public class VmsCommandLauncher extends Java13CommandLauncher {

    /**
     * Launches the given command in a new process.
     */
    @Override
    public Process exec(final CommandLine cmd, final Map<String, String> env)
            throws IOException {
        final CommandLine vmsCmd = new CommandLine(
                createCommandFile(cmd, env).getPath()
        );

        return super.exec(vmsCmd, env);
    }

    /**
     * Launches the given command in a new process, in the given working
     * directory. Note that under Java 1.3.1, 1.4.0 and 1.4.1 on VMS this method
     * only works if {@code workingDir} is null or the logical
     * JAVA$FORK_SUPPORT_CHDIR needs to be set to TRUE.
     */
    @Override
    public Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException {
        final CommandLine vmsCmd = new CommandLine(
                createCommandFile(cmd, env).getPath()
        );

        return super.exec(vmsCmd, env, workingDir);
    }

    /** @see org.apache.commons.exec.launcher.CommandLauncher#isFailure(int) */
    @Override
    public boolean isFailure(final int exitValue) {
        // even exit value signals failure
        return exitValue % 2 == 0;
    }

    /*
     * Writes the command into a temporary DCL script and returns the
     * corresponding File object. The script will be deleted on exit.
     */
    private File createCommandFile(final CommandLine cmd, final Map<String, String> env)
            throws IOException {
        final File script = File.createTempFile("EXEC", ".TMP");
        script.deleteOnExit();
        PrintWriter out = null;
        try {
            out = new PrintWriter(new FileWriter(script.getAbsolutePath(),true));

            // add the environment as global symbols for the DCL script
            if (env != null) {
                final Set<Entry<String, String>> entries = env.entrySet();

                for (final Entry<String, String> entry : entries) {
                    out.print("$ ");
                    out.print(entry.getKey());
                    out.print(" == "); // define as global symbol
                    out.println('\"');
                    String value = entry.getValue();
                    // Any embedded " values need to be doubled
                    if (value.indexOf('\"') > 0) {
                        final StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < value.length(); i++) {
                            final char c = value.charAt(i);
                            if (c == '\"') {
                                sb.append('\"');
                            }
                            sb.append(c);
                        }
                        value=sb.toString();
                    }
                    out.print(value);
                    out.println('\"');
                }
            }

            final String command = cmd.getExecutable();
            if (cmd.isFile()) {// We assume it is it a script file
                out.print("$ @");
                // This is a bit crude, but seems to work
                final String parts[] = StringUtils.split(command,"/");
                out.print(parts[0]); // device
                out.print(":[");
                out.print(parts[1]); // top level directory
                final int lastPart = parts.length-1;
                for (int i=2; i< lastPart; i++) {
                    out.print(".");
                    out.print(parts[i]);
                }
                out.print("]");
                out.print(parts[lastPart]);
            } else {
                out.print("$ ");
                out.print(command);                
            }
            final String[] args = cmd.getArguments();
            for (final String arg : args) {
                out.println(" -");
                out.print(arg);
            }
            out.println();
        } finally {
            if (out != null) {
                out.close();
            }
        }
        return script;
    }
}

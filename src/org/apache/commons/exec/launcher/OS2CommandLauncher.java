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
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.CommandLine;

/**
 * A command launcher for OS/2 that uses 'cmd.exe' when launching commands in
 * directories other than the current working directory.
 * <p>
 * Unlike Windows NT and friends, OS/2's cd doesn't support the /d switch to
 * change drives and directories in one go.
 * </p>
 * Please not that this class is currently unused because the Java13CommandLauncher
 * is used for 0S/2
 *
 * @version $Id: OS2CommandLauncher.java 1557338 2014-01-11 10:34:22Z sebb $
 */
public class OS2CommandLauncher extends CommandLauncherProxy {

    public OS2CommandLauncher(final CommandLauncher launcher) {
        super(launcher);
    }

    /**
     * Launches the given command in a new process, in the given working
     * directory.
     * 
     * @param cmd
     *            the command line to execute as an array of strings
     * @param env
     *            the environment to set as an array of strings
     * @param workingDir
     *            working directory where the command should run
     * @throws IOException
     *             forwarded from the exec method of the command launcher
     */
    @Override
    public Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException {
        if (workingDir == null) {
            return exec(cmd, env);
        }

        final CommandLine newCmd = new CommandLine("cmd");
        newCmd.addArgument("/c");
        newCmd.addArguments(cmd.toStrings());

        return exec(newCmd, env);
    }
}

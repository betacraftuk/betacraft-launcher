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
 * Interface to shield the caller from the various platform-dependent
 * implementations.
 *
 * @version $Id: CommandLauncher.java 1636056 2014-11-01 21:12:52Z ggregory $
 */
public interface CommandLauncher {

    /**
     * Launches the given command in a new process.
     * 
     * @param cmd
     *            The command to execute
     * @param env
     *            The environment for the new process. If null, the environment
     *            of the current process is used.
     * 
     * @return the newly created process
     * @throws IOException
     *             if attempting to run a command in a specific directory
     */
    Process exec(final CommandLine cmd, final Map<String, String> env)
            throws IOException;

    /**
     * Launches the given command in a new process, in the given working
     * directory.
     * 
     * @param cmd
     *            The command to execute
     * @param env
     *            The environment for the new process. If null, the environment
     *            of the current process is used.
     * @param workingDir
     *            The directory to start the command in. If null, the current
     *            directory is used
     *
     * @return the newly created process
     * @throws IOException
     *             if trying to change directory
     */
    Process exec(final CommandLine cmd, final Map<String, String> env,
            final File workingDir) throws IOException;


    /**
     * Checks whether {@code exitValue} signals a failure on the current
     * system (OS specific).
     * <p>
     * <b>Note</b> that this method relies on the conventions of the OS, it
     * will return false results if the application you are running doesn't
     * follow these conventions. One notable exception is the Java VM provided
     * by HP for OpenVMS - it will return 0 if successful (like on any other
     * platform), but this signals a failure on OpenVMS. So if you execute a new
     * Java VM on OpenVMS, you cannot trust this method.
     * </p>
     *
     * @param exitValue the exit value (return code) to be checked
     * @return {@code true} if {@code exitValue} signals a failure
     */
    boolean isFailure(final int exitValue);
}

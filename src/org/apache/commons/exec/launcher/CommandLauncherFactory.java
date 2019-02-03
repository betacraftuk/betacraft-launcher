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

import org.apache.commons.exec.OS;

/**
 * Builds a command launcher for the OS and JVM we are running under.
 *
 * @version $Id: CommandLauncherFactory.java 1556869 2014-01-09 16:51:11Z britter $
 */
public final class CommandLauncherFactory {

    private CommandLauncherFactory() {
    }

    /**
     * Factory method to create an appropriate launcher.
     *
     * @return the command launcher
     */
    public static CommandLauncher createVMLauncher() {
        // Try using a JDK 1.3 launcher
        CommandLauncher launcher;

        if (OS.isFamilyOpenVms()) {
            launcher = new VmsCommandLauncher();
        } else {
            launcher = new Java13CommandLauncher();
        }

        return launcher;
    }
}

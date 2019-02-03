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

package org.apache.commons.exec;

import java.io.IOException;

/**
 * An exception indicating that the executing a subprocesses failed.
 *
 * @version $Id: ExecuteException.java 1636056 2014-11-01 21:12:52Z ggregory $
 */
public class ExecuteException extends IOException {

    /**
     * Comment for {@code serialVersionUID}.
     */
    private static final long serialVersionUID = 3256443620654331699L;

    /**
     * The underlying cause of this exception.
     */
    private final Throwable cause;

    /**
     * The exit value returned by the failed process
     */
    private final int exitValue;
    
    /**
     * Construct a new exception with the specified detail message.
     * 
     * @param message
     *            The detail message
     * @param exitValue The exit value
     */
    public ExecuteException(final String message, final int exitValue) {
        super(message + " (Exit value: " + exitValue + ")");
        this.cause = null;
        this.exitValue = exitValue;
    }

    /**
     * Construct a new exception with the specified detail message and cause.
     * 
     * @param message
     *            The detail message
     * @param exitValue The exit value
     * @param cause
     *            The underlying cause
     */
    public ExecuteException(final String message, final int exitValue, final Throwable cause) {
        super(message + " (Exit value: " + exitValue + ". Caused by " + cause + ")");
        this.cause = cause; // Two-argument version requires JDK 1.4 or later
        this.exitValue = exitValue;
    }

    /**
     * Return the underlying cause of this exception (if any).
     */
    @Override
    public Throwable getCause() {
        return this.cause;
    }

    /**
     * Gets the exit value returned by the failed process
     * @return The exit value
     */
    public int getExitValue() {
        return exitValue;
    }
}

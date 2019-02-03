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

/**
 * A default implementation of 'ExecuteResultHandler' used for asynchronous
 * process handling.
 *
 * @version $Id: DefaultExecuteResultHandler.java 1636057 2014-11-01 21:14:00Z ggregory $
 */
public class DefaultExecuteResultHandler implements ExecuteResultHandler {

    /** the interval polling the result */
    private static final int SLEEP_TIME_MS = 50;

    /** Keep track if the process is still running */
    private volatile boolean hasResult;

    /** The exit value of the finished process */
    private volatile int exitValue;

    /** Any offending exception */
    private volatile ExecuteException exception;

    /**
     * Constructor.
     */
    public DefaultExecuteResultHandler() {
        this.hasResult = false;
        this.exitValue = Executor.INVALID_EXITVALUE;
    }

    /**
     * @see org.apache.commons.exec.ExecuteResultHandler#onProcessComplete(int)
     */
    public void onProcessComplete(final int exitValue) {
        this.exitValue = exitValue;
        this.exception = null;
        this.hasResult = true;
    }

    /**
     * @see org.apache.commons.exec.ExecuteResultHandler#onProcessFailed(org.apache.commons.exec.ExecuteException)
     */
    public void onProcessFailed(final ExecuteException e) {
        this.exitValue = e.getExitValue();            
        this.exception = e;
        this.hasResult = true;
    }

    /**
     * Get the {@code exception} causing the process execution to fail.
     *
     * @return Returns the exception.
     * @throws IllegalStateException if the process has not exited yet
     */
    public ExecuteException getException() {

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exception;
    }

    /**
     * Get the {@code exitValue} of the process.
     *
     * @return Returns the exitValue.
     * @throws IllegalStateException if the process has not exited yet
     */
    public int getExitValue() {

        if (!hasResult) {
            throw new IllegalStateException("The process has not exited yet therefore no result is available ...");
        }

        return exitValue;
    }

    /**
     * Has the process exited and a result is available, i.e. exitCode or exception?
     *
     * @return true if a result of the execution is available
     */
    public boolean hasResult() {
        return hasResult;
    }

    /**
     * Causes the current thread to wait, if necessary, until the
     * process has terminated. This method returns immediately if
     * the process has already terminated. If the process has
     * not yet terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @exception  InterruptedException if the current thread is
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    public void waitFor() throws InterruptedException {

        while (!hasResult()) {
            Thread.sleep(SLEEP_TIME_MS);
        }
    }

    /**
     * Causes the current thread to wait, if necessary, until the
     * process has terminated. This method returns immediately if
     * the process has already terminated. If the process has
     * not yet terminated, the calling thread will be blocked until the
     * process exits.
     *
     * @param timeout the maximum time to wait in milliseconds
     * @exception  InterruptedException if the current thread is
     *             {@linkplain Thread#interrupt() interrupted} by another
     *             thread while it is waiting, then the wait is ended and
     *             an {@link InterruptedException} is thrown.
     */
    public void waitFor(final long timeout) throws InterruptedException {

        final long until = System.currentTimeMillis() + timeout;

        while (!hasResult() && System.currentTimeMillis() < until) {
            Thread.sleep(SLEEP_TIME_MS);
        }
    }
}
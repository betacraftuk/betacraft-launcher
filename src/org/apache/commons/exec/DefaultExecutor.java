/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.exec;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.exec.launcher.CommandLauncher;
import org.apache.commons.exec.launcher.CommandLauncherFactory;

/**
 * The default class to start a subprocess. The implementation
 * allows to
 * <ul>
 *  <li>set a current working directory for the subprocess</li>
 *  <li>provide a set of environment variables passed to the subprocess</li>
 *  <li>capture the subprocess output of stdout and stderr using an ExecuteStreamHandler</li>
 *  <li>kill long-running processes using an ExecuteWatchdog</li>
 *  <li>define a set of expected exit values</li>
 *  <li>terminate any started processes when the main process is terminating using a ProcessDestroyer</li>
 * </ul>
 *
 * The following example shows the basic usage:
 *
 * <pre>
 * Executor exec = new DefaultExecutor();
 * CommandLine cl = new CommandLine("ls -l");
 * int exitvalue = exec.execute(cl);
 * </pre>
 *
 * @version $Id: DefaultExecutor.java 1636056 2014-11-01 21:12:52Z ggregory $
 */
public class DefaultExecutor implements Executor {

    /** taking care of output and error stream */
    private ExecuteStreamHandler streamHandler;

    /** the working directory of the process */
    private File workingDirectory;

    /** monitoring of long running processes */
    private ExecuteWatchdog watchdog;

    /** the exit values considered to be successful */
    private int[] exitValues;

    /** launches the command in a new process */
    private final CommandLauncher launcher;

    /** optional cleanup of started processes */ 
    private ProcessDestroyer processDestroyer;

    /** worker thread for asynchronous execution */
    private Thread executorThread;

    /** the first exception being caught to be thrown to the caller */
    private IOException exceptionCaught;

    /**
     * Default constructor creating a default {@code PumpStreamHandler}
     * and sets the working directory of the subprocess to the current
     * working directory.
     *
     * The {@code PumpStreamHandler} pumps the output of the subprocess
     * into our {@code System.out} and {@code System.err} to avoid
     * into our {@code System.out} and {@code System.err} to avoid
     * a blocked or deadlocked subprocess (see{@link java.lang.Process Process}).
     */
    public DefaultExecutor() {
        this.streamHandler = new PumpStreamHandler();
        this.launcher = CommandLauncherFactory.createVMLauncher();
        this.exitValues = new int[0];
        this.workingDirectory = new File(".");
        this.exceptionCaught = null;
    }

    /**
     * @see org.apache.commons.exec.Executor#getStreamHandler()
     */
    public ExecuteStreamHandler getStreamHandler() {
        return streamHandler;
    }

    /**
     * @see org.apache.commons.exec.Executor#setStreamHandler(org.apache.commons.exec.ExecuteStreamHandler)
     */
    public void setStreamHandler(final ExecuteStreamHandler streamHandler) {
        this.streamHandler = streamHandler;
    }

    /**
     * @see org.apache.commons.exec.Executor#getWatchdog()
     */
    public ExecuteWatchdog getWatchdog() {
        return watchdog;
    }

    /**
     * @see org.apache.commons.exec.Executor#setWatchdog(org.apache.commons.exec.ExecuteWatchdog)
     */
    public void setWatchdog(final ExecuteWatchdog watchDog) {
        this.watchdog = watchDog;
    }

    /**
     * @see org.apache.commons.exec.Executor#getProcessDestroyer()
     */
    public ProcessDestroyer getProcessDestroyer() {
      return this.processDestroyer;
    }

    /**
     * @see org.apache.commons.exec.Executor#setProcessDestroyer(ProcessDestroyer)
     */
    public void setProcessDestroyer(final ProcessDestroyer processDestroyer) {
      this.processDestroyer = processDestroyer;
    }

    /**
     * @see org.apache.commons.exec.Executor#getWorkingDirectory()
     */
    public File getWorkingDirectory() {
        return workingDirectory;
    }

    /**
     * @see org.apache.commons.exec.Executor#setWorkingDirectory(java.io.File)
     */
    public void setWorkingDirectory(final File dir) {
        this.workingDirectory = dir;
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine)
     */
    public int execute(final CommandLine command) throws ExecuteException,
            IOException {
        return execute(command, (Map<String, String>) null);
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine, java.util.Map)
     */
    public int execute(final CommandLine command, final Map<String, String> environment)
            throws ExecuteException, IOException {

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }
        
        return executeInternal(command, environment, workingDirectory, streamHandler);

    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine,
     *      org.apache.commons.exec.ExecuteResultHandler)
     */
    public void execute(final CommandLine command, final ExecuteResultHandler handler)
            throws ExecuteException, IOException {
        execute(command, null, handler);
    }

    /**
     * @see org.apache.commons.exec.Executor#execute(CommandLine,
     *      java.util.Map, org.apache.commons.exec.ExecuteResultHandler)
     */
    public void execute(final CommandLine command, final Map<String, String> environment,
            final ExecuteResultHandler handler) throws ExecuteException, IOException {

        if (workingDirectory != null && !workingDirectory.exists()) {
            throw new IOException(workingDirectory + " doesn't exist.");
        }

        if (watchdog != null) {
            watchdog.setProcessNotStarted();
        }

        final Runnable runnable = new Runnable()
        {
            public void run()
            {
                int exitValue = Executor.INVALID_EXITVALUE;
                try {
                    exitValue = executeInternal(command, environment, workingDirectory, streamHandler);
                    handler.onProcessComplete(exitValue);
                } catch (final ExecuteException e) {
                    handler.onProcessFailed(e);
                } catch (final Exception e) {
                    handler.onProcessFailed(new ExecuteException("Execution failed", exitValue, e));
                }
            }
        };

        this.executorThread = createThread(runnable, "Exec Default Executor");
        getExecutorThread().start();
    }

    /** @see org.apache.commons.exec.Executor#setExitValue(int) */
    public void setExitValue(final int value) {
        this.setExitValues(new int[] {value});
    }


    /** @see org.apache.commons.exec.Executor#setExitValues(int[]) */
    public void setExitValues(final int[] values) {
        this.exitValues = values == null ? null : (int[]) values.clone();
    }

    /** @see org.apache.commons.exec.Executor#isFailure(int) */
    public boolean isFailure(final int exitValue) {

        if (this.exitValues == null) {
            return false;
        }
        else if (this.exitValues.length == 0) {
            return this.launcher.isFailure(exitValue);
        }
        else {
            for (final int exitValue2 : this.exitValues) {
                if (exitValue2 == exitValue) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Factory method to create a thread waiting for the result of an
     * asynchronous execution.
     *
     * @param runnable the runnable passed to the thread
     * @param name the name of the thread
     * @return the thread
     */
    protected Thread createThread(final Runnable runnable, final String name) {
        return new Thread(runnable, name);
    }

    /**
     * Creates a process that runs a command.
     *
     * @param command
     *            the command to run
     * @param env
     *            the environment for the command
     * @param dir
     *            the working directory for the command
     * @return the process started
     * @throws IOException
     *             forwarded from the particular launcher used
     */
    protected Process launch(final CommandLine command, final Map<String, String> env,
            final File dir) throws IOException {

        if (this.launcher == null) {
            throw new IllegalStateException("CommandLauncher can not be null");
        }

        if (dir != null && !dir.exists()) {
            throw new IOException(dir + " doesn't exist.");
        }
        return this.launcher.exec(command, env, dir);
    }

    /**
     * Get the worker thread being used for asynchronous execution.
     *
     * @return the worker thread
     */
    protected Thread getExecutorThread() {
        return executorThread;
    }
    
    /**
     * Close the streams belonging to the given Process.
     *
     * @param process the <CODE>Process</CODE>.
     */
    private void closeProcessStreams(final Process process) {

        try {
            process.getInputStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }

        try {
            process.getOutputStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }

        try {
            process.getErrorStream().close();
        }
        catch (final IOException e) {
            setExceptionCaught(e);
        }
    }

    /**
     * Execute an internal process. If the executing thread is interrupted while waiting for the
     * child process to return the child process will be killed.
     *
     * @param command the command to execute
     * @param environment the execution environment
     * @param dir the working directory
     * @param streams process the streams (in, out, err) of the process
     * @return the exit code of the process
     * @throws IOException executing the process failed
     */
    private int executeInternal(final CommandLine command, final Map<String, String> environment,
            final File dir, final ExecuteStreamHandler streams) throws IOException {

        setExceptionCaught(null);

        final Process process = this.launch(command, environment, dir);

        try {
            streams.setProcessInputStream(process.getOutputStream());
            streams.setProcessOutputStream(process.getInputStream());
            streams.setProcessErrorStream(process.getErrorStream());
        } catch (final IOException e) {
            process.destroy();
            throw e;
        }

        streams.start();

        try {

            // add the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().add(process);
            }

            // associate the watchdog with the newly created process
            if (watchdog != null) {
                watchdog.start(process);
            }

            int exitValue = Executor.INVALID_EXITVALUE;

            try {
                exitValue = process.waitFor();
            } catch (final InterruptedException e) {
                process.destroy();
            }
            finally {
                // see http://bugs.sun.com/view_bug.do?bug_id=6420270
                // see https://issues.apache.org/jira/browse/EXEC-46
                // Process.waitFor should clear interrupt status when throwing InterruptedException
                // but we have to do that manually
                Thread.interrupted();
            }            

            if (watchdog != null) {
                watchdog.stop();
            }

            try {
                streams.stop();
            }
            catch (final IOException e) {
                setExceptionCaught(e);
            }

            closeProcessStreams(process);

            if (getExceptionCaught() != null) {
                throw getExceptionCaught();
            }

            if (watchdog != null) {
                try {
                    watchdog.checkException();
                } catch (final IOException e) {
                    throw e;
                } catch (final Exception e) {
                    throw new IOException(e.getMessage());
                }
            }

            if (this.isFailure(exitValue)) {
                throw new ExecuteException("Process exited with an error: " + exitValue, exitValue);
            }

            return exitValue;
        } finally {
            // remove the process to the list of those to destroy if the VM exits
            if (this.getProcessDestroyer() != null) {
              this.getProcessDestroyer().remove(process);
            }
        }
    }

    /**
     * Keep track of the first IOException being thrown.
     *
     * @param e the IOException
     */
    private void setExceptionCaught(final IOException e) {
        if (this.exceptionCaught == null) {
            this.exceptionCaught = e;
        }
    }

    /**
     * Get the first IOException being thrown.
     *
     * @return the first IOException being caught
     */
    private IOException getExceptionCaught() {
        return this.exceptionCaught;
    }

}

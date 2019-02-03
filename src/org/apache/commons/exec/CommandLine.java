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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.commons.exec.util.StringUtils;

/**
 * CommandLine objects help handling command lines specifying processes to
 * execute. The class can be used to a command line by an application.
 *
 * @version $Id: CommandLine.java 1613094 2014-07-24 12:20:14Z ggregory $
 */
public class CommandLine {

    /**
     * The arguments of the command.
     */
    private final Vector<Argument> arguments = new Vector<Argument>();

    /**
     * The program to execute.
     */
    private final String executable;

    /**
     * A map of name value pairs used to expand command line arguments
     */
    private Map<String, ?> substitutionMap; // N.B. This can contain values other than Strings

    /**
     * Was a file being used to set the executable?
     */
    private final boolean isFile;

    /**
     * Create a command line from a string.
     * 
     * @param line the first element becomes the executable, the rest the arguments
     * @return the parsed command line
     * @throws IllegalArgumentException If line is null or all whitespace
     */
    public static CommandLine parse(final String line) {
        return parse(line, null);
    }

    /**
     * Create a command line from a string.
     *
     * @param line the first element becomes the executable, the rest the arguments
     * @param substitutionMap the name/value pairs used for substitution
     * @return the parsed command line
     * @throws IllegalArgumentException If line is null or all whitespace
     */
    public static CommandLine parse(final String line, final Map<String, ?> substitutionMap) {
                
        if (line == null) {
            throw new IllegalArgumentException("Command line can not be null");
        } else if (line.trim().length() == 0) {
            throw new IllegalArgumentException("Command line can not be empty");
        } else {
            final String[] tmp = translateCommandline(line);

            final CommandLine cl = new CommandLine(tmp[0]);
            cl.setSubstitutionMap(substitutionMap);
            for (int i = 1; i < tmp.length; i++) {
                cl.addArgument(tmp[i]);
            }

            return cl;
        }
    }

    /**
     * Create a command line without any arguments.
     *
     * @param executable the executable
     */
    public CommandLine(final String executable) {
        this.isFile=false;
        this.executable=toCleanExecutable(executable);
    }

    /**
     * Create a command line without any arguments.
     *
     * @param  executable the executable file
     */
    public CommandLine(final File executable) {
        this.isFile=true;
        this.executable=toCleanExecutable(executable.getAbsolutePath());
    }

    /**
     * Copy constructor.
     *
     * @param other the instance to copy
     */
    public CommandLine(final CommandLine other)
    {
        this.executable = other.getExecutable();
        this.isFile = other.isFile();
        this.arguments.addAll(other.arguments);

        if (other.getSubstitutionMap() != null)
        {
            final Map<String, Object> omap = new HashMap<String, Object>();
            this.substitutionMap = omap;
            final Iterator<String> iterator = other.substitutionMap.keySet().iterator();
            while (iterator.hasNext())
            {
                final String key = iterator.next();
                omap.put(key, other.getSubstitutionMap().get(key));
            }
        }
    }

    /**
     * Returns the executable.
     * 
     * @return The executable
     */
    public String getExecutable() {
        // Expand the executable and replace '/' and '\\' with the platform
        // specific file separator char. This is safe here since we know
        // that this is a platform specific command.
        return StringUtils.fixFileSeparatorChar(expandArgument(executable));
    }

    /**
     * Was a file being used to set the executable?
     *
     * @return true if a file was used for setting the executable 
     */
    public boolean isFile() {
        return isFile;
    }

    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     * 
     * @param addArguments An array of arguments
     * @return The command line itself
     */
    public CommandLine addArguments(final String[] addArguments) {
        return this.addArguments(addArguments, true);
    }

    /**
     * Add multiple arguments.
     *
     * @param addArguments An array of arguments
     * @param handleQuoting Add the argument with/without handling quoting
     * @return The command line itself
     */
    public CommandLine addArguments(final String[] addArguments, final boolean handleQuoting) {
        if (addArguments != null) {
            for (final String addArgument : addArguments) {
                addArgument(addArgument, handleQuoting);
            }
        }

        return this;
    }

    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     * Please note that the parsing can have undesired side-effects therefore
     * it is recommended to build the command line incrementally.
     * 
     * @param addArguments An string containing multiple arguments. 
     * @return The command line itself
     */
    public CommandLine addArguments(final String addArguments) {
        return this.addArguments(addArguments, true);
    }

    /**
     * Add multiple arguments. Handles parsing of quotes and whitespace.
     * Please note that the parsing can have undesired side-effects therefore
     * it is recommended to build the command line incrementally.
     *
     * @param addArguments An string containing multiple arguments.
     * @param handleQuoting Add the argument with/without handling quoting
     * @return The command line itself
     */
    public CommandLine addArguments(final String addArguments, final boolean handleQuoting) {
        if (addArguments != null) {
            final String[] argumentsArray = translateCommandline(addArguments);
            addArguments(argumentsArray, handleQuoting);
        }

        return this;
    }

    /**
     * Add a single argument. Handles quoting.
     *
     * @param argument The argument to add
     * @return The command line itself
     * @throws IllegalArgumentException If argument contains both single and double quotes
     */
    public CommandLine addArgument(final String argument) {
        return this.addArgument(argument, true);
    }

   /**
    * Add a single argument.
    *
    * @param argument The argument to add
    * @param handleQuoting Add the argument with/without handling quoting
    * @return The command line itself
    */
   public CommandLine addArgument(final String argument, final boolean handleQuoting) {

       if (argument == null)
       {
           return this;
       }

       // check if we can really quote the argument - if not throw an
       // IllegalArgumentException
       if (handleQuoting)
       {
           StringUtils.quoteArgument(argument);
       }

       arguments.add(new Argument(argument, handleQuoting));
       return this;
   }

    /**
     * Returns the expanded and quoted command line arguments.
     *  
     * @return The quoted arguments
     */
    public String[] getArguments() {

        Argument currArgument;
        String expandedArgument;
        final String[] result = new String[arguments.size()];

        for (int i=0; i<result.length; i++) {
            currArgument = arguments.get(i);
            expandedArgument = expandArgument(currArgument.getValue());
            result[i] = currArgument.isHandleQuoting() ? StringUtils.quoteArgument(expandedArgument) : expandedArgument;
        }

        return result;
    }

    /**
     * @return the substitution map
     */
    public Map<String, ?> getSubstitutionMap() {
        return substitutionMap;
    }

    /**
     * Set the substitutionMap to expand variables in the
     * command line.
     * 
     * @param substitutionMap the map
     */
    public void setSubstitutionMap(final Map<String, ?> substitutionMap) {
        this.substitutionMap = substitutionMap;
    }

    /**
     * Returns the command line as an array of strings.
     *
     * @return The command line as an string array
     */
    public String[] toStrings() {
        final String[] result = new String[arguments.size() + 1];
        result[0] = this.getExecutable();
        System.arraycopy(getArguments(), 0, result, 1, result.length-1);
        return result;
    }

    /**
     * Stringify operator returns the command line as a string.
     * Parameters are correctly quoted when containing a space or
     * left untouched if the are already quoted. 
     *
     * @return the command line as single string
     */
    @Override
    public String toString() {
        return "[" + StringUtils.toString(toStrings(), ", ") + "]";
    }

    // --- Implementation ---------------------------------------------------

    /**
     * Expand variables in a command line argument.
     *
     * @param argument the argument
     * @return the expanded string
     */
    private String expandArgument(final String argument) {
        final StringBuffer stringBuffer = StringUtils.stringSubstitution(argument, this.getSubstitutionMap(), true);
        return stringBuffer.toString();
    }

    /**
     * Crack a command line.
     *
     * @param toProcess
     *            the command line to process
     * @return the command line broken into strings. An empty or null toProcess
     *         parameter results in a zero sized array
     */
    private static String[] translateCommandline(final String toProcess) {
        if (toProcess == null || toProcess.length() == 0) {
            // no command? no string
            return new String[0];
        }

        // parse with a simple finite state machine

        final int normal = 0;
        final int inQuote = 1;
        final int inDoubleQuote = 2;
        int state = normal;
        final StringTokenizer tok = new StringTokenizer(toProcess, "\"\' ", true);
        final ArrayList<String> list = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean lastTokenHasBeenQuoted = false;

        while (tok.hasMoreTokens()) {
            final String nextTok = tok.nextToken();
            switch (state) {
            case inQuote:
                if ("\'".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            case inDoubleQuote:
                if ("\"".equals(nextTok)) {
                    lastTokenHasBeenQuoted = true;
                    state = normal;
                } else {
                    current.append(nextTok);
                }
                break;
            default:
                if ("\'".equals(nextTok)) {
                    state = inQuote;
                } else if ("\"".equals(nextTok)) {
                    state = inDoubleQuote;
                } else if (" ".equals(nextTok)) {
                    if (lastTokenHasBeenQuoted || current.length() != 0) {
                        list.add(current.toString());
                        current = new StringBuilder();
                    }
                } else {
                    current.append(nextTok);
                }
                lastTokenHasBeenQuoted = false;
                break;
            }
        }

        if (lastTokenHasBeenQuoted || current.length() != 0) {
            list.add(current.toString());
        }

        if (state == inQuote || state == inDoubleQuote) {
            throw new IllegalArgumentException("Unbalanced quotes in "
                    + toProcess);
        }

        final String[] args = new String[list.size()];
        return list.toArray(args);
    }

    /**
     * Cleans the executable string. The argument is trimmed and '/' and '\\' are
     * replaced with the platform specific file separator char
     *
     * @param dirtyExecutable the executable
     * @return the platform-specific executable string
     */
    private String toCleanExecutable(final String dirtyExecutable) {
        if (dirtyExecutable == null) {
            throw new IllegalArgumentException("Executable can not be null");
        } else if (dirtyExecutable.trim().length() == 0) {
            throw new IllegalArgumentException("Executable can not be empty");
        } else {
            return StringUtils.fixFileSeparatorChar(dirtyExecutable);
        }
    }

    /**
     * Encapsulates a command line argument.
     */
    class Argument {

        private final String value;
        private final boolean handleQuoting;

        private Argument(final String value, final boolean handleQuoting)
        {
            this.value = value.trim();
            this.handleQuoting = handleQuoting;
        }

        private String getValue()
        {
            return value;
        }

        private boolean isHandleQuoting()
        {
            return handleQuoting;
        }
    }
}

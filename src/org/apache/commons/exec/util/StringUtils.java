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

package org.apache.commons.exec.util;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * Supplement of commons-lang, the stringSubstitution() was in a simpler
 * implementation available in an older commons-lang implementation.
 *
 * This class is not part of the public API and could change without
 * warning.
 *
 * @version $Id: StringUtils.java 1636204 2014-11-02 22:30:31Z ggregory $
 */
public class StringUtils {

    private static final String SINGLE_QUOTE = "\'";
    private static final String DOUBLE_QUOTE = "\"";
    private static final char SLASH_CHAR = '/';
    private static final char BACKSLASH_CHAR = '\\';

    /**
     * Perform a series of substitutions.
     * <p>
     * The substitutions are performed by replacing ${variable} in the target string with the value of provided by the
     * key "variable" in the provided hash table.
     * </p>
     * <p>
     * A key consists of the following characters:
     * </p>
     * <ul>
     * <li>letter
     * <li>digit
     * <li>dot character
     * <li>hyphen character
     * <li>plus character
     * <li>underscore character
     * </ul>
     *
     * @param argStr
     *            the argument string to be processed
     * @param vars
     *            name/value pairs used for substitution
     * @param isLenient
     *            ignore a key not found in vars or throw a RuntimeException?
     * @return String target string with replacements.
     */
    public static StringBuffer stringSubstitution(final String argStr, final Map<? super String, ?> vars, final boolean isLenient) {

        final StringBuffer argBuf = new StringBuffer();

        if (argStr == null || argStr.length() == 0) {
            return argBuf;
        }

        if (vars == null || vars.size() == 0) {
            return argBuf.append(argStr);
        }

        final int argStrLength = argStr.length();

        for (int cIdx = 0; cIdx < argStrLength;) {

            char ch = argStr.charAt(cIdx);
            char del = ' ';

            switch (ch) {

                case '$':
                    final StringBuilder nameBuf = new StringBuilder();
                    del = argStr.charAt(cIdx + 1);
                    if (del == '{') {
                        cIdx++;

                        for (++cIdx; cIdx < argStr.length(); ++cIdx) {
                            ch = argStr.charAt(cIdx);
                            if (ch == '_' || ch == '.' || ch == '-' || ch == '+' || Character.isLetterOrDigit(ch)) {
                                nameBuf.append(ch);
                            } else {
                                break;
                            }
                        }

                        if (nameBuf.length() >= 0) {

                            String value;
                            final Object temp = vars.get(nameBuf.toString());

                            if (temp instanceof File) {
                                // for a file we have to fix the separator chars to allow
                                // cross-platform compatibility
                                value = fixFileSeparatorChar(((File) temp).getAbsolutePath());
                            }
                            else {
                                value = temp != null ? temp.toString() : null;    
                            }

                            if (value != null) {
                                argBuf.append(value);
                            } else {
                                if (isLenient) {
                                    // just append the unresolved variable declaration
                                    argBuf.append("${").append(nameBuf.toString()).append("}");
                                } else {
                                    // complain that no variable was found
                                    throw new RuntimeException("No value found for : " + nameBuf);
                                }
                            }

                            del = argStr.charAt(cIdx);

                            if (del != '}') {
                                throw new RuntimeException("Delimiter not found for : " + nameBuf);
                            }
                        }

                        cIdx++;
                    }
                    else {
                        argBuf.append(ch);
                        ++cIdx;
                    }

                    break;

                default:
                    argBuf.append(ch);
                    ++cIdx;
                    break;
            }
        }

        return argBuf;
    }

    /**
     * Split a string into an array of strings based
     * on a separator.
     *
     * @param input     what to split
     * @param splitChar what to split on
     * @return the array of strings
     */
    public static String[] split(final String input, final String splitChar) {
        final StringTokenizer tokens = new StringTokenizer(input, splitChar);
        final List<String> strList = new ArrayList<String>();
        while (tokens.hasMoreTokens()) {
            strList.add(tokens.nextToken());
        }
        return strList.toArray(new String[strList.size()]);
    }

    /**
     * Fixes the file separator char for the target platform
     * using the following replacement.
     * 
     * <ul>
     *  <li>'/' &#x2192; File.separatorChar</li>
     *  <li>'\\' &#x2192; File.separatorChar</li>
     * </ul>
     *
     * @param arg the argument to fix
     * @return the transformed argument 
     */
    public static String fixFileSeparatorChar(final String arg) {
        return arg.replace(SLASH_CHAR, File.separatorChar).replace(
                BACKSLASH_CHAR, File.separatorChar);
    }

    /**
     * Concatenates an array of string using a separator.
     *
     * @param strings the strings to concatenate
     * @param separator the separator between two strings
     * @return the concatenated strings
     */
    public static String toString(final String[] strings, final String separator) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                sb.append(separator);
            }
            sb.append(strings[i]);
        }
        return sb.toString();
    }

    /**
     * Put quotes around the given String if necessary.
     * <p>
     * If the argument doesn't include spaces or quotes, return it as is. If it
     * contains double quotes, use single quotes - else surround the argument by
     * double quotes.
     * </p>
     *
     * @param argument the argument to be quoted
     * @return the quoted argument
     * @throws IllegalArgumentException If argument contains both types of quotes
     */
    public static String quoteArgument(final String argument) {

        String cleanedArgument = argument.trim();

        // strip the quotes from both ends
        while (cleanedArgument.startsWith(SINGLE_QUOTE) || cleanedArgument.startsWith(DOUBLE_QUOTE)) {
            cleanedArgument = cleanedArgument.substring(1);
        }
        
        while (cleanedArgument.endsWith(SINGLE_QUOTE) || cleanedArgument.endsWith(DOUBLE_QUOTE)) {
            cleanedArgument = cleanedArgument.substring(0, cleanedArgument.length() - 1);
        }

        final StringBuilder buf = new StringBuilder();
        if (cleanedArgument.indexOf(DOUBLE_QUOTE) > -1) {
            if (cleanedArgument.indexOf(SINGLE_QUOTE) > -1) {
                throw new IllegalArgumentException(
                        "Can't handle single and double quotes in same argument");
            }
            return buf.append(SINGLE_QUOTE).append(cleanedArgument).append(
                    SINGLE_QUOTE).toString();
        } else if (cleanedArgument.indexOf(SINGLE_QUOTE) > -1
                || cleanedArgument.indexOf(" ") > -1) {
            return buf.append(DOUBLE_QUOTE).append(cleanedArgument).append(
                    DOUBLE_QUOTE).toString();
        } else {
            return cleanedArgument;
        }
    }

    /**
     * Determines if this is a quoted argument - either single or
     * double quoted.
     *
     * @param argument the argument to check
     * @return true when the argument is quoted
     */
    public static boolean isQuoted(final String argument) {
        return argument.startsWith(SINGLE_QUOTE) && argument.endsWith(SINGLE_QUOTE)
                || argument.startsWith(DOUBLE_QUOTE) && argument.endsWith(DOUBLE_QUOTE);
    }
}
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

//import java.io.BufferedReader;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.commons.exec.CommandLine;

/**
 * Helper class to determine the environment variable
 * for VMS.
 *
 * @version $Id: OpenVmsProcessingEnvironment.java 1636056 2014-11-01 21:12:52Z ggregory $
 * @deprecated No longer needed
 */
@Deprecated
public class OpenVmsProcessingEnvironment extends DefaultProcessingEnvironment {

    /*
     * Hopefully removing super-class overrides won't cause Clirr error.
     * If necessary can just delegate to super-class. 
     */

//    /**
//     * Find the list of environment variables for this process.
//     *
//     * @return a map containing the environment variables
//     * @throws IOException the operation failed
//     */    
//    @Override
//    protected Map<String, String> createProcEnvironment() throws IOException {
//        if (procEnvironment == null) {
//            final BufferedReader in = runProcEnvCommand();
//            procEnvironment = addVMSenvironmentVariables(new HashMap<String, String>(), in);
//        }
//
//        return procEnvironment;
//    }
//
//    /**
//     * Determine the OS specific command line to get a list of environment
//     * variables.
//     *
//     * @return the command line
//     */    
//    @Override
//    protected CommandLine getProcEnvCommand() {
//        final CommandLine commandLine = new CommandLine("show");
//        commandLine.addArgument("symbol/global"); // the parser assumes symbols are global
//        commandLine.addArgument("*");
//        return commandLine;
//    }
//
//    /**
//     * This method is VMS specific and used by getProcEnvironment(). Parses VMS
//     * symbols from {@code in} and adds them to {@code environment}.
//     * {@code in} is expected to be the output of "SHOW SYMBOL/GLOBAL *".
//     *
//     * @param environment the current environment
//     * @param in the reader from the process to determine VMS env variables
//     * @return the updated environment
//     * @throws IOException operation failed
//     */
//    private Map<String, String> addVMSenvironmentVariables(final Map<String, String> environment,
//            final BufferedReader in) throws IOException {
//        String line;
//        while ((line = in.readLine()) != null) {
//            final String SEP = "=="; // global symbol separator
//            final int sepidx = line.indexOf(SEP);
//            if (sepidx > 0) {
//                final String name = line.substring(0, sepidx).trim();
//                String value = line.substring(sepidx+SEP.length()).trim();
//                value = value.substring(1,value.length()-1); // drop enclosing quotes
//                environment.put(name,value);
//            }
//        }
//        return environment;
//    }
}

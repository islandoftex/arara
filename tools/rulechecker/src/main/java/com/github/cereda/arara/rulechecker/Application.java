/**
 * Rule checker, a tool for Arara
 * Copyright (c) 2015, Paulo Roberto Massa Cereda
 * All rights reserved.
 * 
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated documentation  files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute, sublicense,  and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 * 
 * The  above  copyright  notice  and this  permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 * 
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT  SHALL THE AUTHORS OR COPYRIGHT HOLDERS  BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR  OTHER LIABILITY, WHETHER IN AN  ACTION OF CONTRACT,
 * TORT OR  OTHERWISE, ARISING  FROM, OUT  OF OR  IN CONNECTION  WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.github.cereda.arara.rulechecker;

import java.io.File;
import java.util.List;

/**
 * Main class.
 * @author Paulo Roberto Massa Cereda
 * @version 1.0
 * @since 1.0
 */
public class Application {
    
    /**
     * The main method of the main class.
     * @param args Array of command line arguments.
     */
    public static void main(String[] args) {
        
        // print logo
        RuleUtils.printLogo();
        
        // parse command line
        Pair<Boolean, File> action = RuleUtils.parse(args);
        
        // get all files
        List<File> files = RuleUtils.findRules(action.getSecond());
        
        // prepare for release
        if (action.getFirst()) {
            
            // update all rules
            RuleUtils.updateRules(files);

        }
        else {
            
            // get the report and print it
            List<RuleReport> report = RuleUtils.readRules(files);
            RuleUtils.printReport(report);
            
        }
        
        // end application
        System.exit(0);
        
    }
    
}

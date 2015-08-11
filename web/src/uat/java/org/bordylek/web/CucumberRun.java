//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package org.bordylek.web;

import cucumber.api.cli.Main;

// http://stackoverflow.com/questions/18467115/cucumber-jvm-seems-to-use-system-exit
public class CucumberRun {

    public static void main(String[] argv) throws Throwable {
        if (Main.run(argv, Thread.currentThread().getContextClassLoader()) != 0) {
            throw new IllegalStateException();
        }
    }

}

//
// Copyright (c) 2011-2015 Xanadu Consultancy Ltd., 
//

package org.bordylek.web;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.events.EventFiringWebDriver;

import java.io.File;
import java.io.IOException;

public class SharedDriver extends EventFiringWebDriver {

    private static final WebDriver REAL_DRIVER;
    private static final Thread CLOSE_THREAD = new Thread() {
        @Override
        public void run() {
            REAL_DRIVER.close();
        }
    };

    static {
        Runtime.getRuntime().addShutdownHook(CLOSE_THREAD);
        FirefoxProfile profile = new FirefoxProfile();
        File modifyHeaders = new File("web/src/uat/resources/modify_headers-0.7.1.1-fx.xpi");
        try {
            profile.addExtension(modifyHeaders);
            profile.setPreference("modifyheaders.config.active", true);
            profile.setPreference("modifyheaders.config.alwaysOn", true);
            profile.setPreference("modifyheaders.headers.count", 1);
            profile.setPreference("modifyheaders.headers.action0", "Add");
            profile.setPreference("modifyheaders.headers.name0", "Authorization");
            profile.setPreference("modifyheaders.headers.value0", "Basic MTpwd2Q=");
            profile.setPreference("modifyheaders.headers.enabled0", true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        REAL_DRIVER = new FirefoxDriver(profile);
    }

    public SharedDriver() {
        super(REAL_DRIVER);
    }

    @Override
    public Object executeScript(String script, Object... args) {
        return ((JavascriptExecutor)REAL_DRIVER).executeScript(script, args);
    }

    @Override
    public void close() {
        if (Thread.currentThread() != CLOSE_THREAD) {
            throw new UnsupportedOperationException("You shouldn't close this WebDriver. It's shared and will close when the JVM exits.");
        }
        super.close();
    }

}
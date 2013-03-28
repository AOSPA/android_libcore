/*
 * Copyright (c) 2009, 2010, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @bug     6882376 6985460 8010309
 * @summary Test if java.util.logging.Logger is created before and after
 *          logging is enabled.  Also validate some basic PlatformLogger
 *          operations.  othervm mode to make sure java.util.logging
 *          is not initialized.
 *
 * @compile -XDignore.symbol.file PlatformLoggerTest.java
 * @run main/othervm PlatformLoggerTest
 */

import java.lang.reflect.Field;
import java.util.logging.*;
import sun.util.logging.PlatformLogger;

public class PlatformLoggerTest {
    public static void main(String[] args) throws Exception {
        final String FOO_PLATFORM_LOGGER = "test.platformlogger.foo";
        final String BAR_PLATFORM_LOGGER = "test.platformlogger.bar";
        final String GOO_PLATFORM_LOGGER = "test.platformlogger.goo";
        final String BAR_LOGGER = "test.logger.bar";
        PlatformLogger goo = PlatformLogger.getLogger(GOO_PLATFORM_LOGGER);
        // test the PlatformLogger methods
        testLogMethods(goo);

        // Create a platform logger using the default
        PlatformLogger foo = PlatformLogger.getLogger(FOO_PLATFORM_LOGGER);
        checkPlatformLogger(foo, FOO_PLATFORM_LOGGER);

        // create a java.util.logging.Logger
        // now java.util.logging.Logger should be created for each platform logger
        Logger logger = Logger.getLogger(BAR_LOGGER);
        logger.setLevel(Level.WARNING);

        PlatformLogger bar = PlatformLogger.getLogger(BAR_PLATFORM_LOGGER);
        checkPlatformLogger(bar, BAR_PLATFORM_LOGGER);

        // test the PlatformLogger methods
        testLogMethods(goo);
        testLogMethods(bar);

        checkLogger(FOO_PLATFORM_LOGGER, Level.FINER);
        checkLogger(BAR_PLATFORM_LOGGER, Level.FINER);

        checkLogger(GOO_PLATFORM_LOGGER, null);
        checkLogger(BAR_LOGGER, Level.WARNING);

        foo.setLevel(PlatformLogger.SEVERE);
        checkLogger(FOO_PLATFORM_LOGGER, Level.SEVERE);

        checkPlatformLoggerLevels(foo, bar);
    }

    private static void checkPlatformLogger(PlatformLogger logger, String name) {
        if (!logger.getName().equals(name)) {
            throw new RuntimeException("Invalid logger's name " +
                logger.getName() + " but expected " + name);
        }

        if (logger.getLevel() != null) {
            throw new RuntimeException("Invalid default level for logger " +
                logger.getName() + ": " + logger.getLevel());
        }

        if (logger.isLoggable(PlatformLogger.FINE) != false) {
            throw new RuntimeException("isLoggerable(FINE) returns true for logger " +
                logger.getName() + " but expected false");
        }

        logger.setLevel(PlatformLogger.FINER);
        if (logger.getLevel() != PlatformLogger.FINER) {
            throw new RuntimeException("Invalid level for logger " +
                logger.getName() + " " + logger.getLevel());
        }

        if (logger.isLoggable(PlatformLogger.FINE) != true) {
            throw new RuntimeException("isLoggerable(FINE) returns false for logger " +
                logger.getName() + " but expected true");
        }

        logger.info("OK: Testing log message");
    }

    private static void checkLogger(String name, Level level) {
        Logger logger = LogManager.getLogManager().getLogger(name);
        if (logger == null) {
            throw new RuntimeException("Logger " + name +
                " does not exist");
        }

        if (logger.getLevel() != level) {
            throw new RuntimeException("Invalid level for logger " +
                logger.getName() + " " + logger.getLevel());
        }
    }

    private static void testLogMethods(PlatformLogger logger) {
        logger.severe("Test severe(String, Object...) {0} {1}", new Long(1), "string");
        // test Object[]
        logger.severe("Test severe(String, Object...) {0}", (Object[]) getPoints());
        logger.warning("Test warning(String, Throwable)", new Throwable("Testing"));
        logger.info("Test info(String)");
    }

    private static void checkPlatformLoggerLevels(PlatformLogger... loggers) {
        final Level[] levels = new Level[] {
            Level.ALL, Level.CONFIG, Level.FINE, Level.FINER, Level.FINEST,
            Level.INFO, Level.OFF, Level.SEVERE, Level.WARNING
        };

        int count = PlatformLogger.Level.values().length;
        if (levels.length != count) {
            throw new RuntimeException("There are " + count +
                    " PlatformLogger.Level members, but " + levels.length +
                    " standard java.util.logging levels - the numbers should be equal.");
        }
        // check mappings
        for (Level level : levels) {
            checkPlatformLoggerLevelMapping(level);
        }

        for (Level level : levels) {
            PlatformLogger.Level platformLevel = PlatformLogger.Level.valueOf(level.getName());
            for (PlatformLogger logger : loggers) {
                // verify PlatformLogger.setLevel to a given level
                logger.setLevel(platformLevel);
                PlatformLogger.Level retrievedPlatformLevel = logger.getLevel();
                if (platformLevel != retrievedPlatformLevel) {
                    throw new RuntimeException("Retrieved PlatformLogger level " +
                            retrievedPlatformLevel +
                            " is not the same as set level " + platformLevel);
                }

                // check the level set in java.util.logging.Logger
                Logger javaLogger = LogManager.getLogManager().getLogger(logger.getName());
                Level javaLevel = javaLogger.getLevel();
                if (javaLogger.getLevel() != level) {
                    throw new RuntimeException("Retrieved backing java.util.logging.Logger level " +
                            javaLevel + " is not the expected " + level);
                }
            }
        }
    }

    private static void checkPlatformLoggerLevelMapping(Level level) {
        // map the given level to PlatformLogger.Level of the same name and value
        PlatformLogger.Level platformLevel = PlatformLogger.Level.valueOf(level.getName());
        if (platformLevel.intValue() != level.intValue()) {
            throw new RuntimeException("Mismatched level: " + level
                    + " PlatformLogger.Level" + platformLevel);
        }

        PlatformLogger.Level plevel;
        try {
            // validate if there is a public static final field in PlatformLogger
            // matching the level name
            Field platformLevelField = PlatformLogger.class.getField(level.getName());
            plevel = (PlatformLogger.Level) platformLevelField.get(null);
        } catch (Exception e) {
            throw new RuntimeException("No public static PlatformLogger." + level.getName() +
                                       " field", e);
        }
        if (!plevel.name().equals(level.getName()))
            throw new RuntimeException("The value of PlatformLogger." + level.getName() + ".name() is "
                                       + platformLevel.name() + " but expected " + level.getName());

        if (plevel.intValue() != level.intValue())
            throw new RuntimeException("The value of PlatformLogger." + level.intValue() + ".intValue() is "
                                       + platformLevel.intValue() + " but expected " + level.intValue());
    }

    static Point[] getPoints() {
        Point[] res = new Point[3];
        res[0] = new Point(0,0);
        res[1] = new Point(1,1);
        res[2] = new Point(2,2);
        return res;
    }

    static class Point {
        final int x;
        final int y;
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        public String toString() {
            return "{x="+x + ", y=" + y + "}";
        }
    }

}

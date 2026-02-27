/*
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2026, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v2.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package org.slf4j;

import org.slf4j.impl.StaticLoggerBinder;

/**
 * Test utility providing access to package-private {@link LoggerFactory#reset()} method.
 * This class must reside in the {@code org.slf4j} package to access package-private members.
 */
public class LoggerFactoryFriend {

    public static void reset() {
        LoggerFactory.reset();
        StaticLoggerBinder.getSingleton().reset();
    }
}

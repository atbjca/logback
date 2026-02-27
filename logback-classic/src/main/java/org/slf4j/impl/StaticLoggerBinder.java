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
package org.slf4j.impl;

import org.slf4j.ILoggerFactory;
import org.slf4j.helpers.Util;
import org.slf4j.spi.LoggerFactoryBinder;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.status.StatusUtil;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * SLF4J 1.x binding for logback.
 *
 * <p>This class is looked up by {@code org.slf4j.LoggerFactory} via the
 * hardcoded class name {@code org.slf4j.impl.StaticLoggerBinder}.
 *
 * @author Ceki G&uuml;lc&uuml;
 */
public class StaticLoggerBinder implements LoggerFactoryBinder {

    private static final StaticLoggerBinder SINGLETON = new StaticLoggerBinder();

    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.7.36"; // !final

    private LoggerContext defaultLoggerContext = new LoggerContext();

    static {
        SINGLETON.init();
    }

    private StaticLoggerBinder() {
    }

    public static StaticLoggerBinder getSingleton() {
        return SINGLETON;
    }

    void init() {
        defaultLoggerContext.reset();
        defaultLoggerContext.setName(CoreConstants.DEFAULT_CONTEXT_NAME);
        defaultLoggerContext.setMDCAdapter(StaticMDCBinder.mdcAdapter);
        try {
            try {
                new ContextInitializer(defaultLoggerContext).autoConfig();
            } catch (JoranException je) {
                Util.report("Failed to auto configure default logger context", je);
            }
            if (!StatusUtil.contextHasStatusListener(defaultLoggerContext)) {
                StatusPrinter.printInCaseOfErrorsOrWarnings(defaultLoggerContext);
            }
        } catch (Exception t) {
            Util.report("Failed to instantiate [" + LoggerContext.class.getName() + "]", t);
        }
        defaultLoggerContext.start();
    }

    /**
     * Reset the logger context and re-run auto configuration.
     * This is intended for test support.
     */
    public void reset() {
        init();
    }

    @Override
    public ILoggerFactory getLoggerFactory() {
        return defaultLoggerContext;
    }

    @Override
    public String getLoggerFactoryClassStr() {
        return defaultLoggerContext.getClass().getName();
    }

    /**
     * Return the {@link LoggerContext} used by this binding.
     */
    public LoggerContext getLoggerContext() {
        return defaultLoggerContext;
    }
}

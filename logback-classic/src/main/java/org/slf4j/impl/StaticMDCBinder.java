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

import ch.qos.logback.classic.util.LogbackMDCAdapter;
import org.slf4j.spi.MDCAdapter;

/**
 * SLF4J 1.x MDC binding for logback.
 *
 * <p>This class is looked up by {@code org.slf4j.MDC} via the
 * hardcoded class name {@code org.slf4j.impl.StaticMDCBinder}.
 */
public class StaticMDCBinder {

    public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();

    static final LogbackMDCAdapter mdcAdapter = new LogbackMDCAdapter();

    private StaticMDCBinder() {
    }

    public MDCAdapter getMDCA() {
        return mdcAdapter;
    }

    public String getMDCAdapterClassStr() {
        return LogbackMDCAdapter.class.getName();
    }
}

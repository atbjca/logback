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
package ch.qos.logback.classic.control;

import org.slf4j.helpers.MarkerIgnoringBase;

import ch.qos.logback.classic.Level;

/**
 * See javadoc for ControlLoggerContext.
 */
public class ControlLogger extends MarkerIgnoringBase {

    private static final long serialVersionUID = 1L;
    final ControlLogger parent;
    Level level;

    public ControlLogger(String name, ControlLogger parent) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        this.name = name;
        this.parent = parent;
    }

    public Level getLevel() {
        return level;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public final Level getEffectiveLevel() {
        for (ControlLogger cl = this; cl != null; cl = cl.parent) {
            if (cl.level != null)
                return cl.level;
        }
        return null; // If reached will cause an NullPointerException.
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ControlLogger))
            return false;

        final ControlLogger controlLogger = (ControlLogger) o;
        return name.equals(controlLogger.name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {
        if (getEffectiveLevel().levelInt <= Level.TRACE_INT) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public void trace(String format, Object arg) {
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
    }

    @Override
    public void trace(String format, Object... arguments) {
    }

    @Override
    public void trace(String msg, Throwable t) {
    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {
        if (getEffectiveLevel().levelInt <= Level.DEBUG_INT) {
            throw new UnsupportedOperationException("not yet implemented");
        }
    }

    @Override
    public void debug(String format, Object arg) {
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
    }

    @Override
    public void debug(String format, Object... arguments) {
    }

    @Override
    public void debug(String msg, Throwable t) {
    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {
    }

    @Override
    public void info(String format, Object arg) {
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
    }

    @Override
    public void info(String format, Object... arguments) {
    }

    @Override
    public void info(String msg, Throwable t) {
    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {
    }

    @Override
    public void warn(String format, Object arg) {
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
    }

    @Override
    public void warn(String format, Object... arguments) {
    }

    @Override
    public void warn(String msg, Throwable t) {
    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {
    }

    @Override
    public void error(String format, Object arg) {
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
    }

    @Override
    public void error(String format, Object... arguments) {
    }

    @Override
    public void error(String msg, Throwable t) {
    }

}

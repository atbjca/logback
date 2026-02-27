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

package ch.qos.logback.classic.blackbox.util;

import ch.qos.logback.classic.ClassicConstants;
import ch.qos.logback.classic.util.ClassicEnvUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.util.EnvUtil;
import ch.qos.logback.core.util.VersionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Tests in this class are run during the regular build process.
 *
 * <p>It follows that {@link VersionUtil} class is present. Moreover, logback-core and
 * logback-classic versions are the same.
 * </p>
 */
public class EnvUtilTest {

    // Beware: ----------------------------------------
    // Beware:  needs to be updated upon version change
    // Beware: ----------------------------------------
    static final String EXPECTED_VERSION = "1.5";


    @BeforeEach
    public void setUp() throws Exception {

    }

    // this test runs fine if run from logback-classic but fails when
    // run from logback-core. This is due to the fact that package information
    // is added when creating the jar.
    @Test
    public void versionTest() {
        String versionStr = VersionUtil.getVersionOfArtifact(ClassicConstants.class);
        // Package.getImplementationVersion() returns null when running from compiled classes (not JARs)
        assumeTrue(versionStr != null, "version not available outside JAR context");
        assertTrue(versionStr.startsWith(EXPECTED_VERSION));
    }

    @Test
    public void versionCompare() {
        String coreVersionStr = VersionUtil.getVersionOfArtifact(CoreConstants.class);
        String versionOfLogbackClassic = VersionUtil.getVersionOfArtifact(ClassicConstants.class);
        // Package.getImplementationVersion() returns null when running from compiled classes (not JARs)
        assumeTrue(coreVersionStr != null && versionOfLogbackClassic != null, "version not available outside JAR context");

        assertEquals(coreVersionStr, versionOfLogbackClassic);
    }


}

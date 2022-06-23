package com.intershop.tool.architecture.versions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SemanticVersionTest
{
    @Test
    public void testMajorMinorPatch()
    {
        SemanticVersion version_1_2_3 = SemanticVersion.valueOf("1.2.3");
        assertTrue(version_1_2_3.isIncrementable(), "is semantic version");
        assertEquals(1, version_1_2_3.getMajor(), "major");
        assertEquals(2, version_1_2_3.getMinor(), "minor");
        assertEquals(3, version_1_2_3.getPatch(), "patch");
    }

    @Test
    public void testMajorMinor()
    {
        SemanticVersion version_16_3 = SemanticVersion.valueOf("16.3");
        assertTrue(version_16_3.isIncrementable(), "is semantic version");
        assertEquals(16, version_16_3.getMajor(), "major");
        assertEquals(3, version_16_3.getMinor(), "minor");
        assertEquals(0, version_16_3.getPatch(), "patch");
    }

    @Test
    public void testIncrementVersions()
    {
        assertTrue(SemanticVersion.valueOf("2.7.6-rc1").isIncrementable(), "release candidates are not semantic");
        assertTrue(SemanticVersion.valueOf("2.7.6-dev1").isIncrementable(), "development release are not semantic");
        assertEquals(1, SemanticVersion.valueOf("2.7.6-dev1").getIncrement(), "development release are not semantic");
        assertEquals(ReleaseType.DEV, SemanticVersion.valueOf("2.7.6-dev1").getIncrementState(), "development release are not semantic");
    }

    @Test
    public void testNonSemanticVersions()
    {
        assertFalse(SemanticVersion.valueOf("23423842").isIncrementable(), "one number greater than 1000 is not semantic");
        assertFalse(SemanticVersion.valueOf("23423842.3242").isIncrementable(), "one number greater than 1000 is not semantic");
    }

    @Test
    public void testFeatureBranchVersions()
    {
        assertFalse(SemanticVersion.valueOf("FB-12.0.0-ANYISUSE-12233").isSemantic(), "development release are not semantic");
    }

    @Test
    public void testShortSemanticVersions()
    {
        assertTrue(SemanticVersion.valueOf("2").isIncrementable(), "one number less 1000 is semantic");
        assertTrue(SemanticVersion.valueOf("2.7").isIncrementable(), "two digits are valid");
    }

    @Test
    public void testExtendedSemanticVersions()
    {
        assertTrue(SemanticVersion.valueOf("2.7.6.GA").isIncrementable(), "GA is valid with dot");
        assertTrue(SemanticVersion.valueOf("2.7.6.FINAL").isIncrementable(), "FINAL is valid with dot");
    }

    @Test
    public void testExtendedSemanticDashVersions()
    {
        assertTrue(SemanticVersion.valueOf("2.7.6-GA").isIncrementable(), "GA is valid with dash");
        assertEquals(6, SemanticVersion.valueOf("2.7.6-GA").getPatch(), "patch version of GA is correct");
        assertTrue(SemanticVersion.valueOf("2.7.6-FINAL").isIncrementable(), "FINAL is valid with dash");
    }

    @Test
    public void testFourDigits()
    {
        assertTrue(SemanticVersion.valueOf("2.7.6.1").isIncrementable(), "four digits are valid");
    }

    @Test
    public void testFiveAndMoreDigits()
    {
        assertFalse(SemanticVersion.valueOf("5.4.3.2.1").isIncrementable(), "five digits are invalid");
        assertFalse(SemanticVersion.valueOf("6.5.4.3.2.1").isIncrementable(), "six digits are invalid");
    }

    @Test
    public void testJettyVersion()
    {
        assertEquals(0, SemanticVersion.valueOf("9.3.14.v20161028").getIncrement(), "jetty version 14");
    }

    @Test
    public void testGetVersionWithoutBuildExtension()
    {
        assertEquals("11.8.0", SemanticVersion.valueOf("11.8.0-SNAPSHOT").getVersionWithoutBuildExtension());
        assertEquals("11.8.0", SemanticVersion.valueOf("11.8.0-LOCAL").getVersionWithoutBuildExtension());
        assertEquals("11.8.0", SemanticVersion.valueOf("11.8.0-dev1").getVersionWithoutBuildExtension());
        assertEquals("7.8.0.1", SemanticVersion.valueOf("7.8.0.1").getVersionWithoutBuildExtension());
        assertEquals("17.0", SemanticVersion.valueOf("17.0").getVersionWithoutBuildExtension());
        assertEquals("14.0.0", SemanticVersion.valueOf("14.0.0-IS_12345_check_feature_test_master").getVersionWithoutBuildExtension());
    }
}

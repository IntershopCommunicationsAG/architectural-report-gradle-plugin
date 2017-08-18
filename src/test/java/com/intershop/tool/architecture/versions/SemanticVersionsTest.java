package com.intershop.tool.architecture.versions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SemanticVersionsTest
{
    private static final String VERSION_1_2_1 = "1.2.1";
    private static final List<String> AVAILABLE_VERSIONS = createVersions();
    public static final String REGEX_SPLIT_VERSIONS = "[|]";
    /**
     * Creates versions from 1.0.0 to 4.4.4 plus for each .5-rc1 e.g. 1.2.5-rc1
     */
    private static List<String> createVersions()
    {
        List<String> result = new ArrayList<>();
        for (int major = 1; major < 5; major++)
        {
            for (int minor = 0; minor < 5; minor++)
            {
                for (int patch = 0; patch < 5; patch++)
                {
                    result.add("" + major + "." + minor+ "." + patch);
                }
                result.add("" + major + "." + minor + ".5-dev1");
                result.add("" + major + "." + minor + ".5-dev2");
                result.add("" + major + "." + minor + ".5-rc1");
            }
        }
        return result;
    }

    @Test
    public void testGetNewestSemanticVersions()
    {
        assertEquals("find newest patch", "1.2.4", SemanticVersions.getNewestVersion(UpdateStrategy.PATCH, AVAILABLE_VERSIONS, VERSION_1_2_1));
        assertEquals("find newest major", "4.4.4", SemanticVersions.getNewestVersion(UpdateStrategy.MAJOR, AVAILABLE_VERSIONS, VERSION_1_2_1));
    }

    @Test
    public void testGetNewest()
    {
        assertEquals("find newest patch", "4.4.5-rc1", SemanticVersions.getNewestVersion(UpdateStrategy.INC, AVAILABLE_VERSIONS, VERSION_1_2_1));
    }

    @Test
    public void testOlderVersionMayNotExistAnymore()
    {
        List<String> availableVersions = Arrays.asList("2.2.29", "2.2.28", "2.2.27", "2.2.26", "2.2.25", "2.2.24", "2.2.23", "2.2.22");
        assertEquals("current patch is not longer in list", "1.10.19",SemanticVersions.getNewestVersion(UpdateStrategy.PATCH, availableVersions, "1.10.19"));
    }

    @Test
    public void testJettySupport()
    {
        List<String> availableVersions = Arrays.asList("9.4.0.v20161208|9.4.0.RC3|9.4.0.RC2|9.3.14.v20161028|9.4.0.RC1|9.3.13.v20161014|9.3.13.M0|8.1.22.v20160922|9.4.0.RC0|9.3.12.v20160915|8.2.0.v20160908|9.2.19.v20160908|8.1.21.v20160908|7.6.21.v20160908|8.1.20.v20160902|7.6.20.v20160902|9.4.0.M1|9.3.11.v20160721|9.2.18.v20160721|9.3.11.M0|9.3.10.v20160621|9.4.0.M0|9.3.10.M0|9.3.9.v20160517|9.2.17.v20160517|9.2.16.v20160414|9.3.9.M1|9.3.9.M0|9.3.8.v20160314|9.3.8.RC0|9.2.15.v20160210|8.1.19.v20160209|7.6.19.v20160209|9.3.7.v20160115|9.3.7.RC1|9.1.6.v20160112|9.3.7.RC0|9.3.6.v20151106|9.2.14.v20151106|9.3.5.v20151012|9.3.4.v20151007|9.3.4.RC1|8.1.18.v20150929|7.6.18.v20150929|9.3.4.RC0|9.3.3.v20150827|9.3.2.v20150730|9.2.13.v20150730|9.3.1.v20150714|9.2.12.v20150709|9.2.12.M0|9.3.0.v20150612|9.2.11.v20150529|9.3.0.RC1|9.3.0.RC0|7.6.17.v20150415|8.1.17.v20150415|9.2.11.M0|9.3.0.M2|9.2.10.v20150310|9.2.9.v20150224|9.2.8.v20150217|9.2.7.v20150116|9.2.6.v20141205|9.2.5.v20141112|9.3.0.M1|9.2.4.v20141103|9.3.0.M0|9.2.3.v20140905|8.1.16.v20140903|7.6.16.v20140903|9.2.2.v20140723|9.2.1.v20140609|9.2.0.v20140526|9.2.0.RC0|9.2.0.M1|9.1.5.v20140505|8.1.15.v20140411|7.6.15.v20140411|9.2.0.M0|9.1.4.v20140401|9.1.3.v20140225|9.1.2.v20140210|9.1.1.v20140108|9.1.0.v20131115|9.1.0.RC2|9.0.7.v20131107|9.1.0.RC1|8.1.14.v20131031|7.6.14.v20131031|9.0.6.v20130930|9.1.0.RC0|8.1.13.v20130916|9.1.0.M0|7.6.13.v20130916|9.0.5.v20130815|8.1.12.v20130726|7.6.12.v20130726|9.0.4.v20130625|8.1.11.v20130520|7.6.11.v20130520|9.0.3.v20130506|9.0.2.v20130417|9.0.1.v20130408|8.1.10.v20130312|7.6.10.v20130312|9.0.0.v20130308|9.0.0.RC2|9.0.0.RC1|9.0.0.RC0|8.1.9.v20130131|7.6.9.v20130131|9.0.0.M5|9.0.0.M4|9.0.0.M3|8.1.8.v20121106|7.6.8.v20121106|9.0.0.M2|9.0.0.M1|9.0.0.M0|7.6.7.v20120910|8.1.7.v20120910|8.1.6.v20120903|7.6.6.v20120903|8.1.5.v20120716|7.6.5.v20120716|8.1.4.v20120524|7.6.4.v20120524|8.1.3.v20120416|7.6.3.v20120416|8.1.2.v20120308|7.6.2.v20120308|8.1.1.v20120215|7.6.1.v20120215|8.1.0.v20120127|7.6.0.v20120127|8.1.0.RC5|7.6.0.RC5|8.1.0.RC4|7.6.0.RC4|7.6.0.RC3|8.1.0.RC2|7.6.0.RC2|8.1.0.RC1|7.6.0.RC1|8.1.0.RC0|7.6.0.RC0|8.0.4.v20111024|7.5.4.v20111024|8.0.3.v20111011|7.5.3.v20111011|8.0.2.v20111006|7.5.2.v20111006|8.0.1.v20110908|7.5.1.v20110908|8.0.0.v20110901|7.5.0.v20110901|7.5.0.RC2|7.5.0.RC1|8.0.0.RC0|7.5.0.RC0|7.4.5.v20110725|7.4.4.v20110707|7.4.3.v20110701|8.0.0.M3|7.4.2.v20110526|7.4.1.v20110513|7.4.0.v20110414|7.4.0.RC0|7.3.1.v20110307|7.3.0.v20110203|7.2.2.v20101205|8.0.0.M2|7.2.1.v20101111|7.2.0.v20101020|7.2.0.RC0|7.1.6.v20100715|8.0.0.M1|7.1.5.v20100705|7.1.4.v20100610|7.1.3.v20100526|7.1.2.v20100523|7.1.1.v20100517|7.1.0.v20100505|7.1.0.RC1|7.1.0.RC0|7.0.2.v20100331|7.0.2.RC0|8.0.0.M0|7.0.1.v20091125|7.0.0.v20091005|7.0.0.RC6|7.0.0.RC5|7.0.0.RC4|7.0.0.RC3|7.0.0.RC2|7.0.0.RC1|7.0.0.RC0|7.0.0.M4|7.0.0.M3".split(REGEX_SPLIT_VERSIONS));
        assertEquals("specific jetty version major", "9.4.0.v20161208", SemanticVersions.getNewestVersion(UpdateStrategy.MAJOR, availableVersions, "9.3.13.v20161014"));
        assertEquals("specific jetty version patch", "9.3.14.v20161028", SemanticVersions.getNewestVersion(UpdateStrategy.PATCH, availableVersions, "9.3.13.v20161014"));
    }

    @Test
    public void testCompatibleVersion()
    {
        assertTrue("is major update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.3"), SemanticVersion.valueOf("17.0"), UpdateStrategy.MAJOR));
        assertFalse("is major update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.3"), SemanticVersion.valueOf("17.0"), UpdateStrategy.MINOR));
        assertTrue("is minor update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.3"), SemanticVersion.valueOf("16.4"), UpdateStrategy.MINOR));
        assertFalse("is minor update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.3"), SemanticVersion.valueOf("16.4"), UpdateStrategy.PATCH));
        assertTrue("is patch update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.1.3"), SemanticVersion.valueOf("16.1.4"), UpdateStrategy.PATCH));
        assertFalse("is patch update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.1.3"), SemanticVersion.valueOf("16.1.4"), UpdateStrategy.INC));
        assertFalse("is patch update", SemanticVersions.getIsCompatibleVersion(SemanticVersion.valueOf("16.1.3"), SemanticVersion.valueOf("16.1.4"), UpdateStrategy.STICK));
    }
}

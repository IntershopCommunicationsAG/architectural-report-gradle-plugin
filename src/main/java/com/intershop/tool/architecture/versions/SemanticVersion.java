package com.intershop.tool.architecture.versions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemanticVersion
{
    final static Pattern INCREMENT_PATTERN = Pattern.compile("^(.*[^0-9])+([0-9]+)$");
    final static Pattern JETTY_GA_PATTERN = Pattern.compile("^v[0-9]{8}$");

    public static SemanticVersion valueOf(String revision)
    {
        try
        {
            return new SemanticVersion(revision);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't create semantic version object for version '"+revision+"'.");
        }
    }

    public static SemanticVersion valueOf(int major, int minor, int patch)
    {
        return valueOf("" + major + "." + minor + "." + patch);
    }

    private final String version;
    private final boolean isSemantic;
    private final int major;
    private final int minor;
    private final int patch;
    private final int increment;
    private ReleaseType incrementState = ReleaseType.GA;
    private boolean isIncrementable = true;

    private SemanticVersion(String version)
    {
        this.version = version;
        String[] parts = version.split("[\\.-]");
        int numbers[] = { 0, 0, 0, 0 };
        boolean foundIncrementStateAlone = false;
        try
        {
            if (parts.length < 1)
            {
                isIncrementable = false;
            }
            else
            {
                // digits for all except last position
                for (int i = 0; i < parts.length; i++)
                {
                    // are there no numbers
                    ReleaseType localSate = getIncrementState(parts[i].toLowerCase());
                    if (localSate != null)
                    {
                        incrementState = localSate;
                        foundIncrementStateAlone = true;
                    }
                    else
                    {
                        Matcher matcher = INCREMENT_PATTERN.matcher(parts[i]);
                        if (matcher.find()) {
                            incrementState = getIncrementState(matcher.group(1).toLowerCase());
                            String someNumberStr = matcher.group(2);
                            if (!JETTY_GA_PATTERN.matcher(parts[i]).find())
                            {
                                numbers[3] = Integer.parseInt(someNumberStr);
                            }
                        }
                        else
                        {
                            if (i>3)
                            {
                                isIncrementable = false;
                                break;
                            }
                            Integer value = getDigit(parts[i]);
                            if (value == null)
                            {
                                isIncrementable = false;
                                break;
                            }
                            if (foundIncrementStateAlone)
                            {
                                numbers[i - 1] = value;
                            }
                            else
                            {
                                numbers[i] = value;
                            }
                        }
                    }
                }
            }
        }
        catch(NumberFormatException e)
        {
            isIncrementable = false;
        }
        major = numbers[0];
        minor = numbers[1];
        patch = numbers[2];
        increment = numbers[3];
        this.isSemantic = version.equals(major + "." + minor + "." + patch);
    }

    private static ReleaseType getIncrementState(String lowerCased)
    {
        if ("final".equals(lowerCased) || "ga".equals(lowerCased))
        {
            return ReleaseType.GA;
        }
        if ("rc".equals(lowerCased))
        {
            return ReleaseType.RC;
        }
        if ("dev".equals(lowerCased))
        {
            return ReleaseType.DEV;
        }
        if (JETTY_GA_PATTERN.matcher(lowerCased).find())
        {
            return ReleaseType.GA;
        }
        return null;
    }

    private static Integer getDigit(String part)
    {
        Integer value = part.matches("^\\d+$") ? Integer.parseInt(part) : null;
        if (value != null && value < 1000)
        {
            return value;
        }
        return null;
    }

    /**
     * @return true in case the number schema allows sorting
     */
    public boolean isIncrementable()
    {
        return isIncrementable;
    }

    /**
     * @return true in case the number follows the semantic version schema
     */
    public boolean isSemantic()
    {
        return isSemantic;
    }

    public int getMajor()
    {
        return major;
    }

    public int getMinor()
    {
        return minor;
    }

    public int getPatch()
    {
        return patch;
    }

    public int getIncrement()
    {
        return increment;
    }

    public String getVersion()
    {
        return version;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return version.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SemanticVersion other = (SemanticVersion)obj;
        if (version == null)
        {
            if (other.version != null)
                return false;
        }
        else if (!version.equals(other.version))
            return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "SemanticVersion [version=" + version + "]";
    }

    public ReleaseType getIncrementState()
    {
        return incrementState;
    }
}

package com.intershop.tool.architecture.versions;

public enum ReleaseType
{
    /**
     * The order is important for sorting number.
     * In case a RC release is available the DEV releases are behind
     */
    DEV, RC, GA;
}

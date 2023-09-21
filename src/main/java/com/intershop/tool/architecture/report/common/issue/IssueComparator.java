package com.intershop.tool.architecture.report.common.issue;

import java.util.Comparator;

public class IssueComparator implements Comparator<Issue>
{
    private static final Comparator<Issue> DELEGATE = Comparator.comparing(
                    (Issue a) -> a.getProjectRef().getIdentifier())
                .thenComparing(Issue::getKey)
                        .thenComparing(Issue::getParametersString);

    private static final IssueComparator INSTANCE = new IssueComparator();

    public static Comparator<Issue> getInstance() {
        return INSTANCE;
    }

    @Override
    public int compare(Issue issue1, Issue issue2)
    {
        return DELEGATE.compare(issue1, issue2);
    }
}

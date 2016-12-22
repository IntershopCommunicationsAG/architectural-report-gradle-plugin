package com.intershop.tool.architecture.report.isml.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import com.intershop.tool.architecture.report.cmd.ArchitectureReportConstants;
import com.intershop.tool.architecture.report.common.model.Issue;
import com.intershop.tool.architecture.report.project.model.ProjectRef;

public class IsmlTemplateChecker
{
    private static final org.slf4j.Logger Logger = LoggerFactory.getLogger(IsmlTemplateChecker.class);
    private static final String safeIsmlFunctions[] = { "sessionlessurlex", "url", "stringtohtml", "webroot",
                    "stringtoxml", "val", "contenturl", "getvalue", "localizetext", "encodevalue"};
    private static final Pattern charCodePattern = Pattern.compile("#\\d+;");

    private final List<Issue> issues = new ArrayList<>();
    private final File ismlFile;
    private final ProjectRef projectRef;
    private final List<Integer> lineNumbers = new ArrayList<>();
    private final String content;

    public IsmlTemplateChecker(ProjectRef projectRef, File ismlFile)
    {
        this.projectRef = projectRef;
        this.ismlFile = ismlFile;
        this.content = getContent();
    }

    public List<Issue> getIssues()
    {
        try
        {
            checkISMLTemplate();
        }
        catch(StringIndexOutOfBoundsException e)
        {
            // TODO rework end of file detection
        }
        return issues;
    }

    private String getContent()
    {
        try
        {
            StringBuffer buff = new StringBuffer();
            try (BufferedReader reader = new BufferedReader(new FileReader(ismlFile)))
            {
                while(true)
                {
                    String line = reader.readLine();
                    if (line == null)
                        break;

                    buff.append(line);

                    if (line.indexOf("<ISCONTENT ") != -1 || line.indexOf("<iscontent ") != -1
                                    || line.indexOf("<ISContent ") != -1)
                    {
                        if (line.indexOf("\"text/xml\"") != -1)
                        {
                            // ignore XML templates
                            Logger.debug("skipping XML: '{}'", ismlFile);
                        }
                    }

                    buff.append("\r\n");
                    lineNumbers.add(Integer.valueOf(buff.length()));
                }
            }
            return buff.toString();
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void checkISMLTemplate()
    {
        boolean withinJSPCode = false;
        boolean withinISMLComment = false;

        for (int i = 0; i < content.length(); i++)
        {
            char c = content.charAt(i);

            if (withinJSPCode)
            {
                if (c == '%' && content.charAt(i + 1) == '>')
                {
                    withinJSPCode = false;
                }
            }
            else
            {
                if (c == '<' && content.charAt(i + 1) == '%')
                {
                    withinJSPCode = true;
                }
            }

            if (withinJSPCode)
            {
                // skip everything that is within JSP code
                continue;
            }

            if (withinISMLComment)
            {
                if (content.substring(i, i + 4).equals("--->"))
                {
                    withinISMLComment = false;
                }
            }
            else
            {
                if (content.substring(i, i + 5).equals("<!---"))
                {
                    withinISMLComment = true;
                }
            }

            if (withinISMLComment)
            {
                // skip everything that is within an ISML comment
                continue;
            }

            // check for the begin of an ISML tag
            if (c == '<')
            {
                if (content.substring(i, i + 3).equalsIgnoreCase("<IS"))
                {
                    i = parseISMLTag(content, i);
                }
            }

            // Check for the begin of an ISML expression
            else if (c == '#')
            {
                i = parseISMLExpression(content, i);
            }
        }
    }

    private static int parseISMLTag(String content, int currentPos)
    {
        boolean withinSingleQuotes = false;
        boolean withinISMLExpression = false;
        boolean withinFormatter = false;

        // jump past the end of the tag
        //
        int i = currentPos + 3;
        while(i < content.length())
        {
            char c = content.charAt(i);

            if (c == '\'')
            {
                withinSingleQuotes = !withinSingleQuotes;
            }

            if (c == '\"')
            {
                if (withinFormatter)
                {
                    withinFormatter = false;
                }
                if (isFormatter(content, i))
                {
                    withinFormatter = true;
                }
            }

            if (withinSingleQuotes || withinFormatter)
            {
                // skip everything that is within single quotes or formatter
                i++;
                continue;
            }

            if (c == '#')
            {
                withinISMLExpression = !withinISMLExpression;
            }

            if (withinISMLExpression)
            {
                // skip everything that is within an ISML expression
                i++;
                continue;
            }

            if (c == '>')
            {
                break;
            }
            else
            {
                i++;
            }
        }

        return i;
    }

    private int parseISMLExpression(String content, int currentPos)
    {
        // Logger.logStandard( "ISMLExpression: " + template.substring(currentPos, currentPos+10) );

        int i = currentPos + 1;

        boolean isSafe = false;

        if (isCharCode(content, i))
        {
            i++;
            return i;
        }

        if (isSingleHrefRaute(content, i))
        {
            i++;
            return i;
        }

        if (isHrefAnchor(content, i))
        {
            i++;
            return i;
        }

        if (isColorCode(content, i))
        {
            i += 6;
            return i;
        }

        if (isShortColorCode(content, i))
        {
            i += 3;
            return i;
        }

        if (isSafeIsmlFunction(content, i))
        {
            isSafe = true;
        }

        // jump past the end of the ISML expression
        //
        boolean withinSingleQuotes = false;

        while(i < content.length())
        {
            char c = content.charAt(i);

            if (c == '\'')
            {
                withinSingleQuotes = !withinSingleQuotes;
            }

            if (withinSingleQuotes)
            {
                // skip everything that is within single quotes
                i++;
                continue;
            }

            if (c == '#')
            {
                break;
            }

            i++;
        }

        if (!isSafe)
        {
            if (!isSafe(content, currentPos, content.substring(currentPos, i + 1)))
            {
                FilePosition position = getFilePosition(currentPos);
                issues.add(new Issue(projectRef, ArchitectureReportConstants.KEY_XSS, ismlFile.getAbsolutePath(),
                                position.getLine(), position.getColumn()));
            }
        }

        return i;
    }

    private static boolean isSafeIsmlFunction(String content, int currentPos)
    {
        // Logger.logStandard( "ISMLExpression: " + template.substring(currentPos, currentPos+10) );

        // check if the ISML expression is a safe ISML function (like URL)
        for (int j = 0; j < safeIsmlFunctions.length; j++)
        {
            String ismlFunction = safeIsmlFunctions[j];
            int maxPos = currentPos + ismlFunction.length();
            if (maxPos >= content.length())
            {
                maxPos = content.length() - 1;
            }
            if (content.substring(currentPos, maxPos).toLowerCase().startsWith(ismlFunction))
            {
                return true;
            }
        }

        return false;
    }

    static char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f', 'A', 'B',
                    'C', 'D', 'E', 'F' };

    private static boolean isColorCode(String content, int currentPos)
    {
        for (int i = currentPos; i < content.length() && i < currentPos + 6; i++)
        {
            char c = content.charAt(i);

            boolean ok = false;
            for (int j = 0; j < hexChars.length; j++)
            {
                if (c == hexChars[j])
                {
                    ok = true;
                    break;
                }
            }

            if (!ok)
                return false;
        }

        try
        {
            char c = content.charAt(currentPos + 6);
            if (c != ' ' && c != '\"' && c != ',' && c != ';' && c != '>')
            {
                return false;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return true;
    }

    private static boolean isShortColorCode(String content, int currentPos)
    {
        for (int i = currentPos; i < content.length() && i < currentPos + 3; i++)
        {
            char c = content.charAt(i);

            boolean ok = false;
            for (int j = 0; j < hexChars.length; j++)
            {
                if (c == hexChars[j])
                {
                    ok = true;
                    break;
                }
            }

            if (!ok)
                return false;
        }

        try
        {
            char c = content.charAt(currentPos + 3);
            if (c != ' ' && c != '\"' && c != ',' && c != ';' && c != '>')
            {
                return false;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return true;
    }

    /**
     * Check for formatter="#"
     */
    private static boolean isFormatter(String content, int currentPos)
    {
        try
        {
            if (content.charAt(currentPos) == '\"'
                            && content.substring(currentPos - 10, currentPos).equalsIgnoreCase("formatter="))
            {
                return true;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return false;
    }

    /**
     * Check for href="#"
     */
    private static boolean isSingleHrefRaute(String content, int currentPos)
    {
        try
        {
            if (content.charAt(currentPos) == '\"'
                            && content.substring(currentPos - 7, currentPos - 1).equalsIgnoreCase("href=\""))
            {
                return true;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return false;
    }

    /**
     * Check for href="#abc"
     */
    private static boolean isHrefAnchor(String content, int currentPos)
    {
        try
        {
            if (content.substring(currentPos - 7, currentPos - 1).equalsIgnoreCase("href=\"")
                            && !(content.substring(currentPos, currentPos + 4).equalsIgnoreCase("URL(")
                                            || content.substring(currentPos, currentPos + 9)
                                                            .equalsIgnoreCase("WebRoot()")
                            || content.substring(currentPos, currentPos + 6).equalsIgnoreCase("URLEX(")
                            || content.substring(currentPos, currentPos + 3).equalsIgnoreCase("'#'")
                            || content.substring(currentPos, currentPos + 17).equalsIgnoreCase("SessionlessURLEX(")
                            || content.substring(currentPos, currentPos + 12).equalsIgnoreCase("mediaobject:")
                            || content.substring(currentPos, currentPos + 7).equalsIgnoreCase("fileURL")
                            || content.substring(currentPos, currentPos + 4).equalsIgnoreCase("Link")
                            || content.substring(currentPos, currentPos + 26)
                                            .equalsIgnoreCase("EmailMarketingProviderLink")
                            || content.substring(currentPos, currentPos + 11).equalsIgnoreCase("contentURL(")
                            || content.substring(currentPos, currentPos + 13).equalsIgnoreCase("stringToHTML(")
                            || content.substring(currentPos, currentPos + 12).equalsIgnoreCase("stringToXML(")))
            {
                // href that does not start with URL, WebRoot, etc..
                // --> probably just an anchor
                return true;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return false;
    }

    /**
     * Check for char codes, e. g. "#160;", "#35;", etc.
     */
    private static boolean isCharCode(String content, int currentPos)
    {
        try
        {
            String sub = content.substring(currentPos - 1, content.indexOf(";", currentPos) + 1);
            if (charCodePattern.matcher(sub).matches())
            {
                return true;
            }
        }
        catch(Exception e)
        {
            return false;
        }

        return false;
    }

    private FilePosition getFilePosition(int currentPos)
    {
        int prevPos = 0;
        for (int i = 0; i < lineNumbers.size(); i++)
        {
            int pos = lineNumbers.get(i).intValue();
            if (currentPos < pos)
            {
                return new FilePosition(i + 1, currentPos - prevPos + 1);
            }
            prevPos = pos;
        }
        return null;
    }

    private static boolean isSafe(String content, int currentPos, String unsecureISML)
    {
        if (unsecureISML.lastIndexOf("UUID#") != -1)
        {
            // UUIDs are safe per definition
            return true;
        }
        if (unsecureISML.lastIndexOf(":ID#") != -1)
        {
            // IDs are safe
            return true;
        }

        if (unsecureISML.equals("#'#'#"))
        {
            return true;
        }

        if (unsecureISML.equals("#Locale:LocaleID#"))
        {
            return true;
        }
        if (unsecureISML.equals("#Counter#"))
        {
            return true;
        }
        if (unsecureISML.endsWith(":QualifiedName#"))
        {
            return true;
        }
        if (unsecureISML.endsWith(":isProductMaster#"))
        {
            return true;
        }
        return false;
    }

}

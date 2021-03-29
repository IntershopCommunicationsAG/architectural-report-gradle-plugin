package com.intershop.tool.architecture.report.java.model.jclass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

public class JavaHelperTest
{
    private static final String TEST_SIGNATURE = "Lcom.intershop.component.catalog.capi.CatalogType;)Ljava.util.Collection<+Lcom.intershop.component.catalog.capi.CatalogBO;>";
    private static final String[] TEST_CLASSNAMES = { "com.intershop.component.catalog.capi.CatalogType",
                    "java.util.Collection", "com.intershop.component.catalog.capi.CatalogBO" };
    private static final String TEST_SIGNATURE_EXTENDS = "<C::Lcom/intershop/component/catalog/capi/CatalogCategoryBO;>()TC;";
    private static final String[] TEST_CLASSNAMES_EXTENDS = { "com.intershop.component.catalog.capi.CatalogCategoryBO" };
    private static final String TEST_SIGNATURE_EXTENDS_RULE = "(Lcom/intershop/component/spreadsheet/capi/Output;Lcom/intershop/component/spreadsheet/capi/CalculationRule<Lcom/intershop/component/calculation/capi/spreadsheet/ComputedMoneyItem;Lcom/intershop/component/spreadsheet/capi/ComputedItem;Lcom/intershop/beehive/bts/capi/orderprocess/LineItemCtnr;>.Input;Lcom/intershop/component/spreadsheet/capi/CalculationRule<Lcom/intershop/component/calculation/capi/spreadsheet/ComputedMoneyItem;Lcom/intershop/component/spreadsheet/capi/ComputedItem;Lcom/intershop/beehive/bts/capi/orderprocess/LineItemCtnr;>.Context;)V";
    private static final String[] TEST_CLASSNAMES_EXTENDS_RULE = {
                    "com.intershop.component.spreadsheet.capi.Output",
                    "com.intershop.component.spreadsheet.capi.CalculationRule",
                    "com.intershop.component.spreadsheet.capi.ComputedItem",
                    "com.intershop.component.calculation.capi.spreadsheet.ComputedMoneyItem",
                    "com.intershop.beehive.bts.capi.orderprocess.LineItemCtnr"
                    };
    private static final String TEST_COLLECTION_SIGNATURE = "(ZLjava/util/Collection;)Z";
    private static final String[] TEST_COLLECTION_SIGNATURE_RESULT = {
        "java.util.Collection"
    };
    @Test
    public void testSimpleSignature()
    {
        Collection<String> result = JavaHelper.splitSignature(TEST_SIGNATURE);
        assertEquals("only one class", TEST_CLASSNAMES.length, result.size());
        for (String className : TEST_CLASSNAMES)
        {
            assertTrue("found: " + className, result.contains(className));
        }
    }

    @Test
    public void testExtendsSignature()
    {
        Collection<String> result = JavaHelper.splitSignature(TEST_SIGNATURE_EXTENDS);
        assertEquals("only one class", TEST_CLASSNAMES_EXTENDS.length, result.size());
        for (String className : TEST_CLASSNAMES_EXTENDS)
        {
            assertTrue("found: " + className, result.contains(className));
        }
    }

    @Test
    public void testRuleSignature()
    {
        Collection<String> result = JavaHelper.splitSignature(TEST_SIGNATURE_EXTENDS_RULE);
        assertEquals("only one class", TEST_CLASSNAMES_EXTENDS_RULE.length, result.size());
        for (String className : TEST_CLASSNAMES_EXTENDS_RULE)
        {
            assertTrue("found: " + className, result.contains(className));
        }
    }

    @Test
    public void testCollectionSignature()
    {
        Collection<String> result = JavaHelper.splitSignature(TEST_COLLECTION_SIGNATURE);
        assertEquals("only one class", TEST_COLLECTION_SIGNATURE_RESULT.length, result.size());
        for (String className : TEST_COLLECTION_SIGNATURE_RESULT)
        {
            assertTrue("found: " + className, result.contains(className));
        }
    }
}

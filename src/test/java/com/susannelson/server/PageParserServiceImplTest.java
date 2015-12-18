package com.susannelson.server;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class PageParserServiceImplTest {

    @Test
    public void testParsePageWords() throws Exception {

        PageParserService service = new PageParserServiceImpl();
        String title = service.parseAndPersistPageWordCounts("http://www.yahoo.com");

        assertTrue(title != null);
        assertTrue(title.toLowerCase().contains("yahoo"));
    }
}
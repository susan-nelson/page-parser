package com.susannelson.server;


public interface PageParserService {

    /**
     * Accesses the page for the parameter url, parses out non-html text from the page, and persists each unique word
     * with it's frequency in the database.
     *
     * definition of a 'word': a single distinct meaningful element of speech or writing, used with others
     * (or sometimes alone) to form a sentence and typically shown with a space on either side when written or printed.
     *
     * @param url - full http or https url for the page to be accessed.
     * @return String - title of the HTML page
     * @throws RuntimeException if unable to retrieve page, parse it or persist it
     */
    String parseAndPersistPageWordCounts(String url);
}

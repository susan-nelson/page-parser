package com.susannelson.server;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;


public class PageParserServiceImpl implements PageParserService {

    private static final Splitter splitter = Splitter.on(CharMatcher.anyOf(" ;:(),&\"'/[]!?|\\"));
    private static final Pattern patternForPeriods = Pattern.compile("[.]+");
    private static final Pattern patternForAlphaWithNumericPrefix = Pattern.compile("[0-9]+[a-z[\\p{L}]]+");
    private static final Logger log = Logger.getLogger(PageParserServiceImpl.class);

    /**
     * definition of a 'word': a single distinct meaningful element of speech or writing, used with others
     * (or sometimes alone) to form a sentence and typically shown with a space on either side when written or printed.
     * @throws RuntimeException if unable to retrieve page
     */
    @Override
    public String parseAndPersistPageWordCounts(String url) {

        Document doc;

        try {
            doc = Jsoup.connect(url).get();

        } catch (IOException e) {

            log.error("Unable to parse page" + url, e);
            throw new RuntimeException(e);
        }

        if (doc == null || doc.body() == null) {
            return "";
        }

        Map<String, Integer> wordCounts = buildWordCountMap(doc.body().text());

        log.info("Found " + wordCounts.size() +  " words at url: " + url);

        return doc.title();
    }

    Map<String, Integer> buildWordCountMap(String text) {

        Map<String, Integer> wordCounts = new HashMap<>();

        String cleanedRawText = cleanRawText(text);

        Iterable<String> words = splitter.trimResults().omitEmptyStrings().split(cleanedRawText);

        //define as local vars because google does not guarantee thread safety of CharMatcher
        CharMatcher matcherOnSpecialChars =
                CharMatcher.inRange('\u0020', '\u002F')
                        .or(CharMatcher.inRange('\u003A', '\u0040'))
                        .or(CharMatcher.inRange('\u005B', '\u0060'))
                        .or(CharMatcher.inRange('\u007B', '\u00BF'))
                        .or(CharMatcher.inRange('\u200E', '\u2122'));
        CharMatcher matcherOnDoubleQuotes = CharMatcher.inRange('\u201C', '\u201D');
        CharMatcher matcherOnPeriod = CharMatcher.anyOf(".");
        CharMatcher matcherOnElipses = CharMatcher.anyOf("\u2026");

        for (String word: words) {

            //skip single special character and elipses
            if (((word.length() == 1) && matcherOnSpecialChars.matches(word.charAt(0)))
                    || patternForPeriods.matcher(word).matches()) {

                continue;
            }

            String cleaned = cleanWord(word, matcherOnDoubleQuotes, matcherOnPeriod, matcherOnElipses);

            if (!cleaned.isEmpty()) {

                if (wordCounts.containsKey(cleaned)) {

                    int count = wordCounts.get(cleaned);
                    wordCounts.replace(cleaned, ++count);
                } else {
                    wordCounts.put(cleaned, 1);
                }
            }
        }

        return wordCounts;
    }

    private String cleanRawText(String text) {

        String noControl = CharMatcher.JAVA_ISO_CONTROL.removeFrom(text);

        return CharMatcher.WHITESPACE.trimAndCollapseFrom(noControl, ' ');
    }

    String cleanWord(String word,
                             CharMatcher matcherOnDoubleQuotes,
                             CharMatcher matcherOnPeriod,
                             CharMatcher matcherOnElipses) {

        String cleaned = word.toLowerCase();

        cleaned = matcherOnDoubleQuotes.removeFrom(cleaned);

        //remove period(s) from end of word
        if (cleaned.endsWith(".")) {
            cleaned = matcherOnPeriod.trimTrailingFrom(cleaned);
        }

        //remove elipses from the end of word
        cleaned = matcherOnElipses.trimTrailingFrom(cleaned);

        //remove english possesive from end of word
        if (cleaned.endsWith("’s")) {
            cleaned = cleaned.substring(0, cleaned.length() - 2);
        }

        //remove digits from beginning of alpha word (ex: 01TheWord)
        if (patternForAlphaWithNumericPrefix.matcher(cleaned).matches()) {
            cleaned = CharMatcher.javaLetter().retainFrom(cleaned);
        }

        return cleaned;
    }
}

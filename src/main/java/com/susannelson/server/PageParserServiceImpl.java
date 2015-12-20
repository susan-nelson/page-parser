package com.susannelson.server;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.susannelson.domain.Page;
import com.susannelson.domain.PageWord;
import com.susannelson.repositories.PageRepository;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class PageParserServiceImpl implements PageParserService {

    private static final Splitter splitter = Splitter.on(CharMatcher.anyOf(" ;:(),&\"'/[]!?|>\\"));
    private static final Pattern patternForPeriods = Pattern.compile("[.]+");
    private static final Pattern patternForContainsAlphaNumeric = Pattern.compile(".*[a-z0-9].*");
    private static final Pattern patternForAlphaWithNumericPrefix = Pattern.compile("[0-9]+[a-z-[\\p{L}]]+");
    private static final Logger log = Logger.getLogger(PageParserServiceImpl.class);

    private PageRepository repository;

    @Autowired
    public PageParserServiceImpl(PageRepository repository) {
        this.repository = repository;
    }

    @Override
    public String parseAndPersistPageWordCounts(String url) {

        Document doc;

        try {
            doc = Jsoup.connect(url).get();

        } catch (IOException e) {

            log.error("Unable to access page" + url, e);
            throw new RuntimeException(e);
        }

        if (doc == null || doc.body() == null) {
            return "";
        }

        Map<String, Integer> wordCounts = buildWordCountMap(doc.body().text());

        log.info("Found " + wordCounts.size() +  " words at url: " + url);

        Page savedPage = persistPageWords(url, wordCounts);

        log.info("Persisted page with id: " + savedPage.getId());

        return doc.title();
    }

    Map<String, Integer> buildWordCountMap(String text) {

        if (text == null) {
            return ImmutableMap.of();
        }

        Map<String, Integer> wordCounts = new HashMap<>();

        String cleanedRawText = cleanRawText(text);

        Iterable<String> words = splitter.trimResults().omitEmptyStrings().split(cleanedRawText);

        for (String word: words) {

            //skip single special character and elipses
            if (((word.length() == 1) && patternForContainsAlphaNumeric.matcher(word).matches())
                    || patternForPeriods.matcher(word).matches()) {

                continue;
            }

            String cleaned = cleanWord(word);

            if (!cleaned.isEmpty() && patternForContainsAlphaNumeric.matcher(cleaned).matches()) {

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

    String cleanWord(String word) {

        if (word == null) {
            return "";
        }

        //define as local vars because google does not guarantee thread safety of CharMatcher
        CharMatcher matcherOnDoubleQuotes = CharMatcher.inRange('\u201C', '\u201D');
        CharMatcher matcherOnPeriod = CharMatcher.anyOf(".");
        CharMatcher matcherOnElipses = CharMatcher.anyOf("\u2026");

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

    Page persistPageWords(String url, Map<String, Integer> wordCounts) {

        Set<PageWord> words = new HashSet<>();

        Page page = new Page();
        page.setUrl(url);
        page.setWords(words);

        PageWord wordCount;

        for (String word : wordCounts.keySet()) {

            wordCount = new PageWord();
            wordCount.setWord(word);
            wordCount.setCount(wordCounts.get(word));

            words.add(wordCount);
        }

        return repository.save(page);
    }

    private String cleanRawText(String text) {

        return CharMatcher.WHITESPACE.trimAndCollapseFrom(CharMatcher.JAVA_ISO_CONTROL.removeFrom(text), ' ');
    }
}

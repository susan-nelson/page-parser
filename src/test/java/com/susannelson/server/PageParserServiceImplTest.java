package com.susannelson.server;

import com.susannelson.PageParserApplication;
import com.susannelson.domain.Page;
import com.susannelson.repositories.PageRepository;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {PageParserApplication.class})
public class PageParserServiceImplTest {

    private static final String cleanedText =
            "Upgrade to the new Firefox » Yahoo Search  Search: Search Web Open Search Assist 1Shearling coats & UFC " +
                    "champ Holly Holm adjusts to superstardom, Yahoo News Top Stories Watch now Weather Washington \u200E27 °F | °C " +
                    "Light Snow \u200E37°F High \u200E26°F Low Today \u200E31.5 °F | °C \u200E37°F High \u200E26°F Low " +
                    "Tomorrow \u200E27.5 °F | °C \u200E28°F High \u200E27°F Low Friday \u200E29 °F | °C \u200E36°F High " +
                    "\u200E22°F Low Featured Videos Yahoo Screen 'Star Wars' cast sings theme song Santa helps soldier " +
                    "dad surprise his daughters View today's contests A former Costco executive is opening a fast-food " +
                    "chain that's unlike anything that exists in America A former Costco executive is launching " +
                    "America's first fast-food chain that has been certified... Exec who jacked up price of a lifesaving " +
                    "drug is arrested Martin Shkreli, a 32-year-old former hedge fund manager and relentless " +
                    "self-promoter who has called himself “the world’s most eligible bachelor” on Twitter, was arrested " +
                    "in a gray hoodie and taken into federal court in Brooklyn, where he pleaded not guilty. Prosecutors " +
                    "said that between 2009 and… Closest Habitable Planet Is Just 14 Light Years Away, According to " +
                    "Scientists This is the closest habitable planet researchers have discovered. How Huge Are Those " +
                    "‘Star Wars’ Spaceships, Anyway? X-Wings and TIE Fighters weave and dart in a beautiful but deadly " +
                    "ballet. The enormous bulk of a Star Destroyer overshadows an escaping Rebel corvette. The " +
                    "Millennium Falcon gracefully navigates through an asteroid field: The Star Wars movies are full of " +
                    "amazing sequences showing spaceships of all… Dan Moren Star Destroyer Death Star Millennium Falcon " +
                    "Drake hit In-n-Out Burger after Warriors beat Suns Ball Don't Lie TJ Manotoc@tjmanotoc Follow on " +
                    "Twitter RT @YahooSports -- Drake, Stephen Curry and Ayesha Curry grabbed a post-victory meal at " +
                    "In-N-Out burger.";

    private PageParserServiceImpl service;
    private PageRepository mockPageRepository;

    @BeforeClass
    public void setup() {

        mockPageRepository = Mockito.mock(PageRepository.class);
        service = new PageParserServiceImpl(mockPageRepository);
    }

    @Test
    public void testParsePageWords() throws Exception {

        Page persistedPage = new Page();
        persistedPage.setId(1);
        when(mockPageRepository.save(any(Page.class))).thenReturn(persistedPage);

        String title = service.parseAndPersistPageWordCounts("http://www.yahoo.com");

        assertTrue(title != null);
        assertTrue(title.toLowerCase().contains("yahoo"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParsePageWordsNotHttp() throws Exception {

        service.parseAndPersistPageWordCounts("bogus.com");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParsePageWordsEmptyUrl() throws Exception {

        service.parseAndPersistPageWordCounts("");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testParsePageWordsNullUrl() throws Exception {

        service.parseAndPersistPageWordCounts(null);
    }

    @Test
    public void testCleanWord() {

        String cleanedWord = service.cleanWord("1Shearling…");

        assertTrue(cleanedWord != null);
        assertTrue(cleanedWord.equals("shearling"));

        cleanedWord = service.cleanWord("“Affluenza...”");

        assertTrue(cleanedWord != null);
        assertTrue(cleanedWord.equals("affluenza"));

        cleanedWord = service.cleanWord("Shkreli’s");

        assertTrue(cleanedWord != null);
        assertTrue(cleanedWord.equals("shkreli"));
    }

    @Test
    public void testCleanWordEmpty() {

        String cleanedWord = service.cleanWord("");

        assertTrue(cleanedWord != null);
        assertTrue(cleanedWord.equals(""));
    }

    @Test
    public void testCleanWordNull() {

        String cleanedWord = service.cleanWord(null);

        assertTrue(cleanedWord != null);
        assertTrue(cleanedWord.equals(""));
    }

    @Test
    public void testBuildWordCountMap() {

        Map<String, Integer> wordCounts = service.buildWordCountMap(cleanedText);

        assertTrue(wordCounts != null);
        assertTrue(wordCounts.size() > 200);
        assertTrue(wordCounts.containsKey("search"));
        assertTrue(wordCounts.get("search") == 4);
        assertTrue(wordCounts.containsKey("fast-food"));
        assertTrue(wordCounts.get("fast-food") == 2);
        assertTrue(wordCounts.containsKey("today"));
        assertTrue(wordCounts.get("today") == 2);
        assertFalse(wordCounts.containsKey("»"));
        assertFalse(wordCounts.containsKey("--"));
    }

    @Test
    public void testBuildWordCountMapEmptyString() {

        Map<String, Integer> wordCounts = service.buildWordCountMap("");

        assertTrue(wordCounts != null);
        assertTrue(wordCounts.size() == 0);
    }

    @Test
    public void testBuildWordCountMapNullInput() {

        Map<String, Integer> wordCounts = service.buildWordCountMap(null);

        assertTrue(wordCounts != null);
        assertTrue(wordCounts.size() == 0);
    }
}
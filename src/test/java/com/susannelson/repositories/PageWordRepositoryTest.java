package com.susannelson.repositories;

import com.susannelson.configuration.RepositoryConfiguration;
import com.susannelson.domain.Page;
import com.susannelson.domain.PageWord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {RepositoryConfiguration.class})
public class PageWordRepositoryTest {

    private PageRepository pageRepository;

    @Autowired
    public void setPageWordRepository(PageRepository pageRepository) {

        this.pageRepository = pageRepository;
    }

    @Test
    public void savePage() {

        PageWord word1 = new PageWord();
        word1.setWord("word");
        word1.setCount(1);

        PageWord word2 = new PageWord();
        word2.setWord("word");
        word2.setCount(2);

        Page page = new Page();
        page.setUrl("http://www.yahoo.com");

        Set<PageWord> words = new HashSet<>();
        words.add(word1);
        words.add(word2);

        page.setWords(words);

        assertNull(page.getId()); //null before save
        assertNull(word1.getId()); //null before save
        assertNull(word2.getId()); //null before save

        pageRepository.save(page);

        assertNotNull(page.getId()); //not null after save
        assertNotNull(word1.getId()); //not null after save
        assertNotNull(word2.getId()); //not null after save

        //fetch from DB
        Page fetchedPage = pageRepository.findOne(page.getId());

        //should not be null
        assertNotNull(fetchedPage);

        //should equal
        assertEquals(page.getId(), fetchedPage.getId());
        assertEquals(page.getUrl(), fetchedPage.getUrl());
        assertEquals(page.getWords().size(), 2);

        //update description and save
        fetchedPage.setUrl("http://www.google.com");
        pageRepository.save(fetchedPage);

        //get from DB, should be updated
        Page fetchedUpdatedPage = pageRepository.findOne(fetchedPage.getId());
        assertEquals(fetchedPage.getUrl(), fetchedUpdatedPage.getUrl());

        //verify count of pages in DB
        long pageCount = pageRepository.count();
        assertEquals(pageCount, 1);

        //get all pages, list should only have one
        Iterable<Page> pages = pageRepository.findAll();

        pageCount = 0;
        int pageWordCount = 0;

        for(Page p : pages){

            if (p != null) {

                pageCount++;
                pageWordCount = (p.getWords() == null) ? pageWordCount : pageWordCount + p.getWords().size();
            }
        }

        assertEquals(pageCount, 1);
        assertEquals(pageWordCount, 2);
    }
}
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
    private PageWordRepository pageWordrepository;

    @Autowired
    public void setPageWordRepository(PageRepository pageRepository, PageWordRepository pageWordrepository) {

        this.pageRepository = pageRepository;
        this.pageWordrepository = pageWordrepository;
    }

    @Test
    public void savePage() {

        PageWord word = new PageWord();
        word.setWord("word");
        word.setCount(1);

        assertNull(word.getId()); //null before save
        pageWordrepository.save(word);
        assertNotNull(word.getId()); //not null after save

        Page page = new Page();
        page.setUrl("http://www.yahoo.com");

        Set<PageWord> words = new HashSet<>();
        words.add(word);

        page.setWords(words);

        assertNull(page.getId()); //null before save
        pageRepository.save(page);
        assertNotNull(page.getId()); //not null after save

        //fetch from DB
        Page fetchedPage = pageRepository.findOne(page.getId());

        //should not be null
        assertNotNull(fetchedPage);

        //should equal
        assertEquals(page.getId(), fetchedPage.getId());
        assertEquals(page.getUrl(), fetchedPage.getUrl());

        //update description and save
        //fetchedPage.setDescription("New Description");
        pageRepository.save(fetchedPage);

        //get from DB, should be updated
        Page fetchedUpdatedPage = pageRepository.findOne(fetchedPage.getId());
        assertEquals(fetchedPage.getUrl(), fetchedUpdatedPage.getUrl());

        //verify count of pages in DB
        long pageCount = pageRepository.count();
        assertEquals(pageCount, 1);

        //get all pages, list should only have one
        Iterable<Page> pages = pageRepository.findAll();

        int count = 0;

        for(Page p : pages){
            count++;
        }

        assertEquals(count, 1);
    }
}
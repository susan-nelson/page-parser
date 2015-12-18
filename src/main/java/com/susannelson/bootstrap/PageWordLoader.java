package com.susannelson.bootstrap;

import com.susannelson.domain.Page;
import com.susannelson.domain.PageWord;
import com.susannelson.repositories.PageRepository;
import com.susannelson.repositories.PageWordRepository;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PageWordLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = Logger.getLogger(PageWordLoader.class);

    private PageRepository pageRepository;
    private PageWordRepository pageWordrepository;

    @Autowired
    public void setRepository(PageRepository pageRepository, PageWordRepository pageWordrepository) {
        this.pageRepository = pageRepository;
        this.pageWordrepository = pageWordrepository;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        PageWord word1 = new PageWord();
        word1.setWord("word1");
        word1.setCount(1);

        PageWord word2 = new PageWord();
        word2.setWord("word2");
        word2.setCount(2);

        Page page = new Page();
        page.setUrl("http://www.yahoo.com");

        Set<PageWord> words = new HashSet<>();
        words.add(word1);
        words.add(word2);

        page.setWords(words);

        pageRepository.save(page);

        log.info("Saved page - id: " + page.getId());
    }
}

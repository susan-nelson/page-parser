package com.susannelson.bootstrap;

import com.susannelson.domain.Page;
import com.susannelson.domain.PageWord;
import com.susannelson.repositories.PageRepository;
import com.susannelson.server.PageParserService;
import com.susannelson.server.PageParserServiceImpl;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PageWordLoader implements ApplicationListener<ContextRefreshedEvent> {

    private static final Logger log = Logger.getLogger(PageWordLoader.class);

    @Value("${url}")
    private String url = "http://www.yahoo.com";
    private PageParserService service;
    private PageRepository pageRepository;

    @Autowired
    public void setRepository(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    @Autowired
    public void setService(PageParserService service) {
        this.service = service;
    }

    /**
     * Accesses the PageParserService for the configured url to parse text and persist word counts.
     * Runs after application startup is complete.
     * Logs the title of the page that was accessed.
     * Catches and logs any exceptions.
     * @see ContextRefreshedEvent
     * @param event - the ContextRefreshedEvent
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {

        try {
            String title = service.parseAndPersistPageWordCounts(url);

            log.info("Page processed, title: " + title + " at url: " + url);
        } catch (Exception e) {
            log.error("Page was not processed for url: " + url, e);
        }
    }
}

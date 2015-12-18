package com.susannelson.domain;

import javax.persistence.*;
import java.util.Set;

@Entity
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    private String url;
    @OneToMany(cascade=CascadeType.ALL, fetch = FetchType.EAGER)
    @ElementCollection(targetClass=PageWord.class)
    @JoinColumn(name = "PAGE_ID")
    private Set<PageWord> words;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Set<PageWord> getWords() {
        return words;
    }

    public void setWords(Set<PageWord> words) {
        this.words = words;
    }
}

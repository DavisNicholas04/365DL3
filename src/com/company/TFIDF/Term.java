package com.company.TFIDF;

import java.io.Serializable;

public class Term implements Serializable {
    public String key;
    public int numberOfTimesWordUsed = 1;

    public Term(String key) {
        this.key = key;
    }
}
package com.company.TFIDF;

import com.company.dataStructures.MyHashMap;
import java.util.Comparator;

public class SortByTFIDF_Asgnt1 implements Comparator<MyHashMap> {

    // most to least similar sorter
    public int compare(MyHashMap a, MyHashMap b){
        return Double.compare(b.getTFIDF(), a.getTFIDF());
    }
}
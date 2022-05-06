package com.company.dataStructures.KMeans;

import java.util.Comparator;

public class SortDoubles implements Comparator<Double> {

    // most to least similar sorter
    @Override
    public int compare(Double a, Double b){
        return Double.compare(b, a);
    }
}
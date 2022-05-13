package com.company.dataStructures;

import com.company.TFIDF.Term;

import java.io.Serializable;
import java.util.LinkedList;

public class MyHashMap implements Serializable {

    public LinkedList<Term>[] map = new LinkedList[100];
    LinkedList<MyHashMap> shortestPath = new LinkedList<>();
    public int size = 0;
    double tfidf = 0;
    String businessName;
    double latitude;
    double longitude;
    MyHashMap[] neighbors = new MyHashMap[4];
    int disjointSet = -1;
    double distance = Integer.MAX_VALUE;

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public boolean unassignedDisjointSet(){
        return disjointSet == -1;
    }

    public int getDisjointSet() {
        return disjointSet;
    }

    public void setDisjointSet(int disjointSet) {
        this.disjointSet = disjointSet;
    }

    public MyHashMap[] getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(MyHashMap[] neighbors) {
        this.neighbors = neighbors;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public  MyHashMap(){
    }

    /***
     * Every Time a word has its TF-IDF calculated it is added to the businesses TF-IDF overall value
     ***/
    public void addToTfidf(double tfidf)
    {
        this.tfidf = this.tfidf + tfidf;
    }

    public void setTfidf(double tfidf){
        this.tfidf = tfidf;
    }

    public double getTFIDF(){
        return tfidf;
    }

    public String getBusinessName(){
        return businessName;
    }

    public void setBusinessName(String businessName){
        this.businessName = businessName;
    }

    public int getSize(){
        return size;
    }

    public void put(String key){
        int capacity = (int)(map.length * .75);
        if (size >= capacity){
            resize();
        }
        int index = Math.abs(key.hashCode()) % map.length;
        Term term = new Term(key);

        if (map[index] == null){
            map[index] = new LinkedList<>();
            map[index].addLast(term);
            size++;
            return;
        } else {
            for (Term collidedTerm: map[index]) {
                if (collidedTerm.key.equals(key)){
                    getTerm(collidedTerm.key).numberOfTimesWordUsed++;
                    return;
                }
            }
        }
        map[index].addLast(term);
        size++;
    }

    private void resize(){
        LinkedList<Term>[] tempList = map;
        map = new LinkedList[size * 2];
        size = 0;

        for (LinkedList<Term> list : tempList){
            if (list == null)
                continue;
            for (Term term: list) {
                put(term.key);
            }
        }
    }

    public Term getTerm(String key) {
        int index = Math.abs(key.hashCode()) % map.length;
        if (map[index] == null) {
            return null;
        } else {
            int i = 0;
            for (Term term: map[index]){
                if (term.key.equalsIgnoreCase(key)){
                    return map[index].get(i);
                }
                i++;
            }
        }
        return null;
    }

    public LinkedList<MyHashMap> getShortestPath() {
        return shortestPath;
    }

    public void setShortestPath(LinkedList<MyHashMap> shortestPath) {
        this.shortestPath = shortestPath;
    }
}
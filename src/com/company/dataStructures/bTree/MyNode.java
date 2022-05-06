package com.company.dataStructures.bTree;

import com.company.dataStructures.MyHashMap;

import java.io.*;
import java.util.ArrayList;

public class MyNode implements Serializable {
    public MyHashMap[] keys;
    public int currentNumOfKeys;
    int minDegree;
    MyNode[] children;
    boolean leaf;
    public String title;

    public MyNode(int minDegree, boolean leaf) {
        this.minDegree = minDegree;
        this.leaf = leaf;
        this.keys = new MyHashMap[(minDegree * 2) - 1];
        this.children = new MyNode[2 * minDegree];
        this.currentNumOfKeys = 0;
    }

    public void traverse() {
        int i;
        for (i = 0; i < this.currentNumOfKeys; i++) {
            if (!this.leaf) {
                children[i].traverse();
            }
            System.out.print(keys[i].getBusinessName() + ": ");
        }

        if (!leaf) {
            children[i].traverse();
        }
    }

    public ArrayList<String> businessNameArrayListRep() {
        ArrayList<String> businesses = new ArrayList<>();
        ArrayList<String> children1 = new ArrayList<>();
        ArrayList<String> children2 = new ArrayList<>();
        int i;
        for (i = 0; i < this.currentNumOfKeys; i++) {
            if (!this.leaf) {
                children1 = children[i].businessNameArrayListRep();
            }
            businesses.add(keys[i].getBusinessName());
        }

        if (!leaf) {
            children2 = children[i].businessNameArrayListRep();
        }
        businesses.addAll(children1);
        businesses.addAll(children2);
        return businesses;
    }

    public MapIntSet getBusinessByIndex(int stopLoc, int currentLoc) {
        int i;
        MapIntSet set = new MapIntSet();
        for (i = 0; i < this.currentNumOfKeys; i++, currentLoc++) {
            if (currentLoc == stopLoc && keys[i] != null) {
                set.myHashMap = keys[i];
                set.integer = currentLoc;
                return set;
            }
            if (!this.leaf) {
                set = children[i].getBusinessByIndex(stopLoc, ++currentLoc);
                currentLoc = set.integer;
                if (currentLoc == stopLoc && set.myHashMap != null) {
                    return set;
                }
            }
        }

        if (!leaf) {
            set = children[i].getBusinessByIndex(stopLoc, ++currentLoc);
            currentLoc = set.integer;
            if (currentLoc == stopLoc && set.myHashMap != null) {
                return set;
            }
        }

        return new MapIntSet(null, currentLoc);
    }

    static class MapIntSet {
        MyHashMap myHashMap;
        int integer;

        public MapIntSet() {

        }

        public MapIntSet(MyHashMap myHashMap, int integer) {
            this.myHashMap = myHashMap;
            this.integer = integer;
        }
    }

    MyHashMap search(String key) {

        int i = 0;
        while (i < currentNumOfKeys && key.compareToIgnoreCase(keys[i].getBusinessName()) > 0) {
            i++;
        }
        if (i != currentNumOfKeys) {
            if (keys[i] != null && keys[i].getBusinessName().equalsIgnoreCase(key)) {
                return this.keys[i];
            }
        }
        if(!leaf)
            return children[i].search(key);

//        if (leaf) {
                return null;
//            }

//        System.out.println("SEARCHING CHILD OF: " + keys[i].getBusinessName());
//        System.out.println("LEFT CHILD: " + children[i].keys[0].getBusinessName());
//        System.out.println("RIGHT CHILD: " + children[i+1].keys[0].getBusinessName());

    }

    void insert(MyHashMap key) {
        int i = currentNumOfKeys - 1;

        if (leaf) {

            while (i >= 0 && keys[i].getBusinessName().compareToIgnoreCase(key.getBusinessName()) > 0) {
                i--;
            }
            if (i != -1 && keys[i].getBusinessName().compareToIgnoreCase(key.getBusinessName()) == 0) {

            } else {
                i = currentNumOfKeys - 1;
                while (i >= 0 && keys[i].getBusinessName().compareToIgnoreCase(key.getBusinessName()) > 0) {
                    keys[i + 1] = keys[i];
                    i--;
                }
                keys[i + 1] = key;
                currentNumOfKeys += 1;
            }

        } else {

            while (i >= 0 && keys[i].getBusinessName().compareToIgnoreCase(key.getBusinessName()) > 0) {
                i--;
            }

            if (children[i + 1].currentNumOfKeys == ((2 * minDegree) - 1)) {
                splitChild(i + 1, children[i + 1]);

                if (keys[i + 1].getBusinessName().compareToIgnoreCase(key.getBusinessName()) < 0) {
                    i++;
                }
            }
            children[i + 1].insert(key);
        }
    }

    void splitChild(int index, MyNode previousRoot) {
        MyNode newNode = new MyNode(previousRoot.minDegree, previousRoot.leaf);
        newNode.currentNumOfKeys = minDegree - 1;
        newNode.title = this.title;

        for (int i = 0; i < minDegree - 1; i++) {
            newNode.keys[i] = previousRoot.keys[i + minDegree];
        }

        if (!previousRoot.leaf) {
            for (int i = 0; i < minDegree; i++) {
                newNode.children[i] = previousRoot.children[i + minDegree];
            }
        }

        previousRoot.currentNumOfKeys = minDegree - 1;

        for (int i = currentNumOfKeys; i >= index; i--) {
            children[i + 1] = children[i];
        }

        children[index + 1] = newNode;

        for (int i = currentNumOfKeys - 1; i >= index; i--) {
            keys[i + 1] = keys[i];
        }

        keys[index] = previousRoot.keys[minDegree - 1];

        currentNumOfKeys += 1;
    }
}

package com.company.dataStructures.bTree;

import com.company.dataStructures.MyHashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class MyBTree implements Serializable {
    public MyNode root;
    public int minDegree;
    private int size;


    public MyBTree(int minDegree) throws FileNotFoundException {
        this.minDegree = minDegree;
        this.root = null;
    }

    public void traverse() {
        if (this.root != null) {
            this.root.traverse();
        }
    }

    public MyHashMap getSpecificBusiness(int i) {
        return this.root.getBusinessByIndex(i, 0).myHashMap;
    }

    public ArrayList<String> businessNameArrayListRep() {
        return this.root.businessNameArrayListRep();
    }

    public int getSize() {
        return size;
    }

    public ArrayList<MyHashMap> getRandomBusinesses(int num) {
        ArrayList<MyHashMap> randomBusinesses = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < num; i++) {
            MyHashMap temp = this.root.getBusinessByIndex(random.nextInt(this.size), 0).myHashMap;
            if (temp == null) {
                i--;
                continue;
            }
            randomBusinesses.add(temp);
        }

        return randomBusinesses;
    }

    public MyHashMap search(String key) throws IOException {
//        System.out.println(key + size);

        if (this.root == null) {
            return null;
        } else {
            return this.root.search(key);
        }
    }

    public void insert(MyHashMap key) throws IOException {
        if (root == null) {
            root = new MyNode(minDegree, true);
            root.keys[0] = key;
            root.currentNumOfKeys = 1;
        } else {
            boolean rootIsFull = root.currentNumOfKeys == ((minDegree * 2) - 1);
            if (rootIsFull) {
                MyNode newRoot = new MyNode(minDegree, false);
                newRoot.children[0] = root;
                newRoot.splitChild(0, root);
                int i = 0;
                if (newRoot.keys[0].getBusinessName().compareToIgnoreCase(key.getBusinessName()) < 0) {
                    i++;
                }
                newRoot.children[i].insert(key);
                root = newRoot;
            } else {
                root.insert(key);
            }
        }
        size++;
    }

}

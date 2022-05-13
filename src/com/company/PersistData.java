package com.company;

import com.company.dataStructures.bTree.MyBTree;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;

public class PersistData {
    public static void BtreeToBytes(MyBTree myObject, String location) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(location,false);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            // write object to file
            oos.writeObject(myObject);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


//    public static void mapToBytes(Map<MyHashMap, MyBTree> map, String location) throws IOException {
//        try (FileOutputStream fos = new FileOutputStream(location,false);
//             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
//            // write object to file
//            oos.writeObject(map);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }
//    }
    public static void mapToBytes(Map map, String location) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(location,false);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            // write object to file
            oos.writeObject(map);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static Map bytesToMaps(String location) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(location);
        ObjectInputStream ois = new ObjectInputStream(fis);
        Map map = (Map) ois.readObject();
        return map;
    }

    public static MyBTree bytesToBtree(String location) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(location);
        ObjectInputStream ois = new ObjectInputStream(fis);
        MyBTree myBTree = (MyBTree) ois.readObject();
        return myBTree;
    }

    public static void listToBytes(ArrayList list, String location) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(location);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {

            // write object to file
            oos.writeObject(list);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static ArrayList bytesToList(String location) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(location);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList list = (ArrayList) ois.readObject();
        return list;
    }
}

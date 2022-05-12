package com.company.GUI;

import com.company.PersistData;
import com.company.dataStructures.KMeans.KMeans;
import com.company.dataStructures.MyHashMap;
import com.company.dataStructures.bTree.MyBTree;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class app2 extends JFrame {
    JFrame jFrame = this;
    JPanel panelMain;
    int bTreeOrder = 36;
    MyBTree businessTree;
    ArrayList<String> businessNames = new ArrayList<>();

    //File location variables
    final private String jsonFileLocation = "/1k_business_dataset.txt";
    final private String cleanedFileLocation = "/1k_business_dataset(cleaned).txt";
    final private String bTreeFileLocation = "/yelpBtree.dat";
    final private String businessNamesFileLocation = "/businessNames.dat";
    private JComboBox<String> comboBox1;
    private JComboBox<String> comboBox2;
    private JComboBox<String> comboBox3;
    private JComboBox<String> comboBox4;
    private JComboBox<String> comboBox5;
    private JComboBox[] comboBoxes = {comboBox1, comboBox2, comboBox3, comboBox4, comboBox5};

    final private DefaultComboBoxModel[] comboBoxModels = new DefaultComboBoxModel[5];

    public app2() throws IOException, ClassNotFoundException {
        super("K-Means");
        panelMain.setVisible(false);
        if(!new File(System.getProperty("user.dir") + bTreeFileLocation).exists() || !new File(System.getProperty("user.dir") + businessNamesFileLocation).exists()) {
            System.out.println("\u001B[36m Writing file");
            setHashMaps();
        }
        else {
            System.out.println("\u001B[35m Reading file");
            businessTree = PersistData.bytesToBtree(System.getProperty("user.dir") + bTreeFileLocation);
            businessNames = PersistData.bytesToList(System.getProperty("user.dir") + businessNamesFileLocation);
        }
        fillJComboBoxModel();
        setUpPage();
    }

    public void setUpPage(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        for(int i = 0; i < comboBoxes.length; i++) {
            comboBoxes[i].setPreferredSize(new Dimension(250, 28));
            comboBoxes[i].setFont(new Font("Consolas", Font.PLAIN, 18));
            comboBoxes[i].setForeground(Color.blue);
            comboBoxes[i].setBackground(Color.white);
            comboBoxes[i].setToolTipText("Cluster " + i);//setText("Choose a business");
            this.add(comboBoxes[i]);
        }

        this.pack();
        this.setSize(500,500);
        this.setVisible(true);
    }

    public void fillJComboBoxModel() throws IOException, ClassNotFoundException {

        Map<String, ArrayList<String>> clusters =  new KMeans(businessTree).fit(5,1, businessNames);

        int i = 0;
        for(Map.Entry<String, ArrayList<String>> entry: clusters.entrySet()){
            comboBoxModels[i] = new DefaultComboBoxModel<>();
            comboBoxModels[i].addAll(entry.getValue());
            comboBoxes[i].setModel(comboBoxModels[i]);
            comboBoxes[i].setMaximumRowCount(5);
            i++;
        }

    }

    @Deprecated
    private void setBTrees() throws IOException {

        //checks if a cleaned database file exist. If not, it creates one. (cleaned file == removed non-alphanumerics)
        File file;
        file = new File(System.getProperty("user.dir") + cleanedFileLocation);
        if(!file.isFile()) {
            cleanJsonFile();
            file = new File(System.getProperty("user.dir") + cleanedFileLocation);
        }
        Scanner getBusiness = new Scanner(file);
        Scanner getContentKeys;

        String business;
        //each business is on its own line in the file and this reads it as such
        //this will only work if the json file is in newLine-delimited format
        while (getBusiness.hasNextLine()) {
            business = getBusiness.nextLine();
            getContentKeys = new Scanner(business);
            MyBTree myBTree = null;
            for(int j = 0; getContentKeys.hasNext(); j++){
                if (j == 3){
                    //each business is the 4th word and followed by an address for this dataset
                    //as such we make those checks to find the business names
                    //THIS IS HACKY, USE Dynamic Json mapping (https://www.baeldung.com/jackson-mapping-dynamic-object)
                    StringBuilder concatBusinessName = new StringBuilder();
                    for (String temp = getContentKeys.next(); !temp.equalsIgnoreCase("address");){
                        concatBusinessName.append(temp).append(" ");

                        if (temp.equalsIgnoreCase("address")) {
//                            myBTree = new MyBTree(bTreeOrder, concatBusinessName.toString());
//                            myBTree.insert(temp); COMMENTED FOR ERRORS
                        }
                    }
                    continue;
                }

                assert myBTree != null;
//                myBTree.insert(getContentKeys.next()); COMMENTED FOR ERRORS
                if (!getContentKeys.hasNext()){
//                    businessList.add(myBTree); COMMENTED FOR ERROR
                }
            }
            getContentKeys.close();
        }
        getBusiness.close();
    }

    // Removes non-alphanumerics from the whole database file and adds spaces where there should be
    public void cleanJsonFile() throws IOException {

        File file = new File(System.getProperty("user.dir") + jsonFileLocation);
        FileReader fileReader = new FileReader(file);
        FileWriter fileWriter = new FileWriter(System.getProperty("user.dir") + cleanedFileLocation, false);
        Scanner scanner = new Scanner(fileReader);
        String newDoc;
        while (scanner.hasNext()){
            newDoc = scanner.nextLine().replaceAll("[: ,]", " ");
            newDoc = newDoc.replaceAll("[^a-zA-Z0-9 ]", "");
            fileWriter.write(newDoc + "\n");
        }
        fileReader.close();
        fileWriter.flush();
        fileWriter.close();
        scanner.close();
    }

    private void setHashMaps(){
        try {
            businessTree = new MyBTree(bTreeOrder);
            //checks if a cleaned database file exist. If not, it creates one. (cleaned file == removed non-alphanumerics)
            File file;
            file = new File(System.getProperty("user.dir") + cleanedFileLocation);
            if(!file.isFile()) {
                cleanJsonFile();
                file = new File(System.getProperty("user.dir") + cleanedFileLocation);
            }
            Scanner getBusiness = new Scanner(file);
            Scanner getContentKeys;
            String businessName;
            String business;
            //each business is on its own line in the file and this reads it as such
            //this will only work if the json file is in newLine-delimited format
            while (getBusiness.hasNextLine()) {
                business = getBusiness.nextLine();
                getContentKeys = new Scanner(business);
                MyHashMap myHashMap = new MyHashMap();
                for(int j = 0; getContentKeys.hasNext(); j++){
                    if (j == 3){
                        //each business is the 4th word and followed by an address for this dataset
                        //as such we make those checks to find the business names
                        //THIS IS HACKY, USE Dynamic Json mapping (https://www.baeldung.com/jackson-mapping-dynamic-object)
                        StringBuilder concatBusinessName = new StringBuilder();
                        for (String temp = getContentKeys.next(); !temp.equalsIgnoreCase("address");){
                            concatBusinessName.append(temp).append(" ");
                            myHashMap.put(temp);
                            temp = getContentKeys.next();
                            if (temp.equalsIgnoreCase("address"))
                                myHashMap.put(temp);
                        }
                        businessName = String.valueOf(concatBusinessName).trim();
                        myHashMap.setBusinessName(businessName);
                        businessNames.add(businessName);
                        continue;
                    }
                    String key = getContentKeys.next();
                    if (key.equals("latitude")){
                        myHashMap.setLatitude(Double.parseDouble(getContentKeys.next()));
                        continue;
                    }
                    else if (key.equals("longitude")){
                        myHashMap.setLongitude(Double.parseDouble(getContentKeys.next()));
                        continue;
                    }
                    myHashMap.put(key);
                    if (!getContentKeys.hasNext()){
                        businessTree.insert(myHashMap);
                    }
                }
                getContentKeys.close();
            }
            getBusiness.close();
            PersistData.BtreeToBytes(businessTree,System.getProperty("user.dir") + bTreeFileLocation);
            System.out.println("size"+businessNames.size());
            PersistData.listToBytes(businessNames,System.getProperty("user.dir") + businessNamesFileLocation);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

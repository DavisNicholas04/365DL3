package com.company.GUI;

import com.company.PersistData;
import com.company.dataStructures.KMeans.KMeans;
import com.company.dataStructures.MyHashMap;
import com.company.dataStructures.bTree.MyBTree;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class app2 extends JFrame implements ActionListener {
    JFrame jFrame = this;
    JPanel panelMain;
    int bTreeOrder = 36;
    MyBTree businessTree;
    ArrayList<String> businessNames = new ArrayList<>();
    LinkedList<MyHashMap> shortestPath;

    //File location variables
    final private String jsonFileLocation = "/1k_business_dataset.txt";
    final private String cleanedFileLocation = "/1k_business_dataset(cleaned).txt";
    final private String bTreeFileLocation = "/yelpBtree.dat";
    final private String businessNamesFileLocation = "/businessNames.dat";
    final private String clusterFileLocation = "/clusters.dat";
    private JComboBox<String> comboBox;
    private JButton submit;
    JPanel[] panels;
    JLabel[] labels;
    final private DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
    String chosenNode;
    String[] centroids;

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
        labels = new JLabel[businessNames.size()];
        submit.setText("Submit");
        submit.addActionListener(e -> {});
        submit.addActionListener(this);
        this.add(submit);
        comboBox.setPreferredSize(new Dimension(500, 28));
        comboBox.setFont(new Font("Consolas", Font.PLAIN, 18));
        comboBox.setForeground(Color.blue);
        comboBox.setBackground(Color.white);
        comboBox.setToolTipText("businesses");//setText("Choose a business");
        this.add(comboBox);


        this.pack();
        this.setSize(800,800);
        this.setVisible(true);
    }

    public void fillJComboBoxModel() throws IOException, ClassNotFoundException {
        Map<String, ArrayList<String>> clusters = new HashMap<>();
        if (!new File(System.getProperty("user.dir") + clusterFileLocation).exists() || !new File(System.getProperty("user.dir") + clusterFileLocation).exists()) {
            clusters = new KMeans(businessTree).fit(5, 1, businessNames);
        }
        else {
            clusters = PersistData.bytesToMaps(System.getProperty("user.dir") + clusterFileLocation);
            businessTree = PersistData.bytesToBtree(System.getProperty("user.dir") + bTreeFileLocation);
        }
        int i = 0;
        centroids = new String[clusters.size()];
        for(Map.Entry<String, ArrayList<String>> entry: clusters.entrySet()){
            centroids[i] = entry.getKey();
            comboBoxModel.addAll(entry.getValue());
            i++;
        }
        comboBox.setModel(comboBoxModel);
        comboBox.setMaximumRowCount(5);
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

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
            if (actionEvent.getSource() == submit) {
                chosenNode = Objects.requireNonNull(comboBox.getSelectedItem()).toString();
//                ArrayList<String> connectedCentroids = new ArrayList<>();
                try {
//                    connectedCentroids = connectedCentroids(chosenNode);
                    businessTree = calculateShortestPathFromSource(businessTree, businessTree.search(chosenNode));
                    shortestPath = businessTree.search(chosenNode).getShortestPath();
                    labels = new JLabel[shortestPath.size()];
                    panels = new JPanel[shortestPath.size()];

                } catch (IOException e) {
                    e.printStackTrace();
                }
                createLabels();
                createPanels();
                populateLabels();
                populatePanels();
                this.setSize(500, 500);

        }
    }

//    public static MyBTree calculateShortestPathFromSource(MyBTree graph, MyHashMap source) {
//        source.setDistance(0);
//
//        Set<MyHashMap> settledNodes = new HashSet<>();
//        Set<MyHashMap> unsettledNodes = new HashSet<>();
//        unsettledNodes.add(source);
//
//        while (unsettledNodes.size() != 0) {
//            MyHashMap currentNode = getLowestDistanceNode(unsettledNodes);
//            unsettledNodes.remove(currentNode);
//            for (MyHashMap adjacencyPair: currentNode.getNeighbors()) {
//                MyHashMap adjacentNode = adjacencyPair;
//                Double edgeWeight = adjacencyPair.getTFIDF();
//                if (!settledNodes.contains(adjacentNode)) {
//                    calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
//                    unsettledNodes.add(adjacentNode);
//                }
//            }
//            settledNodes.add(currentNode);
//        }
//        return graph;
//    }

    public static MyBTree calculateShortestPathFromSource(MyBTree graph, MyHashMap source) {
        source.setDistance(0);

        Set<MyHashMap> settledNodes = new HashSet<>();
        Set<MyHashMap> unsettledNodes = new HashSet<>();

        unsettledNodes.add(source);

        while (unsettledNodes.size() != 0) {
            MyHashMap currentNode = getLowestDistanceNode(unsettledNodes);
            unsettledNodes.remove(currentNode);
            for (MyHashMap adjacencyPair: currentNode.getNeighbors()) {
                MyHashMap adjacentNode = adjacencyPair;
                Double edgeWeight = adjacencyPair.getTFIDF();
                if (!settledNodes.contains(adjacentNode)) {
                    calculateMinimumDistance(adjacentNode, edgeWeight, currentNode);
                    unsettledNodes.add(adjacentNode);
                }
            }
            settledNodes.add(currentNode);
        }
        return graph;
    }

    private static MyHashMap getLowestDistanceNode(Set < MyHashMap > unsettledNodes) {
        MyHashMap lowestDistanceNode = null;
        double lowestDistance = Integer.MAX_VALUE;
        for (MyHashMap node: unsettledNodes) {
            double nodeDistance = node.getDistance();
            if (nodeDistance < lowestDistance) {
                lowestDistance = nodeDistance;
                lowestDistanceNode = node;
            }
        }
        return lowestDistanceNode;
    }
    private static void calculateMinimumDistance(MyHashMap evaluationNode, Double edgeWeight, MyHashMap sourceNode) {
        double sourceDistance = sourceNode.getDistance();
        if (sourceDistance + edgeWeight < evaluationNode.getDistance()) {
            evaluationNode.setDistance(sourceDistance + edgeWeight);
            LinkedList<MyHashMap> shortestPath = new LinkedList<>(sourceNode.getShortestPath());
            shortestPath.add(sourceNode);
            evaluationNode.setShortestPath(shortestPath);
        }
    }

    private ArrayList<String> connectedCentroids(String chosenNode) throws IOException {
        ArrayList<String> connected = new ArrayList<>();
        for (String centroid : centroids) {
            int centroidSet = businessTree.search(centroid).getDisjointSet();
            int chosenSet = businessTree.search(chosenNode).getDisjointSet();
            if (centroidSet == chosenSet)
                connected.add(centroid);
        }
        return connected;
    }

    private void createLabels(){
        for(int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel();
        }
    }
    //add text to the label
    private void populateLabels(){
        for(int i = 0; i < labels.length; i++) {
            labels[i].setText(businessNames.get(i));
        }
    }
    private void createPanels() {
        panels = new JPanel[businessNames.size()];
        int height = 25;
        int width = 500;
        for (int i = 0; i < panels.length; i++) {
            panels[i] = new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(width, height);
                }
            };
            panels[i].setBounds(0, height * i, width, height);
        }
    }
    //add labels to the panels
    private void populatePanels(){
        int rgb = 200;
        for (int i = 0; i < panels.length; i++) {
            panels[i].setBackground(new Color(rgb, rgb, rgb));
            panels[i].add(labels[i]);
            labels[i].setVerticalAlignment(JLabel.CENTER);
            jFrame.add(panels[i]);
        }
        jFrame.pack();
    }
}

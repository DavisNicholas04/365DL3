package com.company.GUI;

import com.company.TFIDF.SortByTFIDF_Asgnt1;
import com.company.TFIDF.TFIDF_Asgnt1;
import com.company.TFIDF.Term;
import com.company.dataStructures.MyHashMap;
import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.*;

public class app extends JFrame implements ActionListener {

    //JFame setup variables
    JFrame jFrame = this;
    JPanel panelMain;
    JPanel[] panels;
    JLabel[] labels;
    private JComboBox<String> jComboBox;
    final private DefaultComboBoxModel<String> comboBoxModel = new DefaultComboBoxModel<>();
    private JButton submit;

    //File location variables
    final private String jsonFileLocation = "/10k_business_dataset.txt";
    final private String cleanedFileLocation = "/10k_business_dataset(cleaned).txt";

    //limit
    int numOfBusinessesToDisplay = 10;

    //myHashmaps is a Hashmap that contains all the individual business hashTables
    //allHashmaps is explained in the comment for allToOneHashMap function
    //sortedBusiness is used to list the x most similar businesses (where x is numOfBusinessesToDisplay)
    HashMap<String, MyHashMap> myHashMaps = new HashMap<>();
    MyHashMap allHashmaps = new MyHashMap();
    List<MyHashMap> sortedBusinesses;

    //Business used to find other similar business
    String comparatorBusiness;

    //constructor that enables comboBox and button functionality
    //& Takes care of the autocomplete box via the imported library swingx-all-1.6.4
    public app() {
        super("Compare Businesses");
        panelMain.setVisible(false);
        setHashMaps();
        allToOneHashmap();
        fillJComboBoxModel();
        AutoCompleteDecorator.decorate(jComboBox);
        submit.setText("Submit");
        submit.addActionListener(e -> {});
        jFrame.getRootPane().setDefaultButton(submit);
        labels = new JLabel[numOfBusinessesToDisplay];
        createLabels();
        createPanels();
        setUpPage();
    }

    /**Basic page setUp.
     * This will enable the program to halt when the application is closed.
     * Adds functionality to the button, designs the text field, adds everything to the JFrame.
     * Sets the JFrame to visible so that it actually displays.
     */
    public void setUpPage(){
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLayout(new FlowLayout());

        submit.addActionListener(this);

        jComboBox.setPreferredSize(new Dimension(250, 28));
        jComboBox.setFont(new Font("Consolas", Font.PLAIN, 18));
        jComboBox.setForeground(Color.blue);
        jComboBox.setBackground(Color.white);
        jComboBox.setToolTipText("Choose a business");//setText("Choose a business");

        this.add(submit);
        this.add(jComboBox);
        this.pack();
        this.setSize(500,500);
        this.setVisible(true);
    }

    /**Changes the suggested element in text field based on what is currently typed*/
    public void fillJComboBoxModel(){
        String[] keys = myHashMaps.keySet().toArray(new String[0]);
        for(String elements: keys) {
            comboBoxModel.addElement(elements);
        }
        jComboBox.setModel(comboBoxModel);
        jComboBox.setMaximumRowCount(5);
    }

    /**Runs TF-IDF calculation on every key in the comparator hashmap (the document you want to compare against)*/
    private void makeTfIdf(){
        //Layer 1
        for(MyHashMap maps: myHashMaps.values()) {
            maps.setTfidf(0);
            //Layer 2
            for (LinkedList<Term> ll : myHashMaps.get(comparatorBusiness).map) {
                if (ll == null)
                    continue;
                for (Term key : ll) {
                    maps.addToTfidf(TFIDF_Asgnt1.CalculateTFIDF(allHashmaps, maps, key.key, myHashMaps.size()));
                }
            }
        }
    }

    /**Puts all of the hashmaps terms into one hashmap,
     * in turn it records the number of documents that each word appears in due to how the myHashmaps put method works
     */
    public void allToOneHashmap(){
        //Layer 1
        for(MyHashMap maps: myHashMaps.values()) {
            //Layer 2
            for (LinkedList<Term> ll : maps.map) {
                if (ll == null)
                    continue;
                for (Term key : ll) {
                    allHashmaps.put(key.key);
                }
            }
        }
    }

    //Needed for the implements actionListener
    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (actionEvent.getSource() == submit){
            comparatorBusiness = Objects.requireNonNull(jComboBox.getSelectedItem()).toString();
            makeTfIdf();
            setSortedBusinesses();
            populateLabels();
            populatePanels();
            this.setSize(500,500);
        }
    }
    private void createLabels(){
        for(int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel();
        }
    }
    //add text to the label
    private void populateLabels(){
        for(int i = 0; i < labels.length; i++) {
            labels[i].setText(sortedBusinesses.get(i).getBusinessName());
        }
    }
    private void createPanels() {
        panels = new JPanel[numOfBusinessesToDisplay];
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

    /**
     * Reads a json file and puts the different objects into their own hashmap.
     * Stores the information inside an arrayList for sorting later.
     * */
    private void setHashMaps(){
        try {
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

                        myHashMap.setBusinessName(String.valueOf(concatBusinessName).trim());
                        continue;
                    }
                    myHashMap.put(getContentKeys.next());
                    if (!getContentKeys.hasNext()){
                        myHashMaps.put(myHashMap.getBusinessName(),myHashMap);
                    }
                }
                getContentKeys.close();
            }
            getBusiness.close();
            sortedBusinesses = new ArrayList<>(myHashMaps.values());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSortedBusinesses(){
        SortByTFIDF_Asgnt1 sortByTFIDF = new SortByTFIDF_Asgnt1();
        sortedBusinesses.sort(sortByTFIDF);
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
}

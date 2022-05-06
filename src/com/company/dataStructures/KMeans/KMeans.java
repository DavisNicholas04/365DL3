package com.company.dataStructures.KMeans;

import com.company.PersistData;
import com.company.TFIDF.SortByTFIDF_Asgnt1;
import com.company.TFIDF.TFIDF_Asgnt1;
import com.company.TFIDF.Term;
import com.company.dataStructures.MyHashMap;
import com.company.dataStructures.bTree.MyBTree;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.util.stream.Collectors.toList;

public class KMeans {
    static final String disjointSetFileLocation = System.getProperty("user.dir") + "/disjointSet.dat";
    static Map<Integer, ArrayList<String>> disjointSets = new HashMap<>();

    public static Map<String, ArrayList<String>> fit(MyBTree records, int k, int maxIterations, ArrayList<String> businessNames) throws IOException, ClassNotFoundException {
        List<MyHashMap> centroids = records.getRandomBusinesses(k);//randomCentroids(records, k);
        Map<MyHashMap, MyBTree> clusters = new HashMap<>();
//        Map<MyHashMap, MyBTree> lastState = new HashMap<>();

        ArrayList<String> fileNames = new ArrayList<>();

        // iterate for a pre-defined number of times
        for (int i = 0; i < maxIterations; i++) {

            boolean isLastIteration = i == maxIterations - 1;

            // in each iteration we should find the nearest centroid for each record
            for (int j = 0; j < records.getSize(); j++) {

                System.out.println("iteration: " + j + "||||| businessName: " + businessNames.get(j));
                MyHashMap currentBusiness = records.search(businessNames.get(j));

                if (i == 0 && !new File(disjointSetFileLocation).exists()) {
                    int[] indexOfNearestNeighbors = findNearestNeighbors(currentBusiness, records, businessNames);
                    MyHashMap[] nearestNeighbors = {
                            records.search(businessNames.get(indexOfNearestNeighbors[0])),
                            records.search(businessNames.get(indexOfNearestNeighbors[1])),
                            records.search(businessNames.get(indexOfNearestNeighbors[2])),
                            records.search(businessNames.get(indexOfNearestNeighbors[3]))
                    };
                    currentBusiness.setNeighbors(nearestNeighbors);
                }
                System.out.println("CURRENT BUSINESS: " + currentBusiness.getBusinessName() + "\n" +
                                   "Nearest Neighbors: " + currentBusiness.getNeighbors()[0].getBusinessName() + " ||| "
                                                         + currentBusiness.getNeighbors()[1].getBusinessName() + " ||| "
                                                         + currentBusiness.getNeighbors()[2].getBusinessName() + " ||| "
                                                         + currentBusiness.getNeighbors()[3].getBusinessName());
                MyHashMap centroid = nearestCentroid(currentBusiness, centroids);
                //store business names for centroids in files for later use
                String fileName = System.getProperty("user.dir") +"/9_"+centroid.getBusinessName() + ".txt";
                File file = new File(fileName);
                if (!fileNames.contains(fileName))
                    fileNames.add(fileName);
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(businessNames.get(j)+"\n");
                fileWriter.close();


                int x = j;
                clusters.compute(centroid, (key, bTree) -> { try {
                    if (bTree == null)
                        bTree = new MyBTree(36);
                    bTree.insert(records.search(businessNames.get(x)));

                } catch (IOException e) {e.printStackTrace();}
                return bTree;
                });
            }

            // if the assignments do not change, then the algorithm terminates
//            boolean shouldTerminate = isLastIteration || clusters.equals(lastState);

            PersistData.mapToBytes(clusters, System.getProperty("user.dir") + "currentState.dat");
            File lastState2 = new File(System.getProperty("user.dir") + "lastState.dat");
            File currentState2 = new File(System.getProperty("user.dir") + "currentState.dat");

            boolean shouldTerminate2 = isLastIteration || FileUtils.contentEquals(lastState2, currentState2);//Use if you getanother overflow

            System.out.println("TERMINATE CHECK");
//            lastState = clusters;
            PersistData.mapToBytes(clusters, System.getProperty("user.dir") + "lastState.dat");
            if (shouldTerminate2) {
                System.out.println("TERMINATING");
                removeFiles(fileNames);
                break;
            }

            // at the end of each iteration we should relocate the centroids
            System.out.println("RELOCATING CENTROIDS");
            centroids = relocateCentroids(clusters);
            removeFiles(fileNames);
            fileNames.clear();
            clusters = new HashMap<>();
        }


        Map<String, ArrayList<String>> finalClusters = new HashMap<>();
        ArrayList<String> value;
        String key;
        for(Map.Entry<MyHashMap, MyBTree> entry: clusters.entrySet()){
            key = entry.getKey().getBusinessName();
            value = entry.getValue().businessNameArrayListRep();
            finalClusters.put(key, value);
        }


        if (!new File(disjointSetFileLocation).exists()){
            for (int i = 0; i < records.getSize(); i++){
                ArrayList<String> targetSet = new ArrayList<>();
                ArrayList<String> destinationSet = new ArrayList<>();
                ArrayList<String> newGroup = new ArrayList<>();
                MyHashMap currentBusiness = records.search(businessNames.get(i));
                if (currentBusiness.unassignedDisjointSet()){

                    //NEIGHBOR CHECK: check if neighbors are assigned to a group
                    //if so, assign current business with the smallest group number
                    int smallestGroupNum = Integer.MAX_VALUE;
                    for (MyHashMap neighbor : Arrays.asList(currentBusiness.getNeighbors())) {
                        if(!neighbor.unassignedDisjointSet() && neighbor.getDisjointSet() <= smallestGroupNum){
                            smallestGroupNum = neighbor.getDisjointSet();
                            currentBusiness.setDisjointSet(smallestGroupNum);
                        }
                    }
                    //check if currentBusiness got assigned a group during NEIGHBOR CHECK.
                    //if so; adds it, all neighbors, and all business in the same group as the neighbors to the newly assigned group.
                    //removes reference to old group neighbors were in.
                    if (!currentBusiness.unassignedDisjointSet()){
                        for (MyHashMap neighbor : currentBusiness.getNeighbors()) {
                            if (!neighbor.unassignedDisjointSet() && neighbor.getDisjointSet() != currentBusiness.getDisjointSet()) {
                                targetSet = disjointSets.get(neighbor.getDisjointSet());
                                destinationSet = disjointSets.get(currentBusiness.getDisjointSet());

                                for (String s : targetSet) {
                                    records.search(s).setDisjointSet(currentBusiness.getDisjointSet());
                                }
                            }else if (neighbor.unassignedDisjointSet()) {
                                destinationSet = disjointSets.get(currentBusiness.getDisjointSet());
                                targetSet.add(neighbor.getBusinessName());
                            }
                            destinationSet.addAll(targetSet);
                            disjointSets.remove(neighbor.getDisjointSet());
                            disjointSets.put(currentBusiness.getDisjointSet(), destinationSet);
                            targetSet.clear();
                        }
                        destinationSet = disjointSets.get(currentBusiness.getDisjointSet());
                        destinationSet.add(currentBusiness.getBusinessName());
                        disjointSets.put(currentBusiness.getDisjointSet(), destinationSet);

                    }
                    else{
                        currentBusiness.setDisjointSet(i);
                        newGroup.add(currentBusiness.getBusinessName());
                        for (MyHashMap neighbor : currentBusiness.getNeighbors()) {
                            neighbor.setDisjointSet(i);
                            newGroup.add(neighbor.getBusinessName());
                        }
                        disjointSets.put(i, newGroup);
                        newGroup.clear();
                    }
                }
                else{
                    for (MyHashMap neighbor : Arrays.asList(currentBusiness.getNeighbors())) {
                        if (!neighbor.unassignedDisjointSet()){
                            if(currentBusiness.getDisjointSet() < neighbor.getDisjointSet()){
                                moveDisjointSets(currentBusiness, neighbor, records);
                            }
                            else if (currentBusiness.getDisjointSet() > neighbor.getDisjointSet()){
                                targetSet = disjointSets.get(currentBusiness.getDisjointSet());
                                destinationSet = disjointSets.get(neighbor.getDisjointSet());
                                for (String s : targetSet) {
                                    records.search(s).setDisjointSet(neighbor.getDisjointSet());
                                }
                                destinationSet.addAll(targetSet);
                                disjointSets.remove(currentBusiness.getDisjointSet());
                                disjointSets.put(neighbor.getDisjointSet(), destinationSet);
                            }
                        }
                        else {
                            neighbor.setDisjointSet(currentBusiness.getDisjointSet());
                            destinationSet = disjointSets.get(currentBusiness.getDisjointSet());
                            destinationSet.add(neighbor.getBusinessName());
                            disjointSets.put(currentBusiness.getDisjointSet(), destinationSet);
                        }
                    }
                }
            }
        }
        else
            disjointSets = PersistData.bytesToMaps(disjointSetFileLocation);
        int j = -2;
        for (Map.Entry<Integer, ArrayList<String>> entry: disjointSets.entrySet()) {
            if (entry.getKey() != j){
                System.out.println();
                j = entry.getKey();
            }
            System.out.print("[" + entry.getKey() + "] " + entry.getValue() + " ||| ");
        }
            PersistData.mapToBytes(disjointSets, disjointSetFileLocation);
            PersistData.BtreeToBytes(records, System.getProperty("user.dir") + "/yelpBtree.dat" );
//        if (!new File(disjointSetFileLocation).exists()) {
//            AtomicReference<File> disjointSetFile = new AtomicReference<>();
//            AtomicReference<FileWriter> fileWriter = new AtomicReference<>();
//            for (int i = 0, j = 0; i < records.getSize(); i++) {
//                int x = j;
//                MyHashMap currentRecord = records.search(businessNames.get(i));
//                if (currentRecord.getDisjointSet() == -1) {
//                    Arrays.asList(currentRecord.getNeighbors()).forEach( neighbor -> {
//                        if (neighbor.getDisjointSet() != -1) {
//                            currentRecord.setDisjointSet(neighbor.getDisjointSet());
//                            disjointSetFile.set(new File(System.getProperty("user.dir") + "/" + x + "_disjointSet.txt"));
//                            try {
//                                fileWriter.set(new FileWriter(disjointSetFile.get(), true));
//                                fileWriter.get().write(currentRecord.getBusinessName()+"---");
//                            } catch (IOException e) {e.printStackTrace();}
//                        }
//                    });
//                    if (currentRecord.getDisjointSet() != -1)
//                        continue;
//                    disjointSetFile.set(new File(System.getProperty("user.dir") + "/" + j + "_disjointSet.txt"));
//                    fileWriter.set(new FileWriter(disjointSetFile.get(), true));
//                    createSet(records.search(businessNames.get(i)), j, fileWriter.get());
//                    System.out.println();
//                    j++;
//                }
//            }
//            PersistData.listToBytes(new ArrayList(), disjointSetFileLocation);
//            PersistData.objectToBytes(records, System.getProperty("user.dir") + "/yelpBtree.dat" );
//        }
        return finalClusters;
    }

    private static void moveDisjointSets(MyHashMap destination, MyHashMap target, MyBTree records) throws IOException {
        ArrayList<String> targetSet;
        ArrayList<String> destinationSet;
        targetSet = disjointSets.get(target.getDisjointSet());
        destinationSet = disjointSets.get(destination.getDisjointSet());
        for (String s : targetSet) {
            records.search(s).setDisjointSet(destination.getDisjointSet());
        }
        destinationSet.addAll(targetSet);
        disjointSets.remove(target.getDisjointSet());
        disjointSets.put(destination.getDisjointSet(), destinationSet);
    }

//    private static void createSet(MyHashMap myHashMap, int groupNum, FileWriter fileWriter) throws IOException {
//        myHashMap.setDisjointSet(groupNum);
//        fileWriter.write(myHashMap.getBusinessName()+"---");
//        System.out.print("[" + groupNum + "] " + myHashMap.getBusinessName() + " ||| ");
//        for(int i = 0; i < 4; i++){
//            if (myHashMap.getNeighbors()[i].getDisjointSet() == -1){
//                createSet(myHashMap.getNeighbors()[i], groupNum, fileWriter);
//            }
//        }
//    }

    private static MyHashMap average(MyHashMap centroid, MyBTree records) throws IOException {
        if (records == null || records.getSize() == 0) {
            return centroid;
        }
        double[] tfidfs = new double[records.getSize()];
        ArrayList<String> businessNames = new ArrayList<>();
        String fileName = System.getProperty("user.dir") +"/9_"+centroid.getBusinessName() + ".txt";
        File file = new File(fileName);
        FileReader fileReader = new FileReader(file);
        Scanner scanner = new Scanner(fileReader);
        StringBuilder sb = new StringBuilder();
        while (scanner.hasNext())
            businessNames.add(scanner.nextLine());
        fileReader.close();

//        businessNames = sb.toString().split("\n");

        for(int i = 0; i < records.getSize(); i++) {
            MyHashMap currentHashMap = records.search(businessNames.get(i));
            if (currentHashMap == null){
                continue;
            }
//            System.out.println("AVERAGE FUNCTION CURRENT HASHMAP: |" + centroid.getBusinessName() + "| "+ currentHashMap.getBusinessName() + "  " + records.getSize() + " of " + i);
            currentHashMap.setTfidf(0);
            for (LinkedList<Term> ll : centroid.map) {
                if (ll == null)
                    continue;
                for (Term key : ll) {
                    currentHashMap.addToTfidf(TFIDF_Asgnt2.CalculateTFIDF(records, currentHashMap, key.key, records.getSize(), businessNames));
                }
            }
            tfidfs[i] = currentHashMap.getTFIDF();
        }

        int meanIndex = findMeanIndex(tfidfs);
        MyHashMap myHashMap = records.search(businessNames.get(meanIndex));
        System.out.printf("NEW CENTROID: %s ---> %s\n",centroid.getBusinessName() , myHashMap.getBusinessName());
        return myHashMap;
    }

    private static List<MyHashMap> relocateCentroids(Map<MyHashMap, MyBTree> clusters) {
        System.out.print("---List of My OLD Centroids---: ");
        for(Map.Entry<MyHashMap, MyBTree> entry: clusters.entrySet()){
            System.out.print(entry.getKey().getBusinessName() + "|||");
        }
        System.out.println();
        List<MyHashMap> myHashMaps = clusters.entrySet().stream().map(e -> {
            try {
                return average(e.getKey(), e.getValue());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            return null;
        }).collect(toList());
        System.out.printf("List of My new Centroids: %s ||| %s ||| %s ||| %s ||| %s\n",
                myHashMaps.get(0).getBusinessName(),
                myHashMaps.get(1).getBusinessName(),
                myHashMaps.get(2).getBusinessName(),
                myHashMaps.get(3).getBusinessName(),
                myHashMaps.get(4).getBusinessName());
        return myHashMaps;
    }

    public static int findMeanIndex(double[] tfidfs){
        double mean = 0;
        double smallest = tfidfs[0];
        int smallestIndex = 0;
        for (double value : tfidfs) {
            mean += value;
        }
        mean /= tfidfs.length;

        int i = 0;
        for (double tfidf : tfidfs) {
            if (Math.abs(tfidf - mean) < smallest) {
                smallest = Math.abs(tfidf - mean);
                smallestIndex = i;
            }
            i++;
        }
        System.out.print("TFIDFS: ");
        for (int j = 0; j < tfidfs.length; j++)
            System.out.print(tfidfs[j] + ", ");
        System.out.println("\nSmallest Index: " + smallestIndex);
        return smallestIndex;
    }

    private static MyHashMap nearestCentroid(MyHashMap record, List<MyHashMap> centroids) {
        for(MyHashMap maps: centroids) {
            maps.setTfidf(0);
            //Layer 2
            for (LinkedList<Term> ll : record.map) {
                if (ll == null)
                    continue;
                for (Term key : ll) {
                    maps.addToTfidf(TFIDF_Asgnt1.CalculateTFIDF(allToOneHashmap(centroids), maps, key.key, centroids.size()));
                }
            }
        }

        SortByTFIDF_Asgnt1 sortByTFIDF = new SortByTFIDF_Asgnt1();
        centroids.sort(sortByTFIDF);

        return centroids.get(0);
    }

    public static MyHashMap allToOneHashmap(List<MyHashMap> myHashMaps){
        //Layer 1
        MyHashMap allHashMaps = new MyHashMap();
        for(MyHashMap maps: myHashMaps) {
            //Layer 2
            for (LinkedList<Term> ll : maps.map) {
                if (ll == null)
                    continue;
                for (Term key : ll) {
                    allHashMaps.put(key.key);
                }
            }
        }
        return allHashMaps;
    }

    private static void removeFiles(ArrayList<String> fileNames){
        for (String fileName : fileNames) {
            File file = new File(fileName);
            System.out.println(file.exists()+ "|||||||||||||||||||||||||||||||"+fileName);
            if (!file.delete())
                System.out.println("-------------------------NOT DELETED------------------------------");
            else
                System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^deleted^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^");
        }

    }

    private static int[] findNearestNeighbors(MyHashMap myHashMap, MyBTree myBTree, ArrayList<String> businessNames) throws IOException {
        ArrayList<Double> distances = new ArrayList();
        for(int i = 0; i < myBTree.getSize(); i++){
            distances.add(haversine(
                    myHashMap.getLatitude(),
                    myHashMap.getLongitude(),
                    myBTree.search(businessNames.get(i)).getLatitude(),
                    myBTree.search(businessNames.get(i)).getLongitude()
            ));
        }

        double[] nearestFourDistances = findFourSmallestDoubles(distances);

        int[] indexOfNearestNeighbors = new int[4];
        for (int i = 0; i < 4; i++) {
            indexOfNearestNeighbors[i] = distances.indexOf(nearestFourDistances[i]);
        }
        return indexOfNearestNeighbors;
    }

    private static double[] findFourSmallestDoubles(ArrayList<Double> distances){
        SortDoubles sortDoubles = new SortDoubles();
        ArrayList<Double> temp = (ArrayList<Double>) distances.clone();
        temp.sort(sortDoubles);
        double[] fourSmallestDistances = new double[4];
        int finish = 4;
        for(int i = 0, j = 0; i < finish; i++, j++) {
            if (i != 0){
                if (temp.get(i) == fourSmallestDistances[j - 1]) {
                    j--;
                    finish++;
                    continue;
                }
            }
            fourSmallestDistances[j] = temp.get(i);
        }
        return fourSmallestDistances;
    }

    static double haversine(double lat1, double lon1,
                            double lat2, double lon2)
    {
        // distance between latitudes and longitudes
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        // apply formula
        double a = Math.pow(Math.sin(dLat / 2), 2) +
                Math.pow(Math.sin(dLon / 2), 2) *
                        Math.cos(lat1) *
                        Math.cos(lat2);
        double rad = 6371;
        double c = 2 * Math.asin(Math.sqrt(a));
        return rad * c;
    }

}

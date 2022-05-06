package com.company.dataStructures.KMeans;

import com.company.TFIDF.Term;
import com.company.dataStructures.MyHashMap;
import com.company.dataStructures.bTree.MyBTree;

import java.io.IOException;
import java.util.ArrayList;

public class TFIDF_Asgnt2 {
    //Calculates the TF by divided the number of times the term appears
    // by the total number of terms in a document
    public static double tf(MyHashMap doc, String term){
        if (doc.getTerm(term) == null){
            return 0;
        }
        return (double) doc.getTerm(term).numberOfTimesWordUsed / doc.size;
    }

    //Calculates the IDF by counting the number of documents the term shows up in (docFrequency)
    //and taking the log of the total number of documents divided by the docFrequency.
//    public static double idf(MyHashMap myHashMaps, String term, int numOfDocuments){
//        double docFrequency = 0;
//        double idf;
//        Term valueFound = myHashMaps.getTerm(term);
//        if (valueFound != null)
//            docFrequency = valueFound.numberOfTimesWordUsed;
//        if (docFrequency == 0)
//            return 0;
//        idf = Math.log(numOfDocuments / docFrequency );
//        return idf;
//    }

    public static double idf(MyBTree myBTree, String term, int numOfDocuments, ArrayList<String> businessNames) throws IOException {
        double docFrequency = 0;
        double idf;

        for(int i = 0; i < myBTree.getSize(); i++) {
            MyHashMap currentHashMap = myBTree.search(businessNames.get(i));
//            if (currentHashMap == null){
//                continue;
//            }
//            System.out.println("TFIDF -- CURRENT HASHMAP: " + currentHashMap.getBusinessName() +"  "+ myBTree.getSize() + " of " + i);

            Term valueFound = currentHashMap.getTerm(term);
            if (valueFound != null)
                docFrequency++;
            if (docFrequency == 0)
                return 0;
        }


        idf = Math.log(numOfDocuments / docFrequency );
        return idf;
    }

    //Calculates the tf-idf value for a given term by multiplying the TF and the IDF
    public static double CalculateTFIDF(MyBTree myBTree, MyHashMap doc, String term, int numOfDocuments, ArrayList<String> businessNames) throws IOException {
        return tf(doc, term) * idf(myBTree, term, numOfDocuments, businessNames);
    }
}
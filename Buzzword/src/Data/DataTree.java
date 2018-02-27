package Data;

import ui.AppMessageDialogSingleton;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Afzal on 11/9/2016.
 */
public class DataTree {
    // Trie data structure
    DataTreeNode root;

    private String randomWord;
    int randomIndex = 0;
    Random random = new Random();

    public DataTree(){
        root = new DataTreeNode();
    }

    public DataTree(int minWordLength, int mode){
        root = new DataTreeNode();
        randomIndex = random.nextInt(500);
        loadData(minWordLength, mode);
    }


    private void loadData(int minwordlength, int mode){
        // if we are playing general category, load general
        File dictionary;
        if (mode == 1) {
             dictionary = new File(this.getClass().getClassLoader().getResource("").getPath() + "/words/create.txt");
        }
        else if (mode == 2) { // names
             dictionary = new File(this.getClass().getClassLoader().getResource("").getPath() + "/words/names.txt");
        }

        else if (mode == 3) {
             dictionary = new File(this.getClass().getClassLoader().getResource("").getPath() + "/words/medical.txt");
        }

        else {
             dictionary = new File(this.getClass().getClassLoader().getResource("").getPath() + "/words/general.txt");
        }
            // create IO stream to read
            try{
                // standard scanner to read from file
                Scanner s = new Scanner(new BufferedReader(new FileReader(dictionary)));
                int i = 1;
                while (s.hasNext()){
                    String word = s.nextLine();
                    // read word, and if it has something besides a letter, replace it with a blank
                    word = word.replaceAll("[^A-Za-z0-9]", "");

                    // 16 is max length for board, so words longer than that are filtered.
                    if (word.length() >= minwordlength && word.length() <= 16) {
                        if (i == randomIndex){
                            randomWord = word;
                        }

                        insert(word);
                        i++;
                    }
                }
                s.close();
            }
            catch (Exception e){
                e.printStackTrace();
                AppMessageDialogSingleton.getSingleton().show("Error", "Word File not found");
            }
        }

    /*****
     * insert()
     * @param word : word to insert
     * @return boolean : if insertion was sucessful or not
     * recursive method. each iteration cuts the length of the word by 1
     * if the lenght is 0, the current node is a 'completed' node
     * else, we get the child node position by subtracting first letter of string by 'A'
     * since 'A' is the first uppercase letter
     */
    public boolean insert(String word){
        DataTreeNode tempNode = root; // start at root
        // use helper method
        return helpInsert(word.toUpperCase(), tempNode);
    }

    public String getRandomWord(){
        return randomWord;
    }


    private boolean helpInsert(String word, DataTreeNode currentNode){
        if (word.length() == 0){
            if (currentNode.completed)
                return false; // this word is already in the tree

            else{
                currentNode.completed = true; // mark it as a word
                return true; // return true because inserted
            }
        }
        // still has letters, so insert in proper children
        else{
            int position = word.charAt(0) - 'A'; // get first letter
            //position = position - 'A'; // subtract uppercase A

            // if the node does not have a child at that position, create one
            if (currentNode.children[position] == null){
                currentNode.children[position] = new DataTreeNode();

                // move the pointer
                currentNode = currentNode.children[position];
                // remove first letter, do again
                return helpInsert(word.substring(1), currentNode);
            }
            // if the node has a child at that position, dont create new, but just call method again
            else{
                return helpInsert(word.substring(1), currentNode.children[position]);
            }
        }
    }

    /***
     * checks if the word is in the trie
     * @return
     */
    public boolean contains(String word){
        DataTreeNode temp = root; // reset to top of tree

        return helpContain(word.toUpperCase(), temp);
    }

    private boolean helpContain(String word, DataTreeNode currentNode){
        // if we are at last character, return its completed status
        if (word.length() == 0){
            return currentNode.completed;
        }
        // otherwise, keep going through the children
        else{
            int position = word.charAt(0) - 'A'; // get next position that we will search

            if (currentNode.children[position] == null)
                return false;

            else
                return helpContain(word.substring(1), currentNode.children[position]);
        }
    }

    // check if a certain substring exists; cuts seaching time when looking for possible words
    public boolean containsSubstring(String word){
        DataTreeNode temp = root;

        return helpContainsSubstring(word.toUpperCase(), temp);
    }

    /**
     * helpcontainssubstring
     * @param word
     * @param currentNode
     * @return boolean
     * unlike the contains method, this will always return true if we get to the end of a word and a node exists for it.
     * this is useful because then if we are searching words, we can start the search from here as opposed to beginning from the root again
     */
    private boolean helpContainsSubstring(String word, DataTreeNode currentNode){
        if (word.length() == 0){
            return true;
        }

        else{
            int position = word.charAt(0) - 'A';

            if (currentNode.children[position] == null)
                return false;

            else
                return helpContainsSubstring(word.substring(1), currentNode.children[position]);
        }
    }


    // THE NODE DATA STRUCTURE
    private class DataTreeNode{
        // will have 26 children, each letter of alphabet
        DataTreeNode[] children;

        // boolean to keep track of if word ends at this node
        boolean completed;

        public DataTreeNode(){
            completed = false;
            children = new DataTreeNode[26];
        }
    }
}

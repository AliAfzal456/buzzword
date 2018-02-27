package Data;

import Buzzword.Buzzword;
import GUI.Workspace;
import apptemplate.AppTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import components.AppDataComponent;

import java.util.List;

/**
 * Created by Afzal on 11/9/2016.
 */
public class BuzzwordData implements AppDataComponent {

    @JsonIgnore
    private Board gameBoard;
    @JsonIgnore
    private DataTree wordTree;
    private int currentLevel;
    @JsonIgnore
    private boolean isChanged;

    public static DataTree GeneralWords, NameWords, MedicalWords, GeneralV2Words;



    private int wordLength;

    /* game data */
    private String userName;
    private String password;
    private int[] levelsUnlocked;
    private int[] highestScores1, highestScores2, highestScores3, highestScores4;
    private int highestPointTotal;

    public BuzzwordData(){

    }

    public BuzzwordData(AppTemplate appTemplate){
        GeneralWords = new DataTree(3,1);
        NameWords = new DataTree(3,2);
        MedicalWords = new DataTree(3,3);
        GeneralV2Words = new DataTree(3,4);
    }

    @Override
    public void reset() {

    }

    // mode 0: general
    // 1 : names
    // 2 : medical
    // 3 : unknown for now
    // level 1: 3 letter words and up
    // 2 : 4 letter words and up
    // 3 : 5 letter words and up
    // 4 : 6 letter words and up
    public void createGrid(int level, int mode){
        if (level == 1 || level ==2)
            wordLength = 3;

        else if (level == 3 || level ==4)
            wordLength = 3;

        else if (level ==5 || level ==6)
            wordLength = 4;

        else
            wordLength = 4;

        if (mode == 0)
            gameBoard = new Board(wordLength, GeneralWords);

        if (mode == 1)
            gameBoard = new Board(wordLength, NameWords);

        if (mode == 2)
            gameBoard = new Board(wordLength, MedicalWords);

        if (mode == 3)
            gameBoard = new Board(wordLength, GeneralV2Words);
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getUserName(){
        return userName;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getPassword(){
        return password;
    }

    public void setLevels(int[] levels){
        levelsUnlocked = levels;
    }

    public void setLevelsUnlocked(int maxLevel, int index){
        levelsUnlocked[index] = maxLevel;
    }

    public int[] getLevelsUnlocked(){
        return levelsUnlocked;
    }

    public void setCurrentLevel(int level){
        currentLevel = level;
    }

    public int getCurrentLevel(){
        return currentLevel;
    }

    public void setHighestScores1(int[] x){ highestScores1 = x;}

    public int[] getHighestScores1(){ return highestScores1;}

    public void setHighestScores2(int[] x) { highestScores2 = x;}

    public int[] getHighestScores2(){ return highestScores2;}

    public void setHighestScores3(int[] x) { highestScores3 = x;}

    public int[] getHighestScores3(){ return highestScores3;}

    public void setHighestScores4(int[] x) { highestScores4 = x;}

    public int[] getHighestScores4(){ return highestScores4;}

    public void setHighestPointTotal(int x){ highestPointTotal =x;}

    public int getHighestPointTotal(){ return highestPointTotal;}

    @JsonIgnore
    public String getBoard(){
        return gameBoard.getGrid();
    }

    @JsonIgnore
    public char[][] getBoardAgain(){
        return gameBoard.getBoard();
    }

    @JsonIgnore
    public List<String> getWords(){
        for (int i =0; i<4; i++){
            for (int j = 0; j < 4; j++){
                gameBoard.solveBoard(i,j);
            }
        }
        return gameBoard.getWords();
    }

}

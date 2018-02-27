package Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Afzal on 11/9/2016.
 */
public class Board {
    private char[][] board;

    private boolean[][] visited;
    private List<String> wordsInBoard;
    private int minWordLength;

    private Random random = new Random();

    private StringBuilder word;


    DataTree tree;

    /* possible letters on the dice
    1. A E A N E G      9. W N G E E H
    2. A H S P C O     10. L N H N R Z
    3. A S P F F K     11. T S T I Y D
    4. O B J O A B     12. O W T O A T
    5. I O T M U C     13. E R T T Y L
    6. R Y V D E L     14. T O E S S I
    7. L R E I X D     15. T E R W H V
    8. E I U N E S     16. N U I H M Qu
     */

    public Board(int minWordLength, DataTree tree) {
        // start by constructing constructing the trie
        this.tree = tree;
        this.minWordLength = minWordLength;
        wordsInBoard = new ArrayList<>();
        word = new StringBuilder(16); // max length of word is 16

        // this boolean array will keep track of letters that are visited while searching for a word
        visited = new boolean[4][4];

        board = new char[4][4];

        board[0][0] = getLetter("AEANEG".toCharArray());
        board[0][1] = getLetter("AHSPCO".toCharArray());
        board[0][2] = getLetter("ASPFFK".toCharArray());
        board[0][3] = getLetter("OBJOAB".toCharArray());
        //board[0] = tree.getRandomWord().toCharArray();

        board[1][0] = getLetter("IOTMUC".toCharArray());
        board[1][1] = getLetter("RYVDEL".toCharArray());
        board[1][2] = getLetter("LREIXD".toCharArray());
        board[1][3] = getLetter("EIUNES".toCharArray());

        board[2][0] = getLetter("WNGEEH".toCharArray());
        board[2][1] = getLetter("LNHNRZ".toCharArray());
        board[2][2] = getLetter("TSTIYD".toCharArray());
        board[2][3] = getLetter("OWTOAT".toCharArray());

        board[3][0] = getLetter("ERTTYL".toCharArray());
        board[3][1] = getLetter("TOESSI".toCharArray());
        board[3][2] = getLetter("TERWHV".toCharArray());
        board[3][3] = getLetter("NUIHMQ".toCharArray());

    }

    public String getGrid(){
        String toReturn = "";
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                toReturn += this.board[i][j];
            }
        }
        return toReturn;
    }

    public char[][] getBoard(){
        return board;

    }

    private char getLetter(char[] letters){
        return (letters[random.nextInt(6)]);
    }


    public List<String> getWords(){
        return wordsInBoard;
    }



    public void solveBoard(int row, int col){
        if (board[row][col] == 'Q') {
            word.append(board[row][col]);
            word.append('U'); // add the letter to the current word that we have
        }

        else
            word.append(board[row][col]);

        visited[row][col] = true; // we have visited this location, so we cannot use this letter again

        // if the words is in the tree, and its of the required length, print it out
        if(word.length() >= minWordLength && tree.contains(word.toString())){
            wordsInBoard.add(word.toString());
        }

        try {
            if (visited[row - 1][col - 1] == false && tree.containsSubstring((word.toString()) + board[row - 1][col - 1]))
                solveBoard(row - 1, col - 1); // take the top left one
        }// if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            if (visited[row - 1][col] == false && tree.containsSubstring((word.toString()) + board[row - 1][col]))
                solveBoard(row - 1, col); // take the top left one
        }// if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            if (visited[row - 1][col + 1] == false && tree.containsSubstring((word.toString()) + board[row - 1][col + 1]))
                solveBoard(row - 1, col + 1); // take the top left one
        }// if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }


        try {
            if (visited[row][col - 1] == false && tree.containsSubstring((word.toString()) + board[row][col - 1]))
                solveBoard(row, col - 1); // take the top left one
        }// if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            if (visited[row][col + 1] == false && tree.containsSubstring((word.toString()) + board[row][col + 1]))
                solveBoard(row, col + 1); // take the top left one
        }
        // if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }


        try {
            if (visited[row + 1][col - 1] == false && tree.containsSubstring((word.toString()) + board[row + 1][col - 1]))
                solveBoard(row + 1, col - 1); // take the top left one
        }
        // if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            if (visited[row + 1][col] == false && tree.containsSubstring((word.toString()) + board[row + 1][col]))
                solveBoard(row + 1, col); // take the top left one
        }
        // if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }

        try {
            if (visited[row + 1][col + 1] == false && tree.containsSubstring((word.toString()) + board[row + 1][col + 1]))
                solveBoard(row + 1, col + 1); // take the top left one
        }// if no such index exists, then break out of the method
        catch (ArrayIndexOutOfBoundsException e) {
        }

        // after the recursive call either finds a word(or doesnt find one, we rest the value of the current letter to unusd
        visited[row][col] = false; // reset the visited value

        // IMPORTANT:::: REMOVE ONLY 1 LETTER FROM THE WORD
        word.deleteCharAt(word.length()-1);
    }
}

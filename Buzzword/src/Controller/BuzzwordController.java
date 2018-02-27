package Controller;

import Buzzword.Buzzword;
import Data.BuzzwordData;
import Data.DataTree;
import GUI.Workspace;
import Managers.DataManager;
import apptemplate.AppTemplate;
import controller.FileController;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Line;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;
import GUI.ScrollablePane;
import ui.YesNoCancelDialogSingleton;

import java.awt.*;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import static Buzzword.BuzzwordProperties.*;

/**
 * Created by Afzal on 11/9/2016.
 */
public class BuzzwordController implements FileController {
    private AppTemplate appTemplate;
    private Workspace   workspace;
    private BuzzwordData data;

    AnimationTimer t;
    String boardAsString;

    private Button[][] board;
    private int oldWordLength, newWordLength = 0;
    private boolean firstLetter = true;
    private boolean found = false;
    private boolean foundMore = false;

    private Pane buttonPane;

    private int currLevel, currMode;

    ArrayList<String> s;
    ArrayList<Integer>locations = new ArrayList<>();
    ArrayList<Integer>locationsToAdd = new ArrayList<>();
    private int indices = 0;
    private StringBuilder[][] guessedWord = new StringBuilder[4][4];
    private boolean[][] inUse = new boolean[4][4];
    private boolean[][] justVisited = new boolean[4][4];
    private boolean foundThisRun = false;
    ArrayList<Integer> targets = new ArrayList<>();
    private StringBuilder currentWord = new StringBuilder();
    private StringBuilder inProgress = new StringBuilder();

    private ArrayList<Point>[][] paths = new ArrayList[16][1]; // 16 rows, 1 column. Points array to hold locations
    private ArrayList<Point>[][] tempPath = new ArrayList[16][1];
    private ArrayList<Point> pathsToRemove = new ArrayList<>();
    private StringBuilder targetWord = new StringBuilder();
    private int targetScore;
    private ArrayList<Line> lines = new ArrayList<>();
    DropShadow borderGlow = new DropShadow();

    private boolean releaseAll = false;

    public BuzzwordController(AppTemplate appTemplate, Button test){
        this.appTemplate = appTemplate;
    }

    public BuzzwordController(AppTemplate appTemplate){
        this.appTemplate = appTemplate;
    }

    private void askIfSure(){
        YesNoCancelDialogSingleton yesNoCancelDialogSingleton = YesNoCancelDialogSingleton.getSingleton();
        yesNoCancelDialogSingleton.show("Leave?", "Are you sure you want to leave your game? Progress will not be saved if you do.");

        if (yesNoCancelDialogSingleton.getSelection().equals(YesNoCancelDialogSingleton.YES)) {
            Workspace.state = Workspace.GameState.LOGGED_IN_HOME;
            if (t != null){
                stopType = 1;
                t.stop();
                t= null;
            }
            workspace.updateGUI();
        }

        else if (Workspace.state == Workspace.GameState.PAUSED){}

        //else
        //  Workspace.state = Workspace.GameState.RESUME_GAME;
    }

    @Override
    public void handleHomeRequest() {
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();

        if (Workspace.state == Workspace.GameState.IN_GAME || Workspace.state == Workspace.GameState.PAUSED || Workspace.state == Workspace.GameState.RESUME_GAME){
            Workspace.state = Workspace.GameState.PAUSED;
            workspace.getPauseButton().setText(PropertyManager.getManager().getPropertyValue(PAUSE));
            workspace.getPauseButton().fire();
            workspace.pausedExitGui();

            askIfSure();
        }

        else if (workspace.isLoggedIn()) {
            Workspace.state = Workspace.GameState.LOGGED_IN_HOME;
            workspace.updateGUI();
        }

        else {
            Workspace.state = Workspace.GameState.LOGGED_OUT_HOME;

            workspace.updateGUI();
        }
    }

    @Override
    public void handleLoginRequest() throws IOException {
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();

        if (Workspace.state == Workspace.GameState.IN_GAME || Workspace.state == Workspace.GameState.PAUSED || Workspace.state == Workspace.GameState.RESUME_GAME){
            Workspace.state = Workspace.GameState.PAUSED;
            workspace.getPauseButton().fire();
            workspace.pausedExitGui();

            askIfSure();
        }

        else {
            Workspace.state = Workspace.GameState.LOGIN_PROMPT;
            workspace.updateGUI();
        }
    }

    @Override
    public void handleNewUserRequest() throws IOException {
        Workspace.state = Workspace.GameState.NEW_USER_PROMPT;
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();
        workspace.updateGUI();

    }

    @Override
    public void handleModeRequest(int selection) {
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();
        workspace.setModeSelection(selection+1);

        if (Workspace.state == Workspace.GameState.LEVEL_SELECT)
            workspace.updateGUI();
    }

    @Override
    public void handlePlayRequest(){
        Workspace.state = Workspace.GameState.LEVEL_SELECT;
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();
        workspace.updateGUI();
    }

    @Override
    public void handleHelpRequest(){
        Workspace.state = Workspace.GameState.HELP_DIALOG;
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();
        workspace.updateGUI();
    }

    public void startGame(int level, int mode){
        currLevel = level;
        currMode = mode;
        data = (BuzzwordData)appTemplate.getDataComponent();
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();

        data.createGrid(level, mode);


        // if there are no words, we redo
        s = (ArrayList<String>)data.getWords();

        while (s.size() == 0){
            data.createGrid(level, mode);
            s = (ArrayList<String>)data.getWords();
        }

        workspace.updateLetterGrid(data.getBoard().toCharArray());
        // remove duplicate words
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(s);
        s.clear();
        s.addAll(set);

        //Collections.sort(s);

        System.out.println(s.size()); // print size for comparsion


        int maxScore = 0;

        // get max score and show it on the display
        for (String word : s){
            //System.out.println(word);
            if (word.length() == 3 || word.length() == 4)
                maxScore += 1;

            else if (word.length() == 5)
                maxScore += 2;

            else if (word.length() == 6)
                maxScore += 3;

            else if (word.length() == 7)
                maxScore += 5;

            else
                maxScore += 11;
        }

        if (maxScore < 30) {
            startGame(level, mode);
            return;
        }
        targetScore = 0;

        if (level == 1 || level == 5)
            targetScore = 20;

        else if (level == 2 || level == 6)
            targetScore = 24;

        else if (level == 3 || level == 7)
            targetScore = 28;

        else
            targetScore = 30;

        board = workspace.getGameNodes();
        boardAsString = data.getBoard();

        workspace.setMaxScore(maxScore, targetScore);
        System.out.println("");

        found = false;
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++) {
                guessedWord[i][j] = new StringBuilder(16);
                board[i][j].setDisable(false);
                inUse[i][j] = false;
                justVisited[i][j] = false;
            }
        }

        for (int i = 0; i < 16; i++){
            paths[i][0] = null;
        }
        countdown = 100;
        currentWord = new StringBuilder();
        workspace.getCurrentWordLabel().setText("");
        workspace.setScore(0);
        firstLetter = true;
        foundThisRun = false;
        inProgress = new StringBuilder();
        targetWord = new StringBuilder();
        this.buttonPane = workspace.getButtonPane();

        int depth = 200; //Setting the uniform variable for the glow width and height

        borderGlow.setOffsetY(0f);
        borderGlow.setOffsetX(0f);
        borderGlow.setColor(javafx.scene.paint.Color.RED);
        borderGlow.setWidth(depth);
        borderGlow.setHeight(depth);

        for (int i = 0; i <4; i++){
            for (int j = 0; j<4; j++){
                board[i][j].setOnDragDetected(e -> {
                    Button b = (Button) e.getSource();
                    inProgress = new StringBuilder();
                    workspace.getCurrentWordLabel().setText(inProgress.toString());

                    mouseX = b.getLayoutX() + b.getMinWidth()/2;
                    mouseY = b.getLayoutY() + b.getMinHeight() /2;

                    b.startFullDrag();

                });

                board[i][j].setOnMouseDragEntered(e ->{
                    Button b = (Button)e.getSource();
                    if (mouseX == -1 && mouseY == -1){
                        mouseX = b.getLayoutX() + b.getMinWidth()/2;
                        mouseY = b.getLayoutY() + b.getMinHeight() /2;
                    }

                    else if (nMouseX == -1 && nMouseY == -1){
                        nMouseX = b.getLayoutX() + b.getMinWidth()/2;
                        nMouseY = b.getLayoutY() + b.getMinHeight() /2;
                    }

                    if (!(mouseX == -1) && (!(mouseY == -1)) && (!(nMouseX == -1)) && (!(nMouseY == -1))){
                        Line line = new Line();
                        line.setStrokeWidth(3);

                        line.setStartX(mouseX);
                        line.setStartY(mouseY);
                        line.setEndX(nMouseX);
                        line.setEndY(nMouseY);
                        line.toBack();
                        lines.add(line);
                        buttonPane.getChildren().add(line);

                        mouseX = nMouseX;
                        mouseY = nMouseY;
                        nMouseX = -1;
                        nMouseY = -1;
                    }
                });

                board[i][j].setOnMouseDragExited(e -> {
                    Button b = (Button) e.getSource();

                    if ((!(b.contains(e.getX(), e.getY()))) || releaseAll) {
                        inProgress.append(b.getText());
                        b.setDisable(true);
                        workspace.getCurrentWordLabel().setText(inProgress.toString());

                        if (mouseX == -1 && mouseY == -1) {
                            mouseX = b.getLayoutX() + b.getMinWidth() / 2;
                            mouseY = b.getLayoutY() + b.getMinHeight() / 2;
                        } else if (nMouseX == -1 && nMouseY == -1) {
                            nMouseX = b.getLayoutX() + b.getMinWidth() / 2;
                            nMouseY = b.getLayoutY() + b.getMinHeight() / 2;
                        }

                        if (!(mouseX == -1) && (!(mouseY == -1)) && (!(nMouseX == -1)) && (!(nMouseY == -1))) {
                            Line line = new Line();
                            line.setStrokeWidth(3);

                            line.setStartX(mouseX);
                            line.setStartY(mouseY);
                            line.setEndX(nMouseX);
                            line.setEndY(nMouseY);
                            line.toBack();
                            lines.add(line);
                            buttonPane.getChildren().add(line);

                            mouseX = nMouseX;
                            mouseY = nMouseY;
                            nMouseX = -1;
                            nMouseY = -1;
                        }
                    }


                        if (releaseAll) {
                            releaseAll = false;

                            if (s.contains(inProgress.toString())) {
                                s.remove(inProgress.toString());

                                if (inProgress.toString().length() == 3 || inProgress.toString().length() == 4)
                                    workspace.addWord(inProgress.toString(), 1);

                                else if (inProgress.toString().length() == 5)
                                    workspace.addWord(inProgress.toString(), 2);

                                else if (inProgress.toString().length() == 6)
                                    workspace.addWord(inProgress.toString(), 3);

                                else if (inProgress.toString().length() == 7)
                                    workspace.addWord(inProgress.toString(), 5);

                                else
                                    workspace.addWord(inProgress.toString(), 11);
                            }

                            for (int x = 0; x < 4; x++) {
                                for (int y = 0; y < 4; y++) {
                                    board[x][y].setDisable(false);
                                }
                            }

                            for (Line l : lines) {
                                buttonPane.getChildren().remove(l);
                            }

                            inProgress = new StringBuilder();
                            //workspace.getCurrentWordLabel().setText(inProgress.toString());
                        }
                });

                board[i][j].setOnMouseDragReleased(e ->{
                    Button b = (Button)e.getSource();
                    workspace.getCurrentWordLabel().setText(inProgress.toString());
                    releaseAll = true;


                });
            }
        }

        play();
    }

    private double mouseX = -1, mouseY = -1, nMouseX = -1, nMouseY = -1;

    public void handleSaveRequest() {
        DataManager manager = (DataManager) appTemplate.getFileComponent();

        BuzzwordData b = (BuzzwordData)appTemplate.getDataComponent();

        try {
            manager.saveData(appTemplate.getDataComponent(), new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/").toPath());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    public void resetGame(int level, int mode){
        data = (BuzzwordData)appTemplate.getDataComponent();
        this.workspace = (Workspace)appTemplate.getWorkspaceComponent();

        if (Workspace.state == Workspace.GameState.IN_GAME || Workspace.state == Workspace.GameState.RESUME_GAME){
            Workspace.state = Workspace.GameState.PAUSED;
            workspace.getPauseButton().setText(PropertyManager.getManager().getPropertyValue(RESUME));
            workspace.updateGUI();
        }

        YesNoCancelDialogSingleton singleton = YesNoCancelDialogSingleton.getSingleton();
        singleton.show("Are You Sure?", "Are you sure you want to reset the game?");

        if (singleton.getSelection() == YesNoCancelDialogSingleton.YES) {
            Workspace.state = Workspace.GameState.IN_GAME;
            workspace.updateGUI();

            workspace.getPauseButton().setText(PropertyManager.getManager().getPropertyValue(PLAY));
            workspace.getPauseButton().fire();
        }
    }

    long lastNanoTime = 0;
    private int countdown = 100;
    private int stopType;

    private void play(){
        t = new AnimationTimer(){
            @Override
            public void handle(long now){
                if (Workspace.state == Workspace.GameState.PAUSED || Workspace.state == Workspace.GameState.PAUSED_EXIT || Workspace.state == Workspace.GameState.END_GAME){

                }

                else if (Workspace.state == Workspace.GameState.LOGGED_IN_HOME){
                    stopType = 1;
                    super.stop();
                }

                // if the game is NOT paused, we can count the timer down and update it in the GUI
                else{
                    appTemplate.getGUI().getPrimaryScene().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent E) -> {
                        if (E.getCode() == KeyCode.ENTER) {
                            oldWordLength = 0;
                            newWordLength = 0;
                            // get length of longest word. will be used to deactivate nodes that are no longer needed
                            for (int i = 0; i < 4; i++) {
                                for (int j = 0; j < 4; j++) {
                                    newWordLength = guessedWord[i][j].length();
                                    if (oldWordLength <= newWordLength) {
                                        oldWordLength = newWordLength;
                                    }
                                }
                            }

                            /*for (int i = 0; i < 16; i++){
                                // a non null path means its the longest path (or identical to another long path)
                                if (paths[i][0] != null){
                                    // for each point in the path, get its letter and append to the current word
                                    for (int j = 0; j < paths[i][0].size(); j++){
                                        currentWord.append(board
                                                [paths[i][0].get(j).x]
                                                [paths[i][0].get(j).y].getText());
                                    }
                                }
                            } */

                            if (s.contains(inProgress.toString())) {
                                s.remove(inProgress.toString());
                                if (inProgress.toString().length() == 3 || inProgress.toString().length() == 4)
                                    workspace.addWord(inProgress.toString(), 1);

                                else if (inProgress.toString().length() == 5)
                                    workspace.addWord(inProgress.toString(), 2);

                                else if (inProgress.toString().length() == 6)
                                    workspace.addWord(inProgress.toString(), 3);

                                else if (inProgress.toString().length() == 7)
                                    workspace.addWord(inProgress.toString(), 5);

                                else
                                    workspace.addWord(inProgress.toString(), 11);

                            }

                            found = false;
                            for (int i = 0; i < 4; i++){
                                for (int j = 0; j < 4; j++) {
                                    guessedWord[i][j] = new StringBuilder(16);
                                    board[i][j].setDisable(false);
                                    inUse[i][j] = false;
                                    justVisited[i][j] = false;
                                }
                            }

                            for (Line l : lines){
                                buttonPane.getChildren().remove(l);
                            }
                            workspace.getCurrentWordLabel().setText("");
                            inProgress = new StringBuilder();

                            for (int i = 0; i < 16; i++){
                                paths[i][0] = null;
                            }
                            currentWord = new StringBuilder();
                            targetWord = new StringBuilder();
                            firstLetter = true;
                            foundThisRun = false;
                        }
                    });

                    appTemplate.getGUI().getPrimaryScene().setOnKeyTyped((KeyEvent event) -> {
                        char guess = event.getCharacter().charAt(0);

                        // if the board has the letter, we can highlight it
                        if (boardAsString.contains(Character.toString(guess).toUpperCase())){
                            for (int i = 0 ; i < boardAsString.length(); i ++) {

                                // get the index of selection
                                // if its the first letter, we can highlight all instances. Otherwise, we will highlight only connected nodes
                                if (firstLetter) {
                                    if (boardAsString.charAt(i) == Character.toUpperCase(guess)) {
                                        found = true;
                                        //board[i / 4][i % 4].setStyle("-fx-border-color: blue; -fx-background-color: gray; -fx-border-radius: 10px;");
                                        board[i / 4][i % 4].setDisable(true); // disable to mark/prevent clicking
                                        inUse[i / 4][i % 4] = true;

                                        paths[i][0] = new ArrayList<Point>();
                                        paths[i][0].add(new Point(i/4, i%4)); // point where we currently are
                                    }
                                }

                                // not the first letter, so we
                                else {
                                    // if the guess is in the board
                                    // look at surrounding places.
                                    // add current location to be highlighted later
                                    // add current location to be in use later (same as above)
                                    // add the location of the old 'new' to be unmarked afterwards
                                    // add the letter to the word
                                    if (boardAsString.charAt(i) == Character.toUpperCase(guess)) {

                                        int row = i / 4; // row is /4
                                        int col = i % 4; // col is %4

                                        int rowStart  = Math.max( row - 1, 0   );
                                        int rowFinish = Math.min( row + 1, board.length - 1 );
                                        int colStart  = Math.max( col - 1, 0   );
                                        int colFinish = Math.min( col + 1, board.length - 1 );

                                        // check the surrounding valid neighbors to see if they've ever been marked as visited
                                        for ( int curRow = rowStart; curRow <= rowFinish; curRow++ ) {
                                            for ( int curCol = colStart; curCol <= colFinish; curCol++ ) {
                                                if (board[curRow][curCol].isDisable() && inUse[curRow][curCol]){ // if its disabled, we have a winner
                                                    if (!(board[row][col].isDisable()))
                                                        foundMore = true;

                                                    for (int j = 0; j < 16; j++){
                                                        // if its not null, check it for current path
                                                        if (paths[j][0] != null && paths[j][0].size() > 0){
                                                            // if the last position is the one we're at, we work with this path
                                                            if (paths[j][0].get(paths[j][0].size()-1).x == curRow &&
                                                                    paths[j][0].get(paths[j][0].size() -1).y == curCol){

                                                                // after we mark for removal, we add a new path at end of array
                                                                paths[row*4 + col][0] = new ArrayList<Point>();

                                                                for(Point p : paths[j][0]){
                                                                    pathsToRemove.add(p);
                                                                    paths[row * 4 + col][0].add(new Point(p.x, p.y));
                                                                }
                                                                paths[row*4 + col][0].add(new Point(row, col));
                                                            }
                                                        }
                                                    }

                                                    // no longer the freshest, so mark for downgrade
                                                    locations.add(curRow);
                                                    locations.add(curCol);

                                                    justVisited[row][col] = true;

                                                    locationsToAdd.add(row);
                                                    locationsToAdd.add(col);
                                                }
                                            }
                                        }
                                    }
                                }
                            }


                            if (found)
                                firstLetter = false;

                            for (int i = 0; i < locations.size(); i+=2){
                                inUse[locations.get(i)][locations.get(i+1)] = false;
                            }

                            for (int i = 0; i < locationsToAdd.size(); i+=2){
                                board[locationsToAdd.get(i)][locationsToAdd.get(i+1)].setDisable(true);
                                inUse[locationsToAdd.get(i)][locationsToAdd.get(i+1)] = true; // this node is now the last one to have been chosen
                            }

                            locations.clear();
                            locationsToAdd.clear();

                            // longest previous routes
                            int oldLongRoute = 0, newLongRoute = 0;
                            for (int i = 0; i < 16; i++){
                                if (paths[i][0] != null){
                                    newLongRoute = paths[i][0].size(); // get size of current route and compre
                                    if (oldLongRoute <= newLongRoute){
                                        oldLongRoute = newLongRoute;
                                        targets.add(i);
                                    }

                                    else
                                        paths[i][0] = null;
                                }
                            }
                            boolean firstWord = true;

                            for (int i = 0; i < 4; i++){
                                for (int j = 0; j < 4; j++){
                                    board[i][j].toFront();
                                    board[i][j].setDisable(false);
                                   // board[i][j].getStyleClass().removeAll();
                                    //board[i][j].getStyleClass().a

                                    inUse[i][j] = false;
                                }
                            }

                            for (int i = 0; i < targets.size()-1; i++){
                                if (paths[targets.get(i)][0].size() < inProgress.length() +1)//< paths[targets.get(i+1)][0].size())
                                    paths[targets.get(i)][0] = null;
                            }

                            double oldX = -1, oldY = -1, newX = -1, newY = -1; // all the line locations are 0

                            for (Line line : lines){
                                buttonPane.getChildren().remove(line);
                            }
                            lines.clear();

                            //ArrayList<Point> toKeep;
                            workspace.getCurrentWordLabel().setText("");

                                for (int i = 0; i < targets.size(); i++) {
                                    oldX = oldY = newX = newY = -1;
                                    if (paths[targets.get(i)][0] != null) {

                                        for (int j = 0; j < paths[targets.get(i)][0].size(); j++) {
                                            if (oldX == -1 && oldY == -1){
                                                oldX = board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getLayoutX() +
                                                        board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getMinWidth()/2;
                                                oldY = board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getLayoutY() +
                                                        board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getMinHeight()/2;
                                            }

                                            else if (newX == -1 && newY == -1) {
                                                newX = board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getLayoutX() +
                                                        board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getMinWidth()/2;
                                                newY = board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getLayoutY() +
                                                        board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getMinHeight()/2;
                                            }
                                            // we have two values for buttons stored, so connect them with a line
                                            if (!(newX == -1) && !(newY == -1) && !(oldX == -1) && !(oldY == -1)){
                                                Line line = new Line();
                                                line.setStrokeWidth(3);

                                                line.setStartX(oldX);
                                                line.setStartY(oldY);
                                                line.setEndX(newX);
                                                line.setEndY(newY);
                                                line.toBack();
                                                lines.add(line);
                                                buttonPane.getChildren().add(line);

                                                oldX = newX;
                                                oldY = newY;
                                                newX = -1;
                                                newY = -1;
                                            }


                                            if (j == paths[targets.get(i)][0].size() - 1) {
                                                // last index, so mark newest
                                                inUse[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y] = true;
                                                // if first word, its naturally also the longest, and so we add its letters to the boar display
                                                if (i == targets.size()-1 && (foundMore || found)) {
                                                    // inProgress.delete(0,j);
                                                    inProgress.append(board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getText());
                                                    //workspace.getCurrentWordLabel().setText(workspace.getCurrentWordLabel().getText().concat(
                                                    //              board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].getText()));
                                                }
                                            }
                                            // make this path visible

                                            board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].setDisable(true);
                                            board[paths[targets.get(i)][0].get(j).x][paths[targets.get(i)][0].get(j).y].setEffect(borderGlow);
                                        }

                                    /*toKeep = paths[targets.get(i)][0];
                                    for (int j = 0; j < toKeep.size(); j++) {
                                        if (j == toKeep.size() - 1) // last index
                                            inUse[toKeep.get(j).x][toKeep.get(j).y] = true;

                                        board[toKeep.get(j).x][toKeep.get(j).y].setDisable(true);
                                    } */
                                    }
                                }


                            firstWord = false;
                            foundMore = false;
                            found = false;


                            targetWord.append(inProgress.toString());
                            workspace.getCurrentWordLabel().setText(inProgress.toString());
                            //inProgress.delete(0, inProgress.length()-1);

                            targets.clear();
                        }
                    });



                    // if a second has passed, we reset
                    long currenttimeNano = System.nanoTime();
                    if (currenttimeNano > lastNanoTime + 1000000000)
                    {
                        countdown-=1;
                        lastNanoTime = currenttimeNano;
                        workspace.getTimeLabel().setText("Time Left: " + countdown + " seconds");
                    }

                    if(countdown <= 0){
                        stopType = -1;
                        this.stop();
                    }
                }
            }

            @Override
            public void stop(){
                super.stop();

                if (stopType != 1) {
                    end();
                }
            }
        };
        t.start();
    }

    private void end(){

        Platform.runLater(new Runnable() {
            @Override public void run() {
                int currentScore = workspace.getScore(); // the score for this level that we just scored.

                ScrollablePane pane = new ScrollablePane(appTemplate.getPrimaryStage());

                String message = "";
                int oldHighScore = 0;


                if (currMode == 0) {
                    oldHighScore = data.getHighestScores1()[currLevel-1];
                    int[] scores = new int[8];
                    if (currentScore > oldHighScore){
                        AppMessageDialogSingleton.getSingleton().show("New Best!", "You scored a personal best!\nUp from " + oldHighScore + " to " + currentScore);
                        for (int i = 0; i < data.getHighestScores1().length; i++){
                            scores[i] = data.getHighestScores1()[i];
                            if (i == currLevel -1)
                                scores[i] = data.getHighestScores1()[i] = currentScore;
                        }
                    }
                }

                if (currMode == 1){
                    oldHighScore = data.getHighestScores2()[currLevel-1];
                    int[] scores = new int[8];
                    if (currentScore > oldHighScore){
                        AppMessageDialogSingleton.getSingleton().show("New Best!", "You scored a personal best!\nUp from " + oldHighScore + " to " + currentScore);
                        for (int i = 0; i < data.getHighestScores2().length; i++){
                            scores[i] = data.getHighestScores2()[i];
                            if (i == currLevel -1)
                                scores[i] = data.getHighestScores2()[i] = currentScore;
                        }
                    }
                }

                if (currMode == 2){
                    oldHighScore = data.getHighestScores3()[currLevel-1];
                    int[] scores = new int[8];
                    if (currentScore > oldHighScore){
                        AppMessageDialogSingleton.getSingleton().show("New Best!", "You scored a personal best!\nUp from " + oldHighScore + " to " + currentScore);
                        for (int i = 0; i < data.getHighestScores3().length; i++){
                            scores[i] = data.getHighestScores3()[i];
                            if (i == currLevel -1)
                                scores[i] = data.getHighestScores3()[i] = currentScore;
                        }
                    }
                }

                if (currMode == 3){
                    oldHighScore = data.getHighestScores4()[currLevel-1];
                    int[] scores = new int[8];
                    if (currentScore > oldHighScore){
                        AppMessageDialogSingleton.getSingleton().show("New Best!", "You scored a personal best!\nUp from " + oldHighScore + " to " + currentScore);
                        for (int i = 0; i < data.getHighestScores4().length; i++){
                            scores[i] = data.getHighestScores4()[i];
                            if (i == currLevel -1)
                                scores[i] = data.getHighestScores4()[i] = currentScore;
                        }
                    }
                }

                // save the data now
                try {
                    appTemplate.getFileComponent().saveData(appTemplate.getDataComponent(), new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/").toPath());
                }

                catch (IOException e){}

                message = "Unfortunately you lost. The list of all words in this board is: \n";
                if (currentScore < targetScore){
                    for (String s1 : s){
                        message += "\n" + s1;
                    }

                    pane.getCloseButton().setOnAction(e -> pane.close());
                    pane.setButtonText("OK");

                    pane.show("Message", message + "\nSorry! Try again");
                }

                else{
                    message = "You won! The next level is now unlocked\nYour highest score for this level was: " + oldHighScore;
                    data.getLevelsUnlocked()[currMode]+=1;

                    pane.setListener(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            Workspace.state = Workspace.GameState.IN_GAME;
                            workspace.setLevelSelected(currLevel+1);
                            workspace.updateGUI();
                            pane.close();
                        }
                    });

                    pane.setButtonText("Continue To Next Level!");
                    pane.show("Message", message + "\nPress the continue button to play the next level" +
                            "\nIf you would not like to play the next level, simply hit the 'x' button on this window");
                }

                Workspace.state = Workspace.GameState.END_GAME;
                appTemplate.getGUI().getPrimaryScene().setOnKeyTyped(e -> {});
            }
        });
    }
}
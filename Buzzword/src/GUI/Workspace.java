package GUI;
import Controller.BuzzwordController;
import Data.BuzzwordData;
import Data.Word;
import apptemplate.AppTemplate;
import components.AppWorkspaceComponent;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.TextAlignment;
import javafx.stage.StageStyle;
import propertymanager.PropertyManager;
import ui.AppGUI;
import ui.AppMessageDialogSingleton;
import ui.YesNoCancelDialogSingleton;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static Buzzword.BuzzwordProperties.*;
import static GUI.Workspace.GameState.*;
import static settings.AppPropertyType.*;

/**
 * Created by Afzal on 11/9/2016.
 */
public class Workspace extends AppWorkspaceComponent{
    public enum GameState{
        LOGGED_OUT_HOME,
        LOGGED_IN_HOME,
        MODE_SELECT,
        LEVEL_SELECT,
        IN_GAME,
        PAUSED,
        END_GAME,
        NEW_USER_PROMPT,
        LOGIN_PROMPT,
        HELP_DIALOG,
        INITIAL_LAYOUT,
        RESUME_GAME,
        PAUSED_EXIT;
    }
    public static GameState state = INITIAL_LAYOUT;

    AppTemplate app; // shared reference to the application
    AppGUI      gui; // GUI used by the application

    private Label         header;             // app title (buzzword)
    private Label         levelLabel;         // says the word level
    private Label         categoryLabel;      // says the category
    private Label         currentLevelLabel;  // shows the current level that the user is playing (for in game use)
    private Label         timeLabel;          // label to display time
    private Label         currentWordLabel;   // guessed words
    private Label         targetLabel;        // target score label
    private Label         scoreLabel;        // pane to hold guessed words and point values
    private VBox          mainArea;           // main area during the game
    private VBox          rightArea;          // right area (to hold timer, etc)
    private VBox          headerContainer;    // contains header
    private LoginPrompt   prompt;             //
    private NewUserPrompt newUserPrompt;
    private Button[][]    levelNodes;
    private GridPane      levelNodesContainer;
    private Button[][]    gameNodes;
    private Button        pauseButton;
    private BorderPane    graphicsPane;
    private TableView<Word> wordsFound = new TableView<>();
    private BuzzwordData  data;
    private HBox          closeButtonContainer;
    private Button        closeButton;
    private Label         gameInfoLabel;

    private boolean       loggedIn;           // keeps track of if user is logged in
    private int           modeSelection;
    private BuzzwordController controller;
    private int           levelSelected = 1;      // the level the user picks to start game
    private String        categoryTitle;
    private Pane          buttonPane = new Pane();
    private int           time;              // the time displayed on the label
    private boolean       activeGame;
    private ObservableList<Word> words;
    private int           currentScore;
    private ArrayList<Line> lines = new ArrayList<>();

    private Button        resetButton; // resets the game

    private Button thisButton = new Button();

    private final KeyCombination ctrlShiftP = new KeyCodeCombination(KeyCode.P, KeyCombination.SHIFT_DOWN, KeyCombination.CONTROL_DOWN);
    private final KeyCombination ctrlL = new KeyCodeCombination(KeyCode.L, KeyCombination.CONTROL_DOWN);
    private final KeyCombination ctrlQ = new KeyCodeCombination(KeyCode.Q, KeyCombination.CONTROL_DOWN);

    public Workspace(AppTemplate appTemplate){
        app = appTemplate;
        gui = app.getGUI();
        data = (BuzzwordData)app.getDataComponent();
        layoutGUI();     // create the gui based on current state. it always starts at logged out home
        setupHandlers(); // setup the handlers for buttons
    }

    /**
     * This function specifies the CSS for all the UI components known at the time the workspace is initially
     * constructed. Components added and/or removed dynamically as the application runs need to be set up separately.
     */
    @Override
    public void initStyle() {
        PropertyManager propertyManager = PropertyManager.getManager();

        gui.getAppPane().setId(propertyManager.getPropertyValue(ROOT_BORDERPANE_ID));
        gui.getToolbarPane().getStyleClass().setAll(propertyManager.getPropertyValue(SEGMENTED_BUTTON_BAR));
        gui.getToolbarPane().setId(propertyManager.getPropertyValue(TOP_TOOLBAR_ID));

        ObservableList<Node> toolbarChildren = gui.getToolbarPane().getChildren();
        toolbarChildren.get(0).getStyleClass().add(propertyManager.getPropertyValue(FIRST_TOOLBAR_BUTTON));
        toolbarChildren.get(toolbarChildren.size() - 1).getStyleClass().add(propertyManager.getPropertyValue(LAST_TOOLBAR_BUTTON));

        workspace.getStyleClass().add(CLASS_BORDERED_PANE);
        header.getStyleClass().setAll(propertyManager.getPropertyValue(HEADING_LABEL));

    }


    @Override
    public void reloadWorkspace() {
        new Workspace(app);
    }

    private void layoutGUI(){
        PropertyManager manager = PropertyManager.getManager();

        // graphics pane split up into several components
        graphicsPane = new BorderPane();

        // label for app title. add it to center of top of borderpane
        header = new Label(manager.getPropertyValue(WORKSPACE_HEADING_LABEL));
        closeButton = new Button("X");

        closeButton.setOnMouseClicked(e -> {

            if (Workspace.state == GameState.IN_GAME){
                Workspace.state = GameState.PAUSED_EXIT;
                updateGUI();
                activeGame = false;
            }

            YesNoCancelDialogSingleton singleton = YesNoCancelDialogSingleton.getSingleton();
            singleton.show("Exit", "Are you sure you want to exit?");

            if (singleton.getSelection().equals(YesNoCancelDialogSingleton.YES)){
                System.exit(0);
            }

            else{
                if (!(state == GameState.PAUSED) && !(state == GameState.IN_GAME) && !(state == GameState.RESUME_GAME)){}

                else if (state == GameState.PAUSED){}

                else {
                    activeGame = true;
                    pauseButton.setText(manager.getPropertyValue(PAUSE));
                    state = GameState.RESUME_GAME;
                    updateGUI();
                    state = GameState.IN_GAME;
                }
            }
        });

        closeButtonContainer = new HBox(closeButton);
        closeButtonContainer.setAlignment(Pos.CENTER_RIGHT);


        // center header container. separate from other
        // components because this will not be changing
        headerContainer = new VBox();
        headerContainer.getChildren().addAll(header);
        headerContainer.setAlignment(Pos.CENTER);

        // create circles for the BUZZWORD title
        levelNodesContainer = new GridPane();
        levelNodesContainer.setHgap(25);
        levelNodesContainer.setVgap(20);
        levelNodesContainer.setPadding(new Insets(100,0,0,100));
        levelNodes = new Button[4][4];
        for (int i = 0; i<4; i++){
            for (int j = 0; j <4; j++){
                levelNodes[i][j] = new Button();;
                levelNodes[i][j].setDisable(true);
                levelNodes[i][j].setMinSize(60,60);
                levelNodes[i][j].setMaxSize(60,60);
                levelNodes[i][j].setTextFill(Paint.valueOf("#ffffff"));
                levelNodes[i][j].getStyleClass().addAll(manager.getPropertyValue(HOME_SCREEN_NODES));
                levelNodesContainer.add(levelNodes[i][j], j, i);

            }
        }

        // put the letters buzzword into the circles
        levelNodes[0][0].setText("B");
        levelNodes[0][1].setText("U");
        levelNodes[1][0].setText("Z");
        levelNodes[1][1].setText("Z");
        levelNodes[2][2].setText("W");
        levelNodes[2][3].setText("O");
        levelNodes[3][2].setText("R");
        levelNodes[3][3].setText("D");

        // placeholder label to put at top so that board doesnt get shifted later when labels are added
        Label placeHolder = new Label();
        placeHolder.setMinHeight(50);

        // reset the padding so that it doesnt move when a label is added.
        levelNodesContainer.setPadding(new Insets(
                levelNodesContainer.getPadding().getTop() - placeHolder.getMinHeight(),
                levelNodesContainer.getPadding().getRight(),
                levelNodesContainer.getPadding().getBottom(),
                levelNodesContainer.getPadding().getLeft()
        ));

        // set the parts of the graphics pane (top and center are set on home screen
        graphicsPane.setTop(placeHolder);
        graphicsPane.setCenter(levelNodesContainer);

        // create the workspace, and add the children to it
        workspace = new VBox();
        workspace.getChildren().addAll(closeButtonContainer, headerContainer, graphicsPane);


        /*********************************************************
         * this section initializes other components for later use
         ********************************************************/
        gameNodes = new Button[4][4];


        // set the state, and update the gui to reflect it
        state = GameState.INITIAL_LAYOUT;
        updateGUI();


        app.getPrimaryStage().getScene().addEventHandler(KeyEvent.KEY_RELEASED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (ctrlShiftP.match(event)){
                    if (state == LOGGED_OUT_HOME || state == INITIAL_LAYOUT){
                        state = NEW_USER_PROMPT;
                        updateGUI();
                    }
                }

                if (ctrlL.match(event)){
                    if (state == LOGGED_IN_HOME){
                        YesNoCancelDialogSingleton singleton = YesNoCancelDialogSingleton.getSingleton();
                        singleton.show("Are you sure?", "Are you sure you want to log out?");

                        if (singleton.getSelection().equals(YesNoCancelDialogSingleton.YES)) {
                            loggedIn = false;
                            state = LOGGED_OUT_HOME;
                            updateGUI();
                        }
                    }

                    else if (state == LOGGED_OUT_HOME || !loggedIn){
                        state = LOGIN_PROMPT;
                        updateGUI();
                    }
                }

                if (ctrlQ.match(event)){
                    YesNoCancelDialogSingleton singleton = YesNoCancelDialogSingleton.getSingleton();
                    singleton.show("Exit", "Are you sure you want to exit?");

                    if (singleton.getSelection().equals(YesNoCancelDialogSingleton.YES)){
                        app.getPrimaryStage().close();
                    }
                }
            }
        });
    }

    public void updateGUI(){
        switch (state)
        {
            case LOGGED_OUT_HOME:
                loggedOutHomeGui();
                break;

            case LOGGED_IN_HOME:
                loggedInHomeGui();
                break;

            case MODE_SELECT:
                modeSelectGui();
                break;

            case LEVEL_SELECT:
                levelSelectGui();
                break;

            case IN_GAME:
                inGameGui(levelSelected);
                break;

            case PAUSED:
                pausedGui();
                break;

            case END_GAME:
                endGameGui();
                break;

            case NEW_USER_PROMPT:
                newUserPromptGui();
                break;

            case LOGIN_PROMPT:
                loginPromptGui();
                break;

            case HELP_DIALOG:
                helpDialogGui();
                break;

            case RESUME_GAME:
                resumeGui();
                break;

            case PAUSED_EXIT:
                pausedExitGui();
                break;
        }
    }

    /**
     * loggedOutHomeGui()
     *
     * shows all the toolbar buttons, deletes all components from the screen, and draws a home screen
     */
    private void loggedOutHomeGui(){
        showAllToolbarButtons();
        Button temporary = (Button)app.getGUI().getToolbarPane().getChildren().get(2);
        temporary.setText("Login");

        app.getGUI().getToolbarPane().getChildren().get(0).setVisible(false);
        app.getGUI().getToolbarPane().getChildren().get(3).setVisible(false);
        app.getGUI().getToolbarPane().getChildren().get(4).setVisible(false);
        app.getGUI().getToolbarPane().getChildren().get(5).setVisible(false);

        backToHome();
    }

    private void loggedInHomeGui(){
        showAllToolbarButtons();
        app.getGUI().getToolbarPane().getChildren().get(1).setVisible(false);
        Button temporary = (Button)app.getGUI().getToolbarPane().getChildren().get(2);
        temporary.setText("Logged in as: " + data.getUserName());

        backToHome();
    }

    private void modeSelectGui(){

    }

    private void levelSelectGui(){
        PropertyManager manager = PropertyManager.getManager();

        if (loggedIn) {
            ComboBox temporary = (ComboBox) app.getGUI().getToolbarPane().getChildren().get(3);

            modeSelection = temporary.getSelectionModel().getSelectedIndex() + 1;

            levelNodesContainer.setPadding(new Insets(0, 0, 0, 100));
            if (modeSelection == 4)
                categoryTitle = manager.getPropertyValue(CATEGORY4);

            else if (modeSelection == 3)
                categoryTitle = manager.getPropertyValue(CATEGORY3);

            else if (modeSelection == 2)
                categoryTitle = manager.getPropertyValue(CATEGORY2);

            else if (modeSelection == 1){
                temporary.getSelectionModel().selectFirst();
                categoryTitle = manager.getPropertyValue(CATEGORY1);
            }

            else{
                temporary.getSelectionModel().select(data.getCurrentLevel());
                levelSelectGui();
                return;
            }

            data.setCurrentLevel(modeSelection-1);

            try {
                app.getFileComponent().saveData(app.getDataComponent(), new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/").toPath());
            }

            catch (IOException e){}

            levelLabel = new Label(manager.getPropertyValue(LEVEL_LABEL) + ": " + categoryTitle);
            levelLabel.setPadding(new Insets(80, 0, 20, 80));
            levelLabel.setMinHeight(50);
            levelLabel.setStyle("-fx-font-size: 30");



            graphicsPane.setTop(levelLabel);
            BorderPane.setAlignment(levelLabel, Pos.CENTER_LEFT);

            for (int i = 0; i < 4; i++) {
                for (int j = 0; j < 4; j++) {
                    levelNodes[i][j].setDisable(true);
                    levelNodes[i][j].setVisible(false);
                }
            }

            // get number of levels unlocked for current mode that is selected
            // subtract 1 because of reasons above...
            int levelsUnlocked = data.getLevelsUnlocked()[modeSelection-1];

            // in order to unlock the first 3 levels, we take th

            // 8 levels total.
            for (int i = 0; i < 2; i++) {
                for (int j = 0; j < 4; j++) {
                        levelNodes[i][j].setVisible(true);
                        int x = j + (4 * i) + 1;
                        levelNodes[i][j].setText(Integer.toString(x));
                        if (levelsUnlocked > 0) {
                            levelNodes[i][j].setDisable(false);

                            levelNodes[i][j].setOnMouseEntered(e -> {
                                Button button = (Button) e.getSource();
                                button.setScaleX(1.5);
                                button.setScaleY(1.5);
                            });

                            levelNodes[i][j].setOnMouseExited(e -> {
                                Button button = (Button) e.getSource();
                                button.setScaleX(1);
                                button.setScaleY(1);
                            });

                            levelNodes[i][j].setOnMouseClicked(e -> {
                                Button button = (Button) e.getSource();
                                state = GameState.IN_GAME;

                                inGameGui(Integer.parseInt(button.getText()));
                            });
                            levelsUnlocked--;
                    }
                }
            }
        }

        else {
            AppMessageDialogSingleton.getSingleton().setButtonText("OK");
            AppMessageDialogSingleton.getSingleton().show("Error", "You must be logged in to play.");
        }
    }

    private void inGameGui(int level){
        PropertyManager manager = PropertyManager.getManager();

        activeGame = false;


        // set the mode button and the play toolbar button to be invisible because you cannot do that while in game
        app.getGUI().getToolbarPane().getChildren().get(3).setVisible(false);
        app.getGUI().getToolbarPane().getChildren().get(4).setVisible(false);
        app.getGUI().getToolbarPane().getChildren().get(5).setVisible(false);



        levelSelected = level;

        mainArea = new VBox();
        rightArea = new VBox();
        VBox.setMargin(rightArea, new Insets(0,10,0,0));

        // create the label for the category
        categoryLabel = new Label(categoryTitle);
        categoryLabel.setPadding(new Insets(10,0,20,10));
        categoryLabel.setMinHeight(50);
        categoryLabel.setStyle("-fx-font-size: 30");
        categoryLabel.setAlignment(Pos.CENTER);
        mainArea.getChildren().add(categoryLabel);
        mainArea.setAlignment(Pos.CENTER);


        Random random = new Random();
        Line line;

        // create the buttons that are to be displayed
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                gameNodes[i][j] = new Button();
                gameNodes[i][j].setLayoutX(j*125 + 20);
                gameNodes[i][j].setLayoutY(i*115 + 10);
                gameNodes[i][j].setMinSize(60,60);
                gameNodes[i][j].setMaxSize(60,60);
                gameNodes[i][j].getStyleClass().addAll(manager.getPropertyValue(HOME_SCREEN_NODES));

                buttonPane.getChildren().add(gameNodes[i][j]);


            }
        }

        mainArea.getChildren().add(buttonPane);


        // add the current level label
        currentLevelLabel = new Label(manager.getPropertyValue(LEVEL) + Integer.toString(levelSelected));
        currentLevelLabel.setPadding(new Insets(20,60,0,10));
        currentLevelLabel.setMinHeight(50);
        currentLevelLabel.setStyle("-fx-font-size: 30; -fx-underline: true");
        currentLevelLabel.setAlignment(Pos.CENTER_LEFT);

        mainArea.getChildren().add(currentLevelLabel);

        // then add the button to pause, right undernath the label
        pauseButton = new Button(manager.getPropertyValue(PLAY));
        pauseButton.setOnAction(e -> {
            resetButton.setVisible(true);
            if (pauseButton.getText().equals(manager.getPropertyValue(PLAY))) {
                controller.startGame(levelSelected, modeSelection -1);
                activeGame = true;
                pauseButton.setText(manager.getPropertyValue(PAUSE));
            }

            // game is active, pause it
            else if (pauseButton.getText().equals(manager.getPropertyValue(PAUSE))){
                Workspace.state = GameState.PAUSED;
                updateGUI();
                activeGame = false;
                pauseButton.setText(manager.getPropertyValue(RESUME));
            }
            else{
                activeGame = true;
                pauseButton.setText(manager.getPropertyValue(PAUSE));
                state = GameState.RESUME_GAME;
                updateGUI();
            }
        });
        pauseButton.setMinWidth(80);
        pauseButton.setMinHeight(40);
        pauseButton.setPadding(new Insets(10,5,10,5));
        pauseButton.setStyle("-fx-background-color: blanchedalmond");
        VBox.setMargin(pauseButton, new Insets(10,55,0,0));

        mainArea.getChildren().add(pauseButton);


        // NOW TO create the right side of the pane, which goes into rightArea
        resetButton = new Button("Reset");
        resetButton.setMinWidth(80);
        resetButton.setMinHeight(40);
        resetButton.setPadding(new Insets(10,5,10,5));
        resetButton.setVisible(false);

        resetButton.setOnAction(e -> controller.resetGame(level,modeSelection-1));


        VBox.setMargin(resetButton, new Insets(50,100,0,0));
        rightArea.getChildren().add(resetButton);


        // start by creating the timelabel
        time = 60;
        timeLabel = new Label("Time Left: " + time + " seconds");
        timeLabel.getStyleClass().setAll("in-game-labels");
        VBox.setMargin(timeLabel, new Insets(20,100,0,0));

        rightArea.getChildren().add(timeLabel);


        // label for the current word being typed
        currentWordLabel = new Label("W O R D");
        currentWordLabel.getStyleClass().setAll("other-labels");
        currentWordLabel.setAlignment(Pos.CENTER_RIGHT);
        currentLevelLabel.setMinWidth(150);
        VBox.setMargin(currentWordLabel, new Insets(20,0,0,0));

        rightArea.getChildren().add(currentWordLabel);


        // WORDS THAT HAVE BEEN GUESSED
        words = FXCollections.observableArrayList();

        TableColumn wordCol = new TableColumn("Words");
        wordCol.setMinWidth(90);
        wordCol.setMaxWidth(90);
        TableColumn scoreCol = new TableColumn("Score");
        scoreCol.setMinWidth(60);
        scoreCol.setMaxWidth(60);

        wordCol.setCellValueFactory(
                new PropertyValueFactory<Word, String>("word"));

        scoreCol.setCellValueFactory(
                new PropertyValueFactory<Word, Integer>("score"));

        wordsFound = new TableView<>();

        wordsFound.setItems(words);

        // now that we have the current word being typed, add in the scrollpane with the guessed words
        wordsFound.getColumns().addAll(wordCol, scoreCol);
        wordsFound.setMinWidth(175);
        wordsFound.setMaxWidth(175);

        VBox.setMargin(wordsFound, new Insets(20,0,0,0));
        rightArea.getChildren().add(wordsFound);



        // after the words, we want to add in the total score. Do this by adding another label
        for (Word w : wordsFound.getItems()){
            currentScore += w.getScore();
        }

        scoreLabel = new Label(manager.getPropertyValue(SCORE) + "       " + currentScore);
        scoreLabel.setMinWidth(175);
        scoreLabel.setMaxWidth(175);
        scoreLabel.getStyleClass().setAll("other-labels");
        scoreLabel.setPadding(new Insets(5));
        rightArea.getChildren().add(scoreLabel);


        // AND FINALLY, THE TARGET SCORE

        targetLabel = new Label(manager.getPropertyValue(TARGET) + " " + "2000");
        targetLabel.setMinWidth(150);
        targetLabel.setPadding(new Insets(5));
        targetLabel.getStyleClass().setAll("other-labels");
        targetLabel.setStyle("-fx-underline:true");
        VBox.setMargin(targetLabel, new Insets(10,0,0,0));
        rightArea.getChildren().add(targetLabel);

        gameInfoLabel = new Label();
        gameInfoLabel.setMinWidth(150);
        gameInfoLabel.setMaxWidth(300);
        gameInfoLabel.setWrapText(true);
        gameInfoLabel.setPadding(new Insets(5));
        gameInfoLabel.getStyleClass().setAll("other-labels");
        VBox.setMargin(gameInfoLabel, new Insets(30,0,0,0));

        if (level ==1)
            gameInfoLabel.setText("Minimum 3 letter words");

        else if (level == 2)
            gameInfoLabel.setText("Minimum 3 letter words");

        else if (level == 3)
            gameInfoLabel.setText("Minimum 3 letter words");

        else if (level == 4)
            gameInfoLabel.setText("Minimum 3 letter words");

        else if (level == 5)
            gameInfoLabel.setText("Minimum 4 letter words");

        else if (level == 6)
            gameInfoLabel.setText("Minimum 4 letter words");

        else if (level == 7)
            gameInfoLabel.setText("Minimum 4 letter words");

        else
            gameInfoLabel.setText("Minimum 4 letter words");

        rightArea.getChildren().add(gameInfoLabel);

        // add the center and the right to the pane
        graphicsPane.setTop(null);
        graphicsPane.setCenter(mainArea);
        graphicsPane.setRight(rightArea);
    }

    public void setLevelSelected(int level){
        levelSelected = level;
    }

    private void pausedGui(){
        for (int i = 0; i<4; i++) {
            for (int j = 0; j < 4; j++) {
                gameNodes[i][j].setVisible(false);
            }
        }

        //for (Line l : lines)
          //  l.setVisible(false);

        AppMessageDialogSingleton singleton = AppMessageDialogSingleton.getSingleton();
        singleton.setButtonText("OK");
        singleton.show("Paused", "The game is now paused\nPress OK, and then press the resume button\nto resume");
    }

    private void endGameGui(){
        AppMessageDialogSingleton singleton = AppMessageDialogSingleton.getSingleton();
        singleton.setButtonText("OK");
        singleton.show("You Win!", "Congratulations, you won!");
    }

    private void newUserPromptGui(){
        if (loggedIn) {

        }

        else {
            newUserPrompt = new NewUserPrompt(app);
            newUserPrompt.showAndWait();

            if (newUserPrompt.isValid()) {
                loggedIn = true;
                state = GameState.LOGGED_IN_HOME;
                updateGUI();
            }
        }
    }

    private TextField newUserNameInput;
    private PasswordField oldPasswordInput, newPasswordInput;
    private Label newUserNameLabel, currentUserNameLabel, oldPasswordLabel, newPasswordLabel;
    private VBox profileScreen;
    private HBox newUser, newPass, oldPass, buttonsHolder;
    private Button updateButton, additionalStats, logoutButton;

    private void loginPromptGui(){
        if (loggedIn){
            Label placeHolder = new Label();
            placeHolder.setMinHeight(50);

            profileScreen = new VBox();
            graphicsPane.setRight(null);
            graphicsPane.setTop(placeHolder);
            graphicsPane.setBottom(null);
            graphicsPane.setCenter(null);

            currentUserNameLabel = new Label("Current Username: " + data.getUserName());
            currentUserNameLabel.getStyleClass().setAll("other-labels");
            profileScreen.getChildren().add(currentUserNameLabel); // add current username

            newUser = new HBox();
            newPass = new HBox();
            oldPass = new HBox();
            buttonsHolder = new HBox();

            newUserNameLabel = new Label("Enter New Username: ");
            newUserNameLabel.getStyleClass().setAll("other-labels");
            newUserNameInput = new TextField();
            newUserNameLabel.setMinWidth(150);
            newUserNameLabel.setMinHeight(45);

            newUser.setPadding(new Insets(20,0,0,0));
            newUser.getChildren().addAll(newUserNameLabel, newUserNameInput);
            profileScreen.getChildren().add(newUser);


            oldPasswordLabel = new Label("Enter old password: ");
            oldPasswordLabel.getStyleClass().setAll("other-labels");
            oldPasswordInput = new PasswordField();
            oldPasswordInput.setMinWidth(150);

            oldPass.setPadding(new Insets(20,0,0,0));
            oldPass.getChildren().addAll(oldPasswordLabel, oldPasswordInput);
            profileScreen.getChildren().add(oldPass);



            newPasswordLabel = new Label("Enter your new password: ");
            newPasswordLabel.getStyleClass().setAll("other-labels");
            newPasswordInput = new PasswordField();
            newPasswordInput.setMinWidth(150);

            newPass.setPadding(new Insets(20,0,0,0));
            newPass.getChildren().addAll(newPasswordLabel, newPasswordInput);
            profileScreen.getChildren().add(newPass);

            updateButton = new Button("Update Profile");

            additionalStats = new Button("See Additional Stats");

            buttonsHolder.setPadding(new Insets(20,0,0,0));
            buttonsHolder.getChildren().addAll(updateButton, additionalStats);
            HBox.setMargin(additionalStats, new Insets(0,0,0,30));
            profileScreen.getChildren().add(buttonsHolder);


            logoutButton = new Button("Log Out");
            VBox.setMargin(logoutButton, new Insets(20,0,0,0));

            profileScreen.getChildren().add(logoutButton);

            graphicsPane.setCenter(profileScreen);

            updateButton.setOnAction(e -> {
                if (newUserNameInput.getText().trim().isEmpty() || oldPasswordInput.getText().trim().isEmpty() || newPasswordInput.getText().trim().isEmpty()){
                    AppMessageDialogSingleton.getSingleton().show("Error!", "One of the fields is empty");
                }

                if (!(MD5.getMD5(oldPasswordInput.getText()).equals(data.getPassword()))){
                    AppMessageDialogSingleton.getSingleton().show("Error!", "Old password is incorrect");
                }
                // all fields filled out
                else{
                    File oldLocation = new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/" + data.getUserName() + ".json");

                    boolean b = oldLocation.delete();

                    File newLocation = new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/" + data.getUserName());

                    if (newLocation.exists()){
                        AppMessageDialogSingleton.getSingleton().show("Error", "This user already exists");
                    }


                    else {
                        data.setPassword(MD5.getMD5(newPasswordInput.getText()));
                        data.setUserName(newUserNameInput.getText());

                        try {
                            app.getFileComponent().saveData(data, new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/").toPath());
                            Button temporary = (Button)app.getGUI().getToolbarPane().getChildren().get(2);
                            temporary.setText("Logged in as: " + data.getUserName());

                            currentUserNameLabel.setText("Current Username: " + data.getUserName());

                            newUserNameInput.setText("");
                            oldPasswordInput.setText("");
                            newPasswordInput.setText("");

                            AppMessageDialogSingleton.getSingleton().show("Success", "Username and Password Updated Successfully!");
                        }catch (IOException e1){System.out.println("Update Failed");}
                    }
                }
            });

            additionalStats.setOnAction(event -> {
                YesNoCancelDialogSingleton singleton = YesNoCancelDialogSingleton.getSingleton();
                singleton.show("Stats",
                        "You are currently logged in as: " + data.getUserName() +
                                "\n\n\tHere are you stats thus far:" +
                                "\nMode:" +
                                "\n\t" + "General" + ": Highest Score: " + Arrays.toString(data.getHighestScores1())+
                                "\n\t" + "Names" + ": Highest Score: " + Arrays.toString(data.getHighestScores2()) +
                                "\n\t" + "Medical" + ": Highest Score: " + Arrays.toString(data.getHighestScores3()) +
                                "\n\tGeneral Version 2: Highest Score: " + Arrays.toString(data.getHighestScores4()) +
                                "\n\nHighest Point Total: " + data.getHighestPointTotal() +
                                "\n\nLevels Cleared on each mode:" +
                                "\n\tGeneral: " + data.getLevelsUnlocked()[0] +
                                "\n\tNames: " + data.getLevelsUnlocked()[1] +
                                "\n\tMedical: " + data.getLevelsUnlocked()[2] +
                                "\n\tGeneral Version 2: " + data.getLevelsUnlocked()[3]);
            });


            logoutButton.setOnAction(e->{
                YesNoCancelDialogSingleton singleton = YesNoCancelDialogSingleton.getSingleton();
                singleton.show("Are you sure?", "Are you sure you want to log out?");

                if (singleton.getSelection().equals(YesNoCancelDialogSingleton.YES)) {
                    loggedIn = false;
                    state = LOGGED_OUT_HOME;
                    updateGUI();
                }
            });

        }

        else {
            prompt = new LoginPrompt(app);
            prompt.showAndWait();

            if (prompt.isValid()) {
                loggedIn = true;
                state = GameState.LOGGED_IN_HOME;
                updateGUI();
            }
        }
    }


    private  void helpDialogGui(){
        ScrollablePane pane = new ScrollablePane(app.getPrimaryStage());
        pane.setButtonText("Got it");
        pane.getCloseButton().setOnAction(e -> pane.close());;
        pane.show("Message", "How to play this game:" +
                "\nThe rules for the game are quite simple. " +
                "\nYou can start off by selecting a game mode from the drop down menu to the left." +
                "\nYou may then select a level to play. The more levels you beat, the higher level you can challenge!" +
                "\nProfile statistics can be viewed by clicking on your username to the left" +
                "\nThe game will automatically save progress after each victory, but if you feel more comfortable saving the data manually," +
                "\n\tyou may use the save button the left" +
                "\n\n\n\t\t\tRules of the Game:" +
                "\n\nThere are two ways to play the game" +
                "\n\t1. Using the keyboard:" +
                "\n\t\tBegin by entering a letter. All instances of that letter will become highlighted." +
                "\n\t\tThen use letters that are adjacent to form words. when you are done, hit the enter button" +
                "\n\t\tThe enter button will automatically accept the word if it is valid" +
                "\n\n\t2. Using the mouse:" +
                "\n\t\tAlternatively, you may use the mouse to play the game." +
                "\n\t\tHighlight adjacent modes by dragging across them." +
                "\n\t\tRelease the mouse and the entered word will be checked for validity" +
                "\n\n\n\t\t\tScoring Mechanism" +
                "\n\nThe minimum word length is 3. Harder modes will increase this minimum word length" +
                "\n\t3 and 4 letter words - 1 point" +
                "\n\t5 letter words - 2 points" +
                "\n\t6 letter words - 3 points" +
                "\n\t7 letter words - 5 points" +
                "\n\t8+ letter words - 11 points" +
                "\n\nScores can be displayed at any time during the game." +
                "\nThe game can also be paused by pressing pause the pause button." +
                "\nThe game can also be reset by pressing the reset button.");
    }

    private void resumeGui(){
        for (int i = 0; i<4; i++) {
            for (int j = 0; j < 4; j++) {
                gameNodes[i][j].setVisible(true);
            }
        }

        //for (Line l : lines)
           // l.setVisible(true);
    }

    private void showAllToolbarButtons(){
        for (int i = 0; i < app.getGUI().getToolbarPane().getChildren().size(); i++) {
            app.getGUI().getToolbarPane().getChildren().get(i).setVisible(true);
        }
    }

    public boolean isLoggedIn(){
        return loggedIn;
    }

    public int getModeSelection() { return modeSelection;}

    public void setModeSelection(int selection){ modeSelection = selection;}

    private void setupHandlers(){
        this.controller = new BuzzwordController(app);
    }

    private void backToHome(){
        PropertyManager manager = PropertyManager.getManager();

        levelNodesContainer.setHgap(25);
        levelNodesContainer.setVgap(20);
        levelNodesContainer.setPadding(new Insets(100,0,0,100));
        for (int i = 0; i<4; i++){
            for (int j = 0; j <4; j++) {
                levelNodes[i][j].setDisable(true);
                levelNodes[i][j].setVisible(true);
                levelNodes[i][j].setMinSize(60, 60);
                levelNodes[i][j].setMaxSize(60, 60);
                levelNodes[i][j].setTextFill(Paint.valueOf("#ffffff"));
                levelNodes[i][j].getStyleClass().addAll(manager.getPropertyValue(HOME_SCREEN_NODES));
                levelNodes[i][j].setText("");

            }
        }

        /*app.getPrimaryStage().getScene().addEventFilter(MouseEvent.DRAG_DETECTED , new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                app.getPrimaryStage().getScene().startFullDrag();
            }
        });
*/
        // put the letters buzzword into the circles
        levelNodes[0][0].setText("B");
        levelNodes[0][1].setText("U");
        levelNodes[1][0].setText("Z");
        levelNodes[1][1].setText("Z");
        levelNodes[2][2].setText("W");
        levelNodes[2][3].setText("O");
        levelNodes[3][2].setText("R");
        levelNodes[3][3].setText("D");

        // placeholder label to put at top so that board doesnt get shifted later when labels are added
        Label placeHolder = new Label();
        placeHolder.setMinHeight(50);

        // reset the padding so that it doesnt move when a label is added.
        levelNodesContainer.setPadding(new Insets(
                levelNodesContainer.getPadding().getTop() - placeHolder.getMinHeight(),
                levelNodesContainer.getPadding().getRight(),
                levelNodesContainer.getPadding().getBottom(),
                levelNodesContainer.getPadding().getLeft()
        ));

        // set the parts of the graphics pane (top and center are set on home screen
        graphicsPane.setTop(placeHolder);
        graphicsPane.setRight(null);
        graphicsPane.setCenter(levelNodesContainer);
    }

    public void updateLetterGrid(char[] letters){
        for (int i = 0; i < 4; i++){
            for (int j = 0; j < 4; j++){
                if (letters[(i*4) + j] == 'Q')
                    gameNodes[i][j].setText("QU");

                else
                    gameNodes[i][j].setText(Character.toString(letters[(i*4) + j]));
            }
        }
    }

    public void setMaxScore(int maxScore, int targetScore){
        targetLabel.setText(PropertyManager.getManager().getPropertyValue(TARGET) + " " + targetScore + "\nMax Score: " + maxScore);
    }

    public Button getPauseButton(){
        return pauseButton;
    }

    public void pausedExitGui(){
        for (int i = 0; i<4; i++) {
            for (int j = 0; j < 4; j++) {
                gameNodes[i][j].setVisible(false);
            }
        }

        //for (Line l : lines)
           // l.setVisible(false);
    }

    public Button[][] getGameNodes(){
        return gameNodes;
    }

    public void addWord(String word, int score){
        words.add(new Word(word, score));
        currentScore += score;
        scoreLabel.setText(PropertyManager.getManager().getPropertyValue(SCORE) + "       " + currentScore);
    }

    public void setScore(int score){
        currentScore = 0;
        scoreLabel.setText(PropertyManager.getManager().getPropertyValue(SCORE) + "       " + score);
    }

    public int getScore(){
        return currentScore;
    }

    public Label getTimeLabel(){
        return timeLabel;
    }

    public Label getCurrentWordLabel(){
        return currentWordLabel;
    }

    public Pane getButtonPane(){
        return buttonPane;
    }
}

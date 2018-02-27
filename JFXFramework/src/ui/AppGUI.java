package ui;

import apptemplate.AppTemplate;
import components.AppStyleArbiter;
import controller.FileController;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.*;
import javafx.stage.Screen;
import javafx.stage.Stage;
import propertymanager.PropertyManager;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_IMAGEDIR_PATH;

/**
 * This class provides the basic user interface for this application, including all the file controls, but it does not
 * include the workspace, which should be customizable and application dependent.
 *
 * @author Richard McKenna, Ritwik Banerjee
 */
public class AppGUI implements AppStyleArbiter {

    protected FileController fileController;   // to react to file-related controls
    protected Stage          primaryStage;     // the application window
    protected Scene          primaryScene;     // the scene graph
    protected BorderPane     appPane;          // the root node in the scene graph, to organize the containers
    protected FlowPane       toolbarPane;      // the top toolbar

    protected Button         homeButton;        // button to go home
    protected Button         loginButton;       // button to login
    protected Button         newProfileButton;  // button to load a saved game from (json) file
    protected ComboBox<String>  modeButton;        // button to exit application
    protected Button         playButton;        // button to start playing
    protected Button         helpButton;        // shows a help dialog
    protected String         applicationTitle;  // the application title
    protected Button         saveButton;        // a save button that saves user profile


    private int appSpecificWindowWidth;  // optional parameter for window width that can be set by the application
    private int appSpecificWindowHeight; // optional parameter for window height that can be set by the application
    
    /**
     * This constructor initializes the file toolbar for use.
     *
     * @param initPrimaryStage The window for this application.
     * @param initAppTitle     The title of this application, which
     *                         will appear in the window bar.
     * @param app              The app within this gui is used.
     */
    public AppGUI(Stage initPrimaryStage, String initAppTitle, AppTemplate app) throws IOException, InstantiationException {
        this(initPrimaryStage, initAppTitle, app, -1, -1);
    }

    public AppGUI(Stage primaryStage, String applicationTitle, AppTemplate appTemplate, int appSpecificWindowWidth, int appSpecificWindowHeight) throws IOException, InstantiationException {
        this.appSpecificWindowWidth = appSpecificWindowWidth;
        this.appSpecificWindowHeight = appSpecificWindowHeight;
        this.primaryStage = primaryStage;
        this.applicationTitle = applicationTitle;
        initializeToolbar();                    // initialize the top toolbar
        initializeToolbarHandlers(appTemplate); // set the toolbar button handlers
        initializeWindow();                     // start the app window (without the application-specific workspace)

    }

    public FlowPane getToolbarPane() { return toolbarPane; }

    public BorderPane getAppPane() { return appPane; }
    
    /**
     * Accessor method for getting this application's primary stage's,
     * scene.
     *
     * @return This application's window's scene.
     */
    public Scene getPrimaryScene() { return primaryScene; }
    
    /**
     * Accessor method for getting this application's window,
     * which is the primary stage within which the full GUI will be placed.
     *
     * @return This application's primary stage (i.e. window).
     */
    public Stage getWindow() { return primaryStage; }

    public void updateButtonState(Button b, boolean c){
        b.setVisible(c);
    }

    /****************************************************************************/
    /* BELOW ARE ALL THE PRIVATE HELPER METHODS WE USE FOR INITIALIZING OUR AppGUI */
    /****************************************************************************/
    
    /**
     * This function initializes all the buttons in the toolbar at the top of
     * the application window. These are related to file management.
     */
    private void initializeToolbar() throws IOException {
        toolbarPane = new FlowPane(Orientation.VERTICAL);

        homeButton = initializeChildButton(toolbarPane, HOME_TEXT.toString(), false);
        newProfileButton = initializeChildButton(toolbarPane, NEW_PROFILE_TEXT.toString(),true);
        loginButton = initializeChildButton(toolbarPane, LOGIN_TEXT.toString(), true);
        modeButton = initalizeComboBox();//initializeChildButton(toolbarPane, MODE_TEXT.toString(),false);
        playButton = initializeChildButton(toolbarPane, PLAY_TEXT.toString(), false);
        saveButton = initializeChildButton(toolbarPane, SAVE_TEXT.toString(), false);
        helpButton = initializeChildButton(toolbarPane, HELP_TEXT.toString(), true);

        toolbarPane.setMargin(homeButton, new Insets(75,30,20,15));
        toolbarPane.setMargin(newProfileButton, new Insets(0,30,20,15));
        toolbarPane.setMargin(loginButton, new Insets(0,30,20,15));
        toolbarPane.setMargin(modeButton, new Insets(0,30,20,15));
        toolbarPane.setMargin(playButton,new Insets(0,30,20,15));


        toolbarPane.setMargin(saveButton,new Insets(0,30,20,15));

        toolbarPane.setMargin(helpButton,new Insets(60,0,20,15));
    }

    private ComboBox initalizeComboBox(){
        PropertyManager manager = PropertyManager.getManager();
        ComboBox<String> c = new ComboBox<String>();

        c.setPromptText(manager.getPropertyValue(MODE_TEXT.toString()));

        c.getItems().add(manager.getPropertyValue(CATEGORY1));
        c.getItems().add(manager.getPropertyValue(CATEGORY2));
        c.getItems().add(manager.getPropertyValue(CATEGORY3));
        c.getItems().add(manager.getPropertyValue(CATEGORY4));

        c.setButtonCell(new ListCell(){

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if(empty || item==null){
                    // styled like -fx-prompt-text-fill:
                    setStyle("-fx-text-fill: black");
                } else {
                    setStyle("-fx-text-fill: black");
                    setText(item.toString());
                }
            }

        });



        c.setMinWidth(200);
        c.setVisible(false);
        toolbarPane.getChildren().add(c);
        return c;
    }

    private void initializeToolbarHandlers(AppTemplate app) throws InstantiationException {
        try {
            Method         getFileControllerClassMethod = app.getClass().getMethod("getFileControllerClass");
            String         fileControllerClassName      = (String) getFileControllerClassMethod.invoke(app);
            Class<?>       klass                        = Class.forName("Controller." + fileControllerClassName);
            Constructor<?> constructor                  = klass.getConstructor(AppTemplate.class);
            fileController = (FileController) constructor.newInstance(app);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }

        homeButton.setOnAction(e -> fileController.handleHomeRequest());
        loginButton.setOnAction(e -> {
            try {
                fileController.handleLoginRequest();
            } catch (IOException e1) {
                e1.printStackTrace();
                System.exit(1);
            }
        });
        newProfileButton.setOnAction(e -> {
            try {
                fileController.handleNewUserRequest();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        modeButton.setOnAction(e -> fileController.handleModeRequest(modeButton.getSelectionModel().getSelectedIndex()));

        playButton.setOnAction(e -> fileController.handlePlayRequest());

        helpButton.setOnAction(e -> fileController.handleHelpRequest());

        saveButton.setOnAction(e -> {
            try{
                fileController.handleSaveRequest();
            } catch (IOException e1){
                e1.printStackTrace();
            }
        });
    }

    public void updateWorkspaceToolbar(boolean savable) {
        loginButton.setDisable(!savable);
        homeButton.setDisable(false);
        modeButton.setDisable(false);
    }

    // INITIALIZE THE WINDOW (i.e. STAGE) PUTTING ALL THE CONTROLS
    // THERE EXCEPT THE WORKSPACE, WHICH WILL BE ADDED THE FIRST
    // TIME A NEW Page IS CREATED OR LOADED
    private void initializeWindow() throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();

        // SET THE WINDOW TITLE
        primaryStage.setTitle(applicationTitle);

        // GET THE SIZE OF THE SCREEN
        Screen      screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        // AND USE IT TO SIZE THE WINDOW
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(Integer.parseInt(PropertyManager.getManager().getPropertyValue(APP_WINDOW_WIDTH)));
        primaryStage.setHeight(Integer.parseInt(PropertyManager.getManager().getPropertyValue(APP_WINDOW_HEIGHT)));
        primaryStage.centerOnScreen();
        // ADD THE TOOLBAR ONLY, NOTE THAT THE WORKSPACE
        // HAS BEEN CONSTRUCTED, BUT WON'T BE ADDED UNTIL
        // THE USER STARTS EDITING A COURSE
        appPane = new BorderPane();
        appPane.setLeft(toolbarPane);
        primaryScene = appSpecificWindowWidth < 1 || appSpecificWindowHeight < 1 ? new Scene(appPane)
                                                                                 : new Scene(appPane,
                                                                                             appSpecificWindowWidth,
                                                                                             appSpecificWindowHeight);

        URL imgDirURL = AppTemplate.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resrouces folder does not exist.");
        try (InputStream appLogoStream = Files.newInputStream(Paths.get(imgDirURL.toURI()).resolve(propertyManager.getPropertyValue(APP_LOGO)))) {
            primaryStage.getIcons().add(new Image(appLogoStream));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        primaryStage.setScene(primaryScene);
        primaryStage.show();
    }
    
    /**
     * This is a public helper method for initializing a simple button with
     * an icon and tooltip and placing it into a toolbar.
     *
     * @param toolbarPane Toolbar pane into which to place this button.
     * @param disabled    true if the button is to start off disabled, false otherwise.
     * @return A constructed, fully initialized button placed into its appropriate
     * pane container.
     */
    public Button initializeChildButton(Pane toolbarPane, String text, boolean disabled) throws IOException {
        PropertyManager propertyManager = PropertyManager.getManager();

        URL imgDirURL = AppTemplate.class.getClassLoader().getResource(APP_IMAGEDIR_PATH.getParameter());
        if (imgDirURL == null)
            throw new FileNotFoundException("Image resources folder does not exist.");

        Button button = new Button();
        button.setTextFill(javafx.scene.paint.Paint.valueOf("#000000"));
        button.setText(propertyManager.getPropertyValue(text));
        button.setMinWidth(200);
        button.setVisible(disabled);
        toolbarPane.getChildren().add(button);

        return button;
    }
    
    /**
     * This function specifies the CSS style classes for the controls managed
     * by this framework.
     */
    @Override
    public void initStyle() {
        // currently, we do not provide any stylization at the framework-level
    }
}

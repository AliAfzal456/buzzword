package GUI;

import Data.BuzzwordData;
import Managers.DataManager;
import apptemplate.AppTemplate;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static settings.AppPropertyType.*;

/**
 * Created by Afzal on 11/10/2016.
 */
public class NewUserPrompt extends Stage{
    private Label headingLabel, userLabel, passLabel, confirmPassLabel;
    private final TextField userInput = new TextField();
    private final PasswordField passInput = new PasswordField();
    private final PasswordField confirmPassInput = new PasswordField();
    private Button ok, cancel;
    private Scene loginScene;
    private boolean valid = false;
    private AppTemplate app;
    BuzzwordData data;

    public NewUserPrompt(AppTemplate app){
        this.app = app;
        data = (BuzzwordData)app.getDataComponent();

        PropertyManager manager = PropertyManager.getManager();

        // initalization and styling
        initOwner(app.getPrimaryStage());
        initStyle(StageStyle.UNDECORATED);
        initStyle(StageStyle.TRANSPARENT);
        initModality(Modality.WINDOW_MODAL);

        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        headingLabel = new Label(manager.getPropertyValue(NEW_CREDENTIALS_LABEL));
        headingLabel.setTextFill(Color.WHITE);
        gridPane.add(headingLabel, 0, 1, 4, 1);
        GridPane.setHalignment(headingLabel, HPos.CENTER);

        userLabel = new Label(manager.getPropertyValue(USERNAME_LABEL));
        userLabel.setTextFill(Color.WHITE);
        gridPane.add(userLabel, 0, 2);
        gridPane.add(userInput, 3,2);

        passLabel = new Label(manager.getPropertyValue(PASSWORD_LABEL));
        passLabel.setTextFill(Color.WHITE);
        gridPane.add(passLabel, 0, 3);
        gridPane.add(passInput, 3,3);

        confirmPassLabel = new Label(manager.getPropertyValue(CONFIRM_PASSWORD_LABEL));
        confirmPassLabel.setTextFill(Color.WHITE);
        gridPane.add(confirmPassLabel, 0,4);
        gridPane.add(confirmPassInput, 3,4);

        ok = new Button("OK");
        ok.setMinWidth(100);
        ok.setOnAction((event -> {
            if (userInput.getText().trim().isEmpty() || passInput.getText().trim().isEmpty() || confirmPassInput.getText().trim().isEmpty()){
                AppMessageDialogSingleton.getSingleton().show("Error", "One or more of the fields is empty");
            }
            else if (!(passInput.getText().equals(confirmPassInput.getText()))){
                AppMessageDialogSingleton.getSingleton().show("Error", "The passwords do not match");
            }

            else {
                saveNewUser();
                valid = true;
                System.out.println("data recieved");
                this.close();
            }
        }));
        gridPane.add(ok, 0, 5,2,1);
        GridPane.setHalignment(ok, HPos.LEFT);

        cancel = new Button("Cancel");
        cancel.setMinWidth(100);
        cancel.setOnAction((event -> {
            Workspace.state = Workspace.GameState.LOGGED_OUT_HOME;
            valid = false;
            System.out.println("cancelled");
            this.close();
        }));
        gridPane.add(cancel, 3, 5, 2, 1);
        GridPane.setHalignment(cancel, HPos.RIGHT);

        StackPane pane = new StackPane();
        pane.getChildren().addAll(gridPane);
        pane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        loginScene = new Scene(pane);
        loginScene.setFill(Color.TRANSPARENT);
        this.setScene(loginScene);
    }

    public boolean isValid(){
        return valid;
    }

    private void saveNewUser(){
        int[] standard = {1,1,1,1};
        data.setUserName(userInput.getText().trim());
        data.setPassword(MD5.getMD5(passInput.getText().trim()));
        data.setCurrentLevel(0);
        data.setLevels(standard);
        data.setHighestScores1(new int[]{0,0,0,0,0,0,0,0});
        data.setHighestScores2(new int[]{0,0,0,0,0,0,0,0});
        data.setHighestScores3(new int[]{0,0,0,0,0,0,0,0});
        data.setHighestScores4(new int[]{0,0,0,0,0,0,0,0});
        data.setHighestPointTotal(0);

        DataManager manager = (DataManager)app.getFileComponent();
        manager.setAppTemplate(app);

        try {
            manager.saveData(data, new File(this.getClass().getClassLoader().getResource("").getPath() + "/users/").toPath());
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
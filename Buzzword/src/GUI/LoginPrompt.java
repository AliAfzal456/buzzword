package GUI;

import Data.BuzzwordData;
import Managers.DataManager;
import apptemplate.AppTemplate;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static settings.AppPropertyType.*;

/**
 * Created by Afzal on 11/10/2016.
 */
public class LoginPrompt extends Stage {
    private Label headingLabel, userLabel, passLabel;
    private final TextField userInput = new TextField();
    private final PasswordField passInput = new PasswordField();
    private Button ok, cancel;
    private Scene loginScene;
    private boolean valid = false;
    private BuzzwordData data;
    private DataManager dataManager;
    private AppTemplate app;

    public LoginPrompt(AppTemplate appTemplate){
        PropertyManager manager = PropertyManager.getManager();
        data = (BuzzwordData)appTemplate.getDataComponent();
        dataManager = (DataManager) appTemplate.getFileComponent();
        app = appTemplate;

        AppMessageDialogSingleton.getSingleton().setButtonText("Ok");

        // initializaiton and styling
        initOwner(appTemplate.getPrimaryStage());
        initStyle(StageStyle.UNDECORATED); //get rid of x buttons
        initStyle(StageStyle.TRANSPARENT);
        initModality(Modality.WINDOW_MODAL);

        //gridpane to layout the details
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(5));
        gridPane.setHgap(7);
        gridPane.setVgap(7);

        headingLabel = new Label(manager.getPropertyValue(CREDENTIALS_LABEL));
        headingLabel.setTextFill(Color.WHITE);
        gridPane.add(headingLabel, 0,1, 4, 1);
        GridPane.setHalignment(headingLabel,HPos.CENTER);

        userLabel = new Label(manager.getPropertyValue(USERNAME_LABEL));
        userLabel.setTextFill(Color.WHITE);
        gridPane.add(userLabel, 0, 2);
        gridPane.add(userInput, 3,2);

        passLabel = new Label(manager.getPropertyValue(PASSWORD_LABEL));
        passLabel.setTextFill(Color.WHITE);
        gridPane.add(passLabel, 0, 3);
        gridPane.add(passInput, 3,3);

        ok = new Button("OK");
        ok.setMinWidth(100);
        ok.setOnAction((event -> {
            if (userInput.getText().trim().equals("") || passInput.getText().trim().equals(""))
                AppMessageDialogSingleton.getSingleton().show("Error", "One or more of the fields is empty");

            else {
                validateUser();
            }
        }));
        gridPane.add(ok, 0, 4,2,1);
        GridPane.setHalignment(ok, HPos.LEFT);

        cancel = new Button("Cancel");
        cancel.setMinWidth(100);
        cancel.setOnAction((event -> {
            System.out.println("cancelled");
            valid = false;
            this.close();
        }));
        gridPane.add(cancel, 3, 4, 2, 1);
        GridPane.setHalignment(cancel, HPos.RIGHT);

        StackPane pane = new StackPane();
        pane.getChildren().addAll(gridPane);
        pane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        loginScene = new Scene(pane);
        loginScene.setFill(Color.TRANSPARENT);
        this.setScene(loginScene);
    }

    private void validateUser(){
        dataManager.setAppTemplate(app);
        try{
            dataManager.loadData(data, new File(this.getClass().getClassLoader().getResource("").getPath() + "/users" + "/" + userInput.getText() + ".json").toPath());

            if (MD5.getMD5(passInput.getText()).equals(data.getPassword())) {
                System.out.println("data recieved");
                valid = true;
                this.close();
            }
            else
                AppMessageDialogSingleton.getSingleton().show("Error", "Incorrect user/password combination");

        }
        catch (FileNotFoundException e){
            AppMessageDialogSingleton.getSingleton().show("Error", "No such user/password combination");
        }
        catch (IOException e) {
            AppMessageDialogSingleton.getSingleton().show("Error", "No such user exists");
            valid = false;
        }
    }

    public boolean isValid(){
        return valid;
    }
}

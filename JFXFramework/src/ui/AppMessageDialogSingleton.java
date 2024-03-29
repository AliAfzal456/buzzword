package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import settings.InitializationParameters;

//import static settings.InitializationParameters.ERROR_DIALOG_BUTTON_LABEL;

/**
 * This class serves to present custom text messages to the user when
 * events occur. Note that it always provides the same controls, a label
 * with a message, and a single ok button.
 *
 * @author Richard McKenna, Ritwik Banerjee
 * @author ?
 * @version 1.0
 */
public class AppMessageDialogSingleton extends Stage {

    static AppMessageDialogSingleton singleton = null;
    
    VBox   messagePane;
    Scene  messageScene;
    Label  messageLabel;
    Button closeButton;

    private AppMessageDialogSingleton() { }
    
    /**
     * A static accessor method for getting the singleton object.
     *
     * @return The one singleton dialog of this object type.
     */
    public static AppMessageDialogSingleton getSingleton() {
        if (singleton == null)
            singleton = new AppMessageDialogSingleton();
        return singleton;
    }
    
    /**
     * This function fully initializes the singleton dialog for use.
     *
     * @param owner The window above which this dialog will be centered.
     */
    public void init(Stage owner) {
        // MAKE IT MODAL
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);
        
        // LABEL TO DISPLAY THE CUSTOM MESSAGE
        messageLabel = new Label();

        // CLOSE BUTTON
        closeButton = new Button(InitializationParameters.ERROR_DIALOG_BUTTON_LABEL.getParameter());
        closeButton.setOnAction(e -> this.close());

        // WE'LL PUT EVERYTHING HERE
        messagePane = new VBox();
        messagePane.setAlignment(Pos.CENTER);
        messagePane.getChildren().add(messageLabel);
        messagePane.getChildren().add(closeButton);
        
        // MAKE IT LOOK NICE
        messagePane.setPadding(new Insets(80, 60, 80, 60));
        messagePane.setSpacing(20);

        // AND PUT IT IN THE WINDOW
        messageScene = new Scene(messagePane);
        this.setScene(messageScene);
    }

    public void setButtonText(String text){
        closeButton.setText(text);
    }

    /**
     * This method loads a custom message into the label and
     * then pops open the dialog.
     *
     * @param title   The title to appear in the dialog window.
     * @param message Message to appear inside the dialog.
     */
    public void show(String title, String message) {
        // SET THE DIALOG TITLE BAR TITLE
        setTitle(title);

        // SET THE MESSAGE TO DISPLAY TO THE USER
        messageLabel.setText(message);

        // AND OPEN UP THIS DIALOG, MAKING SURE THE APPLICATION
        // WAITS FOR IT TO BE RESOLVED BEFORE LETTING THE USER
        // DO MORE WORK.
        showAndWait();
    }
}
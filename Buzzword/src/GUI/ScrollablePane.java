package GUI;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
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
public class ScrollablePane extends Stage {

    static ScrollablePane singleton = null;

    VBox messagePane;
    Scene messageScene;
    Label messageLabel;
    Button closeButton;

    public ScrollablePane(Stage owner) {
        init(owner);
    }

    /**
     * A static accessor method for getting the singleton object.
     *
     * @return The one singleton dialog of this object type.
     */

    /**
     * This function fully initializes the singleton dialog for use.
     *
     * @param owner The window above which this dialog will be centered.
     */
    public void init(Stage owner) {
        // MAKE IT MODAL
        initModality(Modality.WINDOW_MODAL);
        initOwner(owner);

        messageLabel = new Label();

        // CLOSE BUTTON
        closeButton = new Button(InitializationParameters.ERROR_DIALOG_BUTTON_LABEL.getParameter());


        // everything goes here
        messagePane = new VBox();
        messagePane.setAlignment(Pos.CENTER);
        messagePane.getChildren().add(messageLabel);
        messagePane.getChildren().add(closeButton);

        messagePane.setPadding(new Insets(80, 60, 80, 60));
        messagePane.setSpacing(20);


        ScrollPane scrollPane = new ScrollPane(messagePane);
        scrollPane.setMaxHeight(300);

        // AND PUT IT IN THE WINDOW
        messageScene = new Scene(scrollPane);
        this.setScene(messageScene);
    }

    public void setListener(EventHandler<ActionEvent> eventEventHandler){
        closeButton.setOnAction(eventEventHandler);
    }

    public Button getCloseButton(){
        return closeButton;
    }
    public void setButtonText(String text) {
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
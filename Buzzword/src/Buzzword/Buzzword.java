package Buzzword;

import Data.BuzzwordData;
import Managers.DataManager;
import apptemplate.AppTemplate;
import components.AppComponentsBuilder;
import components.AppDataComponent;
import components.AppFileComponent;
import components.AppWorkspaceComponent;
import GUI.Workspace;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.WindowEvent;
import ui.YesNoCancelDialogSingleton;


/**
 * Created by Afzal on 11/8/2016.
 */
public class Buzzword extends AppTemplate {
    public static void main(String[] args){ launch(args);
    }

    public String getFileControllerClass() { return "BuzzwordController";}

    public AppComponentsBuilder makeAppBuilderHook(){
        return new AppComponentsBuilder() {
            @Override
            public AppDataComponent buildDataComponent() throws Exception {
                return new BuzzwordData(Buzzword.this);
            }

            @Override
            public AppFileComponent buildFileComponent() throws Exception {
                return new DataManager();
            }

            @Override
            public AppWorkspaceComponent buildWorkspaceComponent() throws Exception {
                return new Workspace(Buzzword.this);
            }
        };
    }
}

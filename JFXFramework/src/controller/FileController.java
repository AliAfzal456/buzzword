package controller;

import java.io.IOException;

/**
 * @author Ritwik Banerjee
 */
public interface FileController {

    void handleHomeRequest();

    void handleLoginRequest() throws IOException;

    void handleNewUserRequest() throws IOException;

    void handleModeRequest(int selection);

    void handlePlayRequest();

    void handleHelpRequest();

    void handleSaveRequest() throws IOException;
}

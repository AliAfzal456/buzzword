package Managers;

import Buzzword.Buzzword;
import Data.BuzzwordData;
import apptemplate.AppTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import components.AppDataComponent;
import components.AppFileComponent;
import ui.AppMessageDialogSingleton;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Afzal on 11/9/2016.
 */
public class DataManager implements AppFileComponent {

    private BuzzwordData data;
    private AppTemplate app;

    public DataManager() {
    }

    public void setAppTemplate(AppTemplate appTemplate)
    {
        app = appTemplate;
    }

    @Override
    public void saveData(AppDataComponent data, Path filePath) throws IOException {
        this.data = (BuzzwordData)data;

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(new File(filePath.toString() + "/" + ((BuzzwordData) data).getUserName() + ".json"), this.data);
    }

    @Override
    public void loadData(AppDataComponent data, Path filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        BuzzwordData data2 = new BuzzwordData();
        data2 = mapper.readValue(filePath.toFile(), BuzzwordData.class);

        BuzzwordData currentData = (BuzzwordData)data;
        currentData.setUserName(data2.getUserName());
        currentData.setPassword(data2.getPassword());
        currentData.setCurrentLevel(data2.getCurrentLevel());
        currentData.setLevels(data2.getLevelsUnlocked());
        currentData.setHighestScores1(data2.getHighestScores1());
        currentData.setHighestScores2(data2.getHighestScores2());
        currentData.setHighestScores3(data2.getHighestScores3());
        currentData.setHighestScores4(data2.getHighestScores4());

        // FIX THIS REFERENCE HERE; WHAT'S HAPPENING IS THAT WE ARE CHANGING THE DATA COMPONENT REFERENCE, BUT NO ONE ELSE IN THE PROGRAM REALIZES IT
        //(BuzzwordData)d2 = data;
    }

    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException {

    }

    /*
    information to save:
        username (file name)
        password
        levelsUnlocked for each mode (as an array)
        last level selected
     */
}

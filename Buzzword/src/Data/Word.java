package Data;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Created by Afzal on 11/14/2016.
 */
public class Word {
    private final SimpleStringProperty word;
    private final SimpleIntegerProperty score;

    public Word(String word, int score)
    {
        this.word = new SimpleStringProperty(word);
        this.score = new SimpleIntegerProperty(score);
    }

    public String getWord(){
        return word.get();
    }

    public int getScore(){
        return score.get();
    }
}

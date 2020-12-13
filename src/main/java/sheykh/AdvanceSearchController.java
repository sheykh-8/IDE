package sheykh;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.utils.JFXHighlighter;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import sheykh.dataModel.SearchWord;
import sheykh.dataModel.Word;
import sheykh.dataModel.WordsModel;

import java.nio.file.Paths;

public class AdvanceSearchController {
    @FXML
    private JFXTextField prefixSearch;

    @FXML
    private JFXTextField middleSearch;

    @FXML
    private JFXTextField suffixSearch;

    @FXML
    private JFXToggleButton concatToggle;

    @FXML
    private JFXButton searchButton;

//    private Controller mainController;
    private TabPane tabs;
    private JFXHighlighter activeHighlighter;

    @FXML
    public void initialize(){


        concatToggle.selectedProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue) {
                middleSearch.disableProperty().bind(concatToggle.selectedProperty());
                suffixSearch.disableProperty().unbind();
                prefixSearch.disableProperty().unbind();
                middleSearch.disableProperty().unbind();
            }
            else{
                suffixSearch.disableProperty().bind(prefixSearch.textProperty().isNotEmpty().or(middleSearch.textProperty().isNotEmpty()));
                prefixSearch.disableProperty().bind(suffixSearch.textProperty().isNotEmpty().or(middleSearch.textProperty().isNotEmpty()));
                middleSearch.disableProperty().bind(suffixSearch.textProperty().isNotEmpty().or(prefixSearch.textProperty().isNotEmpty()));
            }
        });

        concatToggle.setSelected(true);


    }

    @FXML
    private void searchButton(){
        Tab opentab = tabs.getSelectionModel().getSelectedItem();
        if(opentab == null)
            return;

        TextArea content = (TextArea) opentab.getContent();
        WordsModel model = new WordsModel(content.getText(), Paths.get("search.txt"));

        boolean concat = concatToggle.isSelected();
        String pre = prefixSearch.getText();
        String suff = suffixSearch.getText();
        String mid = middleSearch.getText();

        if(concat){
            if(pre == null || suff == null)
                return;
            concatSearch(content, pre, suff);
            return;
        } else {
            if(!prefixSearch.isDisable()){
                if(pre == null)
                    return;
                preSearch(model, content, pre);
                return;
            }

            if(!middleSearch.isDisable()){
                if(mid == null)
                    return;
                midSearch(model, content, mid);
                return;
            }

            if(!suffixSearch.isDisable()){
                if(suff == null)
                    return;
                sufSearch(model, content, suff);
                return;
            }
        }
    }


    private void concatSearch(TextArea content, String first, String second){
        int firstIndex = content.getText().indexOf(first);
        int lastIndex = content.getText().lastIndexOf(second);
        highlight(content, firstIndex, lastIndex);
    }
    private void preSearch(WordsModel model, TextArea content, String search){
        SearchWord prefix = new SearchWord(model, search, SearchWord.SearchMode.PREFIX);
        highlight(content, prefix.getResults(), prefixSearch);
    }

    private void midSearch(WordsModel model, TextArea content, String search){
        SearchWord middle = new SearchWord(model, search, SearchWord.SearchMode.MIDDLE);
        highlight(content, middle.getResults(), middleSearch);
    }
    private void sufSearch(WordsModel model, TextArea content, String search){
        SearchWord suffix = new SearchWord(model, search, SearchWord.SearchMode.SUFFIX);
        highlight(content, suffix.getResults(), suffixSearch);
    }

    private void highlight(TextArea content, ObservableList<Word> result, JFXTextField field){
        if(activeHighlighter != null)
            activeHighlighter.setPaint(null);
        JFXHighlighter highlighter = new JFXHighlighter();
        activeHighlighter = highlighter;
        highlighter.setPaint(Color.web("#b3e5ca"));
        result.forEach(word -> {
            Platform.runLater(() -> highlighter.highlight((Parent) content.lookup(".content"), word.getWord()));
            Platform.runLater(() -> highlighter.highlight((Parent) content.lookup(".content"), word.getWord()));
        });

        field.textProperty().addListener((obs, oldvalue, newValue) -> {
            Platform.runLater(() -> {
                highlighter.clear();
                highlighter.setPaint(null);
            });
        });

    }

    private void highlight(TextArea content, int firstIndex, int lastIndex){
        if(firstIndex < 0 || lastIndex < 0 )
            return;
        Platform.runLater(() -> content.selectRange(firstIndex, lastIndex));

    }

    public void setTabs(TabPane tabs){
        this.tabs = tabs;
    }

    public void closeHighlighter(){
        if(activeHighlighter != null)
            activeHighlighter.setPaint(null);
    }


}

package sheykh;

import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import sheykh.dataModel.Word;

import java.io.File;

public class WordsDialogController {

    @FXML
    private ListView<Word.Sentence> wordsView;

    private Controller mainController;

    public void initialize(){
        wordsView.setMinWidth(600);

        wordsView.setCellFactory(new Callback<ListView<Word.Sentence>, ListCell<Word.Sentence>>() {
            @Override
            public ListCell<Word.Sentence> call(ListView<Word.Sentence> param) {
                ListCell<Word.Sentence> cell = new ListCell<>(){
                    @Override
                    protected void updateItem(Word.Sentence item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                            setGraphic(null);
                        } else{
                            setText(item.getSentence());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "letter.png"))));
                        }
                    }
                };

                return cell;
            }
        });

        wordsView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2){
                    Word.Sentence selected = wordsView.getSelectionModel().getSelectedItem();
                    if(selected != null){
                        System.out.println(selected.getFile());
                        mainController.openFile(selected.getFile());
                    }
                }
            }
        });
    }

    public void setMainController(Controller controller){
        this.mainController = controller;
    }

    public void listSet(ObservableList<Word.Sentence> sentence){
        wordsView.setItems(sentence);
    }

}

package sheykh;

import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXSlider;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.util.Callback;

import java.io.File;

public class Settings {

    @FXML
    private JFXComboBox<Font> fontFamily;
    @FXML
    private JFXSlider fontSize;

    @FXML
    public void initialize(){
        fontSize.setValue(14);
        Font times = Font.font("Times new Roman");
        Font tahoma = Font.font("Tahoma");
        Font courier = Font.font("Courier");
        Font georgia = Font.font("Georgia");
        Font palatino = Font.font("Palatino");

        fontFamily.getItems().addAll(times, tahoma, courier, georgia, palatino);

        fontFamily.setCellFactory(new Callback<ListView<Font>, ListCell<Font>>() {
            @Override
            public ListCell<Font> call(ListView<Font> param) {
                ListCell<Font> cell = new ListCell<>(){
                    @Override
                    protected void updateItem(Font item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                            setGraphic(null);
                        } else{
                            setText(item.getName());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "font.png"))));
                        }
                    }
                };
                return cell;
            }
        });



    }


    public Font getFont(){
        if(fontFamily.getValue() == null){
            return Font.font("Times new Roman", 14);
        }
        return Font.font(fontFamily.getValue().getName(), fontSize.getValue());
    }

}

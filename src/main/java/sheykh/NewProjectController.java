package sheykh;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.io.File;
import java.nio.file.Path;

public class NewProjectController {
    @FXML
    private TextField name;

    @FXML
    public void initialize(){

    }

    protected boolean createProject(Path workSpace){
        String projectName = name.getText();
        File project = new File(workSpace.toString() + File.separator + projectName);
        boolean result = project.mkdir();
        if(result)
            System.out.println("New Project was created.");
        else{
            System.out.println("Failed to create a project.");
        }
        return result;
    }
}

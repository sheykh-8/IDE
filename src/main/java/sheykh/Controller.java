package sheykh;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.utils.JFXHighlighter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import sheykh.dataModel.Word;
import sheykh.dataModel.WordsModel;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Optional;

public class Controller {

    @FXML
    private TabPane tabs;
    @FXML
    private TreeView<Path> directories;
    @FXML
    private JFXButton openFolder;
    @FXML
    private Button refreshDirectory;
    @FXML
    private TreeView<Path> projectView;
    @FXML
    private Button newProject;
    @FXML
    private JFXButton workspace;
    @FXML
    private TableView<Word> wordsInProject;
    @FXML
    private TableView<Word> wordsInDirectory;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private TextField regSearchT;
    @FXML
    private JFXButton regSearchB;
    @FXML
    private Hyperlink advanceSearchButton;



    private TreeItem<Path> root;
    private Path openDirectory;
    private Path openedFile;

    //project pane
    private TreeItem<Path> workspaceRoot;
    private Path openedWorkspace;
    private ObservableList<Path> filesInProject = FXCollections.observableArrayList();


    private ObservableList<Path> filesInDirecroty = FXCollections.observableArrayList();
    private WordsModel openTabWords;
    private JFXHighlighter highlighter = new JFXHighlighter();

    //setting properties
    private Font font;
    private double fontSize;


    @FXML
    public void initialize() {
        font = FXRun.getFont();


        //treeview section
        directories.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        refreshDirectory.setOnAction(event -> {
            if(openDirectory!=null)
                treeUpdate(openDirectory);
        });

        ContextMenu fileContextMenu = new ContextMenu();
        MenuItem deleteOp = new MenuItem("Delete");
        deleteOp.setOnAction((event) -> {
            Path deleteFile = directories.getSelectionModel().getSelectedItem().getValue();
            if(deleteFile != null){
                deleteAction(deleteFile);
            }
        });

        MenuItem addToProjectOp = new MenuItem("Add To Project");
        addToProjectOp.setOnAction((event) -> {
            Path add = directories.getSelectionModel().getSelectedItem().getValue();
            try {
                Path destination = projectView.getSelectionModel().getSelectedItem().getValue();
                if(add != null){
                    if(Files.isDirectory(destination)){
                        try{
                            Files.copy(add, Paths.get(destination + File.separator + add.getFileName()), StandardCopyOption.REPLACE_EXISTING);

                        } catch(IOException e){
                            System.out.println("couldent' add the file to project");
                        }
                        updateWorkspace(openedWorkspace);
                    }
                }
            } catch(NullPointerException e){
                System.out.println("choose a project first.");
            }

        });

        fileContextMenu.getItems().addAll(deleteOp, addToProjectOp);

        ContextMenu directoryContextMenu = new ContextMenu();

        directories.setCellFactory(new Callback<>() {
            @Override
            public TreeCell<Path> call(TreeView<Path> param) {
                TreeCell<Path> cell = new TreeCell<>() {
                    @Override
                    protected void updateItem(Path item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setText(null);
                            setGraphic(null);
                        } else if(Files.isRegularFile(item)) {
                            setText(item.getFileName().toString());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "file.png"))));
                        } else {
                            setText(item.getFileName().toString());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "folder.png"))));
                        }
                        //***********************************
                        //***********************************
                        if (!empty && Files.isRegularFile(item)) {
                            setContextMenu(fileContextMenu);
                        }
                    }
                };

                return cell;
            }
        });
        directories.setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                TreeItem<Path> p = directories.getSelectionModel().getSelectedItem();
                if(p != null){
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {
                            Path path = p.getValue();
                            if (Files.isRegularFile(path)) {
                                openFile(path);
                            }
                        }
                    }
                }

            }
        });

        //new Project Button
        newProject.setOnAction((event) -> {
            if(openedWorkspace != null) {
                projectView.getSelectionModel().clearSelection();
                Dialog<ButtonType> dialog = new Dialog<>();
                dialog.initOwner(mainBorderPane.getScene().getWindow());
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource(File.separator + "newProject.fxml"));
                try {
                    dialog.getDialogPane().setContent(loader.load());
                } catch (IOException e) {
                    System.out.println("couldent open the dialog for new project");
                }
                dialog.setTitle("New Project");
                dialog.getDialogPane().getButtonTypes().add(ButtonType.FINISH);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

                Optional<ButtonType> result = dialog.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.FINISH) {
                    if (openedWorkspace != null) {
                        NewProjectController controller = loader.getController();
                        if (controller.createProject(openedWorkspace)) {
                            updateWorkspace(openedWorkspace);
                            projectView.getSelectionModel().select(0);
                        }
                    }
                } else {
                    System.out.println("nothing yet");
                }
            }
        });

        //project view context menue
        ContextMenu projectPaneMenue = new ContextMenu();
        MenuItem delete = new MenuItem("Delete");
        delete.setOnAction((event) -> {
            Path selected = projectView.getSelectionModel().getSelectedItem().getValue();
            if(selected != null){
                deleteAction(selected);
                updateWorkspace(openedWorkspace);
            }

        });
        projectPaneMenue.getItems().add(delete);

        //project view cell factory
        projectView.setCellFactory(new Callback<TreeView<Path>, TreeCell<Path>>() {
            @Override
            public TreeCell<Path> call(TreeView<Path> param) {
                TreeCell<Path> cell = new TreeCell<>(){
                    @Override
                    protected void updateItem(Path item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                            setGraphic(null);
                        } else if(Files.isDirectory(item)){
                            setText(item.getFileName().toString());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "project-management.png"))));
                        } else if(Files.isRegularFile(item)){
                            setText(item.getFileName().toString());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "txt.png"))));
                        }

                        if(!empty){
                            setContextMenu(projectPaneMenue);
                        }
                    }
                };

                return cell;
            }
        });


        //open file on click
        projectView.setOnMouseClicked(new EventHandler<>() {
            @Override
            public void handle(MouseEvent event) {
                TreeItem<Path> p = projectView.getSelectionModel().getSelectedItem();
                if(p != null){
                    if (event.getButton().equals(MouseButton.PRIMARY)) {
                        if (event.getClickCount() == 2) {
                            Path path = p.getValue();
                            if (Files.isRegularFile(path)) {
                                openFile(path);
                            }
                        }
                    }
                }

            }
        });

        //update words in project
        projectView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue == null){
                wordsInProject.setItems(null);
            } else {
                Path chosen = newValue.getValue();
                ObservableList<Path> projectFiles = FXCollections.observableArrayList();
                if (Files.isDirectory(chosen)) {
                    try {
                        DirectoryStream<Path> stream = Files.newDirectoryStream(chosen);
                        for (Path p : stream) {
                            projectFiles.add(p);
                        }
                        wordsInProject.setItems(joinLists(projectFiles));
                    } catch (IOException e) {
                        System.out.println("coulden't reach to the project files.");
                        e.printStackTrace();
                    }
                }
            }
        });


        //Words in project
        TableColumn<Word, String> wordColumn = new TableColumn<>("Words");
        wordColumn.setMinWidth(124);
        wordColumn.setMaxWidth(124);
        wordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));

        TableColumn<Word, String> repeatColumn = new TableColumn<>("Repeat");
        repeatColumn.setMinWidth(60);
        repeatColumn.setMaxWidth(60);
        repeatColumn.setCellValueFactory(new PropertyValueFactory<>("rep"));
        wordsInProject.getColumns().addAll(wordColumn, repeatColumn);

        wordsInProject.setOnMouseClicked((event -> {
            Word word = wordsInProject.getSelectionModel().getSelectedItem();
            if(word != null) {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    Stage wordWindow = new Stage();
                    wordWindow.setTitle(word.getWord());
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource(File.separator + "wordsDialog.fxml"));
                    try{
                        wordWindow.initOwner(mainBorderPane.getScene().getWindow());
                        wordWindow.setScene(new Scene(loader.load(), 600.0, 400.0));
                        WordsDialogController controller = loader.getController();
                        controller.listSet(word.getInvolved());
                        controller.setMainController(this);
                        wordWindow.setResizable(false);
                        wordWindow.show();
                    } catch(IOException e){
                        System.out.println(e.getMessage());
                    }
                }
            }
        }));

        wordColumn.setCellFactory(new Callback<TableColumn<Word, String>, TableCell<Word, String>>() {
            @Override
            public TableCell<Word, String> call(TableColumn<Word, String> param) {
                TableCell<Word, String> cell = new TableCell<>(){
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                            setGraphic(null);
                        } else{
                            setText(item);
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "letter.png"))));
                        }
                    }
                };

                return cell;
            }
        });



        //Words in directory table view

        TableColumn<Word, String> dWordColumn = new TableColumn<>("Words");
        dWordColumn.setMinWidth(124);
        dWordColumn.setMaxWidth(124);
        dWordColumn.setCellValueFactory(new PropertyValueFactory<>("word"));

        TableColumn<Word, String> dRepeatColumn = new TableColumn<>("Repeat");
        dRepeatColumn.setMinWidth(60);
        dRepeatColumn.setMaxWidth(60);
        dRepeatColumn.setCellValueFactory(new PropertyValueFactory<>("rep"));


        this.wordsInDirectory.getColumns().addAll(dWordColumn, dRepeatColumn);

        this.filesInDirecroty.addListener(new ListChangeListener<Path>() {
            @Override
            public void onChanged(Change<? extends Path> c) {
                wordsInDirectory.setItems(joinLists(filesInDirecroty));
            }
        });

        dWordColumn.setCellFactory(new Callback<TableColumn<Word, String>, TableCell<Word, String>>() {
            @Override
            public TableCell<Word, String> call(TableColumn<Word, String> param) {
                TableCell<Word, String> cell = new TableCell<>(){
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(empty){
                            setText(null);
                            setGraphic(null);
                        } else{
                            setText(item);
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(File.separator + "resources" + File.separator + "letter.png"))));
                        }
                    }
                };

                return cell;
            }
        });

        this.wordsInDirectory.setOnMouseClicked( ( event -> {
            Word selected = wordsInDirectory.getSelectionModel().getSelectedItem();
            if(selected != null){
                if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2){
                    Stage wordWindow = new Stage();
                    wordWindow.setTitle(selected.getWord());
                    wordWindow.setResizable(false);
                    wordWindow.initOwner(mainBorderPane.getScene().getWindow());
                    FXMLLoader loader = new FXMLLoader();
                    loader.setLocation(getClass().getResource(File.separator + "wordsDialog.fxml"));

                    try{
                        wordWindow.setScene(new Scene(loader.load(), 600, 400));
                        WordsDialogController controller = loader.getController();
                        controller.listSet(selected.getInvolved());
                        controller.setMainController(this);
                        wordWindow.show();
                    } catch(IOException e){
                        e.printStackTrace();
                    }
                }
            }
        }));

        //Tab Pane
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if(newValue != null){
                FXRun.changeTitle(newValue.getText() + " - Text Editor");
                TextArea content = (TextArea) newValue.getContent();
                content.textProperty().addListener((observable, oValue, nValue) -> {
                    if(nValue != null){
                        String oldTitle = newValue.getText();
                        if(!oldTitle.contains("*")) {
                            StringBuilder newTitle = new StringBuilder(oldTitle);
                            newTitle.append("*");
                            newValue.setText(newTitle.toString());
                        }
                    }
                });

            }else{
                FXRun.changeTitle("Text Editor");
            }
        });


        //Project View
        projectView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        //**************************************************
        //Search section

        //clear highlight on change
        regSearchT.textProperty().addListener((obs, oldValue, newValue) -> {
            Platform.runLater(() -> highlighter.clear() );
        });



    }

    @FXML
    private void settings(){
        Dialog<ButtonType> settings = new Dialog<>();
        settings.initOwner(mainBorderPane.getScene().getWindow());
        settings.setTitle("Settings");
        settings.setResizable(false);
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(File.separator + "settings.fxml"));
        try{
            settings.getDialogPane().setContent(loader.load());
        } catch(IOException e){
            System.out.println("couldn't open the settings window.");
            e.printStackTrace();
        }

        settings.getDialogPane().getButtonTypes().addAll(ButtonType.APPLY, ButtonType.CANCEL);

        Optional<ButtonType> result = settings.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.APPLY){
            Settings controller = loader.getController();
            font = controller.getFont();
            updateSetting(font);
            saveSettings(font);
        }

    }

    private void updateSetting(Font font) {
        for(Tab t: tabs.getTabs()){
            TextArea content = (TextArea) t.getContent();
            content.setFont(font);
        }
    }


    @FXML
    private void exitMethod() {
        Platform.exit();
    }

    @FXML
    private void openDirectory(){
        DirectoryChooser dirChooser = new DirectoryChooser();
        File selectedDirectory = dirChooser.showDialog(mainBorderPane.getScene().getWindow());

        if(selectedDirectory == null){
            return;
        }

        Path path = selectedDirectory.toPath();
        this.openDirectory = path;
        treeUpdate(path);
    }

    private void treeUpdate(Path path){
        root = fileWalker(path);
        root.setExpanded(true);
        ArrayList<Path> existingFiles = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.txt");
        try{
            Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if(matcher.matches(file)) existingFiles.add(file);
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    System.out.println("Couldent open the file: " + file.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch(IOException e){
            e.printStackTrace();
        } finally {
            filesInDirecroty.setAll(existingFiles);
        }
        directories.setRoot(root);
        directories.showRootProperty().setValue(false);
        openFolder.visibleProperty().bind(directories.getRoot().valueProperty().isNull());
    }

    private TreeItem<Path> fileWalker(Path path){
        return new TreeItem<Path>(path){
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<Path>> getChildren() {
                if(isFirstTimeChildren){
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildLeaf(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if(isFirstTimeLeaf){
                    isFirstTimeLeaf = false;
                    Path p = (Path) getValue();
                    isLeaf =  Files.isRegularFile(p);
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<Path>> buildLeaf(TreeItem<Path> treeItem){
                Path path = treeItem.getValue();

                if(path == null)
                    return FXCollections.emptyObservableList();
                else if(Files.isRegularFile(path))
                    return FXCollections.emptyObservableList();

                DirectoryStream.Filter<Path> streamFilter = new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.txt");
                        return (matcher.matches(entry) || Files.isDirectory(entry));
                    }
                };

                try(DirectoryStream<Path> files = Files.newDirectoryStream(path, streamFilter)){
                    if(files != null){
                        ObservableList<TreeItem<Path>> children = FXCollections.observableArrayList();
                        for(Path p: files)
                            children.add(fileWalker(p));
                        return children;
                    }
                } catch(IOException e){
                    e.printStackTrace();
                }


                return FXCollections.emptyObservableList();
            }
        };
    }

    //opening a workspace for the project pane
    @FXML
    private void openWorkspace(){
        DirectoryChooser chooser = new DirectoryChooser();
        File chosen = chooser.showDialog(mainBorderPane.getScene().getWindow());

        if(chosen == null) {
            return;
        }


        Path path = chosen.toPath();
        this.openedWorkspace = path;
        updateWorkspace(path);
    }

    private void updateWorkspace(Path path){
        workspaceRoot = fileWalker(path);
        workspaceRoot.setExpanded(true);
        projectView.setRoot(workspaceRoot);
        projectView.showRootProperty().setValue(false);
        workspace.visibleProperty().bind(projectView.getRoot().valueProperty().isNull());
    }

    private void deleteAction(Path path){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete File");
        alert.setHeaderText("Are you sure to delete the file " + path.getFileName().toString() + " From your PC?");
        alert.setContentText("Press Ok to contubue and Cancel to back down.");

        Optional<ButtonType> result = alert.showAndWait();

        if(result.isPresent() && result.get() == ButtonType.OK){
            try{
                Files.delete(path);
            } catch(DirectoryNotEmptyException e){
                System.out.printf("%s is not empty.", path);
            } catch(IOException e){
                System.out.println(e);
            }
        }

    }

    private ImageView iconBuidler(String path) {
        return new ImageView(new Image(getClass().getResourceAsStream(path)));
    }

    protected void openFile(Path path) {

        Tab newTab = new Tab();
        newTab.setText(path.getFileName().toString());

        //chech if the tab is already opened
        for(Tab tab: tabs.getTabs()){
            if(tab.getUserData().equals(path)){
                System.out.println("this tab is already open in the pane");
                return;
            }
        }

        openedFile = path;
        TextArea content = new TextArea();
        content.setWrapText(true);
        content.setText(readFile(path));
        newTab.setContent(content);
        newTab.setUserData(path);
        tabs.getTabs().add(newTab);
        tabs.getSelectionModel().select(newTab);

    }

    protected String readFile(Path path){
        try(BufferedReader reader = Files.newBufferedReader(path)){
            StringBuilder str = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                str.append(line);
                str.append("\n");
            }
            return str.toString();
        } catch(IOException e){
            System.out.println("Coulden't open the file: " + e.getMessage());
            return "";
        }
    }

    private ObservableList<Word> joinLists(ObservableList<Path> list){
        ArrayList<Word> res = new ArrayList<>();
        ArrayList<Word> test = new ArrayList<>();
        for(int i = 0; i<list.size(); i++){
            WordsModel model = new WordsModel(readFile(list.get(i)), list.get(i));
            model.getColelction(res);
        }
        return FXCollections.observableArrayList(res);
    }

    @FXML
    private void regularSearch(){
        JFXHighlighter highlighter = new JFXHighlighter();
        highlighter.setPaint(Color.web("#b3e5ca"));
        String search = regSearchT.getText();
        TextArea selected = (TextArea) tabs.getSelectionModel().getSelectedItem().getContent();
        if(selected == null && search == null)
            return;

        Platform.runLater(() -> {
            highlighter.highlight((Parent) selected.lookup(".content"), search);
            highlighter.highlight((Parent) selected.lookup(".content"), search);
        });

        regSearchT.textProperty().addListener((obs, oldvalue, newValue) -> {
            Platform.runLater(() -> {
                highlighter.clear();
                highlighter.setPaint(null);
            });
        });

    }

    @FXML
    private void advanceSearch(){
        Stage adSearch = new Stage();
        adSearch.setTitle("Advance Search");
        adSearch.setResizable(false);
        adSearch.initOwner(mainBorderPane.getScene().getWindow());
        advanceSearchButton.disableProperty().bind(adSearch.showingProperty());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(File.separator + "advanceSearch.fxml"));
        try{
            adSearch.setScene(new Scene(loader.load(), 500, 200));
            AdvanceSearchController advanceSearchController = loader.getController();
            adSearch.setOnCloseRequest(event -> {
                advanceSearchController.closeHighlighter();
            });
            advanceSearchController.setTabs(tabs);
            adSearch.show();
        } catch(IOException e){
            e.printStackTrace();
        }

    }


    @FXML
    private void newFile(){
        Tab newTab = new Tab();
        newTab.setText("untitled.ss");
        newTab.setUserData(Paths.get("untitled.ss"));
        TextArea content = new TextArea();
        newTab.setContent(content);
        tabs.getTabs().add(newTab);
        openedFile = null;
    }

    @FXML
    private void saveAs(){
        Tab openTab = tabs.getSelectionModel().getSelectedItem();
        if(openTab == null)
            return;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text", "*.ss"));
        File file = chooser.showSaveDialog(mainBorderPane.getScene().getWindow());

        if(file == null) {
            return;
        }

        try{
            file.createNewFile();
        } catch(IOException e){
            System.out.println("coulden't create a new file.");
            e.printStackTrace();
        }

        openedFile = file.toPath();

        saveFile();
        //tabs.getTabs().remove(tabs.getSelectionModel().getSelectedItem());
        openFile(openedFile);

    }


    @FXML
    private void saveFile() {
        if(openedFile == null){
            saveAs();
            return;
        }
        Tab selectedTab = tabs.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            System.out.println("Nothing to save");
            return;
        }
        Path openFile = openedFile;
        TextArea textArea = (TextArea) selectedTab.getContent();
        String[] content = textArea.getText().split("\n");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(openFile.toAbsolutePath().toString()))) {
            for (String str : content) {
                writer.write(str);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("couldn't access file: " + e.getMessage());
        } finally {
            String tabTitle = selectedTab.getText();
            int starIndex = tabTitle.indexOf('*');
            if (starIndex > 0) {
                selectedTab.setText(tabTitle.substring(0, starIndex));
            }

        }
    }

    public JFXHighlighter getHighlighter(){
        return this.highlighter;
    }

    public TabPane getTabs(){
        return this.tabs;
    }

    private void saveSettings(Font chosenFont) {
        if(chosenFont == null)
            return;

        File file = new File(System.getProperty("user.home") + File.separator + "settings.bin");
        try(ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file))){
            output.writeUTF(chosenFont.getName());
            output.writeDouble(chosenFont.getSize());
        } catch(FileNotFoundException e){
            System.out.println("file wasn't on the disk, try again...");
            try{
                file.createNewFile();
            } catch(IOException e1){
                System.out.println("couldn't create the file.");
            }
        } catch(IOException e){
            System.out.println("some problem in outputting...");
            e.printStackTrace();
        }
    }

    @FXML
    void play(){
        File media = new File("E:\\Language Stuff\\French Memrise\\oui.mp3");
        if(!media.exists()) System.out.println("this file is empty");
        MediaPlayer audio = new MediaPlayer(new Media(media.toURI().toString()));
        audio.setVolume(50);
        Platform.runLater(() -> audio.play());
    }

}
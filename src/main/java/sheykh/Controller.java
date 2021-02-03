package sheykh;

import com.jfoenix.utils.JFXHighlighter;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
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
    private Button openFolder;
    @FXML
    private Button refreshDirectory;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private TextField regSearchT;
    @FXML
    private Button regSearchB;
    @FXML
    private HBox consoleHeader;
    @FXML
    private VBox consoleContent;


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
            if (openDirectory != null)
                treeUpdate(openDirectory);
        });

        ContextMenu fileContextMenu = new ContextMenu();
        MenuItem deleteOp = new MenuItem("Delete");
        deleteOp.setOnAction((event) -> {
            Path deleteFile = directories.getSelectionModel().getSelectedItem().getValue();
            if (deleteFile != null) {
                deleteAction(deleteFile);
            }
        });


        fileContextMenu.getItems().addAll(deleteOp);

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
                        } else if (Files.isRegularFile(item)) {
                            setText(item.getFileName().toString());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(FXRun.pathPreFix + File.separator + "resources" + File.separator + "file.png"))));
                        } else {
                            setText(item.getFileName().toString());
                            setGraphic(new ImageView(new Image(getClass().getResourceAsStream(FXRun.pathPreFix + File.separator + "resources" + File.separator + "folder.png"))));
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
                if (p != null) {
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

        //project view context menue

        //project view cell factory


        //open file on click

        //update words in project


        //Words in project


        //Words in directory table view

        //Tab Pane
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.ALL_TABS);
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                FXRun.changeTitle(newValue.getText() + " - Text Editor");
                CodeArea content = getTabContent(newValue);
                content.textProperty().addListener((observable, oValue, nValue) -> {
                    if (nValue != null) {
                        String oldTitle = newValue.getText();
                        if (!oldTitle.contains("*")) {
                            newValue.setText(oldTitle + "*");
                        }
                    }
                });

            } else {
                FXRun.changeTitle("Text Editor");
            }
        });
        //**************************************************
        //Search section

        //clear highlight on change
        regSearchT.textProperty().addListener((obs, oldValue, newValue) -> {
            Platform.runLater(() -> highlighter.clear());
        });


    }

//    private void updateSetting(Font font) {
//        for (Tab t : tabs.getTabs()) {
//            TextArea content = (TextArea) t.getContent();
//            content.setFont(font);
//        }
//    }


    @FXML
    private void exitMethod() {
        Platform.exit();
    }

    @FXML
    private void openDirectory() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        File selectedDirectory = dirChooser.showDialog(mainBorderPane.getScene().getWindow());

        if (selectedDirectory == null) {
            return;
        }

        Path path = selectedDirectory.toPath();
        this.openDirectory = path;
        treeUpdate(path);
    }

    private void treeUpdate(Path path) {
        root = fileWalker(path);
        root.setExpanded(true);
        ArrayList<Path> existingFiles = new ArrayList<>();
        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.ss");
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(file)) existingFiles.add(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    System.out.println("Couldent open the file: " + file.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            filesInDirecroty.setAll(existingFiles);
        }
        directories.setRoot(root);
        directories.showRootProperty().setValue(false);
//        openFolder.visibleProperty().bind(directories.getRoot().valueProperty().isNull());
    }

    private TreeItem<Path> fileWalker(Path path) {
        return new TreeItem<Path>(path) {
            private boolean isLeaf;
            private boolean isFirstTimeChildren = true;
            private boolean isFirstTimeLeaf = true;

            @Override
            public ObservableList<TreeItem<Path>> getChildren() {
                if (isFirstTimeChildren) {
                    isFirstTimeChildren = false;
                    super.getChildren().setAll(buildLeaf(this));
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                if (isFirstTimeLeaf) {
                    isFirstTimeLeaf = false;
                    Path p = (Path) getValue();
                    isLeaf = Files.isRegularFile(p);
                }
                return isLeaf;
            }

            private ObservableList<TreeItem<Path>> buildLeaf(TreeItem<Path> treeItem) {
                Path path = treeItem.getValue();

                if (path == null)
                    return FXCollections.emptyObservableList();
                else if (Files.isRegularFile(path))
                    return FXCollections.emptyObservableList();

                DirectoryStream.Filter<Path> streamFilter = new DirectoryStream.Filter<Path>() {
                    @Override
                    public boolean accept(Path entry) throws IOException {
                        PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**.ss");
                        return (matcher.matches(entry) || Files.isDirectory(entry));
                    }
                };

                try (DirectoryStream<Path> files = Files.newDirectoryStream(path, streamFilter)) {
                    if (files != null) {
                        ObservableList<TreeItem<Path>> children = FXCollections.observableArrayList();
                        for (Path p : files)
                            children.add(fileWalker(p));
                        return children;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


                return FXCollections.emptyObservableList();
            }
        };
    }


    private void deleteAction(Path path) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete File");
        alert.setHeaderText("Are you sure you want to delete the file " + path.getFileName().toString() + " From your PC?");
        alert.setContentText("Press Ok to continue and Cancel to back down.");

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Files.delete(path);
            } catch (DirectoryNotEmptyException e) {
                System.out.printf("%s is not empty.", path);
            } catch (IOException e) {
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
        for (Tab tab : tabs.getTabs()) {
            if (tab.getUserData().equals(path)) {
                System.out.println("this tab is already open in the pane");
                return;
            }
        }

        openedFile = path;
//        content.setText(readFile(path));
        new SinaSharpTab(readFile(path), newTab);
        newTab.setUserData(path);
        tabs.getTabs().add(newTab);
        tabs.getSelectionModel().select(newTab);

    }

    protected String readFile(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            StringBuilder str = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                str.append(line);
                str.append("\n");
            }
            return str.toString();

        } catch (IOException e) {
            System.out.println("Coulden't open the file: " + e.getMessage());
            return "";
        }
    }

    private ObservableList<Word> joinLists(ObservableList<Path> list) {
        ArrayList<Word> res = new ArrayList<>();
        ArrayList<Word> test = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            WordsModel model = new WordsModel(readFile(list.get(i)), list.get(i));
            model.getColelction(res);
        }
        return FXCollections.observableArrayList(res);
    }

    @FXML
    private void regularSearch() {
        JFXHighlighter highlighter = new JFXHighlighter();
        highlighter.setPaint(Color.web("#b3e5ca"));
        String search = regSearchT.getText();
        CodeArea selected = getTabContent(tabs.getSelectionModel().getSelectedItem());
        if (selected == null && search == null)
            return;

        regSearchT.textProperty().addListener((obs, oldvalue, newValue) -> {
            Platform.runLater(() -> {
                highlighter.clear();
                highlighter.setPaint(null);
            });
        });

    }


    @FXML
    private void newFile() {
        Tab newTab = new Tab();
        newTab.setText("untitled.ss");
        newTab.setUserData(Paths.get("untitled.ss"));
        new SinaSharpTab("", newTab);
        tabs.getTabs().add(newTab);
        openedFile = null;
    }

    @FXML
    private void saveAs() {
        Tab openTab = tabs.getSelectionModel().getSelectedItem();
        if (openTab == null)
            return;
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SinaSharp", "*.ss"));
        File file = chooser.showSaveDialog(mainBorderPane.getScene().getWindow());

        if (file == null) {
            return;
        }

        try {
            file.createNewFile();
        } catch (IOException e) {
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
        if (openedFile == null) {
            saveAs();
            return;
        }
        Tab selectedTab = tabs.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            System.out.println("Nothing to save");
            return;
        }
        Path openFile = openedFile;
        String[] content = getTextFromTab(selectedTab).split("\n");

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


    //IDE specific
    @FXML
    void run() {
        consoleContent.getChildren().clear();
        Path openedFile = (Path) tabs.getSelectionModel().getSelectedItem().getUserData();
        if (openedFile != null) {
            //TODO: read the output in a separate thread.
            Thread th = new Thread(() -> {
                try {

                    final String os = System.getProperty("os.name");
                    InputStream console;
                    Process p;
                    if (os.equals("Linux")) {
                        p = linuxProcess(openedFile);
                    } else {
                        p = msWindowsProcess(openedFile);
                    }
                    console = p.getInputStream();
                    readOutput(console);

                    Thread.sleep(200);
                    console.close();
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            });
            th.start();
        } else {
            saveAs();
        }
    }

    private CodeArea getTabContent(Tab t) {
        StackPane st = (StackPane) t.getContent();
        VirtualizedScrollPane sc = (VirtualizedScrollPane) st.getChildren().get(0);
        return (CodeArea) sc.getContent();
    }

    private String getTextFromTab(Tab t) {
        CodeArea codeArea = getTabContent(t);
        return codeArea.getText();
    }


    private Process linuxProcess(Path file) throws IOException {
        return Runtime.getRuntime().exec("java -jar sinac.jar " + file.toString());
    }

    private Process msWindowsProcess(Path file) throws IOException {
        return Runtime.getRuntime().exec(" java -jar sinac.jar " + file.toString());
    }

    private void readOutput(InputStream stdout) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            String line = "";
            while (true) {
                try {
                    if ((line = br.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (line.startsWith("Error")) {
                    String finalLine1 = line;
                    new Thread(() -> {
                        Platform.runLater(() -> {
                            HBox box = new HBox();
                            Text text = new Text(finalLine1);
                            text.setStyle("-fx-fill: red;");
                            box.getChildren().add(text);
                            consoleContent.getChildren().add(box);
                        });
                    }).start();
                } else {
//                System.out.println(line);
                    String finalLine = line;
                    new Thread(() -> {
                        Platform.runLater(() -> {
                            HBox box = new HBox();
                            Text text = new Text(finalLine);
                            text.setStyle("-fx-fill: grey;");
                            box.getChildren().add(text);
                            consoleContent.getChildren().add(box);
                        });
                    }).start();
                }
            }

            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
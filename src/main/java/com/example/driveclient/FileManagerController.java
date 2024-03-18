package com.example.driveclient;

import com.example.driveclient.dto.DriveFile;
import com.example.driveclient.entity.FileModel;
import com.example.driveclient.entity.UserData;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.ResourceBundle;

public class FileManagerController implements Initializable {

    @FXML
    private TableColumn<DriveFile, Instant> modifiedAt;

    @FXML
    private TableView<DriveFile> table;

    @FXML
    private TableColumn<DriveFile, Long> name;

    @FXML
    private TableColumn<DriveFile, String> owner;

    @FXML
    private BorderPane pane;

    @FXML
    private TableColumn<DriveFile, Long> size;

    @FXML
    private HBox uploadfile;

    @FXML
    private Text fname;

    @FXML
    private HBox user;

    @FXML
    private HBox bin;

    @FXML
    private HBox home;

    private final FileModel model = new FileModel();


    ObservableList<DriveFile> files =
         FXCollections.observableArrayList();


    public void setHomeTable(){
        name.setResizable(false);
        size.setResizable(false);
        owner.setResizable(false);
        modifiedAt.setResizable(false);

        model.getUserInfo();

        name.setCellValueFactory(new PropertyValueFactory<>("id"));
        size.setCellValueFactory(new PropertyValueFactory<>("size"));
        owner.setCellValueFactory(new PropertyValueFactory<>("owner"));
        modifiedAt.setCellValueFactory(new PropertyValueFactory<>("modificationDate"));
        table.setItems(files);

        table.setRowFactory(tv -> {
            TableRow<DriveFile> row = new TableRow<>();
            row.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    row.setStyle("-fx-background-color: #81c483;");
                } else {
                    row.setStyle("");
                }
            });

            ContextMenu rowMenu = new ContextMenu();

            MenuItem updateItem = new MenuItem("update");
            MenuItem renameItem = new MenuItem("Rename");
            MenuItem downloadItem = new MenuItem("Download");
            MenuItem removeItem = new MenuItem("Delete");

            updateItem.setOnAction(event ->  {
                DriveFile file = row.getItem();
                model.getSelectionFile().set(selectFile());
                model.getUpdatedFileProperty().set(file.getId());
                model.doUpdateFile();
            });

            removeItem.setOnAction(event -> {
                DriveFile file = row.getItem();
                model.getRemovalFile().set(file);
                model.doDeleteFiles();
            });

            downloadItem.setOnAction(event -> {
                DriveFile file = row.getItem();
                model.getDownloadFile().set(file);
                selectDownloadLocation();
                model.doDownloadFile();
            });


            renameItem.setOnAction(event -> {

                DriveFile file = row.getItem();
                TextInputDialog dialog = new TextInputDialog(file.getName());
                dialog.setTitle("Rename file");
                dialog.setHeaderText("Rename file");
                dialog.setContentText("File name:");

                Optional<String> result = dialog.showAndWait();

                result.ifPresent(n -> {
                            model.getRenameFile().set(file);
                            model.getFileNewname().set(n);
                            model.doRenameFile();
                        }
                        );
            });

            rowMenu.getItems().addAll(updateItem, downloadItem, renameItem, removeItem);


            // only display context menu for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(rowMenu));


            return row;});


        name.setCellFactory(col -> {
            TableCell<DriveFile, Long> cell = new TableCell<>();
            cell.itemProperty().addListener((observableValue, o, newValue) -> {
                if (newValue != null) {
                    Node graphic = createFileGraphic(newValue);
                    cell.graphicProperty().bind(Bindings.when(cell.emptyProperty()).then((Node) null).otherwise(graphic));
                }
            });
            return cell;
        });

        model.doLoadFiles();
    }

    public void toBinPage(){
        try {
            Stage stage = DriveApplication.getMainStage();
            FXMLLoader signInLoader = new FXMLLoader(getClass().getResource("/bin.fxml"));
            stage.setScene(new Scene(signInLoader.load()));
            stage.show();
        }
        catch (Exception e){
        }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Alert upload status

        setHomeTable();

        MenuItem logout = new MenuItem("Logout");
        logout.setOnAction(e -> {
            doLogOut();
        });

        ContextMenu menu = new ContextMenu(logout);

        user.setOnContextMenuRequested(e -> {
            menu.show(pane.getScene().getWindow(),e.getScreenX(),e.getScreenY());
        });

        model.getUploadSuccess().addListener((obs,old,newval) -> {
           if(newval != null && newval){
               Platform.runLater(() -> showError(Alert.AlertType.CONFIRMATION, pane.getScene().getWindow(), "Upload suceeded" , "Upload Suceeded with no error",false) );
               model.getUploadSuccess().set(false);
               model.doLoadFiles();
           }

        });

        model.getDownloadSucess().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> showError(Alert.AlertType.CONFIRMATION, pane.getScene().getWindow(), "Upload suceeded" , "Download Suceeded with no error",false) );
                model.getUploadSuccess().set(false);
                model.doLoadFiles();
            }

        });

        model.getAlertState().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> showError(Alert.AlertType.ERROR, pane.getScene().getWindow(), "error" , "Something went wrong, try again later",false) );
                model.getAlertState().set(false);
            }
        });

        model.getDeletionErrorState().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> {
                    String erroMsg = model.getDeletionErrorMsg().get();
                    showError(Alert.AlertType.ERROR,pane.getScene().getWindow(),"Deletion Failed!",erroMsg,false);
                    model.getDeletionErrorState().set(false);
                });
            }
        });

        model.getDeletionSucess().addListener((obs,old,newval) -> {
                    if(newval != null && newval){
                        Platform.runLater(() -> {
                            String erroMsg = model.getDeletionErrorMsg().get();
                            Alert alert =showError(Alert.AlertType.CONFIRMATION,pane.getScene().getWindow(),"Deletion Sucessfully!",erroMsg,false);

                            alert.setOnCloseRequest(e -> {
                                model.doLoadFiles();
                            });
                            model.getDeletionErrorState().set(false);
                        });
                    }
                });

        model.getRenameErrorState().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> {
                    String erroMsg = model.getRenameErrorMsg().get();
                    showError(Alert.AlertType.ERROR,pane.getScene().getWindow(),"Rename Failed!",erroMsg,false);
                    model.getRenameErrorState().set(false);
                });
            }
        });

        model.getRenameSucess().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> {
                    String erroMsg = model.getRenameErrorMsg().get();
                    Alert alert =showError(Alert.AlertType.CONFIRMATION,pane.getScene().getWindow(),"Renamed file Sucessfully!",erroMsg,false);

                    alert.setOnCloseRequest(e -> {
                        model.doLoadFiles();
                    });
                    model.getRenameErrorState().set(false);
                });
            }
        });

        model.getLoadFilesSucess().addListener(
                (obs,old,newval) -> {
                    if(newval != null && newval){
                        Platform.runLater(() -> {
                        files.clear();
                        files.addAll(model.getListProperty().get());
                        model.getLoadFilesSucess().set(false);
                        });
                    }
                }
        );

        model.getUserInfoProperty().addListener((obs,old,newval) -> {

            if(newval != null) {
                fname.setText(newval.getFirstName() + " " + newval.getLastName());
            }
        });

    }

    public void selectDownloadLocation(){
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Download location");
        directoryChooser.setInitialDirectory(new java.io.File( System.getProperty("user.home")));
        java.io.File selectedFolder =  directoryChooser.showDialog(pane.getScene().getWindow());
        model.getDownloadLocation().set(selectedFolder);
        System.out.println(model.getDownloadLocation().get().getAbsolutePath());
    }

    public File selectFile(){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a file");
        fileChooser.setInitialDirectory(new java.io.File( System.getProperty("user.home")));
        return fileChooser.showOpenDialog(pane.getScene().getWindow());
    }

    public void uploadfile(){
        File selectedFile = selectFile();

        model.getSelectionFile().set(selectedFile);
        model.doUploadFile();
    }

    private void doLogOut(){
        UserData.setEmail(null);
        UserData.setToken(null);

        try {
            Stage stage = DriveApplication.getMainStage();
            FXMLLoader signInLoader = new FXMLLoader(getClass().getResource("/sign-in.fxml"));
            Scene scene = new Scene(signInLoader.load());
            scene.getStylesheets().add("/Styles.css");
            stage.setScene(scene);
            stage.show();
        }
        catch (IOException e){

        }

    }

    private Node createFileGraphic(Long fileId){
        DriveFile file =
        files.stream()
                .parallel()
                .filter(i -> i.getId().equals(fileId))
                .findFirst().orElseThrow(IllegalArgumentException::new);

        HBox graphicContainer = new HBox();
        graphicContainer.setAlignment(Pos.CENTER_LEFT);
        FontIcon icon;

            icon = new FontIcon("fas-file");

        icon.setIconSize(20);
        icon.setIconColor(Paint.valueOf("#05988f"));

        Text text = new Text(file.getName());
        graphicContainer.getChildren().add(icon);
        graphicContainer.getChildren().add(text);
        graphicContainer.setSpacing(10);
        return graphicContainer;
    }

    private Alert showError(Alert.AlertType alertType, Window owner , String title, String message, boolean redirect){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initOwner(owner);
        alert.show();
        return alert;
    }

}


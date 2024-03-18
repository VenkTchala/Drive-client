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
import javafx.stage.Stage;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.ResourceBundle;

public class BinController implements Initializable {

    @FXML
    private BorderPane pane;

    @FXML
    private Text fname;

    @FXML
    private HBox user;

    @FXML
    private TableView<DriveFile> table;

    @FXML
    private TableColumn<DriveFile, Instant> deletedAt;

    @FXML
    private TableColumn<DriveFile, Long> name;

    @FXML
    private TableColumn<DriveFile, String> owner;

    @FXML
    private TableColumn<DriveFile, Long> size;

    private final FileModel fileModel = new FileModel();

    ObservableList<DriveFile> files =
            FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fileModel.getUserInfo();
        fname.setText(UserData.getFirstName() + " " + UserData.getLastName());


        fileModel.getRestoreErrorState().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> {
                    String erroMsg = fileModel.getDeletionErrorMsg().get();
                    showError(Alert.AlertType.ERROR,pane.getScene().getWindow(),"Restoration Failed!",erroMsg,false);
                    fileModel.getDeletionErrorState().set(false);
                });
            }
        });


        fileModel.getRestoreSucess().addListener((obs,old,newval) -> {
            if(newval != null && newval){
                Platform.runLater(() -> {
                    String erroMsg = fileModel.getDeletionErrorMsg().get();
                    Alert alert =showError(Alert.AlertType.CONFIRMATION,pane.getScene().getWindow(),"Restored File Sucessfully!",erroMsg,false);

                    alert.setOnCloseRequest(e -> {
                        fileModel.doLoadBinFiles();
                    });
                    fileModel.getDeletionErrorState().set(false);
                });
            }
        });


        fileModel.getLoadFilesSucess().addListener(
                (obs,old,newval) -> {
                    if(newval != null && newval){
                        Platform.runLater(() -> {
                            files.clear();
                            files.addAll(fileModel.getListProperty().get());
                            fileModel.getLoadFilesSucess().set(false);
                        });
                    }
                }
        );


        loadBin();
    }

    private void loadBin(){

        name.setResizable(false);
        size.setResizable(false);
        owner.setResizable(false);
        deletedAt.setResizable(false);


        name.setCellValueFactory(new PropertyValueFactory<>("id"));
        size.setCellValueFactory(new PropertyValueFactory<>("size"));
        owner.setCellValueFactory(new PropertyValueFactory<>("owner"));
        deletedAt.setCellValueFactory(new PropertyValueFactory<>("modificationDate"));
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
//            editItem.setOnAction(...);



            MenuItem restoreItem = new MenuItem("Restore");

            restoreItem.setOnAction(event -> {
                DriveFile file = row.getItem();
                fileModel.getRemovalFile().set(file);
                fileModel.doRestoreFiles();
            });

            rowMenu.getItems().addAll(restoreItem);


            // only display context menu for non-empty rows:
            row.contextMenuProperty().bind(
                    Bindings.when(row.emptyProperty())
                            .then((ContextMenu) null)
                            .otherwise(rowMenu));

//            row.contextMenuProperty().bind();

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

        fileModel.doLoadBinFiles();
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

    public void toBinPage(){
        try {
            Stage stage = DriveApplication.getMainStage();
            FXMLLoader signInLoader = new FXMLLoader(getClass().getResource("/file-Manager.fxml"));
            stage.setScene(new Scene(signInLoader.load()));
            stage.show();
        }
        catch (Exception e){
        }
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

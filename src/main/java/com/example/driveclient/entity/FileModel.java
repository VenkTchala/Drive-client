package com.example.driveclient.entity;

import com.example.driveclient.dto.*;
import com.example.driveclient.service.FileService;
import com.example.driveclient.service.UserService;
import com.example.driveclient.util.JsonMapper;
import io.tus.java.client.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.UnirestException;
import lombok.AccessLevel;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Getter
public class FileModel {
    private final BooleanProperty alertState = new SimpleBooleanProperty();
    private final StringProperty alertText = new SimpleStringProperty();
    private final ObjectProperty<File> selectionFile = new SimpleObjectProperty<>();
    private final BooleanProperty uploadSuccess = new SimpleBooleanProperty();
    private final BooleanProperty loadFilesSucess = new SimpleBooleanProperty();
    private final ObjectProperty<List<DriveFile>> listProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<DriveFile> removalFile = new SimpleObjectProperty<>();
    private final ObjectProperty<DriveFile> downloadFile = new SimpleObjectProperty<>();
    private final StringProperty fileNewname = new SimpleStringProperty();

    private final ObjectProperty<Boolean> downloadSucess = new SimpleObjectProperty<>();
    private final ObjectProperty<Boolean> downloadErrorState = new SimpleObjectProperty<>();
    private final ObjectProperty<File> downloadLocation = new SimpleObjectProperty<>();

    private final ObjectProperty<Boolean> deletionErrorState = new SimpleObjectProperty<>();
    private final StringProperty deletionErrorMsg = new SimpleStringProperty();
    private final BooleanProperty deletionSucess = new SimpleBooleanProperty();

    private final ObjectProperty<DriveFile> renameFile = new SimpleObjectProperty<>();
    private final ObjectProperty<Boolean> renameErrorState = new SimpleObjectProperty<>();
    private final StringProperty renameErrorMsg = new SimpleStringProperty();
    private final BooleanProperty renameSucess = new SimpleBooleanProperty();

    private final ObjectProperty<Boolean> restoreErrorState = new SimpleObjectProperty<>();
    private final StringProperty restoreErrorMsg = new SimpleStringProperty();
    private final BooleanProperty restoreSucess = new SimpleBooleanProperty();

    private final ObjectProperty<UserInfo> userInfoProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Long> updatedFileProperty = new SimpleObjectProperty<>();

    private final Service<Void> uploadService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return uploadfileTask();
        }
    };

    private final Service<Void> updateService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return updatefileTask();
        }
    };

    @Getter(AccessLevel.NONE)
    private final Service<Void> userFiles = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return getFilesTask();
        }
    };

    private final Service<Void> userBinFiles = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            return getBinFilesTask();
        }
    };

    private final Service<Void> removeFileService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return deleteFileTask();
        }
    };

    private final Service<Void> renameFileService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return renameFileTask();
        }
    };
    private final Service<Void> restoreFileService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return restoreFileTask();
        }
    };
    private final Service<Void> downloadFileService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return downloadFileTask();
        }
    };

    private final Service<Void> userInfoService = new Service<Void>() {
        @Override
        protected Task<Void> createTask() {
            return userDetailsTask();
        }
    };

    @Getter(AccessLevel.NONE)
    private final Runnable errorAlert = () -> setAlertState(true,"Something went wrong, try again later");

    private Task<Void> getFilesTask(){
        Task<Void> filesTask = new Task<Void>() {
            @Override
            protected Void call(){
                try {

                    HttpResponse<JsonNode> response = FileService.getFiles();

                    if (response.getStatus() != 200) {
                        Platform.runLater(errorAlert);
                    }
                    else {
                        DriveFile[] files  =  JsonMapper.JsonToObj(response.getBody().toString(), DriveFile[].class);
                        listProperty.set(Arrays.asList(files));
                        loadFilesSucess.set(true);
                    }
                }
                catch (Exception e){
                    alertState.set(true);
                }
                return null;
            }
        };

        filesTask.setOnFailed(e -> Platform.runLater(errorAlert));
        return filesTask;
    }


    private Task<Void> getBinFilesTask(){
        Task<Void> filesTask = new Task<Void>() {
            @Override
            protected Void call(){
                try {

                    HttpResponse<JsonNode> response = FileService.getBinFiles();

                    if (response.getStatus() != 200) {
                        Platform.runLater(errorAlert);
                    }
                    else {
                        DriveFile[] files  =  JsonMapper.JsonToObj(response.getBody().toString(), DriveFile[].class);
                        listProperty.set(Arrays.asList(files));
                        loadFilesSucess.set(true);
                    }
                }
                catch (Exception e){
                    alertState.set(true);
                }
                return null;
            }
        };

        filesTask.setOnFailed(e -> Platform.runLater(errorAlert));
        return filesTask;
    }

    private Task<Void> uploadfileTask (){
        Task<Void> uploadTask = new Task<>() {
            @Override
            protected Void call() {
                    Platform.runLater( ()  -> {
                        try {
                            uploadFile(getSelectionFile().get());
                        }
                        catch (Exception e){
                            alertState.set(true);
                        }
                    });
                return null;
            }
        };

        uploadTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return uploadTask;
    }

    private Task<Void> updatefileTask (){
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater( ()  -> {
                    try {
                        updateFile(getSelectionFile().get(),updatedFileProperty.get());
                    }
                    catch (Exception e){
                        alertState.set(true);
                    }
                });
                return null;
            }
        };

        updateTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return updateTask;
    }

    private Task<Void> deleteFileTask(){


        Task<Void> removeTask = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater( ()  -> {
                    try {
                        HttpResponse<JsonNode> response =
                        FileService.removeFile(DeleteRequest.builder()
                                        .id(removalFile.get().getId())
                                .build());

                        if(response.getStatus() != 200){
                            deletionErrorState.set(true);
                            deletionErrorMsg.set("Something went wrong");
                        }
                        else {
                            Status status = JsonMapper.JsonToObj(response.getBody().toString(),Status.class);
                            if(!status.isStatus()){
                                deletionErrorState.set(true);
                                deletionErrorMsg.set(status.getErrMsg());
                            }
                            else {
                                deletionSucess.set(true);
                                deletionErrorMsg.set(status.getErrMsg());
                            }
                        }
                    }
                    catch (Exception e){
                        alertState.set(true);
                    }
                });
                return null;
            }
        };



        removeTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return removeTask;
    }

    private Task<Void> renameFileTask(){


        Task<Void> renameTask = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater( ()  -> {
                    try {
                        HttpResponse<JsonNode> response =
                                FileService.renameFile(
                                        RenameRequest
                                                .builder()
                                                .id(renameFile.get().getId())
                                                .newName(
                                                        fileNewname.get()
                                                )
                                                .build());

                        if(response.getStatus() != 200){
                            renameErrorState.set(true);
                            renameErrorMsg.set("Something went wrong");
                        }
                        else {
                            Status status = JsonMapper.JsonToObj(response.getBody().toString(),Status.class);
                            if(!status.isStatus()){
                                renameErrorState.set(true);
                                renameErrorMsg.set(status.getErrMsg());

                                System.out.println(response.getStatus());
                                System.out.println(status.getErrMsg());
                            }
                            else {
                                renameSucess.set(true);
                                renameErrorMsg.set(status.getErrMsg());
                            }
                        }
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                        alertState.set(true);
                    }
                });
                return null;
            }
        };



        renameTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return renameTask;
    }

    private Task<Void> restoreFileTask(){


        Task<Void> restoreTask = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater( ()  -> {
                    try {
                        HttpResponse<JsonNode> response =
                                FileService.restoreFile(DeleteRequest.builder()
                                        .id(removalFile.get().getId())
                                        .build());

                        if(response.getStatus() != 200){
                            restoreErrorState.set(true);
                            restoreErrorMsg.set("Something went wrong");
                        }
                        else {
                            Status status = JsonMapper.JsonToObj(response.getBody().toString(),Status.class);
                            if(!status.isStatus()){
                                restoreErrorState.set(true);
                                restoreErrorMsg.set(status.getErrMsg());
                            }
                            else {
                                restoreSucess.set(true);
                                restoreErrorMsg.set("File restored Sucessfully");
                            }
                        }
                    }
                    catch (Exception e){
                        alertState.set(true);
                    }
                });
                return null;
            }
        };

        restoreTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return restoreTask;
    }

    private Task<Void> downloadFileTask(){


        Task<Void> downloadTask = new Task<>() {
            @Override
            protected Void call() {
                Platform.runLater( ()  -> {
                    try {
                        Path downloadPath = Paths.get(downloadLocation.get().getAbsolutePath()  , downloadFile.get().getName());
                        HttpResponse<InputStream> response = FileService
                                .downloadFile(downloadFile.get().getName());

                        if(response.getStatus() != 200){
                            downloadErrorState.set(true);
                        }
                        else {
                                downloadSucess.set(true);
                                InputStream inputStream = response.getBody();
                            Files.copy(inputStream,downloadPath);
                            downloadSucess.set(true);
                        }
                    }
                    catch (Exception e){
                        System.out.println(e.getMessage());
                        downloadErrorState.set(true);
                    }
                });
                return null;
            }
        };



        downloadTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return downloadTask;
    }


    private Task<Void> userDetailsTask() {
        Task<Void> userTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                Platform.runLater(() -> {
                    try {
                        HttpResponse<JsonNode> response = UserService.userInfo(UserData.getEmail());
                        if(response.getStatus() != 200)
                            Platform.runLater(errorAlert);
                        else {
                            UserInfo userInfo = JsonMapper.JsonToObj(response.getBody().toString(),UserInfo.class);

                            if(userInfo.isExists()) {
                                userInfoProperty.set(userInfo);
                                UserData.setFirstName(userInfo.getFirstName());
                                UserData.setLastName(userInfo.getLastName());
                            }
                            else
                                Platform.runLater(errorAlert);
                        }
                    }
                    catch (UnirestException e){
                        Platform.runLater(errorAlert);
                    }
                });
                return null;
            }
        };
        userTask.setOnFailed(e -> Platform.runLater(errorAlert));
        return userTask;
    }
    public void doUploadFile(){
        if(!uploadService.isRunning()){
            uploadService.reset();
            uploadService.start();
        }
    }

    public void doLoadFiles(){
        if(!userFiles.isRunning()){
            userFiles.reset();
            userFiles.start();
        }
    }

    public void doLoadBinFiles(){
        if(!userBinFiles.isRunning()){
            userBinFiles.reset();
            userBinFiles.start();
        }
    }

    public void doDeleteFiles(){
        if(!removeFileService.isRunning()){
            removeFileService.reset();
            removeFileService.start();
        }
    }

    public void doUpdateFile(){
        if(!updateService.isRunning()){
            updateService.reset();
            updateService.start();
        }
    }

    public void doRenameFile(){
        if(!renameFileService.isRunning()){
            renameFileService.reset();
            renameFileService.start();
        }
    }
    public void doRestoreFiles(){
        if(!restoreFileService.isRunning()){
            restoreFileService.reset();
            restoreFileService.start();
        }
    }
    public void doDownloadFile(){
        if(!downloadFileService.isRunning()){
            downloadFileService.reset();
            downloadFileService.start();
        }
    }
    public void getUserInfo(){

        if(!userInfoService.isRunning()){
            userInfoService.reset();
            userInfoService.start();
        }
    }

    private void setAlertState (boolean state , String status ){
        alertState.set(state);
        alertText.set(status);
    }

    private void uploadFile(File selected) throws Exception {

        var client = new TusClient();
        client.setHeaders(Map.of("Authorization", "Bearer " + UserData.getToken()
        ));

        client.setUploadCreationURL(URI.create("http://localhost:8080/file/upload").toURL());
        client.enableResuming(new TusURLMemoryStore());

        TusUpload upload = new TusUpload(selected);

        var executor = new TusExecutor() {
            @Override
            protected void makeAttempt() throws ProtocolException, IOException {
                TusUploader uploader = client.resumeOrCreateUpload(upload);
                uploader.setChunkSize(1024);

                do {
                    long totalBytes = upload.getSize();
                    long bytesUploaded = uploader.getOffset();
                    double progress = (double) bytesUploaded / totalBytes * 100;
                    System.out.printf("Upload at %6.2f %%.%n", progress);
                }
                while (uploader.uploadChunk() > -1);
                uploader.finish();
            }

        };
        executor.setDelays(new int[]{2, 4, 8});

        boolean success = executor.makeAttempts();
        uploadSuccess.set(success);
    }


    private void updateFile(File selected, Long fileId) throws Exception {

        var client = new TusClient();
        client.setHeaders(Map.of("Authorization", "Bearer " + UserData.getToken(),
                "fileId" , "" + fileId
                ));

        client.setUploadCreationURL(URI.create("http://localhost:8080/file/upload").toURL());
        client.enableResuming(new TusURLMemoryStore());

        TusUpload upload = new TusUpload(selected);

        var executor = new TusExecutor() {
            @Override
            protected void makeAttempt() throws ProtocolException, IOException {
                TusUploader uploader = client.resumeOrCreateUpload(upload);
                uploader.setChunkSize(1024);

                do {
                    long totalBytes = upload.getSize();
                    long bytesUploaded = uploader.getOffset();
                    double progress = (double) bytesUploaded / totalBytes * 100;
                    System.out.printf("Upload at %6.2f %%.%n", progress);
                }
                while (uploader.uploadChunk() > -1);
                uploader.finish();
            }

        };
        executor.setDelays(new int[]{2, 4, 8});

        boolean success = executor.makeAttempts();
        uploadSuccess.set(success);
    }

}


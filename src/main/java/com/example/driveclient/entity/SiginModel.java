package com.example.driveclient.entity;

import com.example.driveclient.dto.SignInStatus;
import com.example.driveclient.dto.UserSignIn;
import com.example.driveclient.service.UserService;
import com.example.driveclient.util.JsonMapper;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.UnirestException;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
public class SiginModel {
    private final ObjectProperty<Boolean> signinState = new SimpleObjectProperty<>(false);

    private final ObjectProperty<Boolean> alertState = new SimpleObjectProperty<>(false);
    private final StringProperty alertText = new SimpleStringProperty();

    private final ObjectProperty<UserSignIn> details = new SimpleObjectProperty<>(null);

    @Getter(AccessLevel.NONE)
    private final Service<Void> signInService = new Service<>() {
        @Override
        protected Task<Void> createTask() {
            System.out.println("Hello 1");
            return signInTask();
        }
    };

    @Getter(AccessLevel.NONE)
    private final Runnable errorAlert = () -> setAlertState(true,"Something went wrong, try again later");

    private Task<Void> signInTask(){
        Task<Void> signInTask = new Task<>() {

            @Override
            protected Void call() throws Exception {

                try {
                    HttpResponse<JsonNode> response =
                            UserService.signIn(details.get());


                    if(response.getStatus() != 200)
                        Platform.runLater(errorAlert);
                    else {
                        SignInStatus reply = JsonMapper.JsonToObj(response.getBody().toString(),SignInStatus.class);
                        boolean sucess = reply.isSucess();

                        if(!sucess)
                            Platform.runLater(() -> setAlertState(true, reply.getErrorMessage()));
                        else signinState.set(true);
                    }
                }
                catch (Exception e){
                    Platform.runLater(errorAlert);
                }
                return null;
            }
        };

        signInTask.setOnFailed(e -> Platform.runLater(errorAlert));

        return signInTask;
    }


    public void doSignIn() {
        if (!signInService.isRunning()) {
            signInService.reset();
            signInService.start();
        }
    }


    private void setAlertState(boolean state , String text ){
        alertState.set(state);
        alertText.set(text);
    }

}

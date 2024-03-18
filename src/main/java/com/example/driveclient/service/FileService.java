package com.example.driveclient.service;

import com.example.driveclient.dto.DeleteRequest;
import com.example.driveclient.dto.RenameRequest;
import com.example.driveclient.entity.UserData;
import com.example.driveclient.util.JsonMapper;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;

import java.io.InputStream;

public class FileService {

    public static HttpResponse<JsonNode> getFiles() throws UnirestException {
        return Unirest.get("http://localhost:8080/file/files")
                .header("Content-Type", "application/json")
                .header("Authorization " , "Bearer " + UserData.getToken())
                .asJson();
    }


    public static HttpResponse<JsonNode> getBinFiles() throws UnirestException {
        return Unirest.get("http://localhost:8080/file/trash")
                .header("Content-Type", "application/json")
                .header("Authorization " , "Bearer " + UserData.getToken())
                .asJson();
    }

    public static HttpResponse<JsonNode> removeFile(DeleteRequest request) throws UnirestException{

        final String body = JsonMapper.getJson(request);

        return Unirest.post("http://localhost:8080/file/deletefile")
                .header("Content-Type", "application/json")
                .header("Authorization " , "Bearer " + UserData.getToken())
                .body(body)
                .asJson();
    }

    public static HttpResponse<JsonNode> renameFile(RenameRequest request) throws UnirestException{

        final String body = JsonMapper.getJson(request);

        return Unirest.post("http://localhost:8080/file/renamefile")
                .header("Content-Type", "application/json")
                .header("Authorization " , "Bearer " + UserData.getToken())
                .body(body)
                .asJson();
    }

    public static HttpResponse<InputStream> downloadFile(String filename) throws UnirestException{
        return  Unirest.get("http://localhost:8080/file/download")
                .header("Authorization " , "Bearer " + UserData.getToken())
                .queryString("name",filename)
                .asObject(raw -> raw.getContent());
    }

    public static HttpResponse<JsonNode> restoreFile(DeleteRequest request) throws UnirestException{

        final String body = JsonMapper.getJson(request);

        return Unirest.post("http://localhost:8080/file/restorefile")
                .header("Content-Type", "application/json")
                .header("Authorization " , "Bearer " + UserData.getToken())
                .body(body)
                .asJson();
    }

}

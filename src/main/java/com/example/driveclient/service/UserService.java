package com.example.driveclient.service;

import com.example.driveclient.dto.AuthRequest;
import com.example.driveclient.dto.UserSignIn;
import com.example.driveclient.util.JsonMapper;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.JsonNode;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestException;

public class UserService {
    public static HttpResponse<JsonNode> signIn(UserSignIn userSignIn) throws UnirestException {
        System.out.println("heaaaaeth");
        System.out.println(userSignIn.toString());
        final String body;
             body = JsonMapper.getJson(userSignIn);

        System.out.println("haieeeeeeeeeeeeet");
             return  Unirest.post("http://localhost:8080/auth/register")
                     .header("Content-Type", "application/json")
                     .body(body)
                     .asJson();
    }

    public static HttpResponse<JsonNode> logIn (AuthRequest authRequest) throws UnirestException{
        final String body = JsonMapper.getJson(authRequest);

        return Unirest.post("http://localhost:8080/auth/token")
                .header("Content-Type", "application/json")
                .body(body)
                .asJson();
    }

    public static HttpResponse<JsonNode> userInfo(String email) throws UnirestException{
        return Unirest.get("http://localhost:8080/auth/userinfo")
                .header("Content-Type", "application/json")
                .queryString("email",email)
                .asJson();
    }

}

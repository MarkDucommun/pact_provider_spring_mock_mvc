package com.test;

import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@RestController
public class TestController {

    @RequestMapping(path = "/**")
    public ResponseEntity login(RequestEntity requestEntity) throws IOException {
        Map body = (LinkedHashMap) requestEntity.getBody();

        String username = (String) body.get("username");
        String password = (String) body.get("password");

        Map tokenResponse = ImmutableMap.of("message", "invalid credentials");
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        if (username.equals("username") && password.equals("valid-password")) {
            tokenResponse = ImmutableMap.of("token", "A12345");
            status = HttpStatus.CREATED;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("content-type", APPLICATION_JSON_UTF8_VALUE);
        httpHeaders.add("foo", "bar");
        httpHeaders.add("foo", "baz");

        return new ResponseEntity<>(tokenResponse, httpHeaders, status);
    }
}

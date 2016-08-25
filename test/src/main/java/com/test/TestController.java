package com.test;

import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class TestController {

    @RequestMapping(path = "/**")
    public ResponseEntity login(RequestEntity requestEntity) throws IOException {
        Map body = (LinkedHashMap) requestEntity.getBody();

        String username = (String) body.get("username");
        String password = (String) body.get("password");

        Map tokenResponse = null;
        HttpStatus status = HttpStatus.OK;

        if (username.equals("username") && password.equals("valid-password")) {
            tokenResponse = ImmutableMap.of("token", 12345);
            status = HttpStatus.CREATED;
        }

        return new ResponseEntity(tokenResponse, status);
    }
}

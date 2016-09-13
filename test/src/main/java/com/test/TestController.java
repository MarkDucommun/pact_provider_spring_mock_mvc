package com.test;

import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
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

    TestService testService;

    public TestController(TestService testService) {
        this.testService = testService;
    }

    @RequestMapping(path = "/**")
    public ResponseEntity login(RequestEntity requestEntity) throws IOException {
        Map body = (LinkedHashMap) requestEntity.getBody();

        String username = (String) body.get("username");
        String password = (String) body.get("password");

        Map tokenResponse = ImmutableMap.of("message", "invalid credentials");
        HttpStatus status = HttpStatus.UNAUTHORIZED;

        String token = testService.getToken(username, password);
        if (token != null) {
            tokenResponse = ImmutableMap.of("token", token);
            status = HttpStatus.BAD_REQUEST;
        }


        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", APPLICATION_JSON_UTF8_VALUE);

        return new ResponseEntity<>(tokenResponse, httpHeaders, status);
    }
}

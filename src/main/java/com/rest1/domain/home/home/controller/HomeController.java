package com.rest1.domain.home.home.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;

@RestController
@Tag(name="Home", description = "홈 컨트롤러")
public class HomeController {

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String home() throws UnknownHostException {

        InetAddress localhost = InetAddress.getLocalHost();

        return """
                <h1>Welcome to Rest1</h1>
                <p>Server IP Address: %s</p>
                <p>Server Host Name: %s</p>
                <div>
                    <a href="swagger-ui/index.html">API 문서로 이동</a>
                </div>
                """.formatted(localhost.getHostAddress(), localhost.getHostName());
    }

    @GetMapping(value="test/fetchData", produces = MediaType.TEXT_HTML_VALUE)
    public String testFetch() {

        return """
                <script>
                    console.clear();
                    
                    fetch("/api/v1/posts")
                    .then(response => response.json())
                    .then(data => console.log(data))
                    
                    fetch("/api/v1/posts/2")
                    .then(response => response.json())
                    .then(data => console.log(data))
                    
                </script>
                """;
    }
    
}

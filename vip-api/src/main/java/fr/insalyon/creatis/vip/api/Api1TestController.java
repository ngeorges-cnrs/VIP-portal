package fr.insalyon.creatis.vip.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Api1TestController {
    @GetMapping("/test1")
    public String testCall() {
        return "test api1\n";
    }
}

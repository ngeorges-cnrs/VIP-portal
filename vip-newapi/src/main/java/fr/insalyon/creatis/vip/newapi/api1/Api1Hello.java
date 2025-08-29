package fr.insalyon.creatis.vip.newapi.api1;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Api1Hello {
    @GetMapping("/test1")
    public String testCall() {
        return "hello api1\n";
    }
}

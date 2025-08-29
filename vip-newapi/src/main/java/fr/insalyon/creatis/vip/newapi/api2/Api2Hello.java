package fr.insalyon.creatis.vip.newapi.api2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Api2Hello {
    @GetMapping("/test2")
    public String testCall() {
        return "hello api2\n";
    }
}

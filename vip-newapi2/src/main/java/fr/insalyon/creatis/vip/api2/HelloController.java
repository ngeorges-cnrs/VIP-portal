package fr.insalyon.creatis.vip.newapi2;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/test2")
    public String testCall() {
        return "test api2\n";
    }
}

package im.abe.pdfdata.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    @RequestMapping("/setup")
    public String setup() {
        return "setup";
    }
}

package im.abe.pdfdata.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/write")
    public String write() {
        return "write";
    }

    @RequestMapping("/setup")
    public String setup() {
        return "setup";
    }
}

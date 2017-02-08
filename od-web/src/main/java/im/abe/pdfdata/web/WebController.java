package im.abe.pdfdata.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class WebController {
    /**
     * This is a static controller method that just returns the "setup" template.
     * Contains instructions for demoing the service.
     *
     * @return "setup"
     */
    @RequestMapping("/setup")
    public String setup() {
        return "setup";
    }
}

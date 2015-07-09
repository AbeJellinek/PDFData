package me.abje.xmptest.frontend.opt;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Options {
    private Form form;
    private Map<String, Object> formArgs;
    private Map<Option, List<String>> options;
    private Map<String, Option> provided;

    public Options(Form form, Map<String, Object> formArgs, Map<Option, List<String>> options) {
        this.form = form;
        this.formArgs = formArgs;
        this.options = options;
        this.provided = options.keySet().stream().collect(Collectors.toMap(Option::getName, Function.identity()));
    }

    public boolean is(String name) {
        return provided.containsKey(name);
    }

    public List<String> getValue(String name) {
        return options.get(provided.get(name));
    }

    public Form getForm() {
        return form;
    }

    public String get(String name) {
        return formArgs.get(name).toString();
    }

    Object getChoice(String name) {
        return formArgs.get(name);
    }

    public boolean formHas(String name) {
        return formArgs.containsKey(name);
    }
}

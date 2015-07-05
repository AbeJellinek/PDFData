package me.abje.xmptest.frontend.opt;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Options {
    private String[] args;
    private Map<Option, List<String>> options;
    private Map<String, Option> provided;

    public Options(String[] args, Map<Option, List<String>> options) {
        this.args = args;
        this.options = options;
        this.provided = options.keySet().stream().collect(Collectors.toMap(Option::getName, Function.identity()));
    }

    public boolean is(String name) {
        return provided.containsKey(name);
    }

    public List<String> get(String name) {
        return options.get(provided.get(name));
    }

    public String[] getArgs() {
        return args;
    }
}

package me.abje.xmptest.frontend.opt;

import java.util.*;

public class CommandParser {
    private List<Option> options;

    public CommandParser() {
        this.options = new ArrayList<>();
    }

    public Option.Builder option(String name) {
        return new Option.Builder(this).name(name);
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public Options parse(String[] argsArray) {
        List<String> args = Arrays.asList(argsArray);
        Map<Option, List<String>> optionsMap = new HashMap<>();
        return new Options(args.stream().filter(arg -> {
            if (arg.startsWith("-")) {
                String argName = arg.startsWith("--") ? arg.substring(2) : arg.substring(1);
                for (Option option : options) {
                    if (argName.equals(option.getShortArg()) || argName.equals(option.getLongArg())) {
                        optionsMap.put(option, Collections.emptyList());
                        return false;
                    }
                }
                throw new ParseException("Unknown option: " + argName);
            } else {
                return true;
            }
        }).toArray(String[]::new), optionsMap);
    }
}

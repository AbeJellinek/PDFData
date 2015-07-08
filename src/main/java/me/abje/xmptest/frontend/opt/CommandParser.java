package me.abje.xmptest.frontend.opt;

import java.util.*;
import java.util.stream.Collectors;

public class CommandParser {
    private List<Option> options;
    private List<Form> forms;

    public CommandParser() {
        this.options = new ArrayList<>();
        this.forms = new ArrayList<>();
    }

    public Option.Builder option(String name) {
        return new Option.Builder(this).name(name);
    }

    public void addOption(Option option) {
        options.add(option);
    }

    public Options parse(String[] argsArray) {
        List<String> rawArgs = Arrays.asList(argsArray);
        Map<Option, List<String>> optionsMap = new HashMap<>();
        List<String> args = rawArgs.stream().filter(arg -> {
            if (arg.startsWith("--")) {
                String argName = arg.substring(2);
                for (Option option : options) {
                    if (argName.equals(option.getLongArg())) {
                        optionsMap.put(option, Collections.emptyList());
                        return false;
                    }
                }
                throw new ParseException("Unknown option: --" + argName);
            } else if (arg.startsWith("-")) {
                String argName = arg.substring(1);
                for (Option option : options) {
                    if (argName.startsWith(option.getShortArg())) {
                        optionsMap.put(option, Collections.emptyList());
                        argName = argName.substring(option.getShortArg().length());
                        if (argName.isEmpty()) {
                            return false;
                        }
                    }
                }
                throw new ParseException("Unknown option: -" + argName);
            } else {
                return true;
            }
        }).collect(Collectors.toList());

        if (args.isEmpty()) {
            return new Options(null, new HashMap<>(), optionsMap);
        } else {
            Form form = null;
            for (Form testForm : forms) {
                if (testForm.getName().equals(args.get(0))) {
                    form = testForm;
                }
            }

            if (form == null) {
                throw new ParseException("Unknown form: " + args.get(0));
            }

            args.remove(0);

            Map<String, Object> formArgs = new HashMap<>();
            for (int i = 0; i < form.getArgs().size(); i++) {
                Form.Arg formArg = form.getArgs().get(i);
                if (i >= args.size()) {
                    if (formArg.isOptional()) {
                        break;
                    } else {
                        throw new ParseException("Missing required argument: " + formArg.getName());
                    }
                }

                String arg = args.get(i);
                Object value = arg;
                boolean validChoice = true;
                if (formArg.getChoice() != null) {
                    if (formArg.getChoice().getOptions().containsKey(arg)) {
                        value = formArg.getChoice().getOptions().get(arg);
                    } else {
                        validChoice = false;
                    }
                }

                if (!validChoice) {
                    throw new ParseException("Invalid choice for argument " + formArg.getName() + ": " + arg);
                }

                formArgs.put(formArg.getName(), value);
            }

            return new Options(form, formArgs, optionsMap);
        }
    }

    public Form.Builder form(String name) {
        return new Form.Builder(name, this);
    }

    public void addForm(Form form) {
        forms.add(form);
    }
}

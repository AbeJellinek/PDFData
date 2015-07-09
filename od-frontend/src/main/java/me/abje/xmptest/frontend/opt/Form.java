package me.abje.xmptest.frontend.opt;

import java.util.ArrayList;
import java.util.List;

public class Form {
    private String name;
    private List<Arg> args;

    private Form() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Arg> getArgs() {
        return args;
    }

    public void setArgs(List<Arg> args) {
        this.args = args;
    }

    public boolean is(String name) {
        return this.name.equals(name);
    }

    public static class Builder {
        private Form form;
        private Arg lastArg;

        public Builder(String name, CommandParser parser) {
            form = new Form();
            form.setName(name);
            form.setArgs(new ArrayList<>());

            parser.addForm(form);
        }

        public Builder arg(String name) {
            form.getArgs().add(lastArg = new Arg(name));
            return this;
        }

        public Builder optional() {
            lastArg.setOptional(true);
            return this;
        }

        public Builder choice(Choice<?> choice) {
            lastArg.setChoice(choice);
            choice.setName(lastArg.getName());
            return this;
        }
    }

    public static class Arg {
        private String name;
        private boolean optional;
        private Choice<?> choice;

        public Arg(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isOptional() {
            return optional;
        }

        public void setOptional(boolean optional) {
            this.optional = optional;
        }

        public Choice<?> getChoice() {
            return choice;
        }

        public void setChoice(Choice<?> choice) {
            this.choice = choice;
        }
    }
}

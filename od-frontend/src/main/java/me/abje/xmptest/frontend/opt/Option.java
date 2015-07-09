package me.abje.xmptest.frontend.opt;

import java.util.Objects;

public class Option {
    private String name;
    private String shortArg;
    private String longArg;

    public Option(String name, String shortArg, String longArg) {
        this.name = name;
        this.shortArg = shortArg;
        this.longArg = longArg;
    }

    private Option() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShortArg() {
        return shortArg;
    }

    public void setShortArg(String shortArg) {
        this.shortArg = shortArg;
    }

    public String getLongArg() {
        return longArg;
    }

    public void setLongArg(String longArg) {
        this.longArg = longArg;
    }

    @Override
    public String toString() {
        return "Option{" +
                "name='" + name + '\'' +
                ", shortArg='" + shortArg + '\'' +
                ", longArg='" + longArg + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Option option = (Option) o;
        return Objects.equals(name, option.name) &&
                Objects.equals(shortArg, option.shortArg) &&
                Objects.equals(longArg, option.longArg);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, shortArg, longArg);
    }

    public static class Builder {
        private Option option;
        private CommandParser parser;

        public Builder(CommandParser parser) {
            this.parser = parser;
            this.option = new Option();

            parser.addOption(option);
        }

        public Builder and() {
            return new Builder(parser);
        }

        public Builder name(String name) {
            option.setName(name);
            if (option.getLongArg() == null)
                option.setLongArg(name);
            return this;
        }

        public Builder shortArg(String shortArg) {
            option.setShortArg(shortArg);
            return this;
        }

        public Builder longArg(String longArg) {
            option.setLongArg(longArg);
            return this;
        }
    }
}

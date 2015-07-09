package me.abje.xmptest.frontend.opt;

import java.util.HashMap;
import java.util.Map;

public class Choice<T> {
    private String name;
    private Map<String, T> options;

    public Choice(Map<String, T> options) {
        this.options = options;
    }

    public Map<String, T> getOptions() {
        return options;
    }

    public void setOptions(Map<String, T> options) {
        this.options = options;
    }

    @SuppressWarnings("unchecked")
    public T get(Options options) {
        return (T) options.getChoice(name);
    }

    void setName(String name) {
        this.name = name;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static <T> Builder<T> option(T value, String... choices) {
        return new Builder<T>().option(value, choices);
    }

    public static class Builder<T> {
        private Map<String, T> options;

        public Builder() {
            this.options = new HashMap<>();
        }

        public Builder<T> option(T value, String... choices) {
            for (String choice : choices) {
                options.put(choice, value);
            }
            return this;
        }

        public Choice<T> build() {
            return new Choice<>(options);
        }
    }
}

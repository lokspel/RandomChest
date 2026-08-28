package me.lokspel.randomchest.command;

public final class RegisteredCommand {

    private final String name;
    private final SubCommand executor;

    public RegisteredCommand(String name, SubCommand executor) {
        this.name = name;
        this.executor = executor;
    }

    public String name() {
        return name;
    }

    public SubCommand executor() {
        return executor;
    }
}

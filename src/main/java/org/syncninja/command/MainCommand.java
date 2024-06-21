package org.syncninja.command;

import picocli.CommandLine;

@CommandLine.Command(name = "", subcommands = {
        InitCommand.class,
        AddCommand.class,
        CommitCommand.class,
        RestoreCommand.class,
        StatusCommand.class,
        UnstageCommand.class,
        CheckoutCommand.class,
        MergeCommand.class
})
public class MainCommand implements Runnable {
    @Override
    public void run() {
        String helpMessage = """
        Welcome to SyncNinja!

        Start a working area:
          init       : Create an empty SyncNinja directory or reinitialize an existing one.

        Work on current changes:
          add        : Add changes to the staging area to prepare them for a commit.
          unstage    : Remove changes from the staging area.
          restore    : Discard unstaged changes from a file.

        Information display:
          status     : Show the current state of the branch and the staging area.

        Version control and history:
          checkout   : Switch between branches (-b : create a new branch).
          commit     : Record changes to the repository (use -m to add a commit message).
          merge      : Combine changes from different branches.
        """;

        System.out.println(helpMessage);
    }
}

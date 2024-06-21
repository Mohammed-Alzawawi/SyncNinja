package org.syncninja.util;

import org.syncninja.model.NinjaNode;

import java.util.ArrayList;

public class CommitContainer {
    private final ArrayList<NinjaNode> commitsToAdd;
    private final ArrayList<NinjaNode> commitsToRemove;

    public CommitContainer(){
        this.commitsToAdd = new ArrayList<>();
        this.commitsToRemove = new ArrayList<>();
    }

    public ArrayList<NinjaNode> getCommitsToAdd() {
        return commitsToAdd;
    }

    public ArrayList<NinjaNode> getCommitsToRemove() {
        return commitsToRemove;
    }
}

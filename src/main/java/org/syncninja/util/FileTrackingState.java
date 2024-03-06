package org.syncninja.util;

import org.syncninja.dto.StatusFileDTO;
import org.syncninja.model.commitTree.CommitDirectory;
import org.syncninja.model.commitTree.CommitFile;
import org.syncninja.model.commitTree.CommitNode;
import org.syncninja.repository.CommitNodeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileTrackingState {
    private final List<StatusFileDTO> untracked;
    private final List<StatusFileDTO> tracked;
    private final CommitNodeRepository commitNodeRepository;


    public FileTrackingState() {
        this.untracked = new ArrayList<>();
        this.tracked = new ArrayList<>();
        this.commitNodeRepository = new CommitNodeRepository();
    }

    public List<StatusFileDTO> getUntracked() {
        return untracked;
    }

    public List<StatusFileDTO> getTracked() {
        return tracked;
    }




}
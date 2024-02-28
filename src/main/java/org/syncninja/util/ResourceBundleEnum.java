package org.syncninja.util;


public enum ResourceBundleEnum {

    DIRECTORY_ALREADY_INITIALIZED("directory_already_initialized"),
    INVALID_INPUT("invalid_input"),
    DIRECTORY_NOT_INITIALIZED("directory_not_initialized"),
    BRANCH_NAME_EXISTS("branch_name_exists"),
    FILE_ALREADY_EXISTS("file_already_exists"),
    SUB_DIRECTORY_ALREADY_EXISTS("sub_directory_already_exists"),
    FILES_READY_TO_BE_COMMITED("files_to_be_commited"),
    UNTRACKED_FILES("Untracked_files");


    private final String key;

    ResourceBundleEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}

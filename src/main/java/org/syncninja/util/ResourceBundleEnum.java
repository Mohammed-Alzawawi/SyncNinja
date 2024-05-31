package org.syncninja.util;

public enum ResourceBundleEnum {

    DIRECTORY_INITIALIZED_SUCCESSFULLY("directory_initialized_successfully"),
    DIRECTORY_ALREADY_INITIALIZED("directory_already_initialized"),
    INVALID_INPUT("invalid_input"),
    DIRECTORY_NOT_INITIALIZED("directory_not_initialized"),
    BRANCH_NAME_EXISTS("branch_name_exists"),
    FILE_ALREADY_EXISTS("file_already_exists"),
    SUB_DIRECTORY_ALREADY_EXISTS("sub_directory_already_exists"),
    CHANGES_READY_TO_BE_COMMITTED("changes_to_be_committed"),
    FILE_NOT_FOUND("file_is_not_found!"),
    CHANGES_NOT_STAGED_FOR_COMMIT("Changes_not_staged_for_commit"),
    UNTRACKED_FILES("untracked_files"),
    STAGE_AREA_IS_EMPTY("stage_area_is_empty"),
    SUCCESSFULLY_ADDED("successfully_added"),
    SUCCESSFULLY_REMOVED("successfully_removed"),
    BRANCH_ADDED_SUCCESSFULLY("branch_created_successfully"),
    BRANCH_CHECKED_OUT_SUCCESSFULLY("switched_to_branch"),
    BRANCH_NOT_FOUND("branch_not_found"),
    PATH_NOT_FILE("path_not_file"),
    COMMIT_SUCCESSFULLY("commit_successfully"),

    RESTORED_SUCCESSFULLY("restored_successfully"),
    YOU_ARE_ALREADY_IN_BRANCH("you_are_already_in_branch"),
    NO_CHANGES_TO_BE_ADDED("no_changes_to_be_added"),
    FAILED_TO_UPDATE_FILE_SYSTEM("Failed_to_update_file_system_for");

    private final String key;

    ResourceBundleEnum(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
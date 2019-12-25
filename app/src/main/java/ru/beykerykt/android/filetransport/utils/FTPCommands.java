package ru.beykerykt.android.filetransport.utils;

public class FTPCommands {
    public static final int FTP_CUSTOM_ERROR_EXCEPTION = -1;

    public static final int FTP_CMD_CONNECT_DISCONNECT_BY_SERVER = 421;

    public static final int FTP_CMD_CONNECT_FILE_ACTION_SUCCESSFUL = 226;

    public static final int FTP_CMD_AUTH_NOT_LOGGEN_IN = 530;
    public static final int FTP_CMD_AUTH_REQUEST_LOG_IN = 532;

    public static final int FTP_CMD_FILESYSTEM_FILE_NOT_AVAILABLE = 550;
    public static final int FTP_CMD_FILESYSTEM_NOT_ENOUGH_MEMORY_ALLOCATED = 552;
    public static final int FTP_CMD_FILESYSTEM_NOT_INVALID_FILE_NAME = 553;
}

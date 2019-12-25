package ru.beykerykt.android.filetransport.data;

import android.os.Environment;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPConnectionClosedException;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import ru.beykerykt.android.filetransport.data.entity.FileEntity;
import ru.beykerykt.android.filetransport.database.entity.FTPServerEntity;

public class FTPConnectionManager {

    private Hashtable<Integer, FTPClient> openConnections = new Hashtable<Integer, FTPClient>();

    public List<FileEntity> listFiles(FTPServerEntity server, String path) throws IOException {
        if (isActiveConnection(server)) {
            FTPClient ftpClient = getActiveConnection(server);

            if (path != null) {
                if (path.equals("/")) {
                    ftpClient.changeToParentDirectory();
                } else if (!path.equals("/")) {
                    ftpClient.changeWorkingDirectory(path);
                }
            }
            Log.e("TAG", path);

            FTPFile[] list = ftpClient.listFiles();

            List<FileEntity> results = new ArrayList<FileEntity>();

            if (list != null) {
                for (FTPFile file : list) {
                    if (!file.getName().equals("..")) {
                        results.add(this.ftpFileToEntity(file, path));
                    }
                }
            }

            Collections.sort(results);
            return results;
        }
        return null;
    }

    public void createFolder(String path, String name) {
        String fullPath = Environment.getExternalStorageDirectory().toString() + path + "/" + name;

        File folder = new File(fullPath);

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public void deleteFiles(FTPServerEntity server, List<FileEntity> elements) throws IOException {
        if (isActiveConnection(server)) {
            FTPClient ftpClient = getActiveConnection(server);
            for (FileEntity element : elements) {
                if (element.isDirectory()) {
                    ftpClient.removeDirectory(element.getPath());
                } else {
                    ftpClient.deleteFile(element.getPath());
                }
            }
        }
    }

    public FTPClient getActiveConnection(FTPServerEntity server) {
        return openConnections.get(server.getId());
    }

    public boolean isActiveConnection(FTPServerEntity server) {
        return openConnections.containsKey(server.getId());
    }

    public FTPClient openConnection(FTPServerEntity server) throws SocketException, UnknownHostException, IOException {
        if (isActiveConnection(server)) {
            FTPClient ftpClient = getActiveConnection(server);
            return ftpClient;
        }
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(DEFAULT_TIMEOUT);
        ftpClient.setControlKeepAliveTimeout(60);
        ftpClient.setAutodetectUTF8(true);

        if (!ftpClient.isConnected()) {
            ftpClient.connect(server.getHost(), server.getPort());

            // After connection attempt, you should check the reply code to verify
            // success.
            int reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftpClient.disconnect();
                return null;
            }
            if (server.isActiveMode()) {
                ftpClient.enterLocalActiveMode();
            } else {
                ftpClient.enterLocalPassiveMode();
            }
        }
        openConnections.put(server.getId(), ftpClient);
        return ftpClient;
    }

    public void disconnect(FTPServerEntity server) throws IOException {
        if (isActiveConnection(server)) {
            try {
                FTPClient ftpClient = getActiveConnection(server);
                if (ftpClient.isConnected()) {
                    ftpClient.disconnect();
                }
            } finally {
                openConnections.remove(server.getId());
            }
        }
    }

    public static final int DEFAULT_TIMEOUT = 2000; /* ms */

    public boolean isPortOpen(String ip, int port) throws ConnectException, SocketTimeoutException, UnknownHostException, IOException {
        return isPortOpen(ip, port, DEFAULT_TIMEOUT);
    }

    public boolean isPortOpen(String ip, int port, int timeout) throws ConnectException, SocketTimeoutException, UnknownHostException, IOException {
        FTPClient ftpClient = new FTPClient();
        ftpClient.setConnectTimeout(timeout);
        ftpClient.connect(ip, port);

        boolean answer = ftpClient.isAvailable();
        ftpClient.disconnect();
        if (answer) {
            return true;
        }
        return false;
    }

    public boolean login(FTPClient ftpClient, String login, String password) throws FTPConnectionClosedException, IOException {
        boolean answer = ftpClient.login(login, password);
        if (answer) {
            return true;
        }
        return false;
    }

    public boolean logout(FTPClient ftpClient) throws FTPConnectionClosedException, IOException {
        boolean answer = ftpClient.logout();
        if (answer) {
            return true;
        }
        return false;
    }

    private FileEntity ftpFileToEntity(FTPFile file, String currentPath) {
        FileEntity entity = new FileEntity();
        entity.setPath(currentPath.endsWith("/")
                ? currentPath + file.getName()
                : currentPath + "/" + file.getName());

        entity.setName(file.getName());
        entity.setSize(file.getSize());
        entity.setIsDirectory(file.getType() == FTPFile.DIRECTORY_TYPE);
        return entity;
    }
}

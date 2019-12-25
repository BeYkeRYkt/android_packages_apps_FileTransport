package ru.beykerykt.android.filetransport.data;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.beykerykt.android.filetransport.data.entity.FileEntity;

public class FileManager {

    public List<FileEntity> listFiles(String path) {
        String fullPath = path;

        File folder = new File(fullPath);

        File[] files = folder.listFiles();

        List<FileEntity> results = new ArrayList<FileEntity>();

        if (files != null) {
            for (File file : files) {
                results.add(this.fileToEntity(file));
            }
        }

        Collections.sort(results);

        return results;
    }

    public void createFolder(String path, String name) {
        String fullPath = Environment.getExternalStorageDirectory().toString() + path + "/" + name;

        File folder = new File(fullPath);

        if (!folder.exists()) {
            folder.mkdir();
        }
    }

    public void deleteFiles(List<FileEntity> elements) {
        for (FileEntity element : elements) {
            File file = new File(element.getPath());

            if (file.exists()) {
                file.delete();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public FileEntity fileToEntity(File file) {
        FileEntity entity = new FileEntity();
        entity.setPath(file.getPath());
        entity.setName(file.getName());
        entity.setSize(file.length());
        entity.setIsDirectory(file.isDirectory());
        return entity;
    }

}

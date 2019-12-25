package ru.beykerykt.android.filetransport.data.entity;

public class FileEntity implements Comparable<FileEntity> {

    private String name;
    private String path;
    private Long size;
    private boolean isDirectory;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setIsDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    @Override
    public int compareTo(FileEntity file) {
        if (this.isDirectory() && !file.isDirectory()) {
            return -1;
        } else if (!this.isDirectory() && file.isDirectory()) {
            return 1;
        } else {
            return this.getName().compareToIgnoreCase(file.getName());
        }
    }
}
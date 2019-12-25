package ru.beykerykt.android.filetransport.model;

import ru.beykerykt.android.filetransport.data.entity.FileEntity;

public class FileViewModel {

    private boolean isDirectory;
    private String name;
    private String path;
    private Long size;

    public FileViewModel() {
    }

    public FileViewModel(FileEntity entity) {
        this.isDirectory = entity.isDirectory();
        this.name = entity.getName();
        this.path = entity.getPath();
        this.size = entity.getSize();
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        this.isDirectory = directory;
    }

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

    public void setSize(Long size) {
        this.size = size;
    }

    public FileEntity toFileEntity() {
        FileEntity fileEntity = new FileEntity();
        fileEntity.setIsDirectory(isDirectory());
        fileEntity.setPath(getPath());
        fileEntity.setName(getName());
        fileEntity.setSize(getSize());
        return fileEntity;
    }
}

package com.sample.mosquito.utils;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Annotator {

    private final String basePath;
    private String folderName, name;

    public Annotator() {
        this.basePath = App.getContext().getExternalFilesDir(null).getPath();
    }

    public Annotator(String name) {
        this();
        this.name = name;
    }

    public Annotator(String folderName, String name) {
        this();
        this.name = name;
        this.folderName = folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public String getName() {
        return name;
    }

    public boolean exists() {
        return new File(getPath()).exists();
    }

    public void delete() {
        new File(getPath()).delete();
    }

    public void setFullPath(String path) {
        String parent = path.substring(0, path.lastIndexOf("/"));
        name = path.substring(path.lastIndexOf("/") + 1);
        folderName = parent.substring(parent.lastIndexOf("/") + 1);
    }

    public Annotator[] listAll() {
        String path = getPath();
        File[] files = new File(path.substring(0, path.lastIndexOf("/"))).listFiles();
        if (files == null || files.length == 0) return new Annotator[0];
        List<Annotator> list = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                Annotator a = new Annotator();
                a.setFullPath(file.getPath());
                list.add(a);
            }
        }
        return list.toArray(new Annotator[0]);
    }

    public String  getPath() {
        String path = basePath;
        if (folderName != null) path += "/" + folderName;
        if (name == null) name = "data.txt";
        path += "/" + name;
        return path;
    }

    public void setContent(String content) {
        if (content.isEmpty()) return;
        try {
            File file = new File(getPath());
            File folder = file.getParentFile();
            if (folder.isFile() || !folder.exists()) folder.mkdirs();
            FileWriter writer = new FileWriter(file);
            writer.write(content);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getContent() {
        try {
            File file = new File(getPath());
            char[] buffer = new char[(int) file.length()];
            FileReader reader = new FileReader(file);
            int bytesRead = reader.read(buffer);
            reader.close();
            return new String(buffer, 0, bytesRead);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

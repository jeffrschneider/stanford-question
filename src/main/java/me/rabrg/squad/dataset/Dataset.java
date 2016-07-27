package me.rabrg.squad.dataset;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

public final class Dataset {

    private List<Article> data;
    private String version;

    public static Dataset loadDataset(final String path) throws IOException {
        final Gson gson = new GsonBuilder().create();
        return gson.fromJson(new FileReader(path), Dataset.class);
    }

    public List<Article> getData() {
        return data;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return "Dataset{" +
                "data=" + data +
                ", version='" + version + '\'' +
                '}';
    }
}

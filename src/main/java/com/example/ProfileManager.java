package com.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProfileManager {
    private static final String PROFILES_FILE = "profiles.json";
    private List<Profile> profiles;
    private Gson gson;

    public ProfileManager() {
        gson = new GsonBuilder().setPrettyPrinting().create();
        profiles = loadProfiles();
    }

    public List<Profile> getProfiles() {
        return profiles;
    }

    public void saveProfile(Profile profile) {
        // Check if a profile with the same name already exists
        for (int i = 0; i < profiles.size(); i++) {
            if (profiles.get(i).getProfileName().equals(profile.getProfileName())) {
                profiles.set(i, profile); // Update existing profile
                saveProfiles();
                return;
            }
        }
        // If not, add as a new profile
        profiles.add(profile);
        saveProfiles();
    }

    public void deleteProfile(Profile profile) {
        profiles.removeIf(p -> p.getProfileName().equals(profile.getProfileName()));
        saveProfiles();
    }

    private List<Profile> loadProfiles() {
        try {
            File file = new File(PROFILES_FILE);
            if (file.exists()) {
                FileReader reader = new FileReader(file);
                Type type = new TypeToken<ArrayList<Profile>>() {}.getType();
                return gson.fromJson(reader, type);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private void saveProfiles() {
        try (FileWriter writer = new FileWriter(PROFILES_FILE)) {
            gson.toJson(profiles, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

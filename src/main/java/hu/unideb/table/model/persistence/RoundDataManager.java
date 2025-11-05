package hu.unideb.table.model.persistence;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.jetbrains.annotations.NotNull;
import org.tinylog.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class RoundDataManager {
    private static final String FILENAME= "round_data.json";

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void saveRound(Round round) throws IOException {
        Logger.debug("Saving new round: {}", round);
        List<Round> rounds = loadRounds();
        rounds.add(round);
        try (FileWriter writer = new FileWriter(FILENAME)) {
            Logger.info("Round saved successfully. Total rounds: {}", rounds.size());
            gson.toJson(rounds, writer);
        } catch (IOException e) {
            Logger.error(e, "Failed to write round data to file");
            throw e;
        }
    }

    public void saveRound(Round round, String filename) throws IOException {
        Logger.debug("Saving new round: {}", round);
        List<Round> rounds = loadRounds(filename);
        rounds.add(round);
        try (FileWriter writer = new FileWriter(filename)) {
            Logger.info("Round saved successfully. Total rounds: {}", rounds.size());
            gson.toJson(rounds, writer);
        } catch (IOException e) {
            Logger.error(e, "Failed to write round data to file");
            throw e;
        }
    }

    public List<Round> loadRounds() {
        return getRounds(FILENAME);
    }

    @NotNull
    private List<Round> getRounds(String filename) {
        File file = new File(filename);
        if (!file.exists() || file.length() == 0) {
            Logger.warn("Round data file missing or empty, returning empty list");
            return new ArrayList<>();
        }

        try (Reader reader = new FileReader(file)) {
            Type listType = new TypeToken<List<Round>>() {}.getType();
            List<Round> rounds = new Gson().fromJson(reader, listType);
            return rounds != null ? rounds : new ArrayList<>();
        } catch (Exception e) {
            Logger.warn(e, "Could not read round data file, returning empty list");
            return new ArrayList<>();
        }
    }


    public List<Round> loadRounds(String filename) {
        return getRounds(filename);
    }
}

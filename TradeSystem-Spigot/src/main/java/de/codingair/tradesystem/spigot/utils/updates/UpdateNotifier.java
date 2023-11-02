package de.codingair.tradesystem.spigot.utils.updates;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateNotifier {
    private final static String URL_DOWNLOAD = "https://www.spigotmc.org/resources/%s/update?update=%s";
    private final static String GITHUB_URL = "https://api.github.com/repos/erikzimmermann/%s/releases";

    private final String pluginVersion;
    private final String repository;
    private final int spigotId;

    private String version = null;
    private String downloadLink = null;
    private String updateInfo = null;
    private String releasesBehind = null;

    public UpdateNotifier(@NotNull String pluginVersion, @NotNull String repository, int spigotId) {
        this.pluginVersion = pluginVersion;
        this.repository = repository;
        this.spigotId = spigotId;
    }

    public boolean read() {
        String body = readBody();
        if (body == null) return false;

        try {
            JsonArray json = new JsonParser().parse(body).getAsJsonArray(); //Can't compile with old code?

            int firstStable = 0;
            boolean matched = false;
            for (int i = 0; i < json.size(); i++) {
                JsonElement jsonElement = json.get(i);
                JsonObject release = jsonElement.getAsJsonObject();

                if (firstStable == i) {
                    if (!extractLatest(release)) firstStable++;
                }

                // check if current version matches plugin
                String version = release.get("tag_name").getAsString();
                if (version.contains("v")) version = version.substring(1); //remove 'v'

                if (pluginEqualsVersion(version)) {
                    matched = true;
                    if (i - firstStable > 0) releasesBehind = String.valueOf(i - firstStable);
                    break;
                }
            }

            // the releases page has limited entries
            if (!matched) releasesBehind = (json.size() - 1) + "+";

            return releasesBehind != null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean pluginEqualsVersion(@NotNull String version) {
        return pluginVersion.startsWith(version);
    }

    private boolean extractLatest(JsonObject release) {
        String version = release.get("tag_name").getAsString();
        String name = release.get("name").getAsString();

        if (!name.startsWith(version)) return true;

        name = name.replace(version + " - ", "");
        if (version.contains("v")) version = version.substring(1); //remove 'v'
        String content = release.get("body").getAsString();

        Pattern pattern = Pattern.compile("Download: \\d*");
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            downloadLink = String.format(URL_DOWNLOAD,
                    spigotId,
                    matcher.group().replaceAll("\\D*", "")
            );
        } else return false;

        this.version = version;
        this.updateInfo = name;
        return true;
    }

    @Nullable
    private String readBody() {
        String url = String.format(GITHUB_URL, this.repository);
        try (InputStream inputStream = new URL(url).openStream(); Scanner scanner = new Scanner(inputStream)) {
            StringBuilder builder = new StringBuilder();

            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            String s = builder.toString();
            if (!s.isEmpty()) return s;
        } catch (IOException ignored) {
        }

        return null;
    }

    @NotNull
    public String getDownloadLink() {
        if (downloadLink == null) throw new NullPointerException("UpdateNotifier#read() has to be called first!");
        return downloadLink;
    }

    @NotNull
    public String getVersion() {
        if (version == null) throw new NullPointerException("UpdateNotifier#read() has to be called first!");
        return version;
    }

    @NotNull
    public String getUpdateInfo() {
        if (updateInfo == null) throw new NullPointerException("UpdateNotifier#read() has to be called first!");
        return updateInfo;
    }

    @Nullable
    public String getReleasesBehind() {
        if (version == null) throw new NullPointerException("UpdateNotifier#read() has to be called first!");
        return releasesBehind;
    }
}

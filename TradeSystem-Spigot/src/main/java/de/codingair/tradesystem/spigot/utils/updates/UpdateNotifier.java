package de.codingair.tradesystem.spigot.utils.updates;

import de.codingair.tradesystem.spigot.TradeSystem;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateNotifier {
    private final static String URL = "https://api.github.com/repos/CodingAir/TradeSystem/releases/latest";
    private final static String URL_DOWNLOAD = "https://www.spigotmc.org/resources/%s/update?update=%s";
    private final static int ID = 58434;

    private String version = null;
    private String downloadLink = null;
    private String updateInfo = null;

    public boolean read() {
        String body = readBody();

        if (body == null) return false;

        try {
            JSONObject json = (JSONObject) new JSONParser().parse(body);

            String version = (String) json.get("tag_name");
            String name = (String) json.get("name");

            if (!name.startsWith(version)) return false; //may be unstable

            name = name.replace(version + " - ", "");
            version = version.substring(1); //remove 'v'
            String content = (String) json.get("body");

            Pattern pattern = Pattern.compile("Download: \\d*");
            Matcher matcher = pattern.matcher(content);

            if (matcher.find()) downloadLink = String.format(URL_DOWNLOAD, ID, matcher.group().replaceAll("\\D*", ""));
            else return false;

            this.version = version;
            this.updateInfo = name;

            return !TradeSystem.getInstance().getDescription().getVersion().startsWith(version);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return false;
    }

    public String readBody() {
        try (InputStream inputStream = new URL(URL).openStream(); Scanner scanner = new Scanner(inputStream)) {
            StringBuilder builder = new StringBuilder();

            while (scanner.hasNextLine()) {
                builder.append(scanner.nextLine());
            }

            String s = builder.toString();
            if (!s.isEmpty()) return s;
        } catch (IOException ex) {
            //might return code 403 (spam lock)
//            ex.printStackTrace();
        }

        return null;
    }

    public String getDownloadLink() {
        return downloadLink;
    }

    public String getVersion() {
        return version;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }
}

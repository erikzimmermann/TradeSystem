package de.codingair.tradesystem.utils.updates;

import de.codingair.tradesystem.TradeSystem;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateChecker {
    private final String link;
    private URL url;
    private String version = null;
    private String download = null;
    private String updateInfo = null;

    private boolean needsUpdate = false;

    public UpdateChecker(String url) {
        this.link = url;

        try {
            this.url = new URL(url);
        } catch(MalformedURLException ex) {
            ex.printStackTrace();
        }
    }

    public boolean needsUpdate() {
        if(this.url == null) return false;

        this.version = null;
        this.download = null;

        try {
            URLConnection con = this.url.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setConnectTimeout(5000);
            con.connect();

            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while((line = input.readLine()) != null) {
                line = decodeNumericEntities(line);

                if(this.version != null && this.download != null) break;

                if(line.contains("<td class=\"version\">") && this.version == null) {
                    this.version = line.split(">")[1].split("<")[0];
                } else if(line.contains("<td class=\"dataOptions download\">") && download == null) {
                    this.download = "https://www.spigotmc.org/" + line.split("href=\"")[1].split("\"")[0];
                }
            }

            if(this.version == null) return false;
        } catch(Exception ex) {
            return false;
        }

        needsUpdate = false;

        String current = TradeSystem.getInstance().getDescription().getVersion().replaceAll("_Hotfix.*", "");
        if(current.startsWith("v")) current = current.substring(1);
        String newV = version.startsWith("v") ? version.substring(1) : version;

        needsUpdate = !current.equals(newV);
        if(needsUpdate) checkUpdateInfo();

        return needsUpdate;
    }

    public String checkUpdateInfo() {
        if(!needsUpdate) return null;

        String url = this.link;
        url = url.replace("/history", "/updates");

        try {
            URLConnection con = new URL(url).openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");
            con.setConnectTimeout(5000);
            con.connect();

            BufferedReader input = new BufferedReader(new InputStreamReader(con.getInputStream()));

            updateInfo = null;
            boolean atUpdates = false;
            boolean atInfo = false;

            String line;
            while((line = input.readLine()) != null) {
                line = decodeNumericEntities(line);

                if(atUpdates) {
                    if(atInfo) {
                        line = line.replace("</a>", "");
                        line = line.substring(line.lastIndexOf(">") + 1);
                        updateInfo = line;
                        break;
                    }

                    if(line.contains("textHeading")) atInfo = true;
                }

                if(line.contains("updateContainer")) atUpdates = true;
            }

            return updateInfo;
        } catch(Exception ex) {
            return null;
        }
    }

    private String decodeNumericEntities(String s) {
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("\\&#(\\d+);").matcher(s);

        while(m.find()) {
            int uc = Integer.parseInt(m.group(1));
            m.appendReplacement(sb, "");
            sb.appendCodePoint(uc);
        }

        m.appendTail(sb);
        return sb.toString().replace("&amp;", "&");
    }

    public String getDownload() {
        return download;
    }

    public String getVersion() {
        return version;
    }

    public URL getUrl() {
        return url;
    }

    public String getUpdateInfo() {
        return updateInfo;
    }

    public String getLink() {
        return link;
    }
}

package de.codingair.tradesystem.trade.layout.utils;

import de.codingair.tradesystem.trade.layout.Item;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

public class AbstractPattern implements Pattern {
    private final List<Item> items;
    private String name;

    public AbstractPattern(List<Item> items, String name) {
        this.items = items == null ? null : new ArrayList<>(items);
        this.name = name;
    }

    @Override
    public List<Item> getItems() {
        return this.items;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toJSONString() {
        JSONObject json = new JSONObject();

        json.put("Name", this.name);

        JSONArray jsonA = new JSONArray();
        for(Item item : this.items) {
            jsonA.add(item.toJSONString());
        }

        json.put("Items", jsonA.toJSONString());

        return json.toJSONString();
    }

    public static AbstractPattern getFromJSONString(String s) {
        try {
            JSONObject json = (JSONObject) new JSONParser().parse(s);

            String name = (String) json.get("Name");
            List<Item> items = new ArrayList<>();

            JSONArray jsonA = (JSONArray) new JSONParser().parse((String) json.get("Items"));

            for(Object o : jsonA) {
                String s1 = (String) o;
                Item item = Item.fromJSONString(s1);
                if(item != null) items.add(item);
            }

            return new AbstractPattern(items, name);
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean isStandard() {
        return false;
    }
}

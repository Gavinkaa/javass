package ch.epfl.javass;

import java.io.*;
import java.util.*;

public final class Config {
    public static final class Item {
        public final String type;
        public final String name;
        public final String iterations;
        public final String ip;

        public Item(String type, String name, String iterations, String ip) {
            this.type = type;
            this.name = name;
            this.iterations = iterations;
            this.ip = ip;
        }

        public String serialize() {
            StringJoiner s = new StringJoiner(",");
            s.add(this.type);
            s.add(this.name);
            s.add(this.iterations);
            s.add(this.ip);
            return s.toString();
        }

        public static Item fromString(String s) {
            if (s == null) {
                s = "";
            }
            List<String> parts = Arrays.asList(s.split(","));
            String type = "";
            String name = "";
            String iterations = "";
            String ip = "";
            try {
                type = parts.get(0);
                name = parts.get(1);
                iterations = parts.get(2);
                ip = parts.get(3);
            } catch (ArrayIndexOutOfBoundsException e) {
                // ignore it
            }
            return new Item(type, name, iterations, ip);
        }
    }

    public static final String DEFAULT_PATH = ".javassconfig.txt";
    public static final int ITEM_COUNT = 4;

    private final List<Item> items;

    private Config(List<Item> items) {
        this.items = Collections.unmodifiableList(items);
    }

    public static Config fromItems(List<Item> items) {
        return new Config(items);
    }

    public static Config fromDefaultPath() {
        return fromPath(DEFAULT_PATH);
    }

    public static Config fromPath(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                file.createNewFile();
                FileWriter fstream = new FileWriter(file);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(",Aline,10000,XXX.XXX.XXX.XXX\n,Bastien,10000,XXX.XXX.XXX.XXX\n,Colette,10000,XXX.XXX.XXX.XXX\n,David,10000,XXX.XXX.XXX.XXX");
                out.close();
            }
            BufferedReader br = new BufferedReader(new FileReader(file));
            List<Item> items = new ArrayList<>(ITEM_COUNT);
            for (int i = 0; i < ITEM_COUNT; ++i) {
                items.add(Item.fromString(br.readLine()));
            }
            br.close();
            return new Config(items);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Item> getItems() {
        return this.items;
    }

    public void save() {
        this.save(DEFAULT_PATH);
    }

    public void save(String path) {
        try {
            FileWriter fstream = new FileWriter(path);
            BufferedWriter out = new BufferedWriter(fstream);
            for (Item i : this.items) {
                out.write(i.serialize() + '\n');
            }
            out.close();
        } catch (IOException io) {
            System.out.println("Je n'ai pas pu sauver les arguments dans " + path);
        }
    }
}

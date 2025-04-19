package edu.grinnell.csc207.lootgenerator;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main entry point for the loot generator program.
 */
public class LootGenerator {
    private static final String DATA_SET = "data/large";
    private static final Random rng = new Random();

    // In-memory data structures
    private final List<Monster> monsters = new ArrayList<>();
    private final Map<String, TreasureClassEntry> tcMap = new HashMap<>();
    private final Map<String, Armor> armorMap = new HashMap<>();
    private final List<Affix> prefixes = new ArrayList<>();
    private final List<Affix> suffixes = new ArrayList<>();

    public static void main(String[] args) {
        System.out.println("This program kills monsters and generates loot!");
        LootGenerator generator = new LootGenerator();
        try {
            generator.loadData(
                DATA_SET + "/monstats.txt",
                DATA_SET + "/TreasureClassEx.txt",
                DATA_SET + "/armor.txt",
                DATA_SET + "/MagicPrefix.txt",
                DATA_SET + "/MagicSuffix.txt"
            );
            generator.runGenerator();
        } catch (IOException ex) {
            System.err.println("Error loading data: " + ex.getMessage());
        }
    }

    /**
     * Load all data files into memory.
     */
    private void loadData(String monsterFile,
                          String tcFile,
                          String armorFile,
                          String prefixFile,
                          String suffixFile) throws IOException {
        loadMonsters(monsterFile);
        loadTreasureClasses(tcFile);
        loadArmor(armorFile);
        loadAffixes(prefixFile, prefixes);
        loadAffixes(suffixFile, suffixes);
    }

    /**
     * Simple scanner-based loading—no try-with-resources.
     */
    private void loadMonsters(String path) throws IOException {
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\t");
            monsters.add(new Monster(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3]));
        }
        sc.close();
    }

    private void loadTreasureClasses(String path) throws IOException {
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\t");
            List<String> drops = Arrays.asList(parts[1], parts[2], parts[3]);
            tcMap.put(parts[0], new TreasureClassEntry(parts[0], drops));
        }
        sc.close();
    }

    private void loadArmor(String path) throws IOException {
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\t");
            armorMap.put(parts[0], new Armor(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2])));
        }
        sc.close();
    }

    private void loadAffixes(String path, List<Affix> list) throws IOException {
        Scanner sc = new Scanner(new File(path));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\t");
            list.add(new Affix(parts[0], parts[1], Integer.parseInt(parts[2]), Integer.parseInt(parts[3])));
        }
        sc.close();
    }

    /**
     * Main game loop asking to fight until user quits.
     */
    private void runGenerator() {
        Scanner sc = new Scanner(System.in);
        String input;
        do {
            Monster m = pickMonster();
            System.out.println("Fighting " + m.name + "...");
            System.out.println("You have slain " + m.name + "!");
            System.out.println(m.name + " dropped:\n");

            String baseItem = generateBaseItem(m.treasureClass);
            int defense = generateBaseStat(baseItem);
            List<String> affixStats = generateAffixes(baseItem);

            System.out.println(formatItem(baseItem, defense, affixStats));

            System.out.print("Fight again [y/n]? ");
            input = sc.nextLine().trim().toLowerCase();
        } while (input.equals("y"));

        sc.close();
    }

    private Monster pickMonster() {
        return monsters.get(rng.nextInt(monsters.size()));
    }

    private String generateBaseItem(String tc) {
        TreasureClassEntry entry = tcMap.get(tc);
        String choice = entry.drops.get(rng.nextInt(entry.drops.size()));
        if (tcMap.containsKey(choice)) {
            return generateBaseItem(choice);
        }
        return choice;
    }

    private int generateBaseStat(String itemName) {
        Armor a = armorMap.get(itemName);
        return a.minAc + rng.nextInt(a.maxAc - a.minAc + 1);
    }

    private List<String> generateAffixes(String itemName) {
        List<String> lines = new ArrayList<>();
        if (rng.nextBoolean()) {
            Affix p = prefixes.get(rng.nextInt(prefixes.size()));
            int val = p.minVal + rng.nextInt(p.maxVal - p.minVal + 1);
            lines.add(val + " " + p.modCode);
            itemName = p.name + " " + itemName;
        }
        if (rng.nextBoolean()) {
            Affix s = suffixes.get(rng.nextInt(suffixes.size()));
            int val = s.minVal + rng.nextInt(s.maxVal - s.minVal + 1);
            lines.add(val + " " + s.modCode);
            itemName = itemName + " " + s.name;
        }
        return lines;
    }

    private String formatItem(String name, int defense, List<String> stats) {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        sb.append("Defense: ").append(defense).append("\n");
        for (String s : stats) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

    //Helper data classes 
    private static class Monster {
        final String name, type; final int level; final String treasureClass;
        Monster(String name, String type, int level, String treasureClass) {
            this.name = name; this.type = type; this.level = level; this.treasureClass = treasureClass;
        }
    }
    private static class TreasureClassEntry { 
        final String tcName; final List<String> drops;
        TreasureClassEntry(String tcName, List<String> drops) { this.tcName = tcName; this.drops = drops; }
    }
    private static class Armor { 
        final String name; final int minAc, maxAc;
        Armor(String name, int minAc, int maxAc) { this.name = name; this.minAc = minAc; this.maxAc = maxAc; }
    }
    private static class Affix { 
        final String name, modCode; final int minVal, maxVal;
        Affix(String name, String modCode, int minVal, int maxVal) { this.name = name; this.modCode = modCode; this.minVal = minVal; this.maxVal = maxVal; }
    }
}

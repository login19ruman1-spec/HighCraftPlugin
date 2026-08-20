package ru.highcraft;

import org.bukkit.plugin.java.JavaPlugin;

public final class HighCraftPlugin extends JavaPlugin {
    private ItemFactory itemFactory;
    private DataStore dataStore;
    private ConsequenceManager consequences;
    private CauldronManager cauldrons;
    private RecipeManager recipes;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        dataStore = new DataStore(this);
        dataStore.load();

        itemFactory = new ItemFactory(this);
        recipes = new RecipeManager(this, itemFactory);
        recipes.register();

        consequences = new ConsequenceManager(this, dataStore);
        cauldrons = new CauldronManager(this, itemFactory, dataStore, consequences);

        getServer().getPluginManager().registerEvents(new SubstanceListener(this, itemFactory, dataStore, consequences), this);
        getServer().getPluginManager().registerEvents(cauldrons, this);

        DrugCommand command = new DrugCommand(this, itemFactory, dataStore, recipes);
        getCommand("drug").setExecutor(command);
        getCommand("drug").setTabCompleter(command);

        getLogger().info("HighCraftPlugin enabled.");
    }

    @Override
    public void onDisable() {
        if (dataStore != null) dataStore.save();
    }

    public void reloadHighCraft() {
        reloadConfig();
        recipes.register();
    }

    public ItemFactory items() { return itemFactory; }
    public DataStore data() { return dataStore; }
    public ConsequenceManager consequences() { return consequences; }
    public CauldronManager cauldrons() { return cauldrons; }
    public RecipeManager recipes() { return recipes; }
}

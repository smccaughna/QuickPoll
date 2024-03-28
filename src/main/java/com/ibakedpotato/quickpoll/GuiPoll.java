package com.ibakedpotato.quickpoll;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;

public class GuiPoll implements Listener, InventoryHolder {

    private final Inventory inventory;

    private String pollQuestion             = null;
    private int pollDuration                = 0;
    private ArrayList<String> pollResponses = new ArrayList<>();

    public GuiPoll() {
        this.inventory = Bukkit.createInventory(this, 9, "Create Poll");
        this.inventory.addItem(this.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, ""));
        this.inventory.addItem(this.createGuiItem(Material.WRITABLE_BOOK, "Edit question"));
        this.inventory.addItem(this.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        this.inventory.addItem(this.createGuiItem(Material.WRITABLE_BOOK, "Set poll duration"));
        this.inventory.addItem(this.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "  "));
        this.inventory.addItem(this.createGuiItem(Material.WRITABLE_BOOK, "Add responses"));
        this.inventory.addItem(this.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "   "));
        this.inventory.addItem(this.createGuiItem(Material.LIME_DYE, "Create poll"));
        this.inventory.addItem(this.createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "    "));
    }

    private ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);

        return item;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }

    public void openInventory(Player player) {
        HandlerList.unregisterAll(this);
        player.sendMessage("opening outer listener");
        JavaPlugin.getPlugin(QuickPoll.class).getServer().getPluginManager().registerEvents(this, JavaPlugin.getPlugin(QuickPoll.class));
        player.openInventory(this.inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof GuiPoll)) return;

        event.setCancelled(true);

        if (event.getCurrentItem() != null) {
            Player player = (Player) event.getWhoClicked();

            switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
                case "Edit question": {
                    player.closeInventory();

                    ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                            .withFirstPrompt(new PromptSetQuestion())
                            .withLocalEcho(true)
                            .withEscapeSequence("cancel")
                            .thatExcludesNonPlayersWithMessage("This command must be used by a player.");

                    factory.buildConversation(player).begin();
                } break;
                case "Set poll duration": {
                    player.closeInventory();

                    ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                            .withFirstPrompt(new PromptSetDuration())
                            .withLocalEcho(true)
                            .withEscapeSequence("cancel")
                            .thatExcludesNonPlayersWithMessage("This command must be used by a player.");

                    factory.buildConversation(player).begin();
                } break;
                case "Add responses": {
                    GuiAddResponses guiAddResponses = new GuiAddResponses();
                    guiAddResponses.openInventory(player);
                } break;
                case "Create poll": {
                    HandlerList.unregisterAll(this);
                    player.sendMessage("closing outer listener");
                    player.closeInventory();
                    player.sendMessage("Question: " + GuiPoll.this.pollQuestion + "\nDuration: " + String.valueOf(GuiPoll.this.pollDuration));
                    for (int i = 0; i < GuiPoll.this.pollResponses.size(); i++) {
                        player.sendMessage(String.valueOf(i + 1) + ": " + GuiPoll.this.pollResponses.get(i));
                    }
                } break;
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(final InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof GuiPoll) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        if (!(event.getInventory().getHolder(false) instanceof GuiPoll)) return;

        event.getPlayer().sendMessage("closing outer listener");
        HandlerList.unregisterAll(this);
    }

    private class PromptSetQuestion extends StringPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return "Enter the poll question:";
        }

        @Override
        public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String str) {
            GuiPoll.this.pollQuestion = str;

            Player player = (Player) context.getForWhom();
            GuiPoll.this.openInventory(player);

            return Prompt.END_OF_CONVERSATION;
        }
    }

    private class PromptSetDuration extends NumericPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext conversationContext) {
            return "Enter the poll duration in seconds:";
        }

        @Override
        protected @Nullable Prompt acceptValidatedInput(@NotNull ConversationContext context, @NotNull Number number) {
            GuiPoll.this.pollDuration = (int) number;

            Player player = (Player) context.getForWhom();
            GuiPoll.this.openInventory(player);

            return Prompt.END_OF_CONVERSATION;
        }
    }

    private class GuiAddResponses implements Listener, InventoryHolder {

        private final Inventory inventory;

        public GuiAddResponses() {
            this.inventory = Bukkit.createInventory(this, 9, "Add Responses");
            this.inventory.addItem(GuiPoll.this.createGuiItem(Material.RED_DYE, "Back"));
            this.inventory.addItem(GuiPoll.this.createGuiItem(Material.WRITABLE_BOOK, "Add response"));

            for (int i = 0; i < GuiPoll.this.pollResponses.size(); i++) {
                this.inventory.addItem(GuiPoll.this.createGuiItem(Material.PAPER, GuiPoll.this.pollResponses.get(i), String.valueOf(i + 1)));
            }
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }

        public void openInventory(Player player) {
            HandlerList.unregisterAll(this);
            player.sendMessage("opening inner listener");
            JavaPlugin.getPlugin(QuickPoll.class).getServer().getPluginManager().registerEvents(this, JavaPlugin.getPlugin(QuickPoll.class));
            player.openInventory(this.inventory);
        }

        @EventHandler
        public void onInventoryClick(final InventoryClickEvent event) {
            if (!(event.getInventory().getHolder(false) instanceof GuiAddResponses)) return;

            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();

                switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
                    case "Back": {
                        GuiPoll.this.openInventory(player);
                    } break;
                    case "Add response": {
                        if (GuiPoll.this.pollResponses.size() < 7) {
                            player.closeInventory();

                            ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                                    .withFirstPrompt(new PromptAddResponse())
                                    .withLocalEcho(true)
                                    .withEscapeSequence("cancel")
                                    .thatExcludesNonPlayersWithMessage("This command must be used by a player.");

                            factory.buildConversation(player).begin();
                        }
                    } break;
                }
            }
        }

        @EventHandler
        public void onInventoryClick(final InventoryDragEvent event) {
            if (event.getInventory().getHolder(false) instanceof GuiAddResponses) {
                event.setCancelled(true);
            }
        }

        @EventHandler
        public void onInventoryOpen(final InventoryOpenEvent event) {
            JavaPlugin.getPlugin(QuickPoll.class).getServer().getPluginManager().registerEvents(this, JavaPlugin.getPlugin(QuickPoll.class));
        }

        @EventHandler
        public void onInventoryClose(final InventoryCloseEvent event) {
            if (!(event.getInventory().getHolder(false) instanceof GuiAddResponses)) return;

            HandlerList.unregisterAll(this);
            event.getPlayer().sendMessage("closing inner listener");
        }

        private class PromptAddResponse extends StringPrompt {

            @Override
            public @NotNull String getPromptText(@NotNull ConversationContext context) {
                return "Enter a poll response:";
            }

            @Override
            public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String str) {
                GuiPoll.this.pollResponses.add(str);
                GuiAddResponses.this.inventory.addItem(GuiPoll.this.createGuiItem(Material.PAPER, str, String.valueOf(GuiPoll.this.pollResponses.size())));

                Player player = (Player) context.getForWhom();
                GuiAddResponses.this.openInventory(player);

                return Prompt.END_OF_CONVERSATION;
            }
        }
    }
}
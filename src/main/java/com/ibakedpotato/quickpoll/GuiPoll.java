package com.ibakedpotato.quickpoll;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
    private ArrayList<String> pollResponses = new ArrayList<String>();

    public GuiPoll() {
        inventory = Bukkit.createInventory(this, 9, "Create Poll");
        inventory.addItem(createGuiItem(Material.BLACK_STAINED_GLASS_PANE, ""));
        inventory.addItem(createGuiItem(Material.WRITABLE_BOOK, "Edit question"));
        inventory.addItem(createGuiItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        inventory.addItem(createGuiItem(Material.WRITABLE_BOOK, "Set poll duration"));
        inventory.addItem(createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "  "));
        inventory.addItem(createGuiItem(Material.WRITABLE_BOOK, "Add responses"));
        inventory.addItem(createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "   "));
        inventory.addItem(createGuiItem(Material.LIME_DYE, "Create poll"));
        inventory.addItem(createGuiItem(Material.BLACK_STAINED_GLASS_PANE, "    "));
    }

    protected ItemStack createGuiItem(final Material material, final String name, final String... lore) {
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
                            .thatExcludesNonPlayersWithMessage("This command must be used by a player.")
                            .withLocalEcho(true)
                            .withEscapeSequence("cancel");

                    factory.buildConversation(player).begin();
                } break;
                case "Set poll duration": {
                    player.closeInventory();

                    ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                            .withFirstPrompt(new PromptSetDuration())
                            .thatExcludesNonPlayersWithMessage("This command must be used by a player.")
                            .withLocalEcho(true);

                    factory.buildConversation(player).begin();
                } break;
                case "Add responses": {
                    player.closeInventory();

                    GuiAddResponses guiAddResponses;
                    JavaPlugin.getPlugin(QuickPoll.class).getServer().getPluginManager().registerEvents(guiAddResponses = new GuiAddResponses(), JavaPlugin.getPlugin(QuickPoll.class));

                    player.openInventory(guiAddResponses.getInventory());
                } break;
                case "Create poll": {
                    HandlerList.unregisterAll(this);

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
    public void onInventoryClick(final InventoryDragEvent event) {
        if (event.getInventory().getHolder(false) instanceof GuiPoll) {
            event.setCancelled(true);
        }
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
            player.openInventory(GuiPoll.this.getInventory());

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
            player.openInventory(GuiPoll.this.getInventory());

            return Prompt.END_OF_CONVERSATION;
        }
    }

    private class GuiAddResponses implements Listener, InventoryHolder {

        private final Inventory inventory;

        public GuiAddResponses() {
            inventory = Bukkit.createInventory(this, 9, "Add Responses");
            inventory.addItem(GuiPoll.this.createGuiItem(Material.RED_DYE, "Back"));
            inventory.addItem(GuiPoll.this.createGuiItem(Material.WRITABLE_BOOK, "Add response"));

            for (int i = 0; i < GuiPoll.this.pollResponses.size(); i++) {
                inventory.addItem(GuiPoll.this.createGuiItem(Material.PAPER, GuiPoll.this.pollResponses.get(i), String.valueOf(i + 1)));
            }
        }

        @Override
        public @NotNull Inventory getInventory() {
            return this.inventory;
        }

        @EventHandler
        public void onInventoryClick(final InventoryClickEvent event) {
            if (!(event.getInventory().getHolder(false) instanceof GuiAddResponses)) return;

            event.setCancelled(true);

            if (event.getCurrentItem() != null) {
                Player player = (Player) event.getWhoClicked();

                switch (event.getCurrentItem().getItemMeta().getDisplayName()) {
                    case "Back": {
                        HandlerList.unregisterAll(this);
                        player.openInventory(GuiPoll.this.getInventory());
                    } break;
                    case "Add response": {
                        if (GuiPoll.this.pollResponses.size() <= 7) {
                            player.closeInventory();

                            ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                                    .withFirstPrompt(new PromptAddResponse())
                                    .thatExcludesNonPlayersWithMessage("This command must be used by a player.")
                                    .withLocalEcho(true);

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
                player.openInventory(GuiAddResponses.this.getInventory());

                return Prompt.END_OF_CONVERSATION;
            }
        }
    }
}
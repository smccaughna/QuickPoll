package com.ibakedpotato.quickpoll;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.conversations.*;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;

public class GuiPoll implements Listener {

    private final Inventory inventory;

    private String pollQuestion;
    private int pollDuration;
    private HashMap<Integer, String> responses;


    public GuiPoll() {
        inventory = Bukkit.createInventory(null, 9, "Create Poll");
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

    public void openInventory(final HumanEntity player) {
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("Create Poll")) return;

        e.setCancelled(true);

        if (e.getCurrentItem() != null) {
            Player player = (Player) e.getWhoClicked();

            switch (e.getCurrentItem().getItemMeta().getDisplayName()) {
                case "Edit question": {
                    player.closeInventory();

                    ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                            .withModality(true)
                            .withFirstPrompt(new PromptSetQuestion())
                            .withEscapeSequence("cancel")
                            .thatExcludesNonPlayersWithMessage("This command must be used by a player.");

                    factory.buildConversation(player).begin();
                } break;
                case "Set poll duration": {
                    player.closeInventory();

                    ConversationFactory factory = new ConversationFactory(JavaPlugin.getPlugin(QuickPoll.class))
                            .withModality(true)
                            .withFirstPrompt(new PromptSetDuration())
                            .withEscapeSequence("cancel")
                            .thatExcludesNonPlayersWithMessage("This command must be used by a player.");

                    factory.buildConversation(player).begin();
                } break;
                case "Add responses":
                    break;
            }
        }
    }

    @EventHandler
    public void onInventoryClick(final InventoryDragEvent e) {
        if (e.getView().getTitle().equals("Create Poll")) {
            e.setCancelled(true);
        }
    }

    private class PromptSetQuestion extends StringPrompt {

        @Override
        public @NotNull String getPromptText(@NotNull ConversationContext context) {
            return "Enter the poll question:";
        }

        @Override
        public @Nullable Prompt acceptInput(@NotNull ConversationContext context, @Nullable String str) {
            pollQuestion = str;

            Player player = (Player) context.getForWhom();
            player.sendMessage(pollQuestion);
            openInventory(player);

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
            pollDuration = (int) number;

            Player player = (Player) context.getForWhom();
            player.sendMessage(String.valueOf((int)number));
            openInventory(player);

            return Prompt.END_OF_CONVERSATION;
        }
    }
}
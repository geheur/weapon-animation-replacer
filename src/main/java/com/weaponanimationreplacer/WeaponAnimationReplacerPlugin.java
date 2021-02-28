package com.weaponanimationreplacer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.weaponanimationreplacer.AnimationReplacementRule.AnimationReplacement;
import com.weaponanimationreplacer.AnimationReplacementRule.AnimationType;
import com.weaponanimationreplacer.AnimationReplacementRule.ItemRestriction;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemComposition;
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.chatbox.ChatboxItemSearch;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.screenmarkers.ScreenMarkerPlugin;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.weaponanimationreplacer.AnimationReplacementRule.AnimationType.*;

/* TODO
    for release:
    rename rules.
    use rule regardless of item equipped.
    minimize rule.
    item selector "any" option.
    play animation when it is changed.
    @Inject stuff instead of plugin.dothing.
    rule deletion confirm.
    prettify the ui so people know the x and + are clickable.
    why does dwh gadderhammer not use gadderhammer attack with all? I mean I like it, but I didn't expect this.
    ranged and magic weapons, and attack animations for existing weapons..
        staff animations.
        https://github.com/equirs/fashionscape-plugin/blob/master/src/main/java/eq/uirs/fashionscape/data/IdleAnimationID.java
    code attribution.
    Remove plugin panel while plugin is disabled.
    it is unintuitive that unchecking all "use with item" boxes results in all items being affected.
        There should be a dropdown that chooses between "any item" and "specific item".
    having checkboxes on every item is confusing and should be locked behind a toggle.

    keep client focus.

    Fix animations when animations are updated - no more holding chally wrong.
    disambiguate walk/run for shuffle animations.
    weapon swap while walking doesn't activate immediately.

    at some point maybe:
    interference with transmog plugin.
 */
@PluginDescriptor(
        name = "Weapon Animation Replacer",
        description = "replace weapon animations (stand,walk,run,attack) with other ones",
        tags = {"transmog", "fashionscape"},
        loadWhenOutdated = true
)
public class WeaponAnimationReplacerPlugin extends Plugin {

    @Inject
    @Nullable // TODO remove this and loadWhenOutdated.
    private Client client;

    @Inject
    private ChatboxItemSearch itemSearch;

    @Inject
    private ClientUI clientUI;

    @Inject
    ItemManager itemManager;

    @Inject
    public ClientThread clientThread;

    @Inject
    private ClientToolbar clientToolbar;

    @Inject
    private ConfigManager configManager;

    private WeaponAnimationReplacerPluginPanel pluginPanel;
    private NavigationButton navigationButton;

    @Override
    protected void startUp() throws Exception
    {
        try {
            animationReplacementRules = getRulesFromConfig();
        } catch (JsonParseException | IllegalStateException e) {
            System.out.println("issue with json: " + configManager.getConfiguration("WeaponAnimationReplacer", "rules"));
            animationReplacementRules = new ArrayList<>();
            e.printStackTrace();
        }

        pluginPanel = new WeaponAnimationReplacerPluginPanel(this);
        pluginPanel.rebuild();

        final BufferedImage icon = ImageUtil.loadImageResource(ScreenMarkerPlugin.class, "panel_icon.png");

        navigationButton = NavigationButton.builder()
                .tooltip("Weapon Animation Replacer")
                .icon(icon)
                .priority(5)
                .panel(pluginPanel)
                .build();

        clientToolbar.addNavigation(navigationButton);
    }

    @Override
    protected void shutDown() {
        pluginPanel = null;

        transmogManager.shutDown();
    }

    static Gson customGson;
    static Gson customGsonPretty;
    static {
        GsonBuilder gsonBuilder = new GsonBuilder();

        Type merchantListType = new TypeToken<AnimationSet>() {}.getType();
        JsonSerializer<AnimationSet> serializer = new JsonSerializer<AnimationSet>() {
            @Override
            public JsonElement serialize(AnimationSet set, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonPrimitive(set.name);
            }
        };
        gsonBuilder.registerTypeAdapter(merchantListType, serializer);
        JsonDeserializer<AnimationSet> deserializer = new JsonDeserializer<AnimationSet>() {
            @Override
            public AnimationSet deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                if (jsonElement instanceof JsonPrimitive && ((JsonPrimitive) jsonElement).isString()) {
                    AnimationSet animationSet = getAnimationSet(jsonElement.getAsString());
                    if (animationSet == null) return AnimationSet.animationSets.get(0);
                    return animationSet;
                } else {
                    throw new JsonParseException("animationset is supposed to be a string.");
                }
            }
        };
        gsonBuilder.registerTypeAdapter(merchantListType, deserializer);

        customGson = gsonBuilder.create();

        gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(merchantListType, serializer);
        gsonBuilder.registerTypeAdapter(merchantListType, deserializer);
        gsonBuilder.setPrettyPrinting();
        customGsonPretty = gsonBuilder.create();
    }

    public List<AnimationReplacementRule> getRulesFromConfig() {
        String configuration = configManager.getConfiguration("WeaponAnimationReplacer", "rules");
        if (configuration == null) return getDefaultAnimationReplacementRules();
        return customGson.fromJson(configuration, new TypeToken<ArrayList<AnimationReplacementRule>>() {}.getType());
    }

    public void saveRules() {
        String s = customGson.toJson(animationReplacementRules);
        configManager.setConfiguration("WeaponAnimationReplacer", "rules", s);
    }

    @Subscribe
    public void onItemContainerChanged(final ItemContainerChanged event) {
        final ItemContainer container = event.getItemContainer();
        final ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);
        if (container == equipment) {
            // TODO.
        }
    }

    public void updateAnimationsAndTransmog() {
        System.out.println("updating animations and transmog");
        saveRules();

        Player localPlayer = client.getLocalPlayer();
        if (localPlayer != null) {
            localPlayer.setIdlePoseAnimation(808);
            localPlayer.setPoseAnimation(808);
        }
        lastAnimation = -1;
        lastPoseAnimation = -1;
        lastIdlePoseAnimation = -1;

        transmogManager.applyTransmog();

        clientUI.requestFocus();
    }

    public void deleteNewRule(int index) {
        animationReplacementRules.remove(index);
        SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
        saveRules();
    }

    public void addNewRule(int index) {
        animationReplacementRules.add(index + 1, AnimationReplacementRule.createTemplate(AnimationSet.animationSets.get(0)));
        SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
        saveRules();
    }

    private List<AnimationReplacementRule> animationReplacementRules;

    public static List<AnimationReplacementRule> getDefaultAnimationReplacementRules() {
        List<AnimationReplacementRule> rules = new ArrayList<>();

        List<ItemRestriction> itemRestrictions = new ArrayList<>();
        itemRestrictions.add(new ItemRestriction(12006));
        itemRestrictions.add(new ItemRestriction(22324));
        List<AnimationReplacement> animationReplacements = new ArrayList<>();
        animationReplacements.add(new AnimationReplacement(getAnimationSet("scythe"), ALL, null));
        rules.add(new AnimationReplacementRule(itemRestrictions, animationReplacements));

        itemRestrictions = new ArrayList<>();
        itemRestrictions.add(new ItemRestriction(4151));
        animationReplacements = new ArrayList<>();
        animationReplacements.add(new AnimationReplacement(getAnimationSet("maul"), ALL, null));
        rules.add(new AnimationReplacementRule(itemRestrictions, animationReplacements));

        itemRestrictions = new ArrayList<>();
        itemRestrictions.add(new ItemRestriction(23987));
        itemRestrictions.add(new ItemRestriction(3204));
        animationReplacements = new ArrayList<>();
        animationReplacements.add(new AnimationReplacement(getAnimationSet("dharok"), STANCE, null));
        animationReplacements.add(new AnimationReplacement(getAnimationSet("scythe"), ATTACK, ATTACK_SLASH));
        rules.add(new AnimationReplacementRule(itemRestrictions, animationReplacements));

        itemRestrictions = new ArrayList<>();
        itemRestrictions.add(new ItemRestriction(13652));
        animationReplacements = new ArrayList<>();
        animationReplacements.add(new AnimationReplacement(getAnimationSet("banana"), STANCE, null));
        animationReplacements.add(new AnimationReplacement(getAnimationSet("banana"), DEFEND, null));
        animationReplacements.add(new AnimationReplacement(getAnimationSet("banana"), ATTACK_SLASH, ATTACK_CRUSH));
        animationReplacements.add(new AnimationReplacement(getAnimationSet("banana"), ATTACK_STAB, ATTACK_CRUSH));
        rules.add(new AnimationReplacementRule(itemRestrictions, animationReplacements));
        return rules;
    }

    private static AnimationSet getAnimationSet(String name) {
        return AnimationSet.animationSets.stream().filter(a -> name.equals(a.name)).findAny().orElse(null);
    }

    public List<AnimationReplacementRule> getAnimationReplacementRules() {
        return animationReplacementRules;
    }

    public List<AnimationReplacementRule> getApplicableAnimationReplacementRules() {
        Integer weaponItemId = getEquippedWeapon();
        if (weaponItemId == null) weaponItemId = -1;
        Integer finalWeaponItemId = weaponItemId;
        return animationReplacementRules.stream().filter(rule -> rule.enabled && rule.appliesToItem(finalWeaponItemId)).sorted((r1, r2) -> {
            if (r1.appliesSpecificallyToItem(finalWeaponItemId) == r2.appliesSpecificallyToItem(finalWeaponItemId)) return 0;
            return r1.appliesSpecificallyToItem(finalWeaponItemId) ? 1 : -1;
        }).collect(Collectors.toList());
    }

    int lastIdlePoseAnimation = -1;
    int lastPoseAnimation = -1;
    int lastAnimation = -1;
    int poseToUse = -1;
    int animationToUse = -1;
    @Subscribe
    public void onClientTick(ClientTick event)
    {
        Player localPlayer = client.getLocalPlayer();
        AnimationSet currentAnimationSet = getCurrentAnimationSet();
        if (currentAnimationSet == null) {
            poseToUse = -1;
            return;
        }

        int playerIdlePoseAnimation = localPlayer.getIdlePoseAnimation();
        if (lastIdlePoseAnimation != playerIdlePoseAnimation) {
            Integer animationReplace = currentAnimationSet.getAnimation(AnimationType.STAND, isRunning());
            if (animationReplace != null) {
                System.out.println("replaced (idle) " + playerIdlePoseAnimation + " with " + animationReplace);
                localPlayer.setIdlePoseAnimation(animationReplace);
            }
        }
        lastIdlePoseAnimation = playerIdlePoseAnimation;

        int playerPoseAnimation = localPlayer.getPoseAnimation();
        if (lastPoseAnimation != playerPoseAnimation) {
            AnimationType type = getType(playerPoseAnimation);
            if (type != null) {
                Integer animationReplace = currentAnimationSet.getAnimation(type, isRunning());
                if (animationReplace != null) {
                    poseToUse = animationReplace;
                    System.out.println(playerPoseAnimation + " (replaced pose " + playerPoseAnimation + " with " + animationReplace + ")");
                } else {
                    System.out.println(playerPoseAnimation);
                    poseToUse = -1;
                }
            }
        }
        lastPoseAnimation = playerPoseAnimation;
        if (poseToUse != -1) localPlayer.setPoseAnimation(poseToUse);

        int playerAnimation = localPlayer.getAnimation();
        if (lastAnimation != playerAnimation) {
            Integer animationReplace = getReplacementAnimation(playerAnimation);
            if (animationReplace != null) {
                animationToUse = animationReplace;
                System.out.println("replaced animation " + playerAnimation + " with " + animationReplace);
            } else {
                animationToUse = -1;
            }
        }
        lastAnimation = playerAnimation;
        if (animationToUse != -1) {
            localPlayer.setAnimation(animationToUse);
        }
    }

    private AnimationSet lastAnimationSet = null;
    private AnimationSet getCurrentAnimationSet() { // TODO cache maybe.
        AnimationSet currentSet = new AnimationSet();

        List<AnimationReplacementRule> appliedRules = getApplicableAnimationReplacementRules();
        List<AnimationReplacement> replacements = appliedRules.stream()
                .flatMap(replacement -> replacement.animationReplacements.stream())
                .filter(replacement -> replacement.isActive())
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());

        for (AnimationReplacement replacement : replacements) {
            currentSet.applyReplacement(replacement);
            if (replacement.animationtypeToReplace.appliesTo(AnimationType.MOVEMENT)) {
                currentSet.useWalkOrRunForShuffleAndWalkBackwards = replacement.animationSet.useWalkOrRunForShuffleAndWalkBackwards;
            }
        }

        if (!currentSet.equals(lastAnimationSet)) {
            System.out.println("animation set change: " + currentSet + " " + appliedRules + " " + replacements);
            lastAnimationSet = currentSet;
        }
        return currentSet;
    }

    private Integer getEquippedWeapon() {
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer == null) return null;
        Item[] equippedItems = equipmentContainer.getItems();
        if (equippedItems.length < EquipmentInventorySlot.WEAPON.getSlotIdx()) return null;
        int weaponItemId = equippedItems[EquipmentInventorySlot.WEAPON.getSlotIdx()].getId();
        return weaponItemId;
    }

    private Integer getReplacementAnimation(int playerPoseAnimation) {
        AnimationSet currentAnimationSet = getCurrentAnimationSet();
        if (currentAnimationSet == null) {
            return null;
        }
        AnimationType type = getType(playerPoseAnimation);
        if (type != null) {
            return currentAnimationSet.getAnimation(type, isRunning());
        } else {
            for (AnimationSet animationSet : AnimationSet.animationSets) {
                type = animationSet.getType(playerPoseAnimation);
                if (type != null) {
                    return currentAnimationSet.getAnimation(type, isRunning());
                }
            }
        }
        return null;
    }

    private boolean isRunning() { // TODO.
        return client.getVarpValue(173) == 1;
    }

    private AnimationType getType(int animation) {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer.getRunAnimation() == animation) return RUN;
        else if (localPlayer.getWalkAnimation() == animation) return WALK;
        else if (localPlayer.getIdlePoseAnimation() == animation) return STAND;
        else if (localPlayer.getWalkRotate180() == animation) return WALK_BACKWARD;
        else if (localPlayer.getIdleRotateLeft() == animation || localPlayer.getIdleRotateRight() == animation) return ROTATE;
        else if (localPlayer.getWalkRotateLeft() == animation) return SHUFFLE_LEFT;
        else if (localPlayer.getWalkRotateRight() == animation) return SHUFFLE_RIGHT;
        else return null;
    }

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted) {
//            Player swoofii = client.getPlayers().stream().filter(p -> p.getName().equals("swoofii")).findAny().get();
//            Player localPlayer1 = client.getLocalPlayer();
//
//        if ("set".equals(commandExecuted.getCommand())) {
//            String[] arguments = commandExecuted.getArguments();
//            currentAnimationSet = AnimationSet.animationSets.stream().filter(s -> s.name.equals(arguments[0])).findAny().orElseGet(() -> null);
//            if (currentAnimationSet == null) {
//                client.getLocalPlayer().setIdlePoseAnimation(808);
//                client.getLocalPlayer().setPoseAnimation(808);
//            } else {
//                client.getLocalPlayer().setIdlePoseAnimation(currentAnimationSet.getStand());
//                client.getLocalPlayer().setPoseAnimation(currentAnimationSet.getStand());
//            }
//            System.out.println("animation set to " + (currentAnimationSet == null ? "null" : currentAnimationSet.name));
//        }
        if ("dumpanim".equals(commandExecuted.getCommand())) {
            System.out.println("dumping fields: ");
            dumpAnim("", client.getLocalPlayer().getPlayerComposition(), new ArrayList<>());
        }
//        if ("seta".equals(commandExecuted.getCommand())) {
//            String[] arguments = commandExecuted.getArguments();
//            AttackType attackType = Arrays.stream(AttackType.values()).filter(e -> e.toString().toLowerCase().equals(arguments[0].toLowerCase())).findAny().orElse(null);
//            currentAttackType = attackType;
//            System.out.println("attack type set to " + (currentAttackType == null ? "null" : currentAttackType));
//        }
    }

    private void dumpAnim(String prefix, Object o, List<Object> seen) {
        Class<?> aClass = o.getClass();
        seen.add(o);
        while (true) {
            Field[] fields = aClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                if (prefix.length() > 20) {

                    System.out.println("too deep!");
                    return;
                }
                Field field = fields[i];
                try {
                    field.setAccessible(true);
                } catch (Exception e) {
                    System.out.println(prefix + "inaccessible: " + fields[i].getName() + " " + field.getType());
                    continue;
                }
                try {
                    Object o1 = field.get(o);
                    String s;
                    if (o1 != null) {
                        s = o1.toString();
                        if (field.getType().isArray()) {
                            s = "[";
                            for (int j = 0; j < Math.min(Array.getLength(o1), 100); j++) {
                                s += Array.get(o1, j) + ", ";
                            }
                            s += "]";
                            if (Array.getLength(o1) >= 100) {
                                s += " size: " + Array.getLength(o1);
                            }
                        }
                        System.out.println(prefix + fields[i].getName() + " | " + field.getType() + " | " + s + " | " + aClass.getName());
                        if (!field.getType().isPrimitive() && o1 != null && !seen.contains(o1)) {
                            dumpAnim(prefix + "\t", o1, seen);
                        }
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            aClass = aClass.getSuperclass();
            if (aClass == null) {
                break;
            }
        }
    }

    @Inject
    private TransmogrificationManager transmogManager;

    @Subscribe
    public void onGameTick(GameTick e)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }

        // On most teleports, the player kits are reset. This will reapply the transmog if needed.
        final int currentHash = Arrays.hashCode(client.getLocalPlayer().getPlayerComposition().getEquipmentIds());
        if (currentHash != transmogManager.getTransmogHash())
        {
            transmogManager.reapplyTransmog();
        }
    }

    public void doItemSearch(JButton button, Consumer<Integer> onItemChosen) {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            // TODO uncomment or do something similar.
//            JOptionPane.showMessageDialog(panel,
//                    "You must be logged in to search.",
//                    "Cannot Search for Item",
//                    JOptionPane.ERROR_MESSAGE);
//            return;
            System.out.println("cannot do this while logged out.");
        }

        itemSearch
                .tooltipText("only use these changes with this item:")
                .onItemSelected((itemId) ->
                {
                    clientThread.invokeLater(() ->
                    {
                        int finalId = itemManager.canonicalize(itemId);

                        ItemComposition itemComposition = itemManager.getItemComposition(itemId);

                        button.setIcon(new ImageIcon(itemManager.getImage(itemId)));

                        SwingUtilities.invokeLater(() -> {
                            System.out.println("value is " + itemId);
                        });
                        onItemChosen.accept(itemId);
                    });
                })
                .build();
        clientUI.requestFocus();
    }

    public BufferedImage getItemImage(int itemId) {
        return itemManager.getImage(itemId);
    }

    public boolean clientLoaded = false;

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN) {
            if (!clientLoaded) SwingUtilities.invokeLater(pluginPanel::rebuild);
            clientLoaded = true;
        }
    }
}

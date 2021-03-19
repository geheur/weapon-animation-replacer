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
import net.runelite.api.ItemContainer;
import net.runelite.api.Player;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.weaponanimationreplacer.AnimationReplacementRule.AnimationType.*;

/* TODO
    for release:
        A simple dropdown menu to change all animations. This would be an easy way for people to test them all out and see what the plugin can do. The advanced stuff should be shown to people via a checkbox for hiding plugin panel.
        rule deletion confirm.
        Adding a new item restriction should open the search menu immediately.

        last-destination isn't swapped in my plugin if there is no set destination.

        magic weapons.
            staff animations.
            https://github.com/equirs/fashionscape-plugin/blob/master/src/main/java/eq/uirs/fashionscape/data/IdleAnimationID.java
        code attribution in readme.
        tool animations.
            Mining with something like ancient mage animation would be pretty neat.

        tools.

        ask:
            Should I include a config option that is a dropdown with all animation sets, to let people try out the plugin without having to figure out the config panel?
            Do I need to direct people to a plugin panel (e.g. via a checkbox that says "show plugin panel" in the config).
            What should I name this?
            Good example configs to include as default.

        godsword running looks weird.
        what animations does master wand use?

    Fix animation when removing rule.
        Would have to record the players actual pose animations whenever they switch to a weapon, then set those.
    other players.
    custom ids.
    disambiguate walk/run for shuffle animations.
    add back checkboxes? maybe as an option?
    get rid of enabled flags that you do not need.
    Some indicator that a rule is active.
    npc animations - e.g. street sweeper.
    "unarmed" item id.
        This could actually just be the default state of the item button (instead of "None", have it say "unarmed").
        How can you add this for the model swap as well?
            This is kinda shit. Who would use this?

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
    Client client;

    @Inject
    private ChatBoxFilterableSearch itemSearch;

    @Inject
    ClientUI clientUI;

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

        clientToolbar.removeNavigation(navigationButton);
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
        saveRules();

        Player localPlayer = client.getLocalPlayer();
        System.out.println("bla");
//        if (localPlayer != null) {
//            localPlayer.setIdlePoseAnimation(808);
//            localPlayer.setPoseAnimation(808);
//        }
        lastAnimation = -1;
        lastPoseAnimation = -1;
        lastIdlePoseAnimation = -1;

        transmogManager.applyTransmog();

        clientUI.requestFocus();
    }

    public void deleteRule(int index) {
        int delete = JOptionPane.showConfirmDialog(pluginPanel,
                "Are you sure you want to delete that?",
                "Delete?", JOptionPane.OK_CANCEL_OPTION);
        if (delete != JOptionPane.YES_OPTION) return;

        animationReplacementRules.remove(index);
        SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
        saveRules();
    }

    public void addNewRule(int index) {
        animationReplacementRules.add(index, AnimationReplacementRule.createTemplate(AnimationSet.animationSets.get(0)));
        SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
        saveRules();
    }

    public void moveRule(int index, boolean up) {
        if ((!up && index == animationReplacementRules.size() - 1) || (up && index == 0)) return;
        AnimationReplacementRule animationReplacementRule = animationReplacementRules.remove(index);
        animationReplacementRules.add(index + (up ? -1 : 1), animationReplacementRule);
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
        ArrayList<AnimationReplacementRule> animationReplacementRules = new ArrayList<>(this.animationReplacementRules);
        Collections.reverse(animationReplacementRules);
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

    int animationToDemo = -1;
    int demoAnimation = -1;
    int lastDemoAnimation = -1;
    boolean animationDemod = false;

    int lastLogAnimation = -1;
    @Subscribe
    public void onClientTick(ClientTick event)
    {
        Player localPlayer = client.getLocalPlayer();
        if (localPlayer.getAnimation() == -1) {
            if (!animationDemod) {
                demoAnimation = animationToDemo;
                animationDemod = true;
                localPlayer.setActionFrame(0);
            } else {
                demoAnimation = -1;
            }
            if (demoAnimation != -1) localPlayer.setAnimation(demoAnimation);
        }
        lastDemoAnimation = localPlayer.getAnimation();

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
                    System.out.println(playerPoseAnimation + " (pose)");
                    poseToUse = -1;
                }
            }
        }
        lastPoseAnimation = playerPoseAnimation;
        if (poseToUse != -1) localPlayer.setPoseAnimation(poseToUse);

        Player logPlayer = logName == null ? localPlayer : client.getPlayers().stream().filter(p -> logName.equalsIgnoreCase(p.getName())).findFirst().orElse(null);
        if (logPlayer != null) {
            logPoseAnimationChanges(logPlayer);
            int logPlayerAnimation = logPlayer.getAnimation();
            if (lastLogAnimation != logPlayerAnimation) {
                if (log && logPlayerAnimation != -1) {
                    Integer equippedWeapon = getEquippedWeaponOnLoggedPlayer();
                    System.out.println("zz_" + (equippedWeapon == null ? "null" : itemManager.getItemComposition(equippedWeapon).getName()));
                    System.out.println("zz_" + "ATTACK_ZZZ, " + logPlayerAnimation + ",");
                }
            }
            lastLogAnimation = logPlayerAnimation;
        } else {
            lastPlayerPoseAnimations = null;
            lastLogAnimation = -2;
        }

        int playerAnimation = localPlayer.getAnimation();
        if (lastAnimation != playerAnimation) {
            Integer animationReplace = getReplacementAnimation(playerAnimation);
            if (animationReplace != null) {
                animationToUse = animationReplace;
                System.out.println("replaced animation " + playerAnimation + " with " + animationReplace);
            } else {
                System.out.println(playerAnimation + " (animation)");
                animationToUse = -1;
            }
        }
        lastAnimation = playerAnimation;
        if (animationToUse != -1) {
            localPlayer.setAnimation(animationToUse);
        }
    }

    private List<Integer> lastPlayerPoseAnimations = Collections.emptyList();
    private void logPoseAnimationChanges(Player localPlayer) {
        if (localPlayer == null) return;
        ArrayList<Integer> newPoses = new ArrayList<>();
        newPoses.add(localPlayer.getRunAnimation());
        newPoses.add(localPlayer.getWalkAnimation());
        newPoses.add(localPlayer.getIdlePoseAnimation());
        newPoses.add(localPlayer.getWalkRotate180());
        newPoses.add(localPlayer.getIdleRotateLeft());
        newPoses.add(localPlayer.getWalkRotateLeft());
        newPoses.add(localPlayer.getWalkRotateRight());

        if (!newPoses.equals(lastPlayerPoseAnimations)) {
            Integer equippedWeapon = getEquippedWeaponOnLoggedPlayer();
            if (log) {
                System.out.println("zz_" + (equippedWeapon == null ? "no weapon" : itemManager.getItemComposition(equippedWeapon).getName()));
                System.out.println("zz_" + "STAND, " + localPlayer.getIdlePoseAnimation() + ",");
                System.out.println("zz_" + "RUN, " + localPlayer.getRunAnimation() + ",");
                System.out.println("zz_" + "WALK, " + localPlayer.getWalkAnimation() + ",");
                System.out.println("zz_" + "WALK_BACKWARD, " + localPlayer.getWalkRotate180() + ",");
                System.out.println("zz_" + "SHUFFLE_LEFT, " + localPlayer.getWalkRotateLeft() + ",");
                System.out.println("zz_" + "SHUFFLE_RIGHT, " + localPlayer.getWalkRotateRight() + ",");
                System.out.println("zz_" + "ROTATE, " + localPlayer.getIdleRotateLeft() + ",");
                if (localPlayer.getIdleRotateLeft() != localPlayer.getIdleRotateRight()) {
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                    System.out.println("ALKDJSFLAKJSDL:AKSJD:ALKSJD:LASKJD:LAKSJD");
                }
            }
//            System.out.println(newPoses);
            lastPlayerPoseAnimations = newPoses;
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

//        System.out.println("replacements: " + replacements);
        for (AnimationReplacement replacement : replacements) {
//            System.out.println("in loop: " + replacement);
            currentSet.applyReplacement(replacement);
            if (replacement.animationtypeToReplace.appliesTo(AnimationType.MOVEMENT)) {
                currentSet.useWalkOrRunForShuffleAndWalkBackwards = replacement.animationSet.useWalkOrRunForShuffleAndWalkBackwards;
            }
//            System.out.println("animation set update: " + currentSet + " " + appliedRules + " " + replacements);
        }

        if (!currentSet.equals(lastAnimationSet)) {
//            System.out.println("animation set change: " + currentSet + " " + appliedRules + " " + replacements);
            lastAnimationSet = currentSet;
        }
        return currentSet;
    }

    private Integer getEquippedWeapon() {
        ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
        if (equipmentContainer == null) return null;
        Item[] equippedItems = equipmentContainer.getItems();
        if (equippedItems.length < EquipmentInventorySlot.WEAPON.getSlotIdx()) return null;
        return equippedItems[EquipmentInventorySlot.WEAPON.getSlotIdx()].getId();
    }

    private Integer getEquippedWeaponOnLoggedPlayer() {
        Integer weaponItemId;
        if (logName != null) {
            Player logPlayer = client.getPlayers().stream().filter(p -> logName.equalsIgnoreCase(p.getName())).findFirst().orElse(null);
            weaponItemId = logPlayer != null ? logPlayer.getPlayerComposition().getEquipmentIds()[3] - 512 : -1;
        } else {
            ItemContainer equipmentContainer = client.getItemContainer(InventoryID.EQUIPMENT);
            if (equipmentContainer == null) return null;
            Item[] equippedItems = equipmentContainer.getItems();
            if (equippedItems.length < EquipmentInventorySlot.WEAPON.getSlotIdx()) return null;
            weaponItemId = equippedItems[EquipmentInventorySlot.WEAPON.getSlotIdx()].getId();
        }
        return weaponItemId;
    }

    private Integer getReplacementAnimation(int playerPoseAnimation) {
        AnimationSet currentAnimationSet = getCurrentAnimationSet();
        if (currentAnimationSet == null) {
//            System.out.println("1");
            return null;
        }
        AnimationType type = getType(playerPoseAnimation);
//        System.out.println("type: " + type);
        if (type != null) {
            return currentAnimationSet.getAnimation(type, isRunning());
        } else {
            for (AnimationSet animationSet : AnimationSet.animationSets) {
                type = animationSet.getType(playerPoseAnimation);
                if (type != null) {
//                    System.out.println(playerPoseAnimation + " type " + type + " not null for " + animationSet.name);
                    return currentAnimationSet.getAnimation(type, isRunning());
                }
            }
        }
        return null;
    }

    /**
     * @return Whether running animations or walking animations should be used.
     */
    private boolean isRunning() {
        return calculateMovingOneTile() ? false : client.getVarpValue(173) == 1;
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

    private boolean log = false;
    private String logName = null;

    @Subscribe
    public void onCommandExecuted(CommandExecuted commandExecuted) {
        if ("wal".equals(commandExecuted.getCommand())) {
            if (commandExecuted.getArguments().length >= 1) {
                log = true;
                logName = String.join(" ", commandExecuted.getArguments());
                System.out.println("following " + logName);
            } else {
                logName = null;
                log = !log;
                System.out.println("logging is now " + (log ? "on" : "off"));
            }
            lastPlayerPoseAnimations = null;
            lastLogAnimation = -2;
        }
        if ("dumpanim".equals(commandExecuted.getCommand())) {
            System.out.println(
            client.getLocalPlayer().getRunAnimation() + " " +
            client.getLocalPlayer().getWalkAnimation() + " " +
            client.getLocalPlayer().getIdlePoseAnimation() + " " +
            client.getLocalPlayer().getWalkRotate180() + " " +
            client.getLocalPlayer().getIdleRotateLeft() + " " +
            client.getLocalPlayer().getWalkRotateLeft() + " " +
            client.getLocalPlayer().getWalkRotateRight()
            );
        }
    }

    @Inject
    private TransmogrificationManager transmogManager;

    private boolean isMovingOneTile = false;

    @Subscribe
    public void onGameTick(GameTick e)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }

//        isMovingOneTile = calculateMovingOneTile();
//        System.out.println("moving one tile: " + isMovingOneTile);
//
        // On most teleports, the player kits are reset. This will reapply the transmog if needed.
        final int currentHash = Arrays.hashCode(client.getLocalPlayer().getPlayerComposition().getEquipmentIds());
        if (currentHash != transmogManager.getTransmogHash())
        {
            transmogManager.reapplyTransmog();
        }
    }

    private boolean calculateMovingOneTile() {
        LocalPoint destination = client.getLocalDestinationLocation();
        WorldPoint playerPos = client.getLocalPlayer().getWorldLocation();
        System.out.println(destination + " " + playerPos);
        if (destination == null) return false;
        if (playerPos == null) return false;
        LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
        System.out.println(playerPosLocal);
        if (playerPosLocal == null) return false;
        int xDiff = Math.abs(playerPosLocal.getSceneX() - destination.getSceneX());
        int yDiff = Math.abs(playerPosLocal.getSceneY() - destination.getSceneY());
        System.out.println(xDiff + " " + yDiff + " " + !(xDiff > 1 || yDiff > 1 || (xDiff == 0 && yDiff == 0)));
        return !(xDiff > 1 || yDiff > 1 || (xDiff == 0 && yDiff == 0));
    }

    public void doItemSearch(AnimationReplacementRulePanel.ItemSelectionButton button, Consumer<Integer> onItemChosen) {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            JOptionPane.showMessageDialog(pluginPanel,
                    "This plugin uses the in-game item search panel; you must be logged in to use this.",
                    "Log in to choose items",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        itemSearch
                .tooltipText("select")
                .onItemSelected((itemId) ->
                        clientThread.invokeLater(() ->
                        {
                            button.setItem(itemId);

                            onItemChosen.accept(itemId);
                        }))
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

    public void demoAnimation(Integer animation) {
        client.getLocalPlayer().setAnimation(animation);
        animationToDemo = animation;
        animationDemod = false;
    }
}

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
import static com.weaponanimationreplacer.Constants.mapNegativeId;
import com.weaponanimationreplacer.Swap.AnimationReplacement;
import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.MOVEMENT;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.JagexColor;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.kit.KitType;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;

@Slf4j
@PluginDescriptor(
        name = "Weapon/Gear/Anim Replacer",
        description = "replace weapon animations (stand,walk,run,attack) with other ones. Config is in a plugin panel.",
        tags = {"transmog", "fashionscape"}
)
public class WeaponAnimationReplacerPlugin extends Plugin {

    @Inject
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
	ConfigManager configManager;

	@Inject
	public ColorPickerManager colorPickerManager;

	@Inject
	private EventBus eventBus;

	private WeaponAnimationReplacerPluginPanel pluginPanel;
    private NavigationButton navigationButton;

	private static final String TRANSMOG_SET_KEY = "transmogSets";
	private static final String GROUP_NAME = "WeaponAnimationReplacer";

	@Getter
	private List<TransmogSet> transmogSets;

	@Inject
	private TransmogrificationManager transmogManager;

	@Override
    protected void startUp()
    {
		migrate();

        try {
            transmogSets = getTransmogSetsFromConfig();
        } catch (JsonParseException | IllegalStateException e) {
            log.error("issue parsing json: " + configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY), e);
            transmogSets = new ArrayList<>();
        }

        pluginPanel = new WeaponAnimationReplacerPluginPanel(this);
        pluginPanel.rebuild();

        final BufferedImage icon = ImageUtil.loadImageResource(WeaponAnimationReplacerPlugin.class, "panel_icon.png");

        navigationButton = NavigationButton.builder()
                .tooltip("Weapon Animation Replacer")
                .icon(icon)
                .priority(5)
                .panel(pluginPanel)
                .build();

        clientToolbar.addNavigation(navigationButton);
    }

	private void migrate()
	{
		if (configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY) != null) return;

		String configuration = configManager.getConfiguration("WeaponAnimationReplacer", "rules");
		if (configuration == null) return; // do nothing. No existing rules, nothing to convert to new format.

		List<TransmogSet> transmogSets = migrate(configuration);

		this.transmogSets = transmogSets;
		saveTransmogSets();
		configManager.setConfiguration(GROUP_NAME, "rulesbackup", configuration); // just in case!
		configManager.unsetConfiguration(GROUP_NAME, "rules");
	}

	static List<TransmogSet> migrate(String config)
	{
		List<AnimationReplacementRule_OLD> rules = customGson.fromJson(config, new TypeToken<ArrayList<AnimationReplacementRule_OLD>>() {}.getType());
		List<TransmogSet> transmogSets = new ArrayList<>();
		for (AnimationReplacementRule_OLD rule : rules)
		{
			TransmogSet transmogSet = new TransmogSet(
				Collections.singletonList(
					new Swap(
						rule.itemRestrictions.stream().map(r -> r.itemId).collect(Collectors.toList()),
						Collections.singletonList(rule.modelSwap),
						rule.animationReplacements,
						Collections.emptyList())));
			transmogSet.setName(rule.name);
			transmogSets.add(transmogSet);
		}
		return transmogSets;
	}

	@Override
	protected void shutDown() {
        pluginPanel = null;

        transmogManager.shutDown();

		for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
		{
			animation.setAnimation(client.getLocalPlayer(), naturalPlayerPoseAnimations.get(animation.ordinal()));
		}

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
                    String s = jsonElement.getAsString();
                    if ("Godsword".equals(s)) {
                        log.debug("updating \"Godsword\" to \"Godsword (Armadyl)\"");
                        s = "Godsword (Armadyl)";
                    }
                    AnimationSet animationSet = AnimationSet.getAnimationSet(s);
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

	public List<TransmogSet> getTransmogSetsFromConfig() {
        String configuration = configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY);
        if (configuration == null) return getDefaultTransmogSets();
		if (configuration.startsWith("NOT_JSON")) {
			configuration = configuration.substring("NOT_JSON".length());
		}
		return customGson.fromJson(configuration, new TypeToken<ArrayList<TransmogSet>>() {}.getType());
    }

    public void saveTransmogSets() {
    	// Runelite won't store config values that are valid json with a nested depth of 8 or higher. Adding "NOT_JSON"
		// makes the string not be valid json, circumventing this.
        String s = "NOT_JSON" + customGson.toJson(transmogSets);
        configManager.setConfiguration(GROUP_NAME, TRANSMOG_SET_KEY, s);
    }

	/**
	 * Saves transmog sets to config and reapplies transmog and animations.
	 */
	public void updateAnimationsAndTransmog() {
        saveTransmogSets();

        transmogManager.applyTransmog();

		boolean animationSetChanged = updateCurrentAnimationSet();
		if (animationSetChanged) {
			setPlayerPoseAnimations();
		}

        clientUI.requestFocus();
    }

    public void deleteTransmogSet(int index) {
        int delete = JOptionPane.showConfirmDialog(pluginPanel,
                "Are you sure you want to delete that?",
                "Delete?", JOptionPane.OK_CANCEL_OPTION);
        if (delete != JOptionPane.YES_OPTION) return;

		clientThread.invokeLater(() -> {
			transmogSets.remove(index);
			SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
			saveTransmogSets();
			updateAnimationsAndTransmog();
		});
    }

    public void addNewTransmogSet(int index) {
        transmogSets.add(index, TransmogSet.createTemplate());
        SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
        saveTransmogSets();
    }

    public void moveTransmogSet(int index, boolean up) {
        if ((!up && index == transmogSets.size() - 1) || (up && index == 0)) return;
        TransmogSet swap = transmogSets.remove(index);
        transmogSets.add(index + (up ? -1 : 1), swap);
        SwingUtilities.invokeLater(() -> pluginPanel.rebuild());
        saveTransmogSets();
    }

    public static List<TransmogSet> getDefaultTransmogSets() {
        String configuration = "[{\"name\":\"Monkey run\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Cursed banana\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Elder Maul Scythe\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[22324,4151,12006,4587,24551],\"modelSwaps\":[22325],\"animationReplacements\":[{\"animationSet\":\"Elder maul\",\"animationtypeToReplace\":\"ALL\"},{\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SLASH\",\"id\":8056}}],\"graphicEffects\":[{\"type\":\"SCYTHE_SWING\",\"color\":{\"value\":-4030079,\"falpha\":0.0}}]}]},{\"name\":\"Shoulder Halberd\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[3204,23987],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Dharok's greataxe\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}],\"graphicEffects\":[]}]},{\"name\":\"Saeldor Slash\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[24551,23995,23997],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Inquisitor's mace\",\"animationtypeToReplace\":\"ATTACK_SLASH\",\"animationtypeReplacement\":{\"type\":\"ATTACK_CRUSH\",\"id\":4503}}],\"graphicEffects\":[]}]},{\"name\":\"Rich voider\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[8839,8840,8842,11664,13072,13073],\"modelSwaps\":[11826,11828,11830,7462,13237,22249,21898],\"animationReplacements\":[],\"graphicEffects\":[]}]}]";
        return customGson.fromJson(configuration, new TypeToken<ArrayList<TransmogSet>>() {}.getType());
    }

	private void swapPlayerAnimation()
	{
		Player player = client.getLocalPlayer();
		int playerAnimation = player.getAnimation();
		AnimationSet.Animation animationReplacement = getReplacementAttackAnimation(playerAnimation);
		if (animationReplacement != null)
		{
			log.debug("replaced animation " + playerAnimation + " with " + animationReplacement);
			player.setAnimation(animationReplacement.id);

			if (AnimationType.ATTACK.appliesTo(animationReplacement.type) && currentScytheGraphicEffect != null)
			{
				scytheSwingCountdown = 20;
			}
		}
	}

	int scytheSwingCountdown = -1;
	@Subscribe
    public void onClientTick(ClientTick event)
    {
		if (scytheSwingCountdown == 0) {
			createScytheSwing();
		} else {
			scytheSwingCountdown--;
		}
    }

	private void createScytheSwing()
	{
		scytheSwingCountdown = -1;

		WorldPoint point = client.getLocalPlayer().getWorldLocation();
		Actor interacting = client.getLocalPlayer().getInteracting();

		int x = 0, y = 0;
		int id;

		// I know this can happen if you're attacking a target dummy in varrock, probably also in the poh.
		if (interacting == null || !(interacting instanceof NPC)) {
			int orientation = client.getLocalPlayer().getOrientation();
			// 70 is just a number I felt might work nice here.
			if (orientation > 512 - 70 && orientation < 512 + 70) {
				x = -1;
				id = 4006;
			} else if (orientation > 1536 - 70 && orientation < 1536 + 70) {
				x = 1;
				id = 4003;
			} else if (orientation > 512 && orientation < 1536) {
				y = 1;
				id = 4004;
			} else {
				y = -1;
				id = 4005;
			}
		}
		else
		{
			WorldPoint targetPoint = interacting.getWorldLocation();
			int targetSize = ((NPC) interacting).getTransformedComposition().getSize();

			int halfTargetSizeRoundedDown = (targetSize - 1) / 2;
			int playerx = point.getX(), playery = point.getY();
			int npcw = targetPoint.getX(), npcn = targetPoint.getY() + targetSize - 1, npce = targetPoint.getX() + targetSize - 1, npcs = targetPoint.getY();
			boolean directwest = playerx == npcw - 1 && playery == npcs + halfTargetSizeRoundedDown;
			boolean directeast = playerx == npce + 1 && playery == npcs + halfTargetSizeRoundedDown;
			if (directwest) {
				x = 1;
				id = 4003;
			} else if (directeast) {
				x = -1;
				id = 4006;
			} else if (playery >= npcs + halfTargetSizeRoundedDown) {
				y = -1;
				id = 4005;
			} else {
				y = 1;
				id = 4004;
			}
		}

		point = new WorldPoint(point.getX() + x, point.getY() + y, point.getPlane());

		RuneLiteObject runeLiteObject = client.createRuneLiteObject();
		Color scytheSwingColor = currentScytheGraphicEffect != null ? currentScytheGraphicEffect.color : null;
		if (scytheSwingColor != null)
		{
			runeLiteObject.setModel(client.loadModel(
				id,
				new short[]{960},
				new short[]{JagexColor.rgbToHSL(scytheSwingColor.getRGB(), 1.0d)}
			));
		} else {
			runeLiteObject.setModel(client.loadModel(id));
		}

		runeLiteObject.setAnimation(client.loadAnimation(1204));
		LocalPoint localPoint = LocalPoint.fromWorld(client, point);
		runeLiteObject.setLocation(localPoint, client.getPlane());
		// TODO should I set these to inactive at some point?
		runeLiteObject.setActive(true);
	}

	private AnimationSet currentAnimationSet = new AnimationSet();
	private GraphicEffect currentScytheGraphicEffect = null;
    private boolean updateCurrentAnimationSet() { // TODO cache maybe based on the current gear.
        AnimationSet currentSet = new AnimationSet();

        if (recordingOwnGearSlotOverrides)
		{
			currentAnimationSet = currentSet;
			currentScytheGraphicEffect = null;
			return true;
		}

		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		List<Integer> equippedItemIds = itemContainer == null ? Collections.emptyList() : Arrays.stream(itemContainer.getItems()).filter(item -> item.getId() >= 0).map(Item::getId).collect(Collectors.toList());
		List<Swap> matchingSwaps = transmogSets.stream()
			.filter(TransmogSet::isEnabled)
			.flatMap(set -> set.getSwaps().stream())
			.filter(swap -> swap.appliesToGear(equippedItemIds, getSlot))
			.collect(Collectors.toList());

		List<AnimationReplacement> replacements = matchingSwaps.stream()
			.flatMap(swap -> swap.animationReplacements.stream())
			.filter(replacement -> replacement.animationSet != null && replacement.animationtypeToReplace != null)
			.sorted()
			.collect(Collectors.toList());

		for (int i = replacements.size() - 1; i >= 0; i--)
		{
			AnimationReplacement replacement = replacements.get(i);
			currentSet.applyReplacement(replacement);
			if (replacement.animationtypeToReplace.appliesTo(MOVEMENT)) {
				currentSet.useWalkOrRunForShuffleAndWalkBackwards = replacement.animationSet.useWalkOrRunForShuffleAndWalkBackwards;
			}
		}

		boolean animationSetChanged = !currentSet.equals(currentAnimationSet);
		if (animationSetChanged)
		{
			log.debug("animation set change: " + currentSet + " " + currentAnimationSet + " " + replacements);
		}

		currentAnimationSet = currentSet;

		currentScytheGraphicEffect = matchingSwaps.stream()
			.filter(swap -> swap.getGraphicEffects().stream().anyMatch(e -> e.type == GraphicEffect.Type.SCYTHE_SWING))
			.flatMap(swap -> swap.getGraphicEffects().stream())
			.findAny().orElse(null);

		return animationSetChanged;
    }

	public String itemName(Integer itemId)
	{
		if (itemId == null) return "\"null\"";
		String s = (itemId == null) ? "\"null\"" : "\"" + itemManager.getItemComposition(itemId).getName() + "\"";
		if (s.equals("\"null\"")) {
			s = Integer.toString(itemId);
		}
		return s;
	}

	@Getter
	private boolean recordingOwnGearSlotOverrides = false;
	public void toggleRecordingOwnGearSlotOverrides() {
    	recordingOwnGearSlotOverrides = !recordingOwnGearSlotOverrides;
    	updateAnimationsAndTransmog();
	}

	private final List<Integer> naturalPlayerPoseAnimations = new ArrayList<>();

	private void recordNaturalPlayerPoseAnimations()
	{
		naturalPlayerPoseAnimations.clear();
		Player player = client.getLocalPlayer();
		for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
		{
			naturalPlayerPoseAnimations.add(animation.getAnimation(player));
		}
	}

	/**
	 * Sets the player's pose animations (idle/walk/run/etc.). Also sets the player's current pose animation.
	 */
	private void setPlayerPoseAnimations()
	{
		// TODO setting pose animations on equipment swap, if the equipment swap does not trigger new animations, will reset the animation frame. This is a minor graphical bug. It would require additional API from RuneLite to fix.
		// TODO I bet this could also trigger the player disappearing because frame counter is too high.

		Player player = client.getLocalPlayer();
		if (player == null) return;

		Widget equipmentStatsWidget = client.getWidget(84, 0);
		if (equipmentStatsWidget == null || equipmentStatsWidget.isHidden())
		{
			// Doing this while the equipment stats window is open can cause a game crash.
			// TODO confirm that this is true and the bug is fixed.

			// It is necessary to update the player animation via Actor#setPoseAnimation, since the animations set by setPlayerPoseAnimations won't take effect for 1 client tick.
			AnimationType type = getPoseAnimationType(player.getPoseAnimation());
			if (type == null)
			{
				log.debug("null type for " + player.getPoseAnimation() + ", is this animation not recorded in AnimationSet?");
				return;
			}
			Integer poseAnimationId = currentAnimationSet.getAnimation(type, client.getVarpValue(173) == 1);
			if (poseAnimationId != null) player.setPoseAnimation(poseAnimationId);
		}

		for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
		{
			Integer animationId = currentAnimationSet.getAnimation(animation.getType());
			if (animationId == null) animationId = naturalPlayerPoseAnimations.get(animation.ordinal());
			animation.setAnimation(player, animationId);
		}
	}

	private AnimationSet.Animation getReplacementAttackAnimation(int animation) {
		for (AnimationSet animationSet : AnimationSet.animationSets) {
			AnimationType type = animationSet.getType(animation);
			if (type != null) {
				Integer replacementAnim = currentAnimationSet.getAnimation(type);
				if (replacementAnim == null) return null;
				return new AnimationSet.Animation(type, replacementAnim, null);
			}
		}
        return null;
    }

    /**
     * Gets the type of animation. Only usable on movement/idle animations that the local player is currently using.
     */
	// TODO ask abex for API that does this. The way this method currently works, there are issues when certain animation sets don't have unique animations for certain animation types, resulting in the wrong animation being used for one client tick after swapping gear (haven't seen this, just assuming it would happen).
	private AnimationType getPoseAnimationType(int animationId) {
        Player localPlayer = client.getLocalPlayer();
		for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
		{
			if (animation.getAnimation(localPlayer) == animationId) return animation.getType();
		}
        return null;
    }

	@Subscribe
	public void onAnimationChanged(AnimationChanged e)
	{
		Player player = client.getLocalPlayer();
		if (!e.getActor().equals(player)) return;

		handleAnimationChangedInGameTick = client.getTickCount();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged e)
	{
		if (e.getContainerId() != InventoryID.EQUIPMENT.getId()) return;

		recordNaturalPlayerPoseAnimations();
		handleEquipmentChangeInGameTick = client.getTickCount();
	}

	private int handleAnimationChangedInGameTick = -1;
	private int handleEquipmentChangeInGameTick = -1;

	@Subscribe(priority = -1000.0f) // I want to run late, so that plugins that need animation changes don't see my changed animation ids, since mine are cosmetic and don't give information on what the player is actually doing.
    public void onGameTick(GameTick e)
    {
        if (client.getLocalPlayer() == null)
        {
            return;
        }

        /*
         * The normal order onAnimationChanged, then onItemContainerChanged. I need the reverse order.
         * onGameTick runs after both of these, and runs every game tick, so this is the ideal time to handle both.
         */
		if (handleEquipmentChangeInGameTick == client.getTickCount()) {
			updateCurrentAnimationSet();
		}
        if (handleAnimationChangedInGameTick == client.getTickCount()) {
			swapPlayerAnimation();
		}

		// On most teleports, the player kits and animations are reset. So, set the correct value.
        final int currentHash = Arrays.hashCode(client.getLocalPlayer().getPlayerComposition().getEquipmentIds());
        if (currentHash != transmogManager.getTransmogHash())
        {
            transmogManager.reapplyTransmog();
        }

		if (naturalPlayerPoseAnimations.isEmpty()) { // required for when the player logs in.
			recordNaturalPlayerPoseAnimations();
		}
		// Is calling this every game tick bad, due to its pose animation forcing? This forcing was only mean for animation set changes.
		// Answer: No, nothing bad happens, it's just a waste of processing power.
		setPlayerPoseAnimations();
    }

    public void doItemSearch(TransmogSetPanel.ItemSelectionButton button, Consumer<Integer> onItemChosen) {
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
                .onItemSelected((itemId) -> {
                        clientThread.invokeLater(() ->
                        {
                            if (button != null) button.setItem(itemId);

                            onItemChosen.accept(itemId);
                        });})

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
        } else if (event.getGameState() == GameState.LOGGED_IN) {
        	updateCurrentAnimationSet();
			naturalPlayerPoseAnimations.clear();
		}
	}

	public void demoAnimation(Integer animation) {
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			player.setAnimation(animation);
			player.setAnimationFrame(0);
		}
    }

	private static final int DEFAULT_MALE_ARMS = 256 + 28;
	private static final int DEFAULT_FEMALE_ARMS = 256 + 64;
	private static final int DEFAULT_MALE_HAIR = 256 + 0;
	private static final int DEFAULT_FEMALE_HAIR = 256 + 45;
	private static final int DEFAULT_MALE_JAW = 256 + 14;

	private final Function<Integer, Integer> getSlot = i -> {
		ItemStats itemStats = itemManager.getItemStats(i, false);
		if (itemStats == null) return null;
		ItemEquipmentStats equipment = itemStats.getEquipment();
		if (equipment == null) return null;
		return equipment.getSlot();
	};

	public Map<Integer, Integer> getApplicableModelSwaps()
	{
		if (recordingOwnGearSlotOverrides) return new HashMap<>();

		ItemContainer itemContainer = client.getItemContainer(InventoryID.EQUIPMENT);
		List<Integer> equippedItemIds = itemContainer == null ? Collections.emptyList() : Arrays.stream(itemContainer.getItems()).filter(item -> item.getId() >= 0).map(Item::getId).collect(Collectors.toList());
		List<Swap> swaps = transmogSets.stream()
			.filter(TransmogSet::isEnabled)
			.flatMap(set -> set.getSwaps().stream())
			.filter(swap -> swap.appliesToGear(equippedItemIds, getSlot))
			.collect(Collectors.toList());

		Map<Integer, Integer> genericTransmog = new HashMap<>();
		Map<Integer, Integer> specificTransmog = new HashMap<>();
		for (Swap swap : swaps)
		{
			Map<Integer, Integer> transmogMap = swap.appliesSpecificallyToGear(equippedItemIds, getSlot) ? specificTransmog : genericTransmog;
			for (Integer modelSwap : swap.getModelSwaps())
			{
				int slot;
				if (modelSwap < 0) {
					Constants.NegativeId negativeId = mapNegativeId(modelSwap);
					if (negativeId.type == Constants.NegativeIdsMap.HIDE_SLOT) {
						modelSwap = 0;
						slot = negativeId.id;
					}
					else if (negativeId.type == Constants.NegativeIdsMap.SHOW_SLOT) {
						modelSwap =
							negativeId.id == KitType.ARMS.getIndex() ? (TransmogrificationManager.baseArmsKit == -1 ? (client.getLocalPlayer().getPlayerComposition().isFemale() ? DEFAULT_FEMALE_ARMS : DEFAULT_MALE_ARMS) : TransmogrificationManager.baseArmsKit) :
							negativeId.id == KitType.HAIR.getIndex() ? (TransmogrificationManager.baseHairKit == -1 ? (client.getLocalPlayer().getPlayerComposition().isFemale() ? DEFAULT_FEMALE_HAIR : DEFAULT_MALE_HAIR) : TransmogrificationManager.baseHairKit) :
							(TransmogrificationManager.baseJawKit == -1 ? (client.getLocalPlayer().getPlayerComposition().isFemale() ? 0 : DEFAULT_MALE_JAW) : TransmogrificationManager.baseJawKit)
							;
						slot = negativeId.id;
						modelSwap -= 512;
					}
					else
					{
						continue;
					}
				}
				else
				{
					ItemStats itemStats = itemManager.getItemStats(modelSwap, false);
					if (itemStats == null || !itemStats.isEquipable())
					{
						if (Constants.equippableItemsNotMarkedAsEquipment.containsKey(modelSwap)) {
							slot = Constants.equippableItemsNotMarkedAsEquipment.get(modelSwap);
						} else {
							continue;
						}
					} else {
						ItemEquipmentStats stats = itemStats.getEquipment();
						slot = stats.getSlot();
					}

					if (Constants.JAW_SLOT.contains(modelSwap))
					{
						slot = 11;
					}
				}

				if (!transmogMap.containsKey(slot)) {
					transmogMap.put(slot, modelSwap);
				}
			}
		}

		for (Map.Entry<Integer, Integer> entry : specificTransmog.entrySet())
		{
			genericTransmog.put(entry.getKey(), entry.getValue());
		}

		return genericTransmog;
	}
}

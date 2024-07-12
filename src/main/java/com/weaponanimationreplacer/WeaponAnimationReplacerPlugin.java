package com.weaponanimationreplacer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Provides;
import com.weaponanimationreplacer.ChatBoxFilterableSearch.SelectionResult;
import static com.weaponanimationreplacer.Constants.NegativeIdsMap.HIDE_SLOT;
import static com.weaponanimationreplacer.Constants.NegativeIdsMap.SHOW_SLOT;
import static com.weaponanimationreplacer.Constants.WEAPON_SLOT;
import static com.weaponanimationreplacer.Constants.mapNegativeId;
import com.weaponanimationreplacer.Swap.AnimationReplacement;
import com.weaponanimationreplacer.Swap.AnimationType;
import static com.weaponanimationreplacer.Swap.AnimationType.ALL;
import static com.weaponanimationreplacer.Swap.AnimationType.ATTACK;
import com.weaponanimationreplacer.Swap.SoundSwap;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.MODEL_SWAP;
import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.SearchType.SPELL_R;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import lombok.Getter;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Actor;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.JagexColor;
import net.runelite.api.Model;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import static net.runelite.api.PlayerComposition.ITEM_OFFSET;
import net.runelite.api.Projectile;
import net.runelite.api.RuneLiteObject;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.InteractingChanged;
import net.runelite.api.events.PlayerChanged;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.api.events.SoundEffectPlayed;
import net.runelite.api.kit.KitType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.game.chatbox.ChatboxPanelManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.ClientUI;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemEquipmentStats;
import net.runelite.http.api.item.ItemStats;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

@Slf4j
@PluginDescriptor(
        name = "Weapon/Gear/Anim Replacer",
        description = "replace weapon animations (stand,walk,run,attack) with other ones. Config is in a plugin panel.",
        tags = {"transmog", "fashionscape"}
)
public class WeaponAnimationReplacerPlugin extends Plugin {

	public static final String GROUP_NAME = "WeaponAnimationReplacer";

	private static final String TRANSMOG_SET_KEY = "transmogSets";

	@Inject Client client;
	@Inject EventBus eventBus;
	@Inject private ChatBoxFilterableSearch itemSearch;
	@Inject private ClientToolbar clientToolbar;
	@Inject private ConfigManager configManager;
	@Inject private TransmogrificationManager transmogManager;
	@Inject Gson runeliteGson;
	@Inject ClientUI clientUI;
	@Inject ItemManager itemManager;
	@Inject private SpriteManager spriteManager;
	@Inject ClientThread clientThread;
	@Inject ColorPickerManager colorPickerManager;
	@Inject private ChatboxPanelManager chatboxPanelManager;
	@Inject private WeaponAnimationReplacerConfig config;
	@Inject private OkHttpClient okHttpClient;

	@Getter
	List<TransmogSet> transmogSets = null;

	WeaponAnimationReplacerPluginPanel pluginPanel;
	private NavigationButton navigationButton;

	boolean debug = false;

	/**
	 * This is updated earlier than the player's equipment inventory. It uses the kit data, so it will have some negative numbers in it if there is no gear in that slot, or it is a jaw/hair/arms or something like that.
	 */
	private List<Integer> equippedItemsFromKit = new ArrayList<>();
	private final List<Integer> naturalPlayerPoseAnimations = new ArrayList<>();
	private AnimationReplacements currentAnimations = new AnimationReplacements();
	private List<ProjectileSwap> projectileSwaps = Collections.emptyList();
	private List<SoundSwap> soundSwaps = new ArrayList<>();
	private GraphicEffect currentScytheGraphicEffect = null;
	int scytheSwingCountdown = -1;
	int delayedGfxToApply = -1;
	int delayedGfxHeightToApply = -1;
	Actor actorToApplyDelayedGfxTo = null;
	int timeToApplyDelayedGfx = -1;
	// For handling spells that have no projectiles which are harder to identify. This must be toggled off in onProjectileMoved the the spell is replaced there.
	private boolean handlePossibleNoProjectileSpellInClientTick = false;

	int previewItem = -1;
	AnimationReplacements previewAnimationReplacements = null;

	private Gson customGson = null; // Lazy initialized due to timing of @Injected runeliteGson and not being able to use constructor injection.
	@Inject private ScheduledExecutorService executor;

	Gson getGson()
	{
		if (customGson != null) return customGson;

		GsonBuilder gsonBuilder = runeliteGson.newBuilder();

		// Do not serialize empty maps.
		// Map must not use generics or the serializer will not be called.
		gsonBuilder.registerTypeAdapter(new TypeToken<Map>() {}.getType(), new JsonSerializer<Map<?, ?>>() {
			@Override
			public JsonElement serialize(Map<?, ?> map, Type typeOfSrc, JsonSerializationContext context) {
				if (map.isEmpty()) return null;

				JsonObject object = new JsonObject();
				for (Map.Entry<?, ?> entry : map.entrySet())
				{
					JsonElement element = context.serialize(entry.getValue());
					object.add(String.valueOf(entry.getKey()), element);
				}
				return object;
			}
		});
		// Do not serialize empty lists.
		// List must not use generics or the serializer will not be called.
		gsonBuilder.registerTypeAdapter(new TypeToken<List>() {}.getType(), new JsonSerializer<List<?>>() {
			@Override
			public JsonElement serialize(List<?> list, Type typeOfSrc, JsonSerializationContext context) {
				if (list.isEmpty()) return null;

				JsonArray array = new JsonArray();
				for (Object child : list) {
					JsonElement element = context.serialize(child);
					array.add(element);
				}
				return array;
			}
		});
		Type animationSetTypeToken = new TypeToken<AnimationSet>() {}.getType();
		JsonSerializer<AnimationSet> serializer = new JsonSerializer<AnimationSet>() {
			@Override
			public JsonElement serialize(AnimationSet set, Type typeOfSrc, JsonSerializationContext context) {
				return new JsonPrimitive(set.name);
			}
		};
		gsonBuilder.registerTypeAdapter(animationSetTypeToken, serializer);
		JsonDeserializer<AnimationSet> deserializer = new JsonDeserializer<AnimationSet>() {
			@Override
			public AnimationSet deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
				if (jsonElement instanceof JsonPrimitive && ((JsonPrimitive) jsonElement).isString()) {
					String s = jsonElement.getAsString();
					String newS = renames.get(s);
					if (newS != null) {
						log.debug("updating \"" + s + "\" to \"" + newS + "\"");
						s = newS;
					}
					AnimationSet animationSet = AnimationSet.getAnimationSet(s);
					return animationSet;
				} else {
					throw new JsonParseException("animationset is supposed to be a string.");
				}
			}
		};
		gsonBuilder.registerTypeAdapter(animationSetTypeToken, deserializer);
		gsonBuilder.registerTypeAdapter(new TypeToken<AnimationType>(){}.getType(), (JsonDeserializer<AnimationType>) (jsonElement, type, jsonDeserializationContext) -> {
			// there used to be an object here instead of just the enum.
			if (jsonElement instanceof JsonObject) {
				jsonElement = ((JsonObject) jsonElement).get("type");
			}
			return AnimationType.valueOf(jsonElement.getAsString());
		});

		customGson = gsonBuilder.create();
		return customGson;
	}

	@Override
    protected void startUp()
    {
		clientThread.invokeLater(() -> {
			transmogManager.startUp();
			eventBus.register(transmogManager);

			loadAndUpdateData();
			reloadTransmogSetsFromConfig();

			// record player's untransmogged state.
			if (client.getGameState() == GameState.LOGGED_IN) {
				onPlayerChanged(new PlayerChanged(client.getLocalPlayer()));
			}
			else
			{
				equippedItemsFromKit.clear();
				naturalPlayerPoseAnimations.clear();
				currentAnimations = new AnimationReplacements();
			}

			currentScytheGraphicEffect = null;
			scytheSwingCountdown = -1;
			previewItem = -1;
			norecurse = false;

			if (client.getGameState().getState() >= GameState.LOGIN_SCREEN.getState())
			{
				showSidePanel(!config.hideSidePanel());
			}
		});
	}

	private void loadAndUpdateData()
	{
		boolean updateLocalData = false;
		boolean loadingFailure = false;

		Constants.Data bundledData = Constants.getBundledData(runeliteGson);
//		Constants.loadData(bundledData);

		// check filesystem.
		Constants.Data localData = null;
		try {
			byte[] bytes = Files.readAllBytes(Paths.get(System.getProperty("user.home"), ".runelite", "weaponanimationreplacerdata.json"));
			localData = runeliteGson.fromJson(new String(bytes), Constants.Data.class);
		}
		catch (FileNotFoundException | NoSuchFileException e) {
			updateLocalData = true; // This'll make it easy for people to edit if they want to.
		}
		catch (IOException | JsonSyntaxException e) {
			loadingFailure = true;
			clientThread.invoke(() -> client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Weapon/Gear/Anim Replacer: Couldn't load local data, see logs for more info.", ""));
			log.error("Couldn't load local data", e);
		}

		if (updateLocalData || bundledData.version > localData.version) {
			try {
				Files.write(Paths.get(System.getProperty("user.home"), ".runelite", "weaponanimationreplacerdata.json"), runeliteGson.toJson(bundledData).getBytes());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			localData = bundledData;
			log.info("updated local data.");
		}

		Constants.loadData(loadingFailure ? bundledData : localData);

		if (loadingFailure) return; // don't bother updating from online if we already have a broken thing.

		int localVersion = localData.version;
		executor.submit(() -> {
			try (Response res = okHttpClient.newCall(new Request.Builder().url("https://raw.githubusercontent.com/geheur/weapon-animation-replacer/data/dataversion.json").build()).execute()) {
				if (res.code() != 200) {
					log.error("Response code " + res.code());
					return;
				}

				String requestBody = res.body().string();
				int onlineVersion = Integer.parseInt(requestBody.trim());
				log.info("online version is " + onlineVersion + ", local version is " + localVersion);
				if (onlineVersion <= localVersion) return;

				try (Response res2 = okHttpClient.newCall(new Request.Builder().url("https://raw.githubusercontent.com/geheur/weapon-animation-replacer/data/data.json").build()).execute()) {
					String response = res2.body().string();
					Constants.Data onlineData = runeliteGson.fromJson(response, Constants.Data.class);
					if (onlineData.version != onlineVersion) {
						log.warn("online versions do not match.");
						return;
					}

					try {
						Files.write(Paths.get(System.getProperty("user.home"), ".runelite", "weaponanimationreplacerdata.json"), runeliteGson.toJson(onlineData).getBytes());
					} catch (IOException e) {
						e.printStackTrace();
					}
					clientThread.invokeLater(() -> {
						Constants.loadData(onlineData);
						reloadTransmogSetsFromConfig();
						SwingUtilities.invokeLater(() -> {
							if (pluginPanel != null) pluginPanel.rebuild();
						});
					});
				}
			} catch (IOException | JsonSyntaxException e) {
				log.error("error loading online data", e);
			}
		});
	}

	private void migrate()
	{
		String serialVersionString = configManager.getConfiguration(GROUP_NAME, "serialVersion");
		int serialVersion = serialVersionString != null ? Integer.parseInt(serialVersionString) : -1;

		if (serialVersion == -1) {
			// I did a big format change at some point, this handles that.
			if (configManager.getConfiguration("WeaponAnimationReplacer", TRANSMOG_SET_KEY) == null)
			{
				String configuration = configManager.getConfiguration("WeaponAnimationReplacer", "rules");
				if (configuration == null) return; // do nothing. No existing rules, nothing to convert to new format.

				List<TransmogSet> transmogSets = migrate(configuration);

				this.transmogSets = transmogSets;
				saveTransmogSets();
				configManager.setConfiguration(GROUP_NAME, "rulesbackup", configuration); // just in case!
				configManager.unsetConfiguration(GROUP_NAME, "rules");
			}

			// update old stuff for the new sort order and model swap one item per slot.
			updateForSortOrder();
		}

		if (serialVersion <= 1) {
			// I accidentally put the replacement animation as ALL in the auto animation swaps, when it should be null.
			fixBadAutoAnimationReplacements();
		}

		configManager.setConfiguration(GROUP_NAME, "serialVersion", 2);
	}

	private void fixBadAutoAnimationReplacements()
	{
		List<TransmogSet> transmogSets;
		try {
			transmogSets = getTransmogSetsFromConfig();
		} catch (JsonParseException | IllegalStateException e) {
			log.error("issue parsing json: " + configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY), e);
			return;
		}
		for (TransmogSet transmogSet : transmogSets) {
			for (Swap swap : transmogSet.getSwaps()) {
				for (AnimationReplacement animationReplacement : swap.animationReplacements) {
					if (animationReplacement.animationtypeReplacement != null && !ATTACK.appliesTo(animationReplacement.animationtypeReplacement)) {
						animationReplacement.animationtypeReplacement = null;
					}
				}
			}
		}
		this.transmogSets = transmogSets;
		saveTransmogSets();
	}

	private void updateForSortOrder()
	{
		List<TransmogSet> transmogSets;
		try {
			transmogSets = getTransmogSetsFromConfig();
		} catch (JsonParseException | IllegalStateException e) {
			log.error("issue parsing json: " + configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY), e);
			return;
		}
		for (TransmogSet transmogSet : transmogSets)
		{
			for (Swap swap : transmogSet.getSwaps())
			{
				swap.updateForSortOrderAndUniqueness(this);
			}
		}
		this.transmogSets = transmogSets;
		saveTransmogSets();
	}

	List<TransmogSet> migrate(String config)
	{
		List<AnimationReplacementRule_OLD> rules = getGson().fromJson(config, new TypeToken<ArrayList<AnimationReplacementRule_OLD>>() {}.getType());
		List<TransmogSet> transmogSets = new ArrayList<>();
		for (AnimationReplacementRule_OLD rule : rules)
		{
			TransmogSet transmogSet = new TransmogSet(
				Collections.singletonList(
					new Swap(
						rule.itemRestrictions.stream().map(r -> r.itemId).collect(Collectors.toList()),
						Collections.singletonList(rule.modelSwap),
						rule.animationReplacements,
						Collections.emptyList(),
						Collections.emptyList(),
						Collections.emptyList())));
			transmogSet.setName(rule.name);
			transmogSets.add(transmogSet);
		}
		return transmogSets;
	}

	@Override
	protected void shutDown() {
		showSidePanel(false);

		clientThread.invokeLater(() -> {
			eventBus.unregister(transmogManager);
			transmogManager.shutDown();

			if (!naturalPlayerPoseAnimations.isEmpty())
			{
				for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
				{
					animation.setAnimation(client.getLocalPlayer(), naturalPlayerPoseAnimations.get(animation.ordinal()));
				}
			}
		});

		transmogSets = null;
    }

    @Subscribe
	public void onProfileChanged(ProfileChanged e) {
		chatboxPanelManager.close();
		clientThread.invokeLater(() -> {
			reloadTransmogSetsFromConfig();
			handleTransmogSetChange();
			if (pluginPanel != null) SwingUtilities.invokeLater(pluginPanel::rebuild);
		});
	}

	private void reloadTransmogSetsFromConfig()
	{
		migrate();

		try
		{
			transmogSets = getTransmogSetsFromConfig();
		}
		catch (JsonParseException | IllegalStateException ex)
		{
			log.error("issue parsing json: " + configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY), ex);
			transmogSets = new ArrayList<>();
		}
	}

    @Provides
	public WeaponAnimationReplacerConfig provideConfig(ConfigManager configManager) {
		return configManager.getConfig(WeaponAnimationReplacerConfig.class);
	}

    static final Map<String, String> renames = new HashMap<>();
	static {
		renames.put("Godsword", "Godsword (Armadyl)");
		renames.put("unarmed", "Unarmed");
		renames.put("shortsword/scim/saeldor", "Dragon longsword/Saeldor");
		renames.put("staff2/wand", "Staff2/Wand");
		renames.put("Knife", "Knife (non-dragon)");
	}

	public List<TransmogSet> getTransmogSetsFromConfig() {
        String configuration = configManager.getConfiguration(GROUP_NAME, TRANSMOG_SET_KEY);
        if (configuration == null) return getDefaultTransmogSets();
		if (configuration.startsWith("NOT_JSON")) {
			configuration = configuration.substring("NOT_JSON".length());
		}
		List<TransmogSet> transmogSets = getGson().fromJson(configuration, new TypeToken<ArrayList<TransmogSet>>() {}.getType());
		if (transmogSets == null) transmogSets = new ArrayList<>();
		return transmogSets;
    }

    public void saveTransmogSets() {
		if (transmogSets == null) return; // not sure how this could happen, but I've had people report it and I don't want to write null into the config.

    	// Runelite won't store config values that are valid json with a nested depth of 8 or higher. Adding "NOT_JSON"
		// makes the string not be valid json, circumventing this.
		// This might not be necessary anymore but I don't feel like updating it; it works fine as is.
        String s = "NOT_JSON" + getGson().toJson(transmogSets);
        configManager.setConfiguration(GROUP_NAME, TRANSMOG_SET_KEY, s);
    }

	/**
	 * Saves transmog sets to config, reapplies transmog and pose animations.
	 */
	public void handleTransmogSetChange() {
		saveTransmogSets();

		if (client.getLocalPlayer() != null)
		{
			transmogManager.changeTransmog();
			updateAnimations();
			updateSoundSwaps();
		}
    }

    public void deleteTransmogSet(int index) {
		transmogSets.remove(index);
		handleTransmogSetChange();
		SwingUtilities.invokeLater(pluginPanel::rebuild);
	}

    public void addNewTransmogSet(int index) {
        transmogSets.add(index, TransmogSet.createTemplate());
        saveTransmogSets();
		SwingUtilities.invokeLater(pluginPanel::rebuild);
	}

    public void moveTransmogSet(int index, boolean up) {
        if ((!up && index == transmogSets.size() - 1) || (up && index == 0)) return;
        TransmogSet swap = transmogSets.remove(index);
        transmogSets.add(index + (up ? -1 : 1), swap);
        handleTransmogSetChange();
        SwingUtilities.invokeLater(pluginPanel::rebuild);
    }

    private List<TransmogSet> getDefaultTransmogSets() {
        String configuration = "[{\"name\":\"Monkey run\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[-1],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Cursed banana\",\"animationtypeToReplace\":\"ALL\"}],\"graphicEffects\":[]}]},{\"name\":\"Elder Maul Scythe\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[22324,4151,12006,4587,24551],\"modelSwaps\":[22325],\"animationReplacements\":[{\"animationSet\":\"Elder maul\",\"animationtypeToReplace\":\"ALL\"},{\"animationSet\":\"Scythe of Vitur\",\"animationtypeToReplace\":\"ATTACK\",\"animationtypeReplacement\":{\"type\":\"ATTACK_SLASH\",\"id\":8056}}],\"graphicEffects\":[{\"type\":\"SCYTHE_SWING\",\"color\":{\"value\":-4030079,\"falpha\":0.0}}]}]},{\"name\":\"Shoulder Halberd\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[3204,23987],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Dharok's greataxe\",\"animationtypeToReplace\":\"STAND_PLUS_MOVEMENT\"}],\"graphicEffects\":[]}]},{\"name\":\"Saeldor Slash\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[24551,23995,23997],\"modelSwaps\":[-1],\"animationReplacements\":[{\"animationSet\":\"Inquisitor's mace\",\"animationtypeToReplace\":\"ATTACK_SLASH\",\"animationtypeReplacement\":{\"type\":\"ATTACK_CRUSH\",\"id\":4503}}],\"graphicEffects\":[]}]},{\"name\":\"Rich voider\",\"enabled\":false,\"minimized\":false,\"swaps\":[{\"itemRestrictions\":[8839,8840,8842,11664,13072,13073],\"modelSwaps\":[11826,11828,11830,7462,13237,22249,21898],\"animationReplacements\":[],\"graphicEffects\":[]}]}]";
        return getGson().fromJson(configuration, new TypeToken<ArrayList<TransmogSet>>() {}.getType());
    }

	private void swapPlayerAnimation()
	{
		Player player = client.getLocalPlayer();
		int playerAnimation = player.getAnimation();
		if (playerAnimation == -1) return;

		Optional<AnimationType> type = Constants.animationSets.stream()
			.map(set -> set.getType(playerAnimation))
			.filter(t -> t != null)
			.findFirst();
		if (!type.isPresent()) return;

		Integer replacementAnim = currentAnimations.getAnimation(type.get());
		if (replacementAnim != null)
		{
			log.debug("replacing animation {} with {}", playerAnimation, replacementAnim);
			player.setAnimation(replacementAnim);
		}

		if (currentScytheGraphicEffect != null && AnimationType.ATTACK.appliesTo(type.get())) {
			scytheSwingCountdown = 20;
		}
	}

	@Subscribe
	public void onSoundEffectPlayed(SoundEffectPlayed soundEffectPlayed)
	{
		int sound = soundEffectPlayed.getSoundId();
		for (SoundSwap soundSwap : soundSwaps)
		{
			if (soundSwap.toReplace == sound)
			{
				log.debug("Found sound to place, replacing with: "+soundSwap.toReplaceWith);
				clientThread.invokeLater(() -> {
					client.playSoundEffect(soundSwap.toReplaceWith);
				});
				soundEffectPlayed.consume();
				return;
			}
		}
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		if (handlePossibleNoProjectileSpellInClientTick) {
			replaceNoProjectileSpell();
		}

		if (client.getGameCycle() == timeToApplyDelayedGfx) {
//			System.out.println("it is " + client.getGameCycle() + ", applying delayed gfx.");
			actorToApplyDelayedGfxTo.setGraphic(delayedGfxToApply);
			actorToApplyDelayedGfxTo.setSpotAnimFrame(0);
			actorToApplyDelayedGfxTo.setGraphicHeight(delayedGfxHeightToApply);
		}

		if (scytheSwingCountdown == 0) {
			createScytheSwing();
		} else {
			scytheSwingCountdown--;
		}
    }

	private void replaceNoProjectileSpell()
	{
		handlePossibleNoProjectileSpellInClientTick = false;

		Player player = client.getLocalPlayer();
		final WorldPoint playerPos = player.getWorldLocation();
		if (playerPos == null) return;
		final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
		if (playerPosLocal == null) return;
		if (player.getInteracting() == null) return;

		for (ProjectileSwap projectileSwap : projectileSwaps)
		{
			ProjectileCast toReplace = projectileSwap.getToReplace();
			if (toReplace.getCastAnimation() != lastRealAnimation || toReplace.getProjectileId() != -1) {
				continue;
			}

			if (toReplace.getCastGfx() != -1) {
				if (toReplace.getCastGfx() != player.getGraphic()) continue;
			} else {
				// TODO check autocast and last cast spell.
			}

			boolean isBarrage = false;
			int chebyshevDistance = chebyshevDistance(player, player.getInteracting(), isBarrage);
//			System.out.println("distance is " + chebyshevDistance);
			// TODO splash detection.
			int projectileTravelTime;
			int graphicDelay;
			ProjectileCast toReplaceWith = projectileSwap.getToReplaceWith();
			switch (toReplace.getCastAnimation()) {
				// magic spells.
				case 811:
					projectileTravelTime = 120 - toReplaceWith.getStartMovement();
					graphicDelay = 48 + 10 * chebyshevDistance;
					break;
				case 1978:
				case 1979:
					projectileTravelTime = -5 + 10 * chebyshevDistance;
					graphicDelay = 48 + 10 * chebyshevDistance;
					break;
				// arceuus spells.
				case 8972:
				case 8974:
				case 8977:
					// TODO check these values in-game.
					projectileTravelTime = 60; // 2 ticks, according to hit delay article.
					graphicDelay = 60;
					break;
				default:
					return; // shouldn't happen.
			}
			int endCycle = client.getGameCycle() + toReplaceWith.getStartMovement() + projectileTravelTime;
			int targetX = player.getInteracting().getLocalLocation().getX();
			int targetY = player.getInteracting().getLocalLocation().getY();
			int startHeight = Perspective.getTileHeight(client, player.getLocalLocation(), player.getWorldLocation().getPlane()) + toReplaceWith.height;

//			System.out.println("replacing projectile-less spell. " + client.getGameCycle() + " " + endCycle);
			replaceSpell(projectileSwap, player, playerPos.getPlane(), playerPosLocal, startHeight, endCycle, player.getInteracting(), targetX, targetY);
			break;
		}
	}

	int chebyshevDistance(Player player, Actor target, boolean isBarrage)
	{
		if (target == null) return -1;
		/*
		 * see https://oldschool.runescape.wiki/w/Hit_delay
		 * "The distance is typically measured edge-to-edge in game squares, using the same edge for both entities. I.e. distance will be calculated using an NPC's closest edge to the player, and the player's furthest edge from the NPC. However, barrage spells are a notable exception in that they calculate distance from the player to an NPC's south-west tile, which causes abnormally long hit delay when attacking a large NPC from the north or east."
		 */

		LocalPoint playerLocation = player.getLocalLocation();
		LocalPoint targetLocation = target.getLocalLocation();
		int px = playerLocation.getSceneX();
		int py = playerLocation.getSceneY();
		int tx = targetLocation.getSceneX();
		int ty = targetLocation.getSceneY();

		// Special case for >1 tile sized npc with non-barrage attack.
		if (target instanceof NPC && !isBarrage) {
			NPC npc = (NPC) target;
			int targetSize = npc.getTransformedComposition().getSize();
			if (targetSize > 1) {
				// measure distance from all 4 edges.
				int nDiff = py - (ty + targetSize - 1);
				int sDiff = ty - py;
				int wDiff = tx - px;
				int eDiff = px - (tx + targetSize - 1);
				return Math.max(Math.max(nDiff, sDiff), Math.max(wDiff, eDiff));
			}
		}

		return Math.max(Math.abs(tx - px), Math.abs(ty - py));
	}

	/** `Client#createProjectile` calls onProjectileMoved; it is dangerous to allow this to happen because of stackoverflows. */
    private boolean norecurse = false;
	@Subscribe(priority = -1)
	public void onProjectileMoved(ProjectileMoved projectileMoved) {
		if (norecurse) return;

		Projectile projectile = projectileMoved.getProjectile();
		if (client.getGameCycle() >= projectile.getStartCycle()) return; // skip already seen projectiles.

		// This is the player's actual location which is what projectiles use as their start position. Player#getX, #getSceneX, etc., do not work here.
		Player player = client.getLocalPlayer();
		final WorldPoint playerPos = player.getWorldLocation();
		if (playerPos == null) return;
		final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
		if (playerPosLocal == null) return;

		if (projectile.getX1() != playerPosLocal.getX() || projectile.getY1() != playerPosLocal.getY()) return;

		int correctedLastRealAnimation = // Some standard spellbook spells use a different animation depending on the equipped weapon (or lack thereof).
			(lastRealAnimation < 710 || lastRealAnimation > 729) ? lastRealAnimation :
			lastRealAnimation == 710 ? 1161 :
			lastRealAnimation == 711 ? 1162 :
			lastRealAnimation == 716 ? 1163 :
			lastRealAnimation == 717 ? 1164 :
			lastRealAnimation == 718 ? 1165 :
			lastRealAnimation == 724 ? 1166 :
			lastRealAnimation == 727 ? 1167 :
			lastRealAnimation == 728 ? 1168 :
			lastRealAnimation == 729 ? 1169 :
			lastRealAnimation
		;
		for (ProjectileSwap projectileSwap : projectileSwaps)
		{
			ProjectileCast toReplace = projectileSwap.getToReplace();
			if (
				toReplace.getCastAnimation() == correctedLastRealAnimation && correctedLastRealAnimation != -1 &&
				toReplace.getProjectileId() == projectile.getId() &&
				(toReplace.getCastGfx() == -1 || toReplace.getCastGfx() == player.getGraphic())
			) {
				handlePossibleNoProjectileSpellInClientTick = false;
//				System.out.println("matched " + toReplace.getName(itemManager) + " at " + client.getGameCycle());

				int endCycle = projectile.getEndCycle();
				Actor interacting = projectile.getInteracting();
				int x = projectile.getTarget().getX();
				int y = projectile.getTarget().getY();
				int height = projectile.getHeight() - toReplace.height + projectileSwap.getToReplaceWith().height;

				replaceSpell(projectileSwap, player, playerPos.getPlane(), playerPosLocal, height, endCycle, interacting, x, y);
				projectile.setEndCycle(0);

				break;
			}
		}
	}

	private void replaceSpell(
		ProjectileSwap projectileSwap,
		Player player,
		int plane,
		LocalPoint playerPosLocal,
		int startHeight,
		int endCycle,
		Actor interacting,
		int targetX,
		int targetY
	) {
		if (interacting.getLocalLocation().getX() != targetX || interacting.getLocalLocation().getY() != targetY) { // TODO
//			System.out.println("!!!!!!!!!!!!!!!!!!!!!!!! mismatch with projectile target and interacting target.");
		}

		ProjectileCast toReplace = projectileSwap.getToReplace();
		ProjectileCast toReplaceWith = projectileSwap.getToReplaceWith();
		player.setAnimation(toReplaceWith.getCastAnimation());
		// TODO what is startheight?
		if (toReplaceWith.getProjectileId() != -1)
		{
			int startCycle = client.getGameCycle() + toReplaceWith.getStartMovement();
//			System.out.println("start height is " + startHeight + " " + startCycle + " " + endCycle + " " + (endCycle - startCycle));
			norecurse = true;
			Projectile p = client.createProjectile(toReplaceWith.getProjectileId(),
//							projectile.getFloor(),
//							projectile.getX1(), projectile.getY1(),
				plane,
				playerPosLocal.getX(), playerPosLocal.getY(),
				startHeight,
				startCycle, endCycle,
				toReplaceWith.getSlope(),
				toReplaceWith.getStartHeight(), toReplaceWith.getEndHeight(),
				interacting,
				targetX, targetY);
			client.getProjectiles().addLast(p);
			norecurse = false;
		}

		player.setGraphic(toReplaceWith.getCastGfx());
		// TODO set height.
		player.setSpotAnimFrame(0);

		if (player.getInteracting() != null)
		{
			if (toReplace.getHitGfx() != -1)
			{
				// TODO remove this section, timing it yourself probably works better.
				// the spell's hit gfx is on the enemy when the spell is cast, it just has a delay on it.
				int graphic = player.getInteracting().getGraphic();
				if (graphic == toReplace.getHitGfx() || (graphic == 85 && true)) // TODO remove second part.
				{
					player.getInteracting().setGraphic(toReplaceWith.getHitGfx());
					player.getInteracting().setGraphicHeight(toReplaceWith.getHitGfxHeight());
				}
			}
			else
			{
				delayedGfxToApply = toReplaceWith.getHitGfx();
				delayedGfxHeightToApply = toReplaceWith.getHitGfxHeight();
				actorToApplyDelayedGfxTo = player.getInteracting();
				timeToApplyDelayedGfx = endCycle;
			}
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
			Model model = client.loadModelData(id)
				.cloneVertices()
				.cloneColors()
				.recolor((short) 960, JagexColor.rgbToHSL(scytheSwingColor.getRGB(), 1.0d))
				.translate(0, -85, 0)
				.light()
			;
			runeLiteObject.setModel(model);
		} else {
			runeLiteObject.setModel(client.loadModel(id));
		}

		runeLiteObject.setAnimation(client.loadAnimation(1204));
		LocalPoint localPoint = LocalPoint.fromWorld(client, point);
		runeLiteObject.setLocation(localPoint, client.getPlane());
		// TODO should I set these to inactive at some point?
		runeLiteObject.setActive(true);
	}

	public void demoCast(ProjectileCast pc)
	{
		Player p = client.getLocalPlayer();
		if (p == null) return;
		if (pc.projectileId != -1)
		{
			WorldPoint wl = p.getWorldLocation();
			LocalPoint ll = p.getLocalLocation();
			int targetx = ll.getX() + (int) (700 * Math.cos((-512 - p.getOrientation()) / 2048d * 2 * Math.PI));
			int targety = ll.getY() + (int) (700 * Math.sin((-512 - p.getOrientation()) / 2048d * 2 * Math.PI));
			int height = Perspective.getTileHeight(client, p.getLocalLocation(), p.getWorldLocation().getPlane()) + pc.height;
			norecurse = true;
			Projectile projectile = client.createProjectile(pc.projectileId, wl.getPlane(), ll.getX(), ll.getY(), height, client.getGameCycle() + pc.startMovement, client.getGameCycle() + 100, pc.slope, pc.startHeight, pc.endHeight, null, targetx, targety);
			client.getProjectiles().addLast(projectile);
			norecurse = false;
		}
		if (pc.castAnimation != -1) {
			p.setAnimation(pc.castAnimation);
			p.setAnimationFrame(0);
		}
		if (pc.castGfx != -1) p.createSpotAnim("demo".hashCode(), pc.castGfx, 92, 0);
	}

	private static final class AnimationReplacements {
		private final Map<AnimationType, Integer> replacements = new HashMap<>();

		public AnimationReplacements() {
			this(Collections.emptyList());
		}

		public AnimationReplacements(List<AnimationReplacement> replacements) {
			for (int i = replacements.size() - 1; i >= 0; i--)
			{
				applyReplacement(replacements.get(i));
			}
		}

		public void applyReplacement(Swap.AnimationReplacement replacement) {
			replaceAnimations(
				replacement.animationSet,
				replacement.animationtypeToReplace,
				replacement.animationtypeReplacement
			);
		}

		private void replaceAnimations(AnimationSet animationSet, AnimationType toReplace, AnimationType replacement) {
			if (toReplace == ATTACK) {
				int defaultAttack = -1;
				for (AnimationType attackAnimation : animationSet.getAttackAnimations())
				{
					defaultAttack = animationSet.getAnimation(attackAnimation);
					if (defaultAttack != -1) break;
				}
				List<AnimationType> children = new ArrayList<>(ATTACK.children);
				children.add(ATTACK);
				for (AnimationType child : children)
				{
					int id = animationSet.getAnimation(replacement == null ? child : replacement);
					if (id == -1) {
						id = defaultAttack;
					}
					if (id != -1) {
						replacements.put(child, id);
					}
				}
			} else if (!toReplace.hasChildren()) {
				int id = animationSet.getAnimation(replacement == null ? toReplace : replacement);
				if (id != -1) {
					replacements.put(toReplace, id);
				}
			} else {
				for (AnimationType child : toReplace.children) {
					replaceAnimations(animationSet, child, replacement);
				}
			}
		}

		public Integer getAnimation(AnimationType type) {
			return replacements.get(type);
		}
	}

    private void updateAnimations() { // TODO cache maybe based on the current gear.
		List<Swap> matchingSwaps = getApplicableSwaps();

		List<AnimationReplacement> replacements = matchingSwaps.stream()
			.flatMap(swap -> swap.animationReplacements.stream()
				.filter(replacement -> replacement.animationSet != null && replacement.animationtypeToReplace != null)
				.sorted()
			)
			.collect(Collectors.toList());
		currentAnimations = previewAnimationReplacements != null ? previewAnimationReplacements : new AnimationReplacements(replacements);
		setPlayerPoseAnimations();

		projectileSwaps = matchingSwaps.stream().flatMap(swap -> swap.getProjectileSwaps().stream()).filter(swap -> swap.getToReplace() != null && swap.getToReplaceWith() != null).collect(Collectors.toList());
		currentScytheGraphicEffect = matchingSwaps.stream()
			.filter(swap -> swap.getGraphicEffects().stream().anyMatch(e -> e.type == GraphicEffect.Type.SCYTHE_SWING))
			.flatMap(swap -> swap.getGraphicEffects().stream())
			.findAny().orElse(null);
    }

    private void updateSoundSwaps() {
		soundSwaps = getApplicableSwaps().stream().flatMap(swap -> swap.getSoundSwaps().stream()).filter(swap -> swap.getToReplace() != -1 && swap.getToReplaceWith() != -1).collect(Collectors.toList());
	}

    public String itemDisplayName(int itemId) {
		return Constants.getName(itemId, itemManager.getItemComposition(itemId).getMembersName());
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

	/**
	 * Sets the player's pose animations (idle/walk/run/etc.).
	 */
	private void setPlayerPoseAnimations()
	{
		Player player = client.getLocalPlayer();
		if (player == null || naturalPlayerPoseAnimations.isEmpty()) return;

		if (Constants.doNotReplaceIdles.contains(naturalPlayerPoseAnimations.get(Constants.ActorAnimation.IDLE.ordinal()))) return;

		for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
		{
			Integer animationId = currentAnimations.getAnimation(animation.getType());
			if (animationId == null) animationId = naturalPlayerPoseAnimations.get(animation.ordinal());
			animation.setAnimation(player, animationId);
		}
	}

	/** Keep note of the last animationchanged in case some plugin changes it (maybe even us). */
	private int lastRealAnimation = -1;

	@Subscribe(priority = -1000.0f) // I want to run late, so that plugins that need animation changes don't see my changed animation ids, since mine are cosmetic and don't give information on what the player is actually doing.
	public void onAnimationChanged(AnimationChanged e)
	{
		Player player = client.getLocalPlayer();
		if (!e.getActor().equals(player)) return;
//		System.out.println("onanimationchanged");

		lastRealAnimation = player.getAnimation();

		checkForPossibleNoProjectileSpell(player);

		swapPlayerAnimation();
	}

	@Subscribe
	public void onInteractingChanged(InteractingChanged e) {
		if (e.getSource() != client.getLocalPlayer()) return;

//		System.out.println("interactingchanged " + e.getTarget());
	}

	private void checkForPossibleNoProjectileSpell(Player player)
	{
		// Why don't I store this value in onProjectileMoved?
		// These spell casts all lack projectiles to identify them (with the exception of ice blitz).
		// Therefore, other means must be used to determine whether or with what the spell should be replaced.
		// TODO doesn't charge use 811 also?
		if (
			lastRealAnimation == 811 || // god spells
			lastRealAnimation == 1978 || // ancient spells.
			lastRealAnimation == 1979 || // ancient spells.
			lastRealAnimation == 8972 || // arceuus spells.
			lastRealAnimation == 8974 || // arceuus spells.
			lastRealAnimation == 8977 // arceuus spells.
		) {
			// Mark that this needs to be processed in client tick. The reason the projectile replacement can't happen here is because projectilemoved hasn't yet happened, and some spells that have these animations (the ancient spell ones specifically) have projectiles so I'd rather do those in projectilemoved since it's simpler and maybe more consistent.
			handlePossibleNoProjectileSpellInClientTick = true;
		}
	}

	@Subscribe(priority = 1) // I need kit data to determine what the player is wearing (equipment inventory does not update fast enough to avoid flickering), so I need this information before other plugins might change it.
	public void onPlayerChanged(PlayerChanged playerChanged) {
		if (playerChanged.getPlayer() != client.getLocalPlayer()) return;

		equippedItemsFromKit = IntStream.of(client.getLocalPlayer().getPlayerComposition().getEquipmentIds()).map(i -> itemManager.canonicalize(i - ITEM_OFFSET)).boxed().collect(Collectors.toList());
		recordNaturalPlayerPoseAnimations();

		transmogManager.reapplyTransmog();
		updateAnimations();
		updateSoundSwaps();
	}

	private void recordNaturalPlayerPoseAnimations()
	{
		naturalPlayerPoseAnimations.clear();
		Player player = client.getLocalPlayer();
		for (Constants.ActorAnimation animation : Constants.ActorAnimation.values())
		{
			naturalPlayerPoseAnimations.add(animation.getAnimation(player));
		}
	}

	public enum SearchType {
		TRIGGER_ITEM,
		MODEL_SWAP,
		SPELL_L,
		SPELL_R,
	}

	public void doItemSearch(Consumer<SelectionResult> onItemChosen, SearchType searchType) {
		doItemSearch(onItemChosen, () -> {}, searchType);
	}

	/**
	 * Listeners should always be called on the client thread.
	 * @param onItemChosen
	 * @param onItemDeleted
	 */
	public void doItemSearch(Consumer<SelectionResult> onItemChosen, Runnable onItemDeleted, SearchType searchType) {
		doItemSearch(onItemChosen, onItemDeleted, searchType, null);
	}

	public void doItemSearch(Consumer<SelectionResult> onItemChosen, Runnable onItemDeleted, SearchType searchType, Swap swap) {
        if (client.getGameState() != GameState.LOGGED_IN)
        {
            JOptionPane.showMessageDialog(pluginPanel,
                    "This plugin uses the in-game item search panel; you must be logged in to use this.",
                    "Log in to choose items",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        chatboxPanelManager.close();
        // invokelater is necessary because the close call above calls invokelater internally, and we want to run after that.
        clientThread.invokeLater(() -> {
			itemSearch.tooltipText("select");
			itemSearch.onItemSelected(onItemChosen);
			itemSearch.onItemDeleted(onItemDeleted);
			itemSearch.setType(searchType);
			itemSearch.onItemMouseOvered(
				searchType == MODEL_SWAP ?
					itemId -> setPreviewItem(itemId, swap != null && (swap.animationReplacements.isEmpty() || swap.animationReplacements.size() == 1 && swap.animationReplacements.get(0).auto != -1)) :
				searchType == SPELL_R ?
					spellId -> {if (spellId != -1) demoCast(Constants.projectilesById[spellId]);} :
					null
			);
			itemSearch.build();
			clientUI.requestFocus();
		});
	}

	private void setPreviewItem(Integer itemId, boolean previewAnimations)
	{
		previewItem = itemId;
		transmogManager.changeTransmog();

		AnimationSet animationSet = Constants.getAnimationSet(itemId);
		previewAnimationReplacements = animationSet == null || !previewAnimations ? null : new AnimationReplacements(Collections.singletonList(new AnimationReplacement(animationSet, ALL)));
		updateAnimations();
	}

	public AsyncBufferedImage getItemImage(int itemId) {
        return itemManager.getImage(itemId);
    }

	public BufferedImage getSpellImage(ProjectileCast projectileCast)
	{
		return projectileCast.getSpriteIdIcon() != -1 ?
			spriteManager.getSprite(projectileCast.getSpriteIdIcon(), 0) :
			itemManager.getImage(projectileCast.getItemIdIcon(), 1000, false)
			;
	}

    @Subscribe
    public void onGameStateChanged(GameStateChanged event)
    {
        if (event.getGameState() == GameState.LOGIN_SCREEN) {
			if (transmogSets != null) { // Can be null during plugin startup.
				showSidePanel(!config.hideSidePanel());
			}
        } else if (event.getGameState() == GameState.LOGGED_IN) {
        	// This is necessary for transmog to show up on teleports.
			if (client.getLocalPlayer() == null) return; // happens during dcs?
        	if (client.getLocalPlayer().getPlayerComposition() != null) transmogManager.reapplyTransmog();
		}
	}

	public void demoAnimation(Integer animation) {
		Player player = client.getLocalPlayer();
		if (player != null)
		{
			player.setAnimation(animation);
			player.setAnimationFrame(0);
			if (currentScytheGraphicEffect != null) scytheSwingCountdown = 20;
		}
    }

	public Integer getWikiScrapeSlot(int itemId) {
		ItemStats itemStats = itemManager.getItemStats(itemId, false);
		if (itemStats == null) return null;
		ItemEquipmentStats equipment = itemStats.getEquipment();
		if (equipment == null) return null;
		return equipment.getSlot();
	}

	public Integer[] getApplicableModelSwaps()
	{
		Integer[] genericTransmog = new Integer[KitType.values().length];
		Integer[] specificTransmog = new Integer[KitType.values().length];

		for (Swap swap : getApplicableSwaps())
		{
			Integer[] transmogMap = swap.appliesSpecificallyToGear(equippedItemsFromKit, this) ? specificTransmog : genericTransmog;
			for (Integer modelSwap : swap.getModelSwaps())
			{
				SlotAndKitId slotForItem = getSlotAndKitForItem(modelSwap, swap);
				if (slotForItem != null && transmogMap[slotForItem.slot] == null) {
					transmogMap[slotForItem.slot] = slotForItem.kitId;
				}
			}
		}

		for (int i = 0; i < specificTransmog.length; i++)
		{
			if (specificTransmog[i] != null)
			{
				genericTransmog[i] = specificTransmog[i];
			}
		}

		if (previewItem != -1) {
			SlotAndKitId slotForItem = getSlotAndKitForItem(previewItem, null);
			if (slotForItem != null) {
				genericTransmog[slotForItem.slot] = slotForItem.kitId;
			}
		}

		return genericTransmog;
	}

	private List<Swap> getApplicableSwaps()
	{
		return transmogSets.stream()
			.filter(TransmogSet::isEnabled)
			.flatMap(set -> set.getSwaps().stream())
			.filter(swap -> swap.appliesToGear(equippedItemsFromKit, this))
			.collect(Collectors.toList());
	}

	@Value
	public static class SlotAndKitId {
		int slot;
		int kitId;
	}

	public Integer getMySlot(int modelSwap)
	{
		if (modelSwap < 0) {
			Constants.NegativeId negativeId = mapNegativeId(modelSwap);
			if (negativeId.type == HIDE_SLOT || negativeId.type == SHOW_SLOT) {
				return negativeId.id;
			}
			else
			{
				return null;
			}
		}
		else
		{
			return getSlotForNonNegativeModelId(modelSwap);
		}
	}

	public Integer getSlotForNonNegativeModelId(int modelSwap)
	{
		Integer slot = Constants.SLOT_OVERRIDES.get(modelSwap);
		// if the slot is -1, use the wiki slot to prevent messing up people's transmogs if they added the item prior to me making it -1.
		if (slot != null && slot != -1) {
			return slot;
		}

		ItemStats itemStats = itemManager.getItemStats(modelSwap, false);
		if (itemStats == null || itemStats.getEquipment() == null) return null;
		return itemStats.getEquipment().getSlot();
	}

	public SlotAndKitId getSlotAndKitForItem(int modelSwap, Swap swap)
	{
		if (modelSwap < 0) {
			Constants.NegativeId negativeId = mapNegativeId(modelSwap);
			if (negativeId.type == HIDE_SLOT) {
				return new SlotAndKitId(negativeId.id, 0);
			} else if (negativeId.type == SHOW_SLOT) {
				return new SlotAndKitId(negativeId.id, TransmogrificationManager.SHOW_SLOT);
			} else {
				return null;
			}
		}

		int slotOverride = swap != null ? swap.getSlotOverride(modelSwap) : -1;
		if (slotOverride == -1) {
			Integer slot = getSlotForNonNegativeModelId(modelSwap);
			slotOverride = slot != null ? slot : WEAPON_SLOT;
		}
		return new SlotAndKitId(slotOverride, modelSwap);
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged e) {
		if (e.getGroup().equals(GROUP_NAME)) {
			if (e.getKey().equals("hideSidePanel")) {
				showSidePanel(!config.hideSidePanel());
			}
		}
	}

	private void showSidePanel(boolean showSidePanel)
	{
		SwingUtilities.invokeLater(() -> {
			if (showSidePanel) {
				if (navigationButton != null) return;

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
			} else {
				if (navigationButton == null) return;

				clientToolbar.removeNavigation(navigationButton);
				navigationButton = null;
				pluginPanel = null;
			}
		});
	}
}

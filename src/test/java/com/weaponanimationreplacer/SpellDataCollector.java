package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Player;
import net.runelite.api.Projectile;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.GraphicChanged;
import net.runelite.api.events.MenuOptionClicked;
import net.runelite.api.events.ProjectileMoved;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.util.Text;

public class SpellDataCollector
{

	/*
	 * Data to collect:
	 * projectile id
	 * slope
	 * endheight
	 * startheight
	 * cast animation
	 * chebyshev distance (both non-barrage and barrage).
	 * cast gfx
	 * cast gfx height
	 * cast gfx start time?
	 * hit gfx
	 * hit gfx delay
	 * hit gfx height
	 * hitsplat time
	 * projectile travel time
	 * projectile start movement
	 *
	 * Spells with projectiles "start" when onProjectileMoved happens for the first time. Spells without projectiles
	 * "start" when the player does their animation.
	 */

	private WeaponAnimationReplacerPlugin plugin;

	@Inject
	private Client client;

	int spellStart = -1;

	public SpellDataCollector(WeaponAnimationReplacerPlugin plugin) {
		this.plugin = plugin;
	}

	boolean checkSpell = false;

	@Subscribe(priority=-2000)
	public void onAnimationChanged(AnimationChanged e) {
		Player player = client.getLocalPlayer();
		if (!e.getActor().equals(player)) return;

		int animation = player.getAnimation();
		System.out.println("animationchanged " + client.getTickCount() + " " + client.getGameCycle() + " " + e.getActor().getAnimation());

//		if (
//				animation == 811 || // god spells
//				animation == 1978 || // ancient spells.
//				animation == 1979 || // ancient spells.
//				animation == 8972 || // arceuus spells.
//				animation == 8974 || // arceuus spells.
//				animation == 8977 // arceuus spells.
//		) {
		if (animation != -1) {
			System.out.println(client.getGameCycle() + " !!! spell start (animation) !!!");
			checkSpell = true;
		}
	}

	@Subscribe
	public void onGameTick(GameTick e) {
		if (checkSpell) {
			checkSpell = false;
			spellStart(null, client.getLocalPlayer());
		}
	}

	private void spellStart(Projectile projectile, Player player)
	{
		System.out.println(client.getGameCycle() + " !!! spell start from animation !!!");

		if (projectile == null) {
			final WorldPoint playerPos = player.getWorldLocation();
			if (playerPos == null) return;
			final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
			if (playerPosLocal == null) return;
			for (Projectile p : client.getProjectiles()) {
				if (client.getGameCycle() > p.getStartCycle()) continue; // skip already seen projectiles.
				if (p.getX1() != playerPosLocal.getX() || p.getY1() != playerPosLocal.getY()) continue;
				projectile = p;
			}
		}

		spellStart = client.getGameCycle();
		currentSpellChebyshevDistance = player.getInteracting() != null ? plugin.chebyshevDistance(player, player.getInteracting(), false) : -1;

		int castAnimation = player.getAnimation();
		int castGfx = player.getGraphic();
		int castGfxHeight = player.getGraphicHeight();
		int hitGfx = player.getInteracting() != null ? player.getInteracting().getGraphic() : -1;
		int hitGfxHeight = player.getInteracting() != null ? player.getInteracting().getGraphicHeight() : -1;

		/*
		int projectileId, int startMovement, int startPos, int startHeight, int endHeight, int slope) {
		 */
		int projectileId = projectile != null ? projectile.getId() : -1;
		int startMovement = projectile != null ? (projectile.getStartCycle() - client.getGameCycle()) : -1;
		int startPos = projectile != null ? projectile.getStartHeight() : -1; // api v1
//		int startPos = projectile != null ? projectile.getStartPos() : -1; // api v2
		int startHeight = projectile != null ? projectile.getStartHeight() : -1;
		int endHeight = projectile != null ? projectile.getEndHeight() : -1;
		int slope = projectile != null ? projectile.getSlope() : -1;

		currentProjectile = new ProjectileData();
		currentProjectile.name = lastSpellCastName;
		lastSpellCastName = null;
		currentProjectile.castAnimation = castAnimation;
		currentProjectile.castGfx = castGfx;
		if (player.getSpotAnimFrame() != 1) {
			System.out.println("!!!!!!!!!!!!!!!! graphic did not start at step 1, started at " + player.getSpotAnimFrame());
		}
		if (projectile != null)
		{
			currentProjectile.projectileId = projectile.getId();
			currentProjectile.startCycleOffset = (projectile.getStartCycle() - client.getGameCycle());
			currentProjectile.endCycleOffset.put(currentSpellChebyshevDistance, projectile.getEndCycle() - client.getGameCycle());
			currentProjectile.slope = projectile.getSlope();
			currentProjectile.startHeight = projectile.getStartHeight();
			currentProjectile.endHeight = projectile.getEndHeight();
			currentProjectile.height = projectile.getHeight();
		}
		/*
		projectiles.add(p().id(36).name("Smoke Rush").sprite(SpriteID.SPELL_SMOKE_RUSH).ids(1978, -1, 384, 385, 51, 64, 124, 16).build());
		 */
		System.out.println(
			".name(\"" + lastSpellCastName + "\")" +
			".cast(" + castAnimation + ", " + (castGfx != -1 ? (castGfx + ", " + castGfxHeight) : "-1, -1") + ")" +
			(hitGfx != -1 ?
			".hitGfx(" + hitGfx + ", " + hitGfxHeight + ")"
			: "") +
			(projectile != null ?
			".projectile(" + projectileId + ", " + startMovement + ", " + startPos + ", " + startHeight + ", " + endHeight + ", " + slope + ")"
			: "") +
			""
		);
		if (projectile != null) System.out.println("\tprojectile: " +
			"castAnimation: " + castAnimation + " " +
			"id: " + projectile.getId() + " " +
			"startCycle: " + (projectile.getStartCycle() - client.getGameCycle()) + "(" + projectile.getStartCycle() + ") " +
			"endCycle: " + (projectile.getEndCycle() - client.getGameCycle()) + "(" + projectile.getEndCycle() + ") " +
			"slope: " + projectile.getSlope() + " " +
			"startHeight: " + projectile.getStartHeight() + " " +
			"endHeight: " + projectile.getEndHeight() + " " +
			"height?: " + projectile.getHeight() + " " +
			"remainingcycles?: " + projectile.getRemainingCycles() + " " +
			"");

		System.out.println("\tspelldata: " +
			"chebyshev: " + plugin.chebyshevDistance(player, player.getInteracting(), false) + " (" + plugin.chebyshevDistance(player, player.getInteracting(), true) + ") " +
			(castGfx == -1 ? "cast: (-----) " : ("cast: " + castGfx + " (h: " + castGfxHeight + " f: " + player.getSpotAnimFrame() + ") ")) +
			(player.getInteracting().getGraphic() == -1 ? "hit (-----)" : ("hit: " + player.getInteracting().getGraphic() + " (h: " + player.getInteracting().getGraphicHeight() + " f: " + player.getInteracting().getSpotAnimFrame() + ") ")) +
		"");
	}

	@Subscribe
	public void onClientTick(ClientTick event)
	{
		for (NPC npc : client.getNpcs())
		{
			if (npc.getGraphic() != -1) {
//				System.out.println(client.getGameCycle() + " " + npc.getGraphic() + " " + npc.getSpotAnimFrame() + " " + npc.getGraphicHeight() + " " + npc.getName());
			}
		}
		for (Player player : client.getPlayers())
		{
			if (player.getGraphic() != -1) {
//				System.out.println(client.getGameCycle() + " " + player.getGraphic() + " " + player.getSpotAnimFrame() + " " + player.getGraphicHeight() + " " + player.getName());
			}
		}
		if (plugin.client.getLocalPlayer().getInteracting() != null)
		{
			int spotAnimFrame = plugin.client.getLocalPlayer().getInteracting().getSpotAnimFrame();
			if (spotAnimFrame == 1 && lasttargetspotanimframe == 0)
			{
				System.out.println("\tspot anim started " + client.getTickCount() + " " + client.getGameCycle());

				currentProjectile.hitGfxStart.put(currentSpellChebyshevDistance, client.getGameCycle() - spellStart);
				currentProjectile.hitGfx = plugin.client.getLocalPlayer().getInteracting().getGraphic();
				currentProjectile.hitGfxHeight = plugin.client.getLocalPlayer().getInteracting().getGraphicHeight();

				System.out.println("spell data recording complete: " + currentProjectile);
				List<ProjectileData> matches = projectiles.stream().filter(p -> p.getProjectileId() == currentProjectile.projectileId && (p.getCastAnimation() == currentProjectile.getCastAnimation() && p.getHitGfx() == currentProjectile.getHitGfx())).collect(Collectors.toList());
				if (matches.size() > 0) {
					System.out.println("matches: " + matches.size());
				}

				projectiles.add(currentProjectile);
			}
		}

		if (spellStart + 15 < client.getGameCycle() && currentProjectile != null) {
//			System.out.println("stopping spell data collection for reason: 15 client ticks elapsed");

		}
	}

	@Subscribe
	public void onMenuOptionClicked(MenuOptionClicked e) {
		if (e.getMenuOption().equals("Cast")) {
			lastSpellCastName = Text.removeTags(e.getMenuTarget());
			System.out.println("spell cast selected " + lastSpellCastName);
		}
	}

	private List<ProjectileData> projectiles = new ArrayList<>();

	private ProjectileData currentProjectile;
	int currentSpellChebyshevDistance = -1;
	String lastSpellCastName = null;

	@Data
	@EqualsAndHashCode(exclude = {"hitGfxStart", "endCycleOffset"})
	private static class ProjectileData {
		private String name;
		private int castAnimation;
		private int projectileId;
		private int startCycleOffset;
		private Map<Integer, Integer> endCycleOffset = new HashMap<>();
		private int slope;
		private int startHeight;
		private int endHeight;
		private int height; // TODO do I want this?
		private int remainingcycles; // TODO do I want this?
		private int castGfx;
		private int castGfxHeight;
		private int castGfxStart; // TODO do I want this
		private int hitGfx;
		private int hitGfxHeight;
		private Map<Integer, Integer> hitGfxStart = new HashMap<>();

		public boolean isSimilarTo(ProjectileData other) {
			if (!this.equals(other)) return false;

			for (Map.Entry<Integer, Integer> integerIntegerEntry : endCycleOffset.entrySet())
			{
				if (other.endCycleOffset.get(integerIntegerEntry.getKey()) != integerIntegerEntry.getValue()) {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!! WARN mismatch (" + integerIntegerEntry.getKey() + " " + integerIntegerEntry.getValue() + ") (" + other.endCycleOffset.get(integerIntegerEntry.getKey()) + ")");
				}
			}

			for (Map.Entry<Integer, Integer> integerIntegerEntry : hitGfxStart.entrySet())
			{
				if (other.hitGfxStart.get(integerIntegerEntry.getKey()) != integerIntegerEntry.getValue()) {
					System.out.println("!!!!!!!!!!!!!!!!!!!!!!! WARN mismatch (" + integerIntegerEntry.getKey() + " " + integerIntegerEntry.getValue() + ") (" + other.hitGfxStart.get(integerIntegerEntry.getKey()) + ")");
				}
			}

			return true;
		}

		@Override
		public String toString()
		{
			return "ProjectileData{" +
				"castAnimation=" + castAnimation +
				", projectileId=" + projectileId +
				", startCycleOffset=" + startCycleOffset +
				", endCycleOffset=" + endCycleOffset +
				", slope=" + slope +
				", startHeight=" + startHeight +
				", endHeight=" + endHeight +
				", height=" + height +
				", remainingcycles=" + remainingcycles +
				", castGfx=" + castGfx +
				", castGfxHeight=" + castGfxHeight +
				", hitGfx=" + hitGfx +
				", hitGfxHeight=" + hitGfxHeight +
				", hitGfxStart=" + hitGfxStart +
				'}';
		}
	}

	@Subscribe(priority=-2000)
	public void onProjectileMoved(ProjectileMoved projectileMoved) {
		Projectile projectile = projectileMoved.getProjectile();

		// skip already seen projectiles.
		if (client.getGameCycle() >= projectile.getStartCycle()) {
			return;
		}

		Player player = null;
		for (Player p : client.getPlayers())
		{
			// This is the player's actual location which is what projectiles use as their start position. Player#getX, #getSceneX, etc., do not work here.
			final WorldPoint playerPos = p.getWorldLocation();
			if (playerPos == null)
			{
				return;
			}

			final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
			if (playerPosLocal.equals(p.getLocalLocation())) {

//			System.out.println("equal");
			} else {

//			System.out.println("not equal");
			}
			if (playerPosLocal == null)
			{
				return;
			}

			if (projectile.getX1() == playerPosLocal.getX() && projectile.getY1() == playerPosLocal.getY()) {
				player = p;
				break;
			}
		}

		spellStart(projectile, player);

//		// This is the player's actual location which is what projectiles use as their start position. Player#getX, #getSceneX, etc., do not work here.
//		Player player = client.getLocalPlayer();
//		final WorldPoint playerPos = player.getWorldLocation();
//		if (playerPos == null)
//		{
//			return;
//		}
//
//		final LocalPoint playerPosLocal = LocalPoint.fromWorld(client, playerPos);
//		if (playerPosLocal.equals(player.getLocalLocation())) {
//
////			System.out.println("equal");
//		} else {
//
////			System.out.println("not equal");
//		}
//		if (playerPosLocal == null)
//		{
//			return;
//		}
//
//		if (projectile.getX1() == playerPosLocal.getX() && projectile.getY1() == playerPosLocal.getY()) {
//			spellStart(projectile, p);
//		}
	}

	private int lastplayerspotanimframe = -1;
	private int lasttargetspotanimframe = -1;

	@Subscribe
	public void onGraphicChanged(GraphicChanged graphicChanged) {
//		System.out.println("ongraphicchanged " + client.getGameCycle() + " " + graphicChanged.getActor().getGraphicHeight() + " " + graphicChanged.getActor().getGraphic() + " " + graphicChanged.getActor().getName());
		Player player = client.getLocalPlayer();
		if (graphicChanged.getActor().equals(player)) {
			if (player.getSpotAnimFrame() == 1 && lastplayerspotanimframe != 1) {
//				currentProjectile.castGfxStart = client.getGameCycle() - currentProjectile.spellStart;
			}
			lastplayerspotanimframe = player.getSpotAnimFrame();
		} else if (plugin.client.getLocalPlayer() != null && plugin.client.getLocalPlayer().getInteracting() != null && graphicChanged.getActor().equals(plugin.client.getLocalPlayer().getInteracting())) {
			if (plugin.client.getLocalPlayer().getInteracting().getSpotAnimFrame() == 1 && lasttargetspotanimframe != 1) {
//				currentProjectile.hitGfxStart.put() = client.getGameCycle() - currentProjectile.spellStart;
			}
			lasttargetspotanimframe = plugin.client.getLocalPlayer().getInteracting().getSpotAnimFrame();
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted commandExecuted)
	{
		String[] arguments = commandExecuted.getArguments();
		String command = commandExecuted.getCommand();
	}
}


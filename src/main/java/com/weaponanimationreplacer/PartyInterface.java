package com.weaponanimationreplacer;

import static com.weaponanimationreplacer.WeaponAnimationReplacerPlugin.GROUP_NAME;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import net.runelite.api.events.PlayerSpawned;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.PartyChanged;
import net.runelite.client.party.PartyService;
import net.runelite.client.party.WSClient;
import net.runelite.client.party.events.UserPart;
import net.runelite.client.party.messages.PartyMemberMessage;

public class PartyInterface
{
	@Inject PartyService partyService;
	@Inject WeaponAnimationReplacerPlugin plugin;
	@Inject WSClient wsClient;

	Map<Long, PartyMemberData> partyData = new HashMap<>();

	@RequiredArgsConstructor
	static final class PartyMemberData {
		int baseArmsKit;
		int baseHairKit;
		int baseJawKit;
		List<TransmogSet> transmogSets;
		String playerName = null;
		final long memberId;

		public void update(NameUpdateMessage e)
		{
			this.playerName = e.playerName;
			this.baseArmsKit = e.baseArmsKit;
			this.baseHairKit = e.baseHairKit;
			this.baseJawKit = e.baseJawKit;
		}

		public void update(TransmogUpdateMessage e) {
			this.transmogSets = e.transmogSets;
		}
	}

	PartyMemberData getData(String playerName) {
		for (PartyMemberData data : partyData.values())
		{
			if (data.playerName != null && data.playerName.equals(playerName)) {
				return data;
			}
		}
		return null;
	}

	// wait for someone else to respond before sending transmog. No point sending if no one else has the plugin!
	boolean awaitingHelloResponse = false;
	@Subscribe public void onPartyChanged(PartyChanged e) {
		if (e.getPartyId() != null) {
			partyJoin();
		} else {
			partyLeave();
		}
	}

	@Subscribe public void onTransmogUpdateMessage(TransmogUpdateMessage e) {
		if (partyService.getLocalMember().getMemberId() == e.getMemberId()) return;
//		System.out.println("TransmogUpdateMessage " + partyService.getMemberById(e.getMemberId()).getDisplayName());

		PartyMemberData data = getPartyData(e.getMemberId());
		data.update(e);
		updateTransmog(data);

		if (awaitingHelloResponse) {
			awaitingHelloResponse = false;
			sendNameAndTransmog();
		}
	}

	@Subscribe public void onNameUpdateMessage(NameUpdateMessage e) {
		if (partyService.getLocalMember().getMemberId() == e.getMemberId()) return;

		PartyMemberData data = getPartyData(e.getMemberId());
//		System.out.println("Name update " + data.playerName + " -> " + e.playerName);
		data.update(e);
		updateTransmog(data);

		if (awaitingHelloResponse) {
			awaitingHelloResponse = false;
			sendNameAndTransmog();
		}
	}

	private void updateTransmog(PartyMemberData data)
	{
		updateTransmog(data, data.memberId);
	}

	private void updateTransmog(PartyMemberData data, long memberId)
	{
		plugin.clientThread.invoke(() -> {
			if (data == null) {
				plugin.leftParty(memberId);
			} else {
				plugin.partyTransmogReceived(data, memberId);
			}
		});
	}

	private PartyMemberData getPartyData(long memberId)
	{
		PartyMemberData data = partyData.get(memberId);
		if (data == null) {
			data = new PartyMemberData(memberId);
			partyData.put(memberId, data);
		}
		return data;
	}

	long lastSentTransmogHash;
	public void sendTransmog() {
		List<TransmogSet> activeTransmogs = plugin.transmogSets.stream().filter(set -> set.isEnabled()).collect(Collectors.toList());
//		System.out.println("sending transmog " + plugin.getGson().toJson(activeTransmogs).length() + " " + lastSentTransmogHash);
		lastSentTransmogHash = activeTransmogs.hashCode();
		plugin.pluginPanel.updatePartyButton();
//		System.out.println("   new hash is " + lastSentTransmogHash);
		partyService.send(new TransmogUpdateMessage(plugin.transmogSets));
	}

	private void sendName() {
//		System.out.println("sending name " + plugin.client.getLocalPlayer().getName());
		Integer baseArmsKit = plugin.configManager.getRSProfileConfiguration(GROUP_NAME, "baseArmsKit", Integer.class);
		baseArmsKit = baseArmsKit != null ? baseArmsKit : -1;
		Integer baseHairKit = plugin.configManager.getRSProfileConfiguration(GROUP_NAME, "baseHairKit", Integer.class);
		baseHairKit = baseHairKit != null ? baseHairKit : -1;
		Integer baseJawKit = plugin.configManager.getRSProfileConfiguration(GROUP_NAME, "baseJawKit", Integer.class);
		baseJawKit = baseJawKit != null ? baseJawKit : -1;
		partyService.send(new NameUpdateMessage(plugin.client.getLocalPlayer().getName(), baseArmsKit, baseHairKit, baseJawKit));
	}

	private void sendNameAndTransmog() {
		sendTransmog();
		if (plugin.client.getLocalPlayer() != null) {
			sendName();
		}
	}

	@Subscribe public void onWarpHello(WarpHello e) {
		if (partyService.getLocalMember().getMemberId() == e.getMemberId()) return;
//		System.out.println("WarpHello " + partyService.getMemberById(e.getMemberId()).getDisplayName());

		sendNameAndTransmog();
	}

	@Subscribe public void onWarpBye(WarpBye e) {
		if (partyService.getLocalMember().getMemberId() == e.getMemberId()) return;
//		System.out.println("WarpBye " + partyService.getMemberById(e.getMemberId()).getDisplayName());

		partyData.remove(e.getMemberId());
		updateTransmog(null, e.getMemberId());
	}

	@Subscribe public void onUserPart(UserPart e) {
		partyData.remove(e.getMemberId());
		updateTransmog(null, e.getMemberId());
	}

	@Subscribe public void onPlayerSpawned(PlayerSpawned e) {
		if (partyService.isInParty() && e.getPlayer() == plugin.client.getLocalPlayer()) {
			sendName();
		}
	}

	@RequiredArgsConstructor
	public static final class TransmogUpdateMessage extends PartyMemberMessage {
		final List<TransmogSet> transmogSets;
	}

	@RequiredArgsConstructor
	public static final class NameUpdateMessage extends PartyMemberMessage {
		final String playerName;
		final int baseArmsKit;
		final int baseHairKit;
		final int baseJawKit;
	}

	public static final class WarpHello extends PartyMemberMessage {
	}

	public static final class WarpBye extends PartyMemberMessage {
	}

	public void startUp()
	{
//		System.out.println("party share startup");
		wsClient.registerMessage(PartyInterface.WarpHello.class);
		wsClient.registerMessage(PartyInterface.TransmogUpdateMessage.class);
		wsClient.registerMessage(PartyInterface.NameUpdateMessage.class);
		wsClient.registerMessage(PartyInterface.WarpBye.class);

		if (partyService.isInParty()) {
//			System.out.println("   in party, sending hello");
			partyJoin();
		}
	}

	public void shutDown()
	{
//		System.out.println("party share shutdown");
		partyLeave();

		wsClient.unregisterMessage(PartyInterface.WarpHello.class);
		wsClient.unregisterMessage(PartyInterface.TransmogUpdateMessage.class);
		wsClient.unregisterMessage(PartyInterface.NameUpdateMessage.class);
		wsClient.unregisterMessage(PartyInterface.WarpBye.class);
	}

	private void partyJoin() {
		partyService.send(new WarpHello());
		awaitingHelloResponse = true;

		if (plugin.pluginPanel != null) plugin.pluginPanel.rebuild();
	}

	private void partyLeave() {
		if (partyService.isInParty()) {
			partyService.send(new WarpBye());
		}
		for (Long memberId : partyData.keySet()) {
			updateTransmog(null, memberId);
		}
		partyData.clear();

		if (plugin.pluginPanel != null) plugin.pluginPanel.rebuild();
	}

	public boolean canUpdate() {
		return lastSentTransmogHash != plugin.transmogSets.stream().filter(set -> set.isEnabled()).collect(Collectors.toList()).hashCode();
	}
}

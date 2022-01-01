package com.weaponanimationreplacer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

public class TransmogSet
{
	@Getter
	@Setter
	private String name = "";
	@Getter
	@Setter
	private boolean enabled = true;
	@Getter
	@Setter
	private boolean minimized = false;

	// TODO assert that this list is not empty.
	@Getter
	private final List<Swap> swaps = new ArrayList<>();

	public TransmogSet(List<Swap> swaps) {
		this.swaps.addAll(swaps);
	}

	public static TransmogSet createTemplate()
	{
		return new TransmogSet(Collections.singletonList(new Swap(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyList())));
	}

	public void addNewSwap()
	{
		swaps.add(Swap.createTemplate());
	}

	public void removeSwap(Swap swap)
	{
		swaps.remove(swap);
	}

	public void moveSwap(Swap swap, int i)
	{
		int swapIndex = swaps.indexOf(swap);
		if (swapIndex == -1) throw new IllegalArgumentException();
		if (swapIndex + i < 0 || swapIndex + i >= swaps.size()) return;

		swaps.remove(swap);
		swaps.add(swapIndex + i, swap);
	}

	@Override
	public String toString()
	{
		return "TransmogSet{" +
			"name='" + name + '\'' +
			", enabled=" + enabled +
			", minimized=" + minimized +
			", swaps=" + swaps +
			'}';
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}
		TransmogSet that = (TransmogSet) o;
		return enabled == that.enabled && minimized == that.minimized && name.equals(that.name) && swaps.equals(that.swaps);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(name, enabled, minimized, swaps);
	}
}

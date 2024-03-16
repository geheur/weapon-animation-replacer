package com.weaponanimationreplacer;

import lombok.Getter;
import lombok.Setter;
public class SoundSwap
{
	@Getter
	@Setter
	int toReplace = -1;

	@Getter
	@Setter
	int toReplaceWith = -1;

	public static SoundSwap createTemplate()
	{
		return new SoundSwap();
	}
}

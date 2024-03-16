package com.weaponanimationreplacer;

public class SoundSwap
{
	int toReplace = -1;
	int toReplaceWith = -1;

	public int getToReplace() {
		return toReplace;
	}

	public int getToReplaceWith() {
		return toReplaceWith;
	}

	public void setToReplace(int newValue)
	{
		toReplace = newValue;
	}

	public void setToReplaceWith(int newValue)
	{
		toReplaceWith = newValue;
	}

	public static SoundSwap createTemplate()
	{
		return new SoundSwap();
	}
}

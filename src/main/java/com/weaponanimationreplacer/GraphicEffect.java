package com.weaponanimationreplacer;

import java.awt.Color;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public class GraphicEffect
{
	enum Type {
		SCYTHE_SWING
	}

	public Type type;
	public Color color;

	public GraphicEffect(Type type, Color color) {
		this.type = type;
		this.color = color;
	}

	public static GraphicEffect createTemplate() {
		return new GraphicEffect(null, Color.CYAN);
	}
}

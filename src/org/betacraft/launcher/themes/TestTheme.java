package org.betacraft.launcher.themes;

import java.awt.Color;
import java.awt.Graphics;

public class TestTheme implements Theme {

	@Override
	public Color getFontColor1() {
		return Color.BLACK;
	}

	@Override
	public Color getFontColor2() {
		return Color.LIGHT_GRAY;
	}

	@Override
	public Color getFontColor3() {
		return Color.WHITE;
	}

	@Override
	public Color getBackground1() {
		return Color.WHITE;
	}

	@Override
	public Color getBackground2() {
		return Color.BLACK;
	}

	@Override
	public Graphics renderBackgroundDirt(int width, int height) {
		return null;
	}

	@Override
	public Graphics renderBackgroundStone(int width, int height) {
		return null;
	}
}

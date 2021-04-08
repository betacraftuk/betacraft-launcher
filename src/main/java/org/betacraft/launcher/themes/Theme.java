package org.betacraft.launcher.themes;

import java.awt.Color;
import java.awt.Graphics;

public interface Theme {

	public Color getFontColor1();
	public Color getFontColor2();
	public Color getFontColor3();
	public Color getBackground1();
	public Color getBackground2();
	public Graphics renderBackgroundDirt(int width, int height);
	public Graphics renderBackgroundStone(int width, int height);
}

package org.betacraft;

import java.util.ArrayList;
import java.util.List;

public abstract class Addon {

	public WhatToDo preAppletInit(Wrapper wrapper, ArrayList<Addon> addonsLeft) {
		return WhatToDo.NORMAL;
	}

	public WhatToDo postAppletInit(Wrapper wrapper, ArrayList<Addon> addonsLeft) {
		return WhatToDo.NORMAL;
	}

	/**
	 * @return List of addons to load before this addon
	 */
	public List<String> applyAfter() {
		return new ArrayList<String>();
	}

	public String getName() {
		String[] name = this.getClass().getName().split("\\.");
		return name[name.length - 1];
	}

	public enum WhatToDo {
		/** Stops later addons to get involved */
		STOP_LOOP,
		/** Everything as usual */
		NORMAL,
		/** Stops the code in a certain place */
		STOP_CODE;
	}
}

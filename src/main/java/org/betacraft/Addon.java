package org.betacraft;

import java.util.ArrayList;
import java.util.List;

public interface Addon {

	default public WhatToDo preAppletInit(Wrapper wrapper, ArrayList<Addon> addonsLeft) {
		return WhatToDo.NORMAL;
	}

	default public WhatToDo postAppletInit(Wrapper wrapper, ArrayList<Addon> addonsLeft) {
		return WhatToDo.NORMAL;
	}

	/**
	 * @return List of addons to load before this addon
	 */
	default public List<String> applyAfter() {
		return new ArrayList<>();
	}

	default public String getName() {
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

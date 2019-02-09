package org.betacraft.launcher;

public class VersionSorter {

	public static Release[] sort(Order order) {
		Release[] list = new Release[Release.versions.size()];
		if (order == Order.FROM_OLDEST) {
			int i = 0;
			for (Release item : Release.versions) {
				list[i] = item;
				i++;
			}
		} else if (order == Order.FROM_NEWEST) {
			int i = list.length - 1;
			for (Release item : Release.versions) {
				list[i] = item;
				i--;
			}
		}
		return list;
	}

	public enum Order {
		FROM_NEWEST,
		FROM_OLDEST;
	}
}

package com.polixis.batchtask.util;

import java.util.HashMap;
import java.util.Map;

public class DateFormatterUtils {

	private static final Map<Long, String> ORDINAL_SUFFIX_MAP;

	static {
		Map<Long, String> map = new HashMap<>();
		for (int i = 1; i <= 31; i++) {
			String suffix = switch (i) {
				case 1, 21, 31 -> "st";
				case 2, 22     -> "nd";
				case 3, 23     -> "rd";
				default        -> "th";
			};
			map.put((long) i, i + suffix);
		}
		ORDINAL_SUFFIX_MAP = Map.copyOf(map);
	}

	public static Map<Long, String> getOrdinalSuffixMap() {
		return ORDINAL_SUFFIX_MAP;
	}
}

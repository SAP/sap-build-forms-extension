package com.sap.bfx.definition;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

@Slf4j
public class StandardVisualEvaluator implements Evaluator<Boolean> {

	public static final char SEPARATOR = ';';
	private static final int MATCH_TYPE_EXACT = 0;
	private static final int MATCH_TYPE_STARTS_WITH = 1;
	private static final int MATCH_TYPE_ENDS_WITH = 2;
	private static final int MATCH_TYPE_CONTAINS = 3;

	private Collection<StateInfo> states  = new ArrayList<>();

	/**
	 * Constructor
	 *
	 * @param state Semicolon separated list of visual states
	 */
	public StandardVisualEvaluator(final String state) {
		final var states = StringUtils.split(StringUtils.trim(state), SEPARATOR);
		if (states != null) {
			this.states = Stream.of(states).filter(StringUtils::isNotBlank).map(s -> {
				final var stateInfo = new StateInfo(s, MATCH_TYPE_EXACT);
				if (s.endsWith("*") && s.startsWith("*")) {
					stateInfo.state = StringUtils.trim(StringUtils.removeEnd(StringUtils.removeStart(s, "*"), "*"));
					stateInfo.matchType = MATCH_TYPE_CONTAINS;
				} else if (s.endsWith("*")) {
					stateInfo.state = StringUtils.trim(StringUtils.removeEnd(s, "*"));
					stateInfo.matchType = MATCH_TYPE_STARTS_WITH;
				} else if (s.startsWith("*")) {
					stateInfo.state = StringUtils.trim(StringUtils.removeStart(s, "*"));
					stateInfo.matchType = MATCH_TYPE_ENDS_WITH;
				}
				return stateInfo;
			}).toList();
		}
	}

	/**
	 * Evaluate visual state
	 *
	 * @param ctx          Context
	 * @param isInitial    true if initial evaluation
	 * @param defaultValue Default value
	 * @return true if visual state matches
	 */
	@Override
	public Boolean eval(Context<? extends AccessClass> ctx, boolean isInitial, Boolean defaultValue) {
		if (!isInitial) {
			return defaultValue;
		}

		if (states != null) {
			for (var state : states) {
				switch (state.matchType) {
				case MATCH_TYPE_EXACT:
					if (StringUtils.equalsIgnoreCase(state.state, ctx.getDisplayState())) {
						return Boolean.TRUE;
					}
					break;
				case MATCH_TYPE_STARTS_WITH:
					if (StringUtils.startsWithIgnoreCase(ctx.getDisplayState(), state.state)) {
						return Boolean.TRUE;
					}
					break;				
				case MATCH_TYPE_ENDS_WITH:
					if (StringUtils.endsWithIgnoreCase(ctx.getDisplayState(), state.state)) {
						return Boolean.TRUE;
					}
					break;
				case MATCH_TYPE_CONTAINS:
						if (StringUtils.containsIgnoreCase(ctx.getDisplayState(), state.state)) {
						return Boolean.TRUE;
					}
						break;
				}
			}
		}
		return Boolean.FALSE;
	}

	/**
	 * State information
	 */
	@Data
	@AllArgsConstructor
	static class StateInfo {
		String state;
		int matchType; // 0 = exact, 1 = startsWith, 2 = endsWith, 3 = contains
	}
}

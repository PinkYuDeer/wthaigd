package com.pinkyudeer.wthaigd.task.team;

import com.pinkyudeer.wthaigd.integration.betterquesting.BetterQuestingTeamProvider;
import com.pinkyudeer.wthaigd.integration.gtnhlib.GtnhLibTeamProvider;

public final class TeamProviders {

    private static final TeamProvider LOCAL = new LocalTeamProvider();
    private static final TeamProvider BETTER_QUESTING = new BetterQuestingTeamProvider();
    private static final TeamProvider GTNH_LIB = new GtnhLibTeamProvider();

    private TeamProviders() {}

    public static TeamProvider local() {
        return LOCAL;
    }

    public static TeamProvider betterQuesting() {
        return BETTER_QUESTING;
    }

    public static TeamProvider gtnhLib() {
        return GTNH_LIB;
    }
}

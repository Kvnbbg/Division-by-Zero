package fr.kvnbbg.tdaah.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Side-hustle machine — OFFER → VALIDATED → SOLD → REPEATABLE → SYSTEM.
 * Named refusals only (aligned with TDAAH / Instructions Datacenter notes).
 */
public enum SideHustleState {
    OFFER,
    VALIDATED,
    SOLD,
    REPEATABLE,
    SYSTEM;

    private static final Map<SideHustleState, Set<SideHustleState>> NEXT =
            Map.of(
                    OFFER, EnumSet.of(VALIDATED),
                    VALIDATED, EnumSet.of(SOLD),
                    SOLD, EnumSet.of(REPEATABLE),
                    REPEATABLE, EnumSet.of(SYSTEM),
                    SYSTEM, EnumSet.noneOf(SideHustleState.class));

    public boolean canGoTo(SideHustleState to) {
        return NEXT.getOrDefault(this, Set.of()).contains(to);
    }

    public SideHustleState goTo(SideHustleState to) {
        if (!canGoTo(to)) {
            throw new IllegalStateException(
                    "Named refusal: cannot move " + this + " → " + to);
        }
        return to;
    }
}

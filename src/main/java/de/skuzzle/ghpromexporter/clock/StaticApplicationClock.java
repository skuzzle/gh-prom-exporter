package de.skuzzle.ghpromexporter.clock;

import java.time.Clock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

public final class StaticApplicationClock {

    private static final Logger logger = LoggerFactory.getLogger(StaticApplicationClock.class);

    private StaticApplicationClock() {
        // hidden
    }

    private static volatile Clock APPLICATION_CLOCK = Clock.systemUTC();

    public static Clock get() {
        if (logger.isDebugEnabled()) {
            final boolean clockChanged = !APPLICATION_CLOCK.equals(Clock.systemUTC());
            if (clockChanged) {
                logger.debug("Accessed global application clock which has been changed from UTC to {}",
                        APPLICATION_CLOCK);
            }
        }
        return APPLICATION_CLOCK;
    }

    public static void resetToDefaultClock() {
        final boolean clockChanged = !APPLICATION_CLOCK.equals(Clock.systemUTC());
        if (clockChanged) {
            APPLICATION_CLOCK = Clock.systemUTC();
            logger.info("Reset Application clock to UTC");
        }
    }

    public static void changeTo(Clock clock) {
        Preconditions.checkArgument(clock != null, "global StaticApplicationClock can not be null");
        if (clock.equals(APPLICATION_CLOCK)) {
            // early return to avoid warning log message
            return;
        }
        APPLICATION_CLOCK = clock;
        final boolean clockChanged = !APPLICATION_CLOCK.equals(Clock.systemUTC());
        if (clockChanged) {
            logger.warn("Application clock has been changed globally to {}", clock);
        }
    }
}

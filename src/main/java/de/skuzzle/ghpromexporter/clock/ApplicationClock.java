package de.skuzzle.ghpromexporter.clock;

import java.time.Clock;

public interface ApplicationClock {

    static final ApplicationClock DEFAULT = StaticApplicationClock::get;

    Clock get();
}

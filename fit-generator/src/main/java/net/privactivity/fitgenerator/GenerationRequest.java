package net.privactivity.fitgenerator;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;

public record GenerationRequest(String fromTown,
                                String toTown,
                                LocalDate date,
                                LocalTime time,
                                long seed,
                                Path outDir) {
}

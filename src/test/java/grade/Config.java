package grade;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
	/**
	 * The seed used to randomize test cases.
	 * <p>
	 * When grading, use the original number.
	 * <p>
	 * When debugging, use any fixed number for
	 * deterministic or null for nondeterministic.
	 */
	static final Integer RANDOM_SEED = 2024_08;

	/**
	 * The folder where logs are generated.
	 */
	static final Path LOGS_DIRECTORY = Paths.get("db", "logs");
}

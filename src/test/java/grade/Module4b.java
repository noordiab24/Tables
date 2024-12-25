package grade;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.ClassOrderer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestMethodOrder;

@DisplayName("M4b XML Table")
@TestClassOrder(ClassOrderer.ClassName.class)
final class Module4b extends AbstractModule {
	@BeforeAll
	void defineModule() {
		containers = 4;
		operations = 250;
		elements = 100;
	}

	@Nested
	@DisplayName("m4b_table1 [hit/miss only]")
	class Table1 extends XMLTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m4b_table1";
			columns = List.of("k1", "f1a", "f1b", "f1c");
			coverage = TestCoverage.HIT_MISS_ONLY;
		}
	}

	@Nested
	@DisplayName("m4b_table2 [hit/miss, fingerprint, iterator]")
	class Table2 extends XMLTableContainer {
		@BeforeAll
		void defineTable() {
			name = "m4b_table2";
			columns = List.of("k2", "f2a", "f2b", "f2c", "f2d", "f2e");
			coverage = TestCoverage.HM_FP_ITERATOR;
		}
	}

	@TestMethodOrder(MethodOrderer.MethodName.class)
	abstract class XMLTableContainer extends AbstractTableContainer {
		static final List<String> exempt = List.of(
    		"model",
			"tables",
			"java.nio.file.Path",
			"org.dom4j.Document",
			"org.dom4j.DocumentFactory"
		);

		@TestFactory
		@DisplayName("New XML Table")
		Stream<DynamicTest> testNewTable() {
			logStart("new");

			subject = testConstructor(
				"tables.XMLTable",
				List.of(String.class, List.class),
				List.of(name, columns),
				exempt
			);

			control = new ControlTable();

			return IntStream.range(0, operations).mapToObj(i -> {
				if (i == 0)
					return testName();
				else if (i == 1)
					return testColumns();
				else if (i == 2)
					return testClear();
				else if ((i % 20 == 0 || i == operations-1) && coverage.compareTo(TestCoverage.HM_FP_ITERATOR) >= 0)
					return testIterator();
				else {
					if (control.size() < elements * .99)
						return testPut(false, null);
					else if (control.size() > elements * 1.01)
						return testRemove(true, null);
					else if (RNG.nextBoolean())
						return testPut(RNG.nextBoolean(), null);
					else
						return testRemove(RNG.nextBoolean(), null);
				}
			});
		}

		@TestFactory
		@DisplayName("Existing XML Table")
		Stream<DynamicTest> thenTestExistingTable() {
			logStart("existing");

			subject = testConstructor(
				"tables.XMLTable",
				List.of(String.class),
				List.of(name),
				exempt
			);

			return IntStream.range(0, operations).mapToObj(i -> {
				if (i == 0)
					return testName();
				else if (i == 1)
					return testColumns();
				else if ((i == 2 || i % 20 == 0 || i == operations-1) && coverage.compareTo(TestCoverage.HM_FP_ITERATOR) >= 0)
					return testIterator();
				else {
					if (control.size() < elements * .99)
						return testPut(false, null);
					else if (control.size() > elements * 1.01)
						return testRemove(true, null);
					else if (RNG.nextBoolean())
						return testGet(RNG.nextBoolean());
					else if (RNG.nextBoolean())
						return testPut(RNG.nextBoolean(), null);
					else
						return testRemove(RNG.nextBoolean(), null);
				}
			});
		}
	}
}
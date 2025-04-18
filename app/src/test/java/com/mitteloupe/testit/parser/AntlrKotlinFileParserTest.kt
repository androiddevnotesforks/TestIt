package com.mitteloupe.testit.parser

import com.mitteloupe.testit.grammer.KotlinParserException
import com.mitteloupe.testit.model.ClassMetadata
import com.mitteloupe.testit.model.DataType
import com.mitteloupe.testit.model.FileMetadata
import com.mitteloupe.testit.model.FunctionMetadata
import com.mitteloupe.testit.model.StaticFunctionsMetadata
import com.mitteloupe.testit.model.TypedParameter
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.given

private const val PACKAGE_NAME = "com.test.String"
private val singleImport = mapOf("ClassName" to "com.import.ClassName")

private val unitDataType = DataType.Specific("Unit", false)

@RunWith(MockitoJUnitRunner::class)
class AntlrKotlinFileParserTest {
    private lateinit var classUnderTest: AntlrKotlinFileParser

    @Mock
    lateinit var dataTypeParser: DataTypeParser

    @Before
    fun setUp() {
        classUnderTest = AntlrKotlinFileParser(dataTypeParser)
    }

    @Test
    fun `Given minimal class when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package $PACKAGE_NAME\n" +
            "class Test {}\n"

        val expected = getExpectedFileMetadata(
            listOf(
                ClassMetadata(PACKAGE_NAME, emptyMap(), "Test", false, emptyList(), emptyList())
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given function with reserved name when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package $PACKAGE_NAME\n" +
            "class Test {" +
            "override fun onTest(data: List<Data>) {}\n" +
            "}\n"

        val expectedParameterDataType = DataType.Specific("ListOfData", false)
        given { dataTypeParser.parse("List<Data>") }
            .willReturn(expectedParameterDataType)

        val expected = getExpectedFileMetadata(
            listOf(
                ClassMetadata(
                    PACKAGE_NAME,
                    emptyMap(),
                    "Test",
                    false,
                    emptyList(),
                    listOf(
                        FunctionMetadata(
                            "onTest",
                            false,
                            listOf(TypedParameter("data", expectedParameterDataType)),
                            null,
                            unitDataType
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given code with trailing commas when String#parse then returns valid result`() {
        // Given
        val receiver = "package $PACKAGE_NAME\n" +
            "class Test {\n" +
            "    fun onTest(data: List<Data>,) {\n" +
            "        val list = listOf()\n" +
            "        val lambda = { argument: String, ->\n" +
            "        }" +
            "    }\n" +
            "}\n"

        val expectedParameterDataType = DataType.Specific("ListOfData", false)
        given { dataTypeParser.parse("List<Data>") }
            .willReturn(expectedParameterDataType)

        val expected = getExpectedFileMetadata(
            listOf(
                ClassMetadata(
                    PACKAGE_NAME,
                    emptyMap(),
                    "Test",
                    false,
                    emptyList(),
                    listOf(
                        FunctionMetadata(
                            "onTest",
                            false,
                            listOf(TypedParameter("data", expectedParameterDataType)),
                            null,
                            unitDataType
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    fun `Given child dependency when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package $PACKAGE_NAME\n" +
            "import com.import.ClassName\n" +
            "class Test {" +
            "override fun onTest(dependency: ClassName.ChildDependency) {}\n" +
            "}\n"

        val expectedParameterDataType = DataType.Specific("ClassName.ChildDependency", false)
        given { dataTypeParser.parse("ClassName.ChildDependency") }
            .willReturn(expectedParameterDataType)

        val expected = getExpectedFileMetadata(
            listOf(
                ClassMetadata(
                    PACKAGE_NAME,
                    mapOf("ClassName" to "com.import.ClassName"),
                    "Test",
                    false,
                    emptyList(),
                    listOf(
                        FunctionMetadata(
                            "onTest",
                            false,
                            listOf(TypedParameter("dependency", expectedParameterDataType)),
                            null,
                            unitDataType
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Given class with function returning value when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package com.test.String\n" +
            "import com.import.ClassName\n" +
            "class Test {\n" +
            "fun doIt():ClassName{}\n" +
            "}\n"

        given { dataTypeParser.parse("ClassName") }
            .willReturn(DataType.Specific("ClassName", false))

        val expected = getExpectedFileMetadata(
            listOf(
                getExpectedClassMetadata(
                    imports = emptyMap(),
                    functions = listOf(
                        FunctionMetadata(
                            "doIt",
                            false,
                            emptyList(),
                            null,
                            DataType.Specific("ClassName", false)
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Given class with function returning nullable value when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package com.test.String\n" +
            "import com.import.ClassName\n" +
            "class Test {\n" +
            "fun doIt():ClassName?{}\n" +
            "}\n"

        given { dataTypeParser.parse("ClassName?") }
            .willReturn(DataType.Specific("ClassName", true))

        val expected = getExpectedFileMetadata(
            listOf(
                getExpectedClassMetadata(
                    imports = emptyMap(),
                    functions = listOf(
                        FunctionMetadata(
                            "doIt",
                            false,
                            emptyList(),
                            null,
                            DataType.Specific("ClassName", true)
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Given class with function with parameter when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package com.test.String\n" +
            "import com.import.ClassName\n" +
            "class Test {\n" +
            "fun doIt(className:ClassName){}\n" +
            "}\n"

        given { dataTypeParser.parse("ClassName") }
            .willReturn(DataType.Specific("ClassName", false))

        val expected = getExpectedFileMetadata(
            listOf(
                getExpectedClassMetadata(
                    imports = singleImport,
                    functions = listOf(
                        FunctionMetadata(
                            "doIt",
                            false,
                            listOf(
                                TypedParameter("className", DataType.Specific("ClassName", false))
                            ),
                            null,
                            unitDataType
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test
    @Suppress("ktlint:standard:max-line-length")
    fun `Given class with function with nullable parameter when String#parse then returns expected metadata`() {
        // Given
        val receiver = "package com.test.String\n" +
            "import com.import.ClassName\n" +
            "class Test {\n" +
            "fun doIt(className:ClassName<Type?>?){}\n" +
            "}\n"

        given { dataTypeParser.parse("ClassName<Type?>?") }
            .willReturn(DataType.Specific("ClassName", true))

        val expected = getExpectedFileMetadata(
            listOf(
                getExpectedClassMetadata(
                    imports = singleImport,
                    functions = listOf(
                        FunctionMetadata(
                            "doIt",
                            false,
                            listOf(
                                TypedParameter("className", DataType.Specific("ClassName", true))
                            ),
                            null,
                            unitDataType
                        )
                    )
                )
            )
        )

        // When
        val actualValue = with(classUnderTest) {
            receiver.parse()
        }

        // Then
        assertEquals(expected, actualValue)
    }

    @Test(expected = KotlinParserException::class)
    fun `Given invalid code when String#parse then throws exception`() {
        // Given
        val receiver = "Not valid code"

        // When
        with(classUnderTest) {
            receiver.parse()
        }

        // Then
        // Exception is thrown
    }

    private fun getExpectedClassMetadata(
        imports: Map<String, String> = emptyMap(),
        isClassAbstract: Boolean = false,
        constructorParameters: List<TypedParameter> = emptyList(),
        functions: List<FunctionMetadata> = emptyList()
    ) = ClassMetadata(
        PACKAGE_NAME,
        imports,
        "Test",
        isClassAbstract,
        constructorParameters,
        functions
    )

    private fun getExpectedFileMetadata(
        classMetaDatas: List<ClassMetadata> = emptyList(),
        staticFunctionsMetadata: StaticFunctionsMetadata =
            StaticFunctionsMetadata(PACKAGE_NAME, emptyMap(), emptyList())
    ) = FileMetadata(
        classMetaDatas,
        staticFunctionsMetadata
    )
}

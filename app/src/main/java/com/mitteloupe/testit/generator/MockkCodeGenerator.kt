package com.mitteloupe.testit.generator

import com.mitteloupe.testit.model.ClassMetadata

class MockkCodeGenerator : MockerCodeGenerator() {
    private val requiredImports = mutableSetOf<String>()

    private var _hasMockedConstructorParameters = false

    private val stringBuilder by lazy { StringBuilder() }

    override val testClassAnnotation: String? = null

    override val usedImports = emptyMap<String, String>()

    override val knownImports = mapOf(
        "MockKAnnotations" to "io.mockk.MockKAnnotations",
        "MockK" to "io.mockk.impl.annotations.MockK",
        "mockk" to "io.mockk.mockk"
    )

    override val setUpStatements: String?
        get() = if (_hasMockedConstructorParameters) {
            "${INDENT_2}MockKAnnotations.init(this, relaxUnitFun = true)\n"
        } else {
            null
        }

    override fun getConstructorMock(parameterName: String, parameterType: String) =
        "$INDENT@MockK\n" +
                "${INDENT}lateinit var $parameterName: $parameterType"

    override fun getAbstractClassUnderTest(classUnderTest: ClassMetadata): String {
        stringBuilder.clear()
            .append("object : ${classUnderTest.className}() {\n")

        classUnderTest.functions
            .filter { it.isAbstract }
            .forEach { function ->
                val mockedValue = getMockedValue(function.returnType, function.name)
                stringBuilder.append("${INDENT_3}override fun test2() = $mockedValue\n")
            }

        stringBuilder.append("$INDENT_2}")

        return stringBuilder.toString()
    }

    override fun getMockedInstance(variableType: String) = "mockk<$variableType>()"

    override fun getRequiredImports() = requiredImports

    override fun setHasMockedConstructorParameters() {
        _hasMockedConstructorParameters = true
        requiredImports.add("MockKAnnotations")
        requiredImports.add("MockK")
    }

    override fun setHasMockedFunctionParameters() {
        requiredImports.add("mockk")
    }

    override fun setIsAbstractClassUnderTest() {
    }
}
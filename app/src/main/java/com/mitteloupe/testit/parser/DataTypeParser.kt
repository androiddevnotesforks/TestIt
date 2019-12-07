package com.mitteloupe.testit.parser

import com.mitteloupe.testit.model.DataType

class DataTypeParser {
    fun parse(dataType: String): DataType {
        val parsedTreeResult = parseToTree(dataType)
        val token = parsedTreeResult.tokens.firstOrNull()
            ?: throw IllegalArgumentException("Input could not be parsed: $dataType")
        return token.toDataType()
    }

    private fun parseToTree(sourceData: String): TokenParsingResult {
        val tokens = mutableListOf<ParsingToken>()

        val totalCharacter = sourceData.length
        var position = 0
        var tokenStartPosition = 0
        do {
            when (sourceData.substring(position, position + 1)) {
                "<" -> {
                    val childrenParsingResult = parseToTree(sourceData.substring(position + 1))
                    if (position != tokenStartPosition) {
                        val tokenName = sourceData.take(position)
                        tokens.add(ParsingToken(tokenName, childrenParsingResult.tokens))
                    }
                    position += childrenParsingResult.charactersParsed + 1
                    tokenStartPosition = position
                }
                "," -> {
                    if (position != 0) {
                        val tokenName = sourceData.substring(tokenStartPosition, position)
                        tokens.addOrAppendIfNullable(tokenName)
                    }
                    val siblingsParsingResult = parseToTree(sourceData.substring(position + 1))
                    tokens.addAll(siblingsParsingResult.tokens)
                    position += siblingsParsingResult.charactersParsed
                    tokenStartPosition = position
                }
                ">" -> {
                    if (tokenStartPosition != position) {
                        val tokenName = sourceData.substring(tokenStartPosition, position)
                        tokens.add(ParsingToken(tokenName))
                    }
                    return TokenParsingResult(tokens, position + 1)
                }
                else -> {
                    position++
                }
            }
        } while (position < totalCharacter)

        if (position != tokenStartPosition) {
            val tokenName = sourceData.substring(tokenStartPosition, position)
            tokens.addOrAppendIfNullable(tokenName)
        }

        return TokenParsingResult(tokens, position)
    }
}

private fun MutableList<ParsingToken>.addOrAppendIfNullable(tokenName: String) {
    if (tokenName == "?") {
        val lastToken = removeAt(size - 1)
        add(ParsingToken(lastToken.name + "?", lastToken.children))
    } else {
        add(ParsingToken(tokenName))
    }
}

private fun ParsingToken.toDataType(): DataType {
    val isNullable = name.endsWith("?")
    val dataTypeName = if (isNullable) {
        name.dropLast(1)
    } else {
        name
    }
    return if (children.isEmpty()) {
        DataType.Specific(dataTypeName, isNullable)
    } else {
        DataType.Generic(
            dataTypeName,
            isNullable,
            *children.map { token -> token.toDataType() }.toTypedArray()
        )
    }
}

private data class ParsingToken(
    val name: String,
    val children: List<ParsingToken> = mutableListOf()
)

private data class TokenParsingResult(
    val tokens: List<ParsingToken>,
    val charactersParsed: Int
)
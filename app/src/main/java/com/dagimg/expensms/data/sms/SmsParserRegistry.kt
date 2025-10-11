package com.dagimg.expensms.data.sms

/**
 * Registry that manages all bank SMS parsers
 * Uses Strategy Pattern to find appropriate parser for each SMS
 */
class SmsParserRegistry {
    private val parsers: MutableList<BankSmsParser> = mutableListOf()

    init {
        // Register all available parsers
        registerParser(CbeSmsParser())
        registerParser(TelebirrSmsParser())
        registerParser(AbyssiniaSmsParser())
    }

    /**
     * Register a new bank parser
     */
    fun registerParser(parser: BankSmsParser) {
        parsers.add(parser)
    }

    /**
     * Find the appropriate parser for the given SMS
     */
    fun findParser(
        sender: String,
        message: String,
    ): BankSmsParser? =
        parsers.firstOrNull { parser ->
            parser.canParse(sender, message)
        }

    /**
     * Parse SMS using the appropriate parser
     */
    fun parseTransaction(
        sender: String,
        message: String,
    ): ParsedTransaction? = findParser(sender, message)?.parse(sender, message)

    /**
     * Get all registered bank names
     */
    fun getSupportedBanks(): List<String> = parsers.map { it.bankName }

    /**
     * Get all supported sender IDs
     */
    fun getAllSenderIds(): List<String> = parsers.flatMap { it.senderIds }

    /**
     * Get all registered parsers (for CLI/testing purposes)
     */
    fun getAllParsers(): List<BankSmsParser> = parsers.toList()
}

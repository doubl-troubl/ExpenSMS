package com.dagimg.expensms

import com.dagimg.expensms.data.sms.BankSmsParser
import com.dagimg.expensms.data.sms.ParsedTransaction
import com.dagimg.expensms.data.sms.SmsParserRegistry

/**
 * Command Line Interface for testing SMS parsing functionality
 *
 * Usage patterns:
 *   SmsParserCLI --help                    # Show help
 *   SmsParserCLI --list                    # List all available banks
 *   SmsParserCLI <bank> <sms_text>         # Test specific bank parser
 *   SmsParserCLI auto <sms_text>           # Auto-detect bank and parse
 *   SmsParserCLI test-all <sms_text>       # Test all parsers and show results
 *
 * Examples:
 *   SmsParserCLI cbe "Dear User your Account 1*****123 has been debited with ETB 500.00. Your Current Balance is ETB 25000.00"
 *   SmsParserCLI auto "Your account has been credited with ETB 1000.00. Balance: ETB 15000.00"
 *   SmsParserCLI test-all "Transfer of ETB 200.00 to merchant ABC on 15/12/2024"
 */
fun main(args: Array<String>) {
    if (args.isEmpty() || args[0] == "--help" || args[0] == "-h") {
        showHelp()
        return
    }

    if (args[0] == "--list" || args[0] == "-l") {
        listBanks()
        return
    }

    // Initialize parser registry
    val parserRegistry = SmsParserRegistry()

    when {
        args.size >= 2 && args[0] == "auto" -> {
            val smsText = args.drop(1).joinToString(" ")
            testAutoDetect(parserRegistry, smsText)
        }
        args.size >= 2 && args[0] == "test-all" -> {
            val smsText = args.drop(1).joinToString(" ")
            testAllParsers(parserRegistry, smsText)
        }
        args.size >= 2 -> {
            val bank = args[0].lowercase()
            val smsText = args.drop(1).joinToString(" ")
            testSpecificBank(parserRegistry, bank, smsText)
        }
        else -> {
            println("❌ Invalid usage")
            showHelp()
        }
    }
}

private fun showHelp() {
    println("🔍 SMS Parser CLI - Test Bank SMS Parsing")
    println()
    println("USAGE:")
    println("  SmsParserCLI <command> [options]")
    println()
    println("COMMANDS:")
    println("  --help, -h              Show this help message")
    println("  --list, -l              List all available bank parsers")
    println("  <bank> <sms_text>       Test specific bank parser")
    println("  auto <sms_text>         Auto-detect bank and parse SMS")
    println("  test-all <sms_text>     Test all parsers and show results")
    println()
    println("AVAILABLE BANKS:")
    listBanks(showHeader = false)
    println()
    println("EXAMPLES:")
    println(
        "  SmsParserCLI cbe \"Dear User your Account 1*****123 has been debited with ETB 500.00. Your Current Balance is ETB 25000.00\"",
    )
    println("  SmsParserCLI auto \"Your account has been credited with ETB 1000.00. Balance: ETB 15000.00\"")
    println("  SmsParserCLI test-all \"Transfer of ETB 200.00 to merchant ABC on 15/12/2024\"")
    println()
    println("NOTES:")
    println("  - SMS text should be enclosed in quotes if it contains spaces")
    println("  - Use 'auto' for automatic bank detection")
    println("  - Use 'test-all' to see how all parsers handle the same SMS")
}

private fun listBanks(showHeader: Boolean = true) {
    val parserRegistry = SmsParserRegistry()

    if (showHeader) {
        println("🏦 Available Bank Parsers:")
        println()
    }

    val banks = parserRegistry.getSupportedBanks()
    if (banks.isEmpty()) {
        println("  No parsers registered")
    } else {
        banks.forEachIndexed { index, bank ->
            println("  ${index + 1}. $bank")
        }
    }

    if (showHeader) {
        println()
        println("💡 Tip: Use 'auto' mode to let the system detect the bank automatically")
    }
}

private fun testSpecificBank(
    parserRegistry: SmsParserRegistry,
    bankName: String,
    smsText: String,
) {
    println("🔍 Testing SMS Parser")
    println("Bank: $bankName")
    println("SMS: $smsText")
    println()

    // Find the specific parser
    val parser =
        parserRegistry.getAllParsers().firstOrNull {
            it.bankName.lowercase().contains(bankName) || bankName in it.senderIds.map { it.lowercase() }
        }

    if (parser == null) {
        println("❌ No parser found for bank: $bankName")
        println()
        println("Available banks:")
        listBanks(showHeader = false)
        return
    }

    println("✅ Found parser: ${parser.bankName}")

    // Test if parser can handle this SMS
    val canParse = parser.canParse("", smsText)
    if (!canParse) {
        println("⚠️  Parser indicates it cannot handle this SMS format")
        println("   This might be expected if the SMS format doesn't match ${parser.bankName} patterns")
    }

    // Parse the SMS
    val result = parser.parse("", smsText)

    if (result == null) {
        println("❌ Failed to parse SMS")
        return
    }

    // Display results
    displayParsedTransaction(result)
}

private fun testAutoDetect(
    parserRegistry: SmsParserRegistry,
    smsText: String,
) {
    println("🔍 Auto-Detecting Bank Parser")
    println("SMS: $smsText")
    println()

    // Find appropriate parser
    val parser = parserRegistry.findParser("", smsText)

    if (parser == null) {
        println("❌ No parser found for this SMS")
        println()
        println("💡 Try using 'test-all' to see results from all parsers")
        return
    }

    println("✅ Auto-detected parser: ${parser.bankName}")

    // Parse the SMS
    val result = parser.parse("", smsText)

    if (result == null) {
        println("❌ Failed to parse SMS")
        return
    }

    // Display results
    displayParsedTransaction(result)
}

private fun testAllParsers(
    parserRegistry: SmsParserRegistry,
    smsText: String,
) {
    println("🔍 Testing All SMS Parsers")
    println("SMS: $smsText")
    println()

    var successfulParses = 0
    val results = mutableListOf<Pair<BankSmsParser, ParsedTransaction?>>()

    for (parser in parserRegistry.getAllParsers()) {
        println("Testing ${parser.bankName}...")
        val canParse = parser.canParse("", smsText)
        val result = if (canParse) parser.parse("", smsText) else null

        results.add(parser to result)

        if (result != null) {
            successfulParses++
            println("  ✅ Parsed successfully")
        } else if (canParse) {
            println("  ❌ Can parse but parsing failed")
        } else {
            println("  ⏭️  Skipped (format not recognized)")
        }
    }

    println()
    println("📊 Summary: $successfulParses/${parserRegistry.getAllParsers().size} parsers succeeded")
    println()

    // Show detailed results
    for ((parser, result) in results) {
        if (result != null) {
            println("🏦 ${parser.bankName} Results:")
            displayParsedTransaction(result, compact = true)
            println()
        }
    }

    if (successfulParses == 0) {
        println("💡 No parsers were able to handle this SMS format")
        println("   This might indicate an unsupported bank or invalid SMS format")
    }
}

private fun displayParsedTransaction(
    result: ParsedTransaction,
    compact: Boolean = false,
) {
    if (compact) {
        println("  📊 Merchant: ${result.merchant}")
        println("     Amount: ${result.amount} ETB (${result.type})")
        println("     Balance: ${result.balanceAfter} ETB")
        println("     URL: ${result.transactionUrl ?: "None"}")
    } else {
        println("📊 Parsed Transaction:")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("Merchant: ${result.merchant}")
        println("Amount: ${result.amount} ETB")
        println("Type: ${result.type}")
        println("Balance After: ${result.balanceAfter} ETB")
        println("Timestamp: ${result.timestamp}")
        println("Transaction URL: ${result.transactionUrl ?: "None"}")
        println("Bank: ${result.bankName}")
        println("Raw SMS: ${result.rawSms}")
        println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("✅ Parsing successful!")
    }
}

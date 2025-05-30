# Azrova's Simple Economy

A simple, lightweight, and configurable economy plugin for PaperMC servers, designed to be easy to use and integrate with other plugins like Vault and PlaceholderAPI.

Made by CNethuka with ❤️

For the latest updates, bug reports, and feature requests, please visit the [GitHub repository](https://github.com/azrova/AzrovasEconomy)

## Features

*   Basic economy functions: pay, balance, give, withdraw.
*   Configurable starting balance for new players.
*   Customizable currency symbol.
*   Customizable base for the `/eco` command via config (though `/eco` itself is fixed in `plugin.yml`).
*   `/eco top` command to display wealthiest players (configurable list length and update interval).
*   Vault support for compatibility with other economy plugins and features.
*   PlaceholderAPI support for displaying balance and currency symbols in other plugins (e.g., scoreboards, chat formats).
    *   `%azeco_balance%` - Shows the player's formatted balance.
    *   `%azeco_balance_formatted%` - Same as above.
    *   `%azeco_currency_symbol%` - Shows the configured currency symbol.
*   Permissions for all commands.

## Commands & Permissions

**Player Commands:**

*   `/balance` (Aliases: `/bal`)
    *   Description: Shows your current balance.
    *   Permission: `azrovaseconomy.balance` (Default: true)
*   `/eco pay [player] [amount]`
    *   Description: Pay another player.
    *   Permission: `azrovaseconomy.pay` (Default: true)
*   `/eco top`
    *   Description: View the list of the richest players.
    *   Permission: `azrovaseconomy.top` (Default: true)
*   `/eco help`
    *   Description: Shows the list of available commands for the user.
    *   Permission: `azrovaseconomy.help` (Default: true)

**Admin Commands:** (Require `azrovaseconomy.access` and specific admin permission)

*   `/eco add [player] [amount]`
    *   Description: Add money to a player's balance.
    *   Permission: `azrovaseconomy.admin.add` (Default: op)
*   `/eco withdraw [player] [amount]` (Aliases: `/eco remove`, `/eco take`)
    *   Description: Remove money from a player's balance.
    *   Permission: `azrovaseconomy.admin.withdraw` (Default: op)
*   `/eco help` (when user has admin permissions like `azrovaseconomy.admin.help`)
    *   Description: Shows all available commands, including admin commands.
    *   Permission: `azrovaseconomy.admin.help` (Default: op) - *Note: The help display automatically includes admin commands if the user has other admin perms like add/withdraw.*

## Installation

1.  Download the latest `AzrovasSimpleEconomy-X.X.jar` from the releases page (or build it yourself).
2.  Place the JAR file into your server's `plugins` folder.
3.  Ensure you have [Vault](https://www.spigotmc.org/resources/vault.34315/) installed for economy features to work.
4.  (Optional) Install [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) if you want to use the placeholders.
5.  Restart or reload your server.

## Configuration

The configuration file (`config.yml`) is located in the `plugins/AzrovasSimpleEconomy/` directory. You can customize:

*   `options.currency-symbol`: The symbol for your currency (e.g., "$", "Coins").
*   `options.eco-command-alias`: An alternative alias for `/eco` for some administrative commands or user preference (default: "eco"). The main command `/eco` remains.
*   `options.starting-balance`: The amount new players start with (default: 100.0).
*   `options.eco-top.list-length`: How many players to show in `/eco top` (default: 10).
*   `options.eco-top.update-interval`: How often (in seconds) the `/eco top` list is refreshed (default: 300 seconds / 5 minutes).

## Building from Source (for Developers)

1.  Clone the repository: `git clone https://github.com/azrova/AzrovasEconomy.git` (update link)
2.  Navigate to the project directory: `cd AzrovasEconomy`
3.  Build the plugin using Gradle: `./gradlew shadowJar` (Linux/macOS) or `gradlew.bat shadowJar` (Windows).
4.  The compiled JAR will be in the `build/libs/` directory.

---

This README provides a basic template. Feel free to expand upon it with more details, contribution guidelines, or a license if you make the repository public. 
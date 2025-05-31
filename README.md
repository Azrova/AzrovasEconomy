# Azrova's Economy


A robust and feature-rich economy plugin for PaperMC servers, designed to provide essential economic functionalities with support for Vault and optional PlaceholderAPI integration.

---

## Features

*   **Core Economy via Vault:** Leverages Vault for broad compatibility with other economy-related plugins.
*   **Player Accounts:** Basic account management for player balances.
*   **Configurable Starting Balance:** Set a starting amount for new players.
*   **Daily Rewards:** Allow players to claim a configurable daily reward.
*   **Player-to-Player Payments:** `/pay` command for transferring money.
*   **Banknotes:** `/withdraw` command to create physical banknotes that can be redeemed.
*   **Balance Checking:** `/balance` command for players to check their own or others' balances (permission-based).
*   **Richest Players List:** `/money top` command to display the top N richest players.
*   **Admin Commands:** Comprehensive set of `/eco` commands for administrators to manage player economies.
*   **Help System:** In-game help via `/help [topic]` for economy and admin commands.
*   **PlaceholderAPI Support (Optional):** Display player balances via `%azrovaseconomy_balance%`.
*   **Configurable Currency Symbol:** Customize the currency symbol (e.g., $, €, etc.) in `config.yml`.
*   **SQLite Data Storage:** Player data is saved locally using SQLite, requiring no external database setup.

---

## Dependencies

*   **[Vault](https://www.spigotmc.org/resources/vault.34315/) (Required):** Azrova's Economy relies on Vault to provide the economy G_API. This plugin will not function without Vault installed.
*   **[PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (Optional):** If you wish to use placeholders like `%azrovaseconomy_balance%` in other plugins (e.g., scoreboards, chat), PlaceholderAPI must be installed. The plugin will function normally without it, but placeholders will not be available.

---

## Installation

1.  Ensure your server is running PaperMC 1.21.x or a compatible version.
2.  Install **Vault** if you haven't already. Download it from [SpigotMC](https://www.spigotmc.org/resources/vault.34315/) and place the JAR file in your server's `plugins` folder.
3.  (Optional) Install **PlaceholderAPI** if you intend to use placeholders. Download it from [SpigotMC](https://www.spigotmc.org/resources/placeholderapi.6245/) and place the JAR file in your server's `plugins` folder.
4.  Download the latest release of `AzrovasEconomy.jar` from the [Releases Page](https://github.com/Azrova/AzrovasEconomy/releases).
5.  Place the `AzrovasEconomy.jar` file into your server's `plugins` folder.
6.  Restart or reload your server.

---

## Configuration

The plugin will generate a `config.yml` file in its plugin folder (`plugins/AzrovasEconomy/config.yml`) upon first run. You can modify these settings:

```yaml
starting-balance: 100.0
daily-reward-amount: 100.0
currency-symbol: "$"
```

*   `starting-balance`: The amount of money a player receives when they join the server for the first time and an account is created for them.
*   `daily-reward-amount`: The amount of money a player receives when they use the `/daily` command.
*   `currency-symbol`: The symbol used before amounts in messages (e.g., "$100.00").

---

## Commands & Permissions

Below is a list of commands and their associated permissions. Permissions set to `true` under `default` are enabled for all users by default. Permissions set to `op` are enabled for server operators by default.

| Command                      | Description                                      | Permission                        | Default    |
| ---------------------------- | ------------------------------------------------ | --------------------------------- | ---------- |
| `/pay [user] [amount]`       | Pay a user an amount of money.                   | `azrova.economy.pay`              | `true`     |
| `/withdraw [amount]`         | Withdraw money as a physical banknote.           | `azrova.economy.withdraw`         | `true`     |
| `/help [topic]`              | Shows help for specified topic (economy, admin). | `azrova.economy.help`             | `true`     |
| `/balance` or `/bal`         | Shows your current balance.                      | `azrova.economy.balance`          | `true`     |
| `/balance [user]`            | Shows another user's balance.                    | `azrova.economy.balance.others`   | `op`       |
| `/money top`                 | Shows the top richest players.                   | `azrova.economy.money.top`        | `true`     |
| `/daily`                     | Claim your daily reward.                         | `azrova.economy.daily`            | `true`     |
| **Admin Commands (`/eco`)**  | Base permission for all `/eco` subcommands.      | `azrova.admin`                    | `op`       |
| `/eco set [user] [amount]`   | Set a user's balance.                            | `azrova.admin.set`                | `op`       |
| `/eco remove [user] [amount]`| Remove money from a user.                        | `azrova.admin.remove`             | `op`       |
| `/eco delete [user]`         | Delete a user's economy account.                 | `azrova.admin.delete`             | `op`       |

---

## Placeholders (PlaceholderAPI)

If PlaceholderAPI is installed, the following placeholder is available:

*   `%azrovaseconomy_balance%`: Displays the player's current balance, formatted with the configured currency symbol.
    *   Example: `You have: %azrovaseconomy_balance%` might display as `You have: $150.50`

---

## Building from Source

If you wish to build the plugin from source:

1.  Clone the repository: `git clone https://github.com/Azrova/AzrovasEconomy.git`
2.  Navigate to the project directory: `cd AzrovasEconomy`
3.  Ensure you have Java 21 (or the version specified in `build.gradle.kts`) installed and configured.
4.  Run the Gradle build command:
    *   On Windows: `.\gradlew build`
    *   On Linux/macOS: `./gradlew build`
5.  The compiled JAR file will be located in `build/libs/`.

---

## Authors & Support

*   **Authors:** CNethuka, Sobble
*   **Support & Bug Reports:** Please use the [Issues Page](https://github.com/Azrova/AzrovasEconomy/issues) on GitHub.
*   **Project Repository:** [https://github.com/Azrova/AzrovasEconomy](https://github.com/Azrova/AzrovasEconomy)

---

Made with ❤ by CNethuka and Sobble
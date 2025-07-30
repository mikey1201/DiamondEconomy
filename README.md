# DiamondEconomy

A lightweight, diamond-based economy plugin using Vault.

## Features

*   **Diamond-Based Currency:** Uses the vanilla diamond item for physical deposits and withdrawals. If the withdrawn  diamonds can't fit in your inventory they will be dropped on the ground
*   **Vault Integration:** Hooks into Vault to for easy compatibility with other plugins.
*   **Player-to-Player Payments:** Safely pay other players directly from your balance.
*   **Balance Leaderboard:** Display the richest players on your server.
*   **SQLite Database:** Uses an SQLite database to prevent data loss from server crashes and to ensure high performance, even with many players.
*   **Fractional Balances:** Supports payments down to `0.01`, but players can only withdraw whole diamonds.

## Dependencies

*   [**Vault**](https://github.com/MilkBowl/Vault) - This plugin is **required** for DiamondEconomy to function.

## Installation

1.  Make sure you have **Vault** installed on your server.
2.  Download the latest release of **DiamondEconomy**.
3.  Place the JAR file into your server's `plugins` folder.
4.  Restart or reload your server.

## Commands

| Command | Alias(es) | Description |
| :--- | :--- | :--- |
| `/balance` | `/bal` | Checks your current balance. |
| `/deposit <amount\|all>` | `/dep`, `/depo` | Deposits diamonds from your inventory. |
| `/withdraw <amount\|all>` | `/with` | Withdraws diamonds into your inventory. |
| `/pay <player> <amount>`| *none* | Pays another player from your balance. |
| `/baltop` | `/balancetop` | Shows the server's balance leaderboard. |

## Examples

Deposit 32 diamonds if you have enough in your inventory
`/depo 32`

Deposit all of the diamonds in your inventory
`/deposit all`

Withdraw 16 diamonds from your account
`/withdraw 16`

Withdraw all whole diamonds from your account, leaving any fractions behind
`/wit all`

Pay your friend Herobrine 10.50 diamonds
`/pay Herobrine 10.50`

Pay another player a very small amount
`/pay Steve 0.01`

See who is the richest on the server
`/baltop`
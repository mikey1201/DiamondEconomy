# DiamondEconomy

A lightweight, highly configurable economy plugin that bridges Vault banking with physical items.

## Features

*   **Configurable Currency:** Tired of Diamonds? Easily switch to Emeralds, Iron Ingots, or Amethyst Shards via `config.yml`.
*   **Custom Symbols & Formatting:** Change the currency symbol (e.g., from `⬧` to `$`) and fully customize all message colors/translations using `messages.yml`.
*   **Vault Integration:** Hooks into Vault for seamless compatibility with [Vault](https://github.com/MilkBowl/Vault) plugins (ShopGUIPlus, Essentials, etc.).
*   **Physical Economy:** Deposit items to bank, withdraw items from bank. Automatically handles inventory space (drops items if full).
*   **Admin Tools:** Full control over the economy with `/eco` (give, take, set) and `/balance [player]` for offline support.
*   **Privacy Features:** Players can toggle their visibility on `/baltop`.
*   **SQLite Database:** Uses a local SQLite database for high performance and crash-safe data storage.
*   **Fractional Balances:** Supports precise payments down to `0.01`, but players can only withdraw whole items.

## Dependencies

*   **[Vault](https://github.com/MilkBowl/Vault)** - This plugin is **required** for DiamondEconomy to function.

## Installation

1.  Make sure you have **Vault** installed on your server.
2.  Download the latest release of **DiamondEconomy**.
3.  Place the JAR file into your server's `plugins` folder.
4.  Restart your server.

## Configuration & Localization

DiamondEconomy creates two files in the plugin folder for easy customization:

**`config.yml`**
*   `currency-item`: The material players deposit/withdraw (e.g., `DIAMOND`, `EMERALD`, `AMETHYST_SHARD`).
*   `currency-symbol`: The symbol used in chat (e.g., `⬧`, `$`, `€`).

**`messages.yml`**
*   All user-facing text can be edited here.
*   Supports Minecraft color codes (e.g., `&a`, `&c`).
*   Useful for translating the plugin to other languages.

## Permissions

| Permission | Description | Default |
| :--- | :--- | :--- |
| `diamondeconomy.use` | Use `/pay`, `/deposit`, `/withdraw`. | `true` |
| `diamondeconomy.balance.others` | Check balances of other players via `/balance [player]`. | `op` |
| `diamondeconomy.toggle` | Toggle own visibility in `/baltop`. | `true` |
| `diamondeconomy.admin` | Use `/eco`, `/diamondeconomy reload`, and checks offline balances. | `op` |

## Commands

| Command | Alias(es) | Description |
| :--- | :--- | :--- |
| `/balance [player]` | `/bal` | Check your balance or another player's. |
| `/deposit <amount\|all>` | `/dep` | Deposit currency items from your inventory. |
| `/withdraw <amount\|all>` | `/with` | Withdraw currency items into your inventory. |
| `/pay <player> <amount>` | `none` | Pays another player from your balance. |
| `/baltop [toggle]` | `/balancetop` | View the richest players, or toggle visibility. |
| `/eco <give\|take\|set> <player> <amount>` | `none` | Modify player balances (Admin). |
| `/diamondeconomy reload` | `/de` | Reload configuration and messages. |

## Usage Examples

**Depositing**
```yaml
/deposit 5        # Deposits 5 currency items
/dep all          # Deposits all currency items in your inventory
```

**Withdrawing**
```yaml
/withdraw 64      # Withdraws 64 items (gives 1 stack)
/with all         # Withdraws as many whole items as your balance allows
```

**Paying**
```yaml
/pay Steve 50     # Pays Steve 50.00 currency
/pay Alex 0.50    # Pays Alex 50 cents
```

**Admin Management**
```yaml
/eco give Steve 1000      # Give Steve 1000 currency
/eco take Alex 500         # Take 500 currency from Alex
/eco set Herobrine 0       # Set Herobrine's balance to 0
/de reload                 # Reload config/messages
```
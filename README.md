# Simple Economy

**Simple Economy** is a Minecraft economy mod for **Minecraft 1.21.1**, built for **NeoForge**.

The mod adds a virtual currency system and a player-driven marketplace, allowing players to earn, spend, and trade money without using physical currency items.

The main goal of the mod is to provide a simple and convenient economy system for multiplayer servers, where players can trade resources, set their own prices, and build an in-game economy.

## Features

### Virtual Economy
- A virtual currency system using `money`.
- Players can receive, spend, and transfer value through in-game economy mechanics.
- Administrators can manage player balances.
- No physical currency items are required.

### Player Marketplace
- Players can create listings to sell their items to other players.
- Sellers choose their own prices.
- Listings remain active indefinitely until they are purchased or removed by the seller.
- The listing price applies to the entire item stack, not to individual items.
- A 5% commission is charged when an item is sold.
- Sellers receive the payment after another player purchases their listing.

### Quick Sell
- Administrators can set standard selling prices for specific items.
- Players can quickly sell items using the configured prices.
- The selling price is calculated based on the item quantity.
- Items without an administrator-defined price cannot be sold through this feature.

### Economy Management
- Administrative commands for managing player balances.
- Commands for setting, checking, listing, and removing standard item prices.
- Support for managing the economy through the server console.

### Transaction History
- A history of recent economy operations.
- Helps players keep track of their financial activity.

### User Interface
- A graphical interface for accessing economy features.
- Dedicated marketplace tabs for browsing listings, managing personal listings, and selling items.
- The economy menu can be opened through a keybind or a command.

## Integration API

Other NeoForge mods can use the public server-side API:

`com.simpleeconomy.api.EconomyAPI.addContractReward(ServerPlayer, long, String)`

This method awards virtual money and adds a transaction-history entry with the type
`Награда за контракт` and the supplied contract name. Call it only on the logical
server thread, after validating the completed task on the server.

## Marketplace Rules

The marketplace follows several important rules:

1. The seller chooses the price for the entire listing.
2. A 5% commission is charged on successful sales.
3. Listings do not expire automatically.
4. Items are transferred directly to the buyer's inventory.
5. A purchase is cancelled if the buyer does not have enough inventory space for the entire item stack.
6. Removing a listing is also prevented if the seller cannot fit the returned items into their inventory.
7. Failed transactions do not remove items or charge money.

## Technical Information

- **Minecraft:** 1.21.1
- **Mod Loader:** NeoForge
- **Java:** 21
- **Mod ID:** `simpleeconomy`

## Project Status

Simple Economy is currently under development. New features, improvements, and bug fixes are being added as development progresses.

The project is primarily focused on creating a practical economy system for multiplayer Minecraft servers.

View all commands: https://github.com/nikhunt3r/simple-economy-mod.wiki.git

# Essentials for Fabric

Essentials for Fabric aims to port the popular "Essentials" plugin to Fabric, as well as adding features from other plugins and QoL changes, to provide an all-in-one mod for your server's needs.

Essentials for Fabric is easily extendable using the api module, which is made available on curseforge. A maven may be created if deemed necessary.

## Support

If you find a bug or want to suggest a feature, go to the [Issues tab](https://github.com/NyliumMC/Essentials/issues)

If you want to ask for help and/or clarify a bug, or even contribute to the project, you can find us on the [AOF discord in #essentials](https://discord.gg/6rkdm48)


## Modules

<details>
<summary>API</summary>
<h5>Description</h5>

Essentials-api is the module used for developing your own modules against.
</details>

<details>
<summary>Base</summary>
<h5>Description</h5>

Essentials-base is the core implementation of essentials. This is needed for most modules to work.
</details>

<details>
<summary>Chat</summary>
<h5>Description</h5>

Chat module. Provides nicknames and chat colors.

##### Commands
- /nickname [nickname]
    - Sets your current nickname or clears it. Supports color codes.
</details>

<details>
<summary>Claims</summary>

The Claims module provides the API backend for claiming chunks.
</details>

<details>
<summary>Currency</summary>
<h5>Description</h5>

Module providing the currency handling in essentials as well as some basic commands

##### Commands
- /balance [player]
    - Show your current account balance, or from a specified player
- /pay \<user> \<amount>
    - Send money from your account to someone else
</details>

<details>
<summary>Datapacks</summary>
<h5>Description</h5>

Essentials-datapacks is a module that aims to port various utility datapacks for better performance.
Permission has been granted for this by the respective authors, either through explicit permission or following the terms.

Included:
- [Vanilla Tweaks by Xisumavoid](https://www.xisumavoid.com/vanillatweaks)
  - Anti Creeper Grief
  - Anti Enderman Grief
  - Anti Ghast Grief
  - Silence Mobs
  - Player Head Drops
  - More Mob Heads
  
- [Datapacks by VoodooBeard](http://mc.voodoobeard.com/)
  - Shulkermites
  - Auto-Plant Saplings
  - Server Friendly Wither
  - Anti Zombie Breach
  - Apiarist Suit
  - Invisible Item Frames
</details>

<details>
<summary>Market</summary>
<h5>Description</h5>

Public Market module

##### Commands

- /market
    - Opens the market GUI
- /market add \<price> [amount]
    - Adds the current item the player is holding to the market
</details>

<details>
<summary>SimpleClaims</summary>
<h5>Description</h5>

Adds an implementation to claim chunks. If module-currency is installed, claiming can cost money.

##### Commands

- /claim
    - Claim this chunk
- /claim confirm
    - Confirm claiming this chunk if module-currency is installed
- /claim owner
    - Get the owner of this chunk
- /claim add \<player>
    - Allow a player to access this chunk
- /claim remove \<player>
    - Remove access to this chunk from a player
- /claim delete
    - Delete this chunk from being claimed

</details>

<details>
<summary>Teleport</summary>
<h5>Description</h5>

Module for everything related to teleports such as homes and warps

##### Commands

- /sethome [name]
    - Sets a home at your current location
- /home [name]
    - Teleports you to the specified home
- /setwarp [name]
    - Sets a warp at the current location
- /warp [name]
    - Teleports you to the specified warp
- /tpa [player]
    - Sends a teleport request to the specified player
- /tpahere [player]
    - Sends a request to teleport a player to you
</details>

<details>
<summary>Utilities</summary>
<h5>Description</h5>

Includes a bunch of commands and miscellaneous features that don't belong to any specific module.

##### Commands
- /enderchest
    - Opens your ender chest
- /fly 
    - Allows toggling a player's ability to fly.
- /god 
    - Makes a player invulnerable
- /hat 
    - Allows a player to place the item in their hand on their head
- /head 
    - Gives a player another player's head
- /heal 
    - Heals a player
- /invsee [player]
    - Opens a player's inventory
- /speed 
    - Allows a player's walking and flying speed to be changed
- /tps 
    - Prints the server's current tick rate in ticks per second
- /trash
    - Opens a trash can
- /vanish
    - Allows you to disappear for other players
- /workbench
    - Opens a crafting table
</details>

## License
This mod is available under the [MIT license](LICENSE).

## Contributing
More information will be provided soon. Contributions should follow the guidelines in [CONTRIBUTING.md](CONTRIBUTING.md).

## Extending
If you want to use fabric, add the following to your build.gradle:

```gradle
dependencies {
    implementation "io.github.nyliummc:essentials-api:${essentials_version}+${minecraft_version}"
    // add modRuntimes for other modules; DO NOT COMPILE AGAINST THEM
}

repositories {
    maven {
        name = "Essentials"
        url = "https://maven.martmists.com"
    }
}
```

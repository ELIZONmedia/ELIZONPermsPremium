# ELIZONPerms - Permissions System with Forge (1.16.5), Velocity, Spigot (Paper) +1.16.5 and Bungeecord support with a Discord Bot
[![Maven Package](https://github.com/ELIZONmedia/ELIZONPermsPremium/actions/workflows/maven-publish.yml/badge.svg?event=push)](https://github.com/ELIZONmedia/ELIZONPermsPremium/actions/workflows/maven-publish.yml)

This is a comprehensive Minecraft permissions system designed to facilitate managing player permissions, groups, and ranks efficiently. It offers a wide range of features including group management, player assignment to groups, permission management, rank tracking, prefix/suffix registration, and more.

## Features

- **Group Management**:
  - Create, delete, rename, duplicate groups.
  - Add and remove permissions from groups.
  - Manage group inheritance.

- **Player Management**:
  - Assign, set, and remove players from groups.
  - Add and remove permissions for players.

- **MultiState Permissions**:
  - Utilizes the DATA option for saving JSON Data for plugin development.
  - Condition-Based-Rights for integrating into gameplay mechanics.

- **Integrated Autocomplete**:
  - Fully integrated autocomplete functionality on Velocity, Bungeecord, and Spigot platforms.

- **Rank Traces & Tracks**:
  - Log and directly upgrade ranks for players.

- **Prefix & Suffix Registration**:
  - Register prefixes and suffixes for players for seamless integration with plugins.

- **Rank Timings**:
  - Temporarily assign ranks with specific time durations.

- **Rank Priorities**:
  - Set priorities for different ranks to resolve conflicts.

- **Group Inherits**:
  - Define group inheritance for streamlined permission management.

- **Discord Bot Integration**:
  - Link Minecraft activities with Discord through a dedicated bot.

## Commands

Here's a summary of the commands available:

### Group Management
```
/epb group <name> info
/epb group <name> permission info
/epb group <name> create
/epb group <name> delete
/epb group <name> rename <new name>
/epb group <name> clone <name>
/epb group <name> permission add <permission>
/epb group <name> permission remove <permission>
/epb group <name> permission set <permission> true 
/epb group <name> permission set <permission> false
/epb group <name> permission set <permission> data (Initialize data via API)
/epb group <name> inherit info
/epb group <name> inherit add <group to inherit>
/epb group <name> inherit remove <group>
/epb group <name> setdefault
/epb group <name> setprefix <prefix>
/epb group <name> setsuffix <suffix>
/epb group <name> setheight <height>
```

### User Management
```
/epb user <name> info
/epb user <name> permission info
/epb user <name> group info
/epb user <name> group add <group>
/epb user <name> group addtimed <group> <time in hours>
/epb user <name> group set <group> true
/epb user <name> group set <group> false
/epb user <name> group set <group> data (Initialize data via API)
/epb user <name> group settimed <group> true <time in hours>
/epb user <name> group settimed <group> false <time in hours>
/epb user <name> group settimed <group> data <time in hours> (Initialize data via API)
/epb user <name> permission add <permission>
/epb user <name> permission remove <permission>
/epb user <name> permission set <permission> true
/epb user <name> permission set <permission> false
/epb user <name> permission set <permission> data (Initialize data via API)
```

### Rank Management
```
/epb track <name> list
/epb track <name> delete
/epb track <name> create <group> <group> <group>...
/epb track <name> insertbefore <group to insert> <next higher group>
/epb track <name> add <new highest group>
/epb track <name> remove <group>
/epb track <name> next <group>
/epb track <name> rankup <username>
```

This system offers robust control over player permissions and group management, enhancing the administrative capabilities of your Minecraft server. Feel free to explore and customize according to your server's needs. Happy gaming!

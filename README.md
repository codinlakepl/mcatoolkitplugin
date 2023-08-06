# MCAdmin-Toolkit Connector

> MCAdmin-Toolkit is a complete system that allows you to manage players on your Minecraft server from a mobile phone.

<br />

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

<br />



<details open="open">
<summary>Table of Contents</summary>

- [About](#about)
  - [Built With](#built-with)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
- [Usage](#usage)
- [Roadmap](#roadmap)
- [Support](#support)
- [Project assistance](#project-assistance)
- [Contributing](#contributing)
- [Authors & contributors](#authors--contributors)
- [License](#license)

</details>

---

## About

> MCAdmin-Toolkit Connector is a backend of the systme. Futhermore it is a bukkit plugin. Thanks to it our app can connect and manage your server. It provides rest API secured by your own ssl cert.
> Features that our project contain are:
> 1. Current server status report (CPU usage, RAM usage and players online)
> 2. Live logs preview
> 3. Logs dump to file
> 4. Kicking
> 5. Baning
> 6. IP-Banning
> 7. Unbanning
> 8. IP-Unbanning
> 9. Whitelist on/off
> 10. Whitelist add or remove player
> 11. Listing online and offline players, banned players and whitelisted players.
> 
> All from our mobile app.
> Mobile app repo: [https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit](https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit)



### Built With

> The plugin was built with Java obviously :stuck_out_tongue_winking_eye: <br />
> And now more seriously, we used:
> - Amazon Corretto 1.8
> - Spigot API 1.16.4-R0.1-SNAPSHOT
> - SQLite
> - java-express [https://github.com/simonwep/java-express](https://github.com/simonwep/java-express)
> 
> Those were main tools/libraries that we used to create this plugin. <br  />
> In future we want to change java-express to something else. It's because this lib is not maintained anymore. The opotion that we are looking on right now is gRPC but we are open to suggestions.

## Getting Started

### Prerequisites

> To get started with the plugin you have to set up a bukkit/spigot Minecraft server. **Plugin won't work with Vanilla or Forge server. Only bukkit and bukkit variants are supported.** You can check how to setup bukkit server here: [https://bukkit.fandom.com/wiki/Setting_up_a_server](https://bukkit.fandom.com/wiki/Setting_up_a_server) <br />
> Also make sure that you can open another port on TCP. <br />
> You would also need ssl cert generator. Such as openssl on Linux.

### Installation

> Installation is not that standard as in other plugins. You have to provide ssl certs to make it works. But don't worry it's simple. We recommend to go through installation process on Linux machine but if your server is running on Windows, then you would be also able to do all of the steps. So, the steps:
> 1. Download newest plugin version from github releases: [https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit-plugin/releases](https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit-plugin/releases).
> 2. Copy downloaded .jar file to folder: `<your_server_root_folder>/plugins`.
> 3. Run the server.
> 4. After server sterted successfully new folder should apper in `plugins`. It sould be called `MCAdmin-Toolkit-Connector`.
> 5. Go to that folder: 
> ```sh
> cd ./plugins/MCAdmin-Toolkit-Connector
> ```
> 6. Create ssl certificate. It must be splitted into 2 files: `rootCA.crt` and `rootCA.key`:
> ```sh
> openssl req -x509 -sha256 -days 365 -nodes -newkey rsa:2048 -keyout rootCA.key -out rootCA.crt
> ```
> 7. Restart your Minecraft server.

## Usage

> You can read commands and config documentation [here](docs/COMMANDS.md)
> <br /><br />
> In order to use our app you have to create access key. This plugin lets you do it from minecraft via command:
> ```minecraft
> createauthkey 1 <your_nick>
> ```
> So for me it would be:
> ```minecraft
> createauthkey 1 ipyz
> ```

## Roadmap

We want to implement new things to our system. Here's what we plan for closest future:
- Configurable security levels
- Move from java-express to gRPC (or other technology)
- Implement in-app permission management based on Luckperms API

## Support

> Working on this

Reach out to the maintainer at one of the following places:

- Github:
  - [IpyZ](https://github.com/IpyZ)
  - [Diratix](https://github.com/Diratix)

- Discord:
  - Diratix#6897
  - IpyZ#0639
  - Still working on official discord server

- Mails:
  - [ipyz@mcadmin.me](mailto:ipyz@mcadmin.me)
  - [diratix@mcadmin.me](mailto:diratix@mcadmin.me)

## Contributing

First off, thanks for taking the time to contribute! Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make will benefit everybody else and are **greatly appreciated**.


Please read [our contribution guidelines](docs/CONTRIBUTING.md), and thank you for being involved!

## Authors & contributors

The original setup of this repository is by:
- [IpyZ (Filip Śliwa)](https://github.com/IpyZ)
- [Diratix (Daniel Pawelec)](https://github.com/Diratix)

For a full list of all authors and contributors, see [the contributors page](https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit-plugin/contributors).

We also want to thank a person, who motivated us to create this whole project:<br />
[majlenaadrianna (Milena Bilewska)](https://github.com/majlenaadrianna)<br />
Thank you! Without your help we wouldn't be here


## License

This project is licensed under the **GNU General Public License v3**.

See [LICENSE](LICENSE) for more information.

## About a readme
This readme was created with this template: [https://github.com/dec0dOS/amazing-github-template#readme](https://github.com/dec0dOS/amazing-github-template#readme)
# MCAdmin-Toolkit Connector
MCAdmin Toolkit is a tool that allow administrators and moderators manage players on their server from mobile app. This is a backend part of an application. Futhermore it's a bukkit plugin. It works with spigot as well.

**Link to Android app:** [https://github.com/MCAdmin-Toolkit-dev-team/android-app](https://github.com/MCAdmin-Toolkit-dev-team/android-app)

### Installation
First set up a Minecraft bukkit server. Spigot will work as well. Then download the plugin:

[https://github.com/MCAdmin-Toolkit-dev-team/bukkit-backend/releases/download/prealpha-3.1/MCAdmin-Toolkit-Connector-2023-06-01-prealpha-3.jar](https://github.com/MCAdmin-Toolkit-dev-team/bukkit-backend/releases/download/prealpha-3.1/MCAdmin-Toolkit-Connector-2023-06-01-prealpha-3.jar)

**IMPORTANT!** Next fire up your server, and then after server successfully launched STOP it.

Next, go to plugin's folder:

```bash
cd ./plugins/MCAdmin-Toolkit-Connector
```

Next, you have to generate SSL cert splitted into 2 files:
- rootCA.crt
- rootCA.key

On Linux you can do it via this command:

```bash
openssl req -x509 -sha256 -days 365 -nodes -newkey rsa:2048 -keyout rootCA.key -out rootCA.crt
```

Next, run your server.

And last, most important thing: have fun!

### Configuration

Plugin's config is located under:

```
plugins/MCAdmin-Toolkit-Connector/config.json
```

Default config looks like this:
```json
{
    "port": 4096,
    "commandLogging": {
        "ban": {"log": "true", "push": true},
        "ban-ip": {"log": "true", "push": true},
        "pardon": {"log": "true", "push": true},
        "pardon-ip": {"log": "true", "push": true},
        "kick": {"log": "true", "push": false},
        "whitelist": {"log": "true", "push": true}
    },
    "appLogging": {
        "ban": {"log": "true", "push": true},
        "banIp": {"log": "true", "push": true},
        "kick": {"log": "true", "push": false},
        "unban": {"log": "true", "push": true},
        "unbanIp": {"log": "true", "push": true},
        "whitelistOnOff": {"log": "true", "push": true},
        "whitelistAddRemovePlayer": {"log": "true", "push": true}
    }
}
```

- `port` - port on which https server will start
- `commandLogging` - section about creating the logs about executed commands:
    - `"[commandName]": {"log": true/false, "push": true/false}` - a syntax
    - `[commandName]` - name of Minecraft command
    - `log` - should execution of this command be logged?
    - `push` - unused; it's a placeholder for future plans
    - in this section you can add or remove elements as needed
- `appLogging` - section about creating the logs about actions peformed from our mobile app
    - `"[featureName]": {"log": true/false, "push": true/false}` - a syntax
    - `[featureName]` - name of our project's feature
    - `log` - should performance of this action be logged?
    - `push` - unused; it's a placeholder for future plans
    - in this section you cannot add or remove elements
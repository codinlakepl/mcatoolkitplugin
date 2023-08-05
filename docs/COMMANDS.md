# Command usage

> All commands require op to be executed from minecraft. They can be executed from console as well.

### createauthkey
> `createauthkey` - it creates access key for application <br /><br />
> **Usage**:
> ```minecraft
> /createauthkey <secLvl> <label>
> ```
> `secLvl` - number from 1 to 6. Grater number menas less privilages. We are still working on it so we reccomend setting it to 1 to get full privilages <br />
> `label` - friendly name that you will use to identify access key owner and for access key deletion <br /><br />
> **Example**:
> ```minecraft
> /createauthkey 1 ipyz
> ```
> **Example output**:
> ```
> [21:18:17] [Server thread/INFO]: Successfully generated auth key.
> [21:18:17] [Server thread/INFO]: This is code to download it in MC-Admin-Toolkit application:
> [21:18:17] [Server thread/INFO]: U6tQf
> [21:18:17] [Server thread/INFO]: It will resist 5 minutes
> ```
> Download code is a code, that you should enter in our app. You can read more about it here: [https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit/docs/USAGE.md#download-key](https://github.com/MCAdmin-Toolkit-dev-team/MCAdmin-Toolkit/docs/USAGE.md#download-key)

### listauthkeys
> `listauthkeys` - it lists all active access keys<br /><br />
> **Usage**:
> ```minecraft
> /listauthkeys
> ```
> **Example output**:
> ```
> [21:24:57] [Server thread/INFO]: Registered auth keys:
> [21:24:57] [Server thread/INFO]: ipyz
> [21:24:57] [Server thread/INFO]: diratix
> ```

### removeauthkey
> `removeauthkey` - it removes given access key<br /><br />
> **Usage**:
> ```minecraft
> /removeauthkey <label>
> ```
> `label` - friendly name<br /><br />
> **Example**:
> ```minecraft
> /removeauthkey diratix
> ```
> **Example output**:
> ```
> [21:29:25] [Server thread/INFO]: Successfully removed auth key with label 'diratix'
> ```

# Configuration

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
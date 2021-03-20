<h2>Duty carousel feature</h2>
This feature selects next developer on duty from list and notifies channel about it. Iteration over the list works every day except
holidays, taking in to the account user's vacations.

#Launch slack-vacation in your workspace
Create new slash command and set down fields as shown on the next picture:
![Alt text](img/duty-slash.png?raw=true)

<h3How to add new duty list</h3>
1) Add row into  `DUTY_LIST` table:
```
TEAM_ID - workspace id
CHANNEL_ID - notified channel id
MNEMONIC_NAME - list's mnemonic name (optional)
```
2) Add rows with slack user into `DUTY_USER` table, which participate in duty activities
3) Invite this bot to notified channel via `/invite` slash command

<h3>How to select current developer on duty manually</h3>
Just use `/duty` slash command. This command works only in notified channel. 
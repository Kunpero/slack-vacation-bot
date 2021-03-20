<h2>How to launch application on localhost</h2>

<h3>Slack</h3>
1) Create new workspace for testing purposes (if you don't have one) https://slack.com/create
2) Go to https://api.slack.com/apps and create new app in your slack workspace
3) Add slash command:
* [for vacation features](vacation.md#launch-slack-vacation-in-your-workspace)
* [duty features](duty.md#launch-slack-vacation-in-your-workspace)
4) Enable interactivity in your app (`/interactivity/handle` endpoint):
5) Set app permission scopes:
   ![Alt text](./img/permission&scopes.png?raw=true)
6) Install app to your slack workspace

<h3>App</h3>
1) Define your application properties:
* Secrets
``` 
slack.signing.secret=[Signing Secret]
slack.access.token=[Auth Access Token]
```
* Vacation
```
channel.notification.enabled=[true/false]
notified.channel.id=[channel id for vacation notifications]
```
* Duty
```
next.duty.cron=[duty iteration cron]
```
2) Run schema.sql for your database. In project you can find scripts for [Oracle](/core/src/main/resources/db/oracle/schema.sql) 
and [Postgres](/core/src/main/resources/db/postgresql/schema.sql).
3) Deploy build to your own server or any cloud application platform you prefer

<h3>Note</h3>
To route requests from slack to your local app, use [ngrok](https://ngrok.com/)
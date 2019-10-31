# slack-vacation
<h4>Simple slack-vacation bot</h4>

To activate start menu just type `/vacation`. At this moment it's possible to create and delete vacation info 
with next parameters:
* Date from [mandatory]
* Date to [mandatory]
* Substitution users [optional]

You can't add vacations with crossing dates.
  
To show active vacations that interfere with current date, type `/vacation now`.
To show upcoming vacations type `/vacation all`

<h3>Launch slack-vacation in your workspace</h3>
1) Go to https://api.slack.com/apps and create new app in your slack workspace
2) Create new slash command and set down fields as shown on the next picture:
![Alt text](img/slash.png?raw=true)

3) Enable interactivity in your app:
![Alt text](img/interactivity.png?raw=true)

4) Set app permission scopes:
![Alt text](img/scopes.png?raw=true)

5) Define your application secret properties:
   ``` 
   slack.signing.secret=[Signing Secret]
   slack.access.token=*[Auth Access Token]
   ```
6) Install app to your slack workspace
7) Run schema.sql for your database. In project you can find scripts for Oracle and Postgres.
8) Deploy build to your own server or any cloud application platform you prefer



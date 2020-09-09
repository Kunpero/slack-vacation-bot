# slack-vacation
<h4>Simple slack-vacation bot</h4>

To activate start menu just type `/vacation`. At this moment it's possible to create and delete vacation info 
with next parameters:
* Date from [mandatory]
* Date to [mandatory]
* Substitution users [optional]
* Comment [optional]

You can't add vacations with crossing dates.
  
To show active vacations that interfere with current date, type `/vacation now`.<br>
To show upcoming vacations type `/vacation all`<br>
<br><br>
If you want to notify channel with actual vacation info, set next properties:
`
channel.notification.enabled=true 
notified.channel.id={selected channel id}
`

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


<h3>Vacation admin feature</h3>
If you want to allow some of your users to add/delete vacation info of other team members, 
you need to directly insert specified rows into VACATION_ADMIN table. E.g.:<br>

`INSERT INTO VACATION_ADMIN (USER_ID, TEAM_ID) VALUES ('USER_ID', 'TEAM_ID')`
<h3>Vacation info in user's status feature</h3>
Every day at 12 A.M. actual vacation info is set to connected user's status:

![Alt text](img/charlie1.png?raw=true)

As you can see, a little palm next to the username is shown,
so you can be aware if user is on vacation or not.
![Alt text](img/charlie2.png?raw=true)

Also, this status expires at 12 A.M. on the next day after the last vacation day.<br>
FYI: This feature is based on your server's system timezone.

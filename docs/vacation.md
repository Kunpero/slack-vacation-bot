<h2>Vacation features</h2>

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
To make notifications work you need to invite bot to the selected channel (just type `/invite` in the selected channel and
 choose this bot).

#Launch slack-vacation in your workspace
Create new slash command and set down fields as shown on the next picture:
![Alt text](img/vacation-slash.png?raw=true)

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
FYI: This feature is based on your server's system timezone and only available for admins
on paid plans :C
#Non Blocking Server
In this exercise we will implement a basic client-server application, such that server would be non-blocking.
The communication between clients and server will be done via TCP.

##Client Side
Client will have a command line user interface that includes the following commands:
1.	Login <CLIENT_NAME> - logs this client into the system
2.	Logout – logs this client out from the system
3.	SendMessageToClient <CLIENT_NAME_TO_SEND>|<MESSAGE> - send a dedicated message to a specific logged in client
4.	BroadcastNessageToAllClients <MESSAGE> - send a message to all logged in clients
5.	GetAllLoggedInClients – return all logged in clients
6.	GetDBData <FORMAT> – get All DB data via the following format.
 
    1. Currently, DB data is returned via JSON format only, but there is a requirement from USA headquarters to extend the support to other formats. Please note that.
            When getting the data, client needs to parse it and print it to the console in a well formatted way.
    2.	Please see table 1 for example.
    3.	Client will communicate through server for all commands. Client A cannot access client directly.
    4.	All commands are having a unique identifier that will be sent to server. You can assume that client name is also unique.
    5.	Client will not be blocked due to performing operations against server. Some of the server operation mat take time – please consider that!

## Server Side
1.  Server will need to react to all clients’ commands.
2. Per each command from client, server will return a message to the sender OR to another client OR to all clients. Please see table 1 for example.
3. You can safely assume that the max number of clients is 20.
4. You need to consider that some of the server operations may take time.
5. Currently, DB data is returned via JSON format only, but there is a requirement from US headquarters to extend the support to other formats. Please note that.
6. Currently, the data is located at the DB_data.json file located in your eclipse project.

##Commands and Corresponding Output Examples

|Operation	            |User Interface Command	       |Server Operation               |
|-----------------------|------------------------------|-------------------------------|
|Shimi is logging in   	|Login Shimi	               |Hello Shimi. You are logged in.|
|Moshe is logging in 	|Login Moshe	               |Hello Moshe. You are logged in.|
|Yossi is logging in 	|Login Yossi	               |Hello Yossi. You are logged in.|
|Shimi is logging out	|Logout	                       |Server is sending Shimi: “Goodbye Shimi. You are logged out.” Server is sending the rest of the logged in clients: “Shimi has logged out”.|
|Baruch is sending a message to Moshe|SendMessageToClient Moshe: Hello Moshe! How are you?| Server is sending to Moshe only a message: “Baruch sent you a message: hello Moshe! How are you??”|
|Baruch is sending a broadcast message|BroadcastNessageToAllClients hello folks! Let’s go dancing tonight|Server is sending to all clients (besides Baruch) the message: “Hello folks! Let’s go dancing tonight”|
Baruch is requesting DB data from server|GetDBData JSON|DB data in JSON format is sent to Baruch|
Moshe is asking for all logged in clients|GetAllLoggedInClients|Server is returning: “The logged in clients are: Moshe, Yossi, Baruch”|

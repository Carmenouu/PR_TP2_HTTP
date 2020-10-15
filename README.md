# PR_TP2_HTTP

# Run the server

Run WebServer class to run the server.
Requested parameters :
* Host's address (String) - ex : localhost,
* Host's port (int) - ex : 80.

# Perform tests trough a browser

Open a browser and navigate to **http://_<host_address>_:_<host_port>_/**.  

Ressources :
* **http://_<host_address>_:_<host_port>_/** : 
  * HTML file.
  * JPG image.
* **http://_<host_address>_:_<host_port>_/signInPage.html** : 
  * HTML file.
  * JavaScript script.
  * Dynamic ressource (.java) once the sign in form has been send.

# Perform tests trough Postman

Here are some quick request to test server's features.
### PUT
* http://_<host_address>_:_<host_port>_/put.html - Body : Put request has work.
  * Should return 201 CREATED + Body or 200 OK + Body.
  
### HEAD
* http://_<host_address>_:_<host_port>_/put.html.
  * Should return 204 NO CONTENT or 404 NOT FOUND.
  
### GET
* http://_<host_address>_:_<host_port>_/put.html.
  * Should return 200 OK + Body or 404 NOT FOUND.
* http://_<host_address>_:_<host_port>_/img.jpg.
  * Should return 200 OK.
* http://_<host_address>_:_<host_port>_/backend/forms/SignInForm.java - Params : signInFirstName, signInLastName.
  * Should return 200 OK + Body. If there is no body, check parameters.
  
### POST
* http://_<host_address>_:_<host_port>_/put.html.
  * Should return 201 CREATED + Body or 200 OK + Body.
* http://_<host_address>_:_<host_port>_/put.html - Body (raw type) : Post has work.
  * Should return 201 CREATED + Body or 200 OK + Body.
* http://_<host_address>_:_<host_port>_/backend/forms/SignInForm.js - Body (x-www-form-urlencoded) : signInFirstName, signInLastName.
  * Should return 200 OK + Body. If there is no body, check parameters.
* http://_<host_address>_:_<host_port>_/backend/forms/SignInForm.php.
  * Should return 501 NOT IMPLEMENTED.
  
### DELETE
* http://_<host_address>_:_<host_port>_/put.html.
  * Should return 204 NO CONTENT or 404 NOT FOUND.
  
### VIEW
* http://_<host_address>_:_<host_port>_/.
  * Should return 405 METHOD NOT ALLOWED.

# Javadoc

Stop the server and open doc/javadoc/index.html.

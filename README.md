#### Backend for Six Degrees of Kevin Bacon
  Keywords: `Java backend`, `NoSQL (Neo4j)`, `REST API`, `Maven`
  
### 1. Descriptions<br/>
  This is an Java implementation of the backend for a service that computes [Six Degrees of Kevin Bacon](https://en.wikipedia.org/wiki/Six_Degrees_of_Kevin_Bacon).This problem can be restated as finding the shortest path
between Kevin Bacon and a given actor (via shared movies). 
* The backend is running on port 8080. 
* Neo4j Username: neo4j; 
* Password: 1234;

------

### 2. Examples <br/>
* Consider the actor `George Clooney`:
  * George Clooney acted in “Good Night, and Good Luck” with Patricia Clarkson
  * Patricia Clarkson acted in “Beyond All Boundaries” Kevin Bacon<br/>
  
So we would say that George Clooney has a “Bacon number” of 2.<br/>
* Consider the actor `Bud Collyer`:
 * Bud Collyer was in “Secret Agent” with Jackson Beck
 * Jackson Beck was in “Hysterical History” with Mae Questel
 * Mae Questel was in “Who Framed Roger Rabbit” with Frank Welker
 * Frank Welker was in “Balto” with Kevin Bacon
  
So we would say that Bud Collyer has a “Bacon number” of 4.<br/>
  
------

### 3. Project/IDE Setup

* Command Line:
  * Install [maven](https://maven.apache.org/index.html)
  * To compile your code simply run mvn compile in the root directory (the folder
that has pom.xml)
  * To run your code run mvn exec:java 
* Eclipse:
  * File → Import → Maven → Existing Maven Projects
  * Navigate to the project location
  * Right click the project → Maven Build…
  * Input compile exec:java in the Goals input box
  * Click apply then run/close
  * You can now run the project by pressing the green play button
* Intellij:  
  * Import project
  * Navigate to the project location
  * Import project from external model → Maven
  * Next → Next → Next → Finish
  * Add Configuration → + Button → Maven
  * Name the configuration and input `exec:java` in the Command Line box
  * Apply → Ok
  * You can now run the project by pressing the green play button
  
------

### 4. Node/Relationship Property
* Actor:
  * node label actor
  * node properties: id, Name

* Movie:
  * node label movie
  * node properties: id, Name
  
* Relationship:
  * relationship label ACTED_IN
  
------

### 5. Testing
* Response Body:
  * JSON format
  
* Response Status:
  * 200 OK for a successful add
  * 400 BAD REQUEST if the request body is improperly formatted or missing required
information
  * 404 NOT FOUND if the actors or movies do not exist when adding a relationship
  * 500 INTERNAL SERVER ERROR if save or add was unsuccessful (Java Exception is
thrown)

* Existing endpoints:
  * (PUT) http://localhost:8080/api/v1/addActor
    * parameter:
      * name: string
      * actorID: string
  * (PUT) http://localhost:8080/api/v1/addMovie
    * parameter:
      * name: string
      * movieID: string      
  * (PUT) http://localhost:8080/api/v1/addRelationship
    * parameter:
      * name: string
      * movieID: string 
  * (GET) http://localhost:8080/api/v1/getActor
    * parameter:
      * actorID: string  
  * (GET) http://localhost:8080/api/v1/getMovie
    * parameter:
      * actorID: string
  * (GET) http://localhost:8080/api/v1/hasRelationship
    * parameter:
      * actorID: string
      * movieID: string 
  * (GET) http://localhost:8080/api/v1/computeBaconNumber
    * parameter:
      * actorID: string
  * (GET) http://localhost:8080/api/v1/computeBaconPath
    * parameter:
      * actorID: string
* PostMan Testing Package:
  You can download it from the PostMan folder.





  



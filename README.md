# CS936Final-StudentInfoSystem
An incomplete set of files used in my CS936 Final Project. The project is a college database system with a GUI front end.
Records are stored in a MySQL database in an AWS RDS instance and users can create, read, and update records with a Java GUI front end desktop client*. <br/>
Specifically, users can:<br/>
1. Register or update a student with personal information like name, age, major, year, and address.<br/>
2. Similarly register or update a professor, but with info on rank and department instead.<br/>
3. Establish or update a department.<br/>
4. Create a course under a department and schedule it as a class.<br/>
5. Enroll students for classes.<br/>
6. View and add student grades for a class, and obtain a report of all the students enrolled in a class and their grades in table format.<br/>
<b>NOTE:</b> The necessary information to connect to the server, contained in a .properties file, is not included for security reasons. The keystore and truststore are also not contained. Because of this, the GUI cannot connect to the server. <br/>
*The record fields shown to the user are obtained by reading table metadata from the database, allowing for tables to
be changed without breaking the GUI.

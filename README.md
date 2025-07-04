
# Employee Recommendation Application

This application is designed to streamline the employee recommendation process within an organization. It enables employees to submit recommendations for potential candidates, which are then tracked and managed by HR personnel.

## Features

- **Employee Role**: Employees can fill out recommendation forms for potential candidates, providing information such as contact details, skills, experience, and resumes. The form is converted to a **PDF** format and initially saved locally. The URL to the file is stored in the database.
  
- **HR Role**: HR employees can view and manage all recommendations submitted by employees, track their status (e.g., submitted, in progress, rejected, hired), and update them accordingly.

- **Backend**: The backend is powered by **Spring Boot**, which handles all the business logic, user authentication, and API endpoints. It serves the frontend with the necessary data and processes the recommendation forms submitted by employees. The backend communicates with the **MariaDB** database to store and retrieve recommendation data.

- **Database**: Uses **MariaDB** to store all recommendation data, including employee details, recommendation forms, and the status of each recommendation. The database schema is initialized using SQL scripts and can be restored from a dump file.

- **Frontend**: Built with **JavaFX**, providing a user-friendly interface for both employees and HR staff. It allows employees to submit forms and track recommendations, while HR staff can review and update the status of recommendations.


## Technologies Used

- **Spring Boot**: Backend framework
- **JavaFX**: Frontend framework
- **MariaDB**: Database for storing recommendations
- **Maven**: Build automation tool

## Getting Started

To get the application running locally, follow these steps:

### Prerequisites

- **Java 17+** (Install [JDK](https://adoptopenjdk.net/))
- **Maven** (Install from [Maven](https://maven.apache.org/))
- **MariaDB** (Install [MariaDB](https://mariadb.org/download/))

### Installation

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/HosseinKrouna/employee-recommendation-fx.git
   ```

2. **Set Up MariaDB Database**:
   - Ensure **MariaDB** is installed and running.
   - Create a database (e.g., `emp_recommendations`).
   - Run the following SQL script to initialize the schema:
     ```bash
     mysql -u root -p < database-scripts/init.sql
     ```

3. **Clone the Backend Project**:
   - Although the backend project is not necessary to be run separately, it should still be cloned alongside the frontend for the full project setup:
     ```bash
     git clone https://github.com/HosseinKrouna/employee-recruits-employees.git
     ```

4. **Build and Run the Application**:
   - Navigate to the **frontend** project folder and build the application:
     ```bash
     mvn clean install
     ```
   - The JavaFX **frontend** will automatically start the Spring Boot **backend** when launched, so you don’t need to manually start the backend in your IDE. Just run the frontend project:
     ```bash
     mvn javafx:run
     ```

5. **Frontend**:
   - The JavaFX frontend is automatically launched when the Spring Boot backend starts. You only need to run the frontend project (JavaFX), which will handle starting the backend.

### Usage

- After running the frontend, the application will be available at `http://localhost:8080`.
- **Employees** can log in, fill out the recommendation form, and submit it.
- **HR Personnel** can log in to manage recommendations, view generated PDFs, and track candidate statuses.


### Admin Setup (HR Role)

HR users do not have the ability to register themselves through the frontend like employees. Instead, the **Admin username and password** must be set using Postman by making a **POST request** to the backend.

Follow these steps to set up an HR user in Postman:

1. **Open Postman** and create a new **POST request**.
2. **URL**: 
   ```
   http://localhost:8080/api/users/register-hr
   ```
3. **Headers**:
   - Set the `Content-Type` to `application/json`.

4. **Body**:
   Select the **raw** option and choose **JSON** format, then enter the following JSON data (replace with the desired admin username and password):

   ```json
   {
     "username": "hr_admin",
     "password": "your_admin_password"
   }
   ```

5. **Send the Request**: 
   - Click the **Send** button to make the request. If successful, the response will confirm that the admin user has been created.

6. **Login as HR**:
   - After setting up the HR user, you can now log in to the application as an HR user with the credentials provided in Postman (e.g., `hr_admin` and `your_admin_password`).

### Usage for Employees

- **Employees** can register themselves through the frontend. Once registered, they can log in to submit recommendations.
- **HR Personnel** (Admins) will have their credentials set via Postman and can log in to manage recommendations, view generated PDFs, and track candidate statuses.

### Documentation and Database Dump

The **documentation** and the **database dump** for this project can be found in the **backend repository**.

- **Documentation**: Contains detailed instructions on setting up and using the backend, including API endpoints, configuration, and setup procedures.
- **Database Dump**: A dump of the MariaDB database (`emp_recommendations_dump.sql`) is available for quick setup and can be restored using Postman or directly in your MariaDB instance.

You can access the backend repository here: [Backend Repository](https://github.com/HosseinKrouna/employee-recruits-employees)


## Documentation

The application documentation is available in the `docs/` directory. It includes:

- **User Guide**: Detailed instructions for both employees and HR users.
- **Database Schema**: Information about the database structure and how to initialize it.
- **Developer Guide**: Instructions for setting up the development environment, extending the application, and contributing.

## Database Dump

For convenience, a database dump (`emp_recommendations_dump.sql`) is available in the `database-dump/` directory. You can use this dump to quickly restore the database to a predefined state.

To restore the database from the dump:
```bash
mysql -u root -p < database-dump/emp_recommendations_dump.sql
```

## Contributing

Contributions are welcome! To contribute to this project:

1. Fork the repository.
2. Create a new branch (`git checkout -b feature-branch`).
3. Make your changes and commit them.
4. Push to your forked repository (`git push origin feature-branch`).
5. Create a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


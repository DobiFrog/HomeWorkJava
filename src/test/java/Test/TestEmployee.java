package Test;

import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.*;


public class TestEmployee {

    public static  String TOKEN;
    public static  int newCompanyId;
    public static String URL = "https://x-clients-be.onrender.com/employee?company=" + newCompanyId;
    public static String URL_EMPLOYEE = "https://x-clients-be.onrender.com/employee/{id}";
    public static final String URL_AUTH = "https://x-clients-be.onrender.com/auth/login";

    public static final String URL_COMPANY = "https://x-clients-be.onrender.com/company";
    public static final String URL_COMPANY_DELETE = "https://x-clients-be.onrender.com/company/delete/{id}";


    @BeforeAll
    static void authorization(){
        String creds = """
                {
                  "username": "tecna",
                  "password": "tecna-fairy"
                }
                """;

        // авторизоваться
        TOKEN = given()
                .body(creds)
                .contentType(ContentType.JSON)
                .when().post(URL_AUTH)
                .then()
                .statusCode(201)
                .extract().path("userToken");

        //получить айди компании из которой будем искать сотрудников
        //Создать свою компанию и получить айдишник
        String myJson = """ 
                {
                  "name": "Компания ПатрикТеструетАпи"
                }
                """;

         //создать компанию
        newCompanyId = given()
                .body(myJson)
                .header("x-client-token", TOKEN)
                .contentType(ContentType.JSON)
                .when().post(URL_COMPANY)
                .then()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");
    }



    @Test
    @DisplayName("Получить список сотрудников компании")
    public void shouldReturnListOfEmployees() {

        given() // настраиваем запрос
                .log().all()
                .header("x-client-token", TOKEN)
                .get(URL)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(is(notNullValue()))
                .log().all();
    }

    @Test
    @DisplayName("Добавить нового сотрудника и получить его")
    public void newEmployee() {

        int saveNewEmployeeId = newEmployeeId(myJson());

        given()
                .log().all()
                .pathParams("id", saveNewEmployeeId)
                .header("x-client-token", TOKEN)
                .get(URL_EMPLOYEE)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(is(notNullValue()))
                .body("id", equalTo(saveNewEmployeeId))
                .body("companyId", equalTo(newCompanyId))
                .log().all();
    }

    @Test
    @DisplayName("Создать сотрудника без отчества")
    public void newEmployeeNoMiddleName() {
        String myJson = """ 
                {
                  "firstName": "Петрович",
                  "lastName": "Боб",
                  "companyId": """ + newCompanyId + """
                  ,
                  "url": "string",
                  "phone": "string",
                  "birthdate": "2024-03-16T14:18:28.674Z",
                  "isActive": true
                  }
                """;
        int saveNewEmployeeId = newEmployeeId(myJson);


        given()
                .log().all()
                .pathParams("id", saveNewEmployeeId)
                .header("x-client-token", TOKEN)
                .get(URL_EMPLOYEE)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(is(notNullValue()))
                .body("id", equalTo(saveNewEmployeeId))
                .body("middleName", equalTo(null))
                .body("companyId", equalTo(newCompanyId))
                .log().all();
    }

    @Test
    @DisplayName("Создать сотрудника без имени")
    public void newEmployeeNoFirstName() {
        String myJson = """ 
                {
                  "lastName": "Боб",
                  "middleName": "Иванович",
                  "companyId": """ + newCompanyId + """
                  ,
                  "url": "string",
                  "phone": "string",
                  "birthdate": "2024-03-16T14:18:28.674Z",
                  "isActive": true
                  }
                """;

        given()
                .log().all()
                .body(myJson)
                .header("x-client-token", TOKEN)
                .contentType(ContentType.JSON)
                .when().post(URL)
                .then()
                .log().all()
                .statusCode(500)
                .body("message", equalTo("Internal server error"));

    }


    @Test
    @DisplayName("Запросить несуществующего сотрудника")
    public void nonExistentEmployee(){

        int newEmployeeId = 2;

        given()
                .log().all()
                .pathParams("id", newEmployeeId)
                .header("x-client-token", "TOKEN")
                .header("content-length", 0)
                .get(URL_EMPLOYEE)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(404)
                .log().all();
    }

    //В документации код 201, но тут стоит 200 для проверки корректности теста
    @Test
    @DisplayName("Изменить информацию о сотруднике")
    public void changeEmployee(){

        //создаем сотрудника для изменений
        int saveNewEmployeeId = newEmployeeId(myJson());

        String changedJson = """
                {
                  "id": """ + saveNewEmployeeId + """
                  ,
                  "firstName": "Патрик",
                  "lastName": "Стар",
                  "middleName": "Петрович",
                  "companyId": """ + newCompanyId + """
                  ,"url": "string",
                  "phone": "string",
                  "birthdate": "2024-03-16T14:18:28.674Z",
                  "isActive": false
                  }
                  """
                ;

        //меняем сотрудника
        given()
                .body(changedJson)
                .log().all()
                .pathParams("id", saveNewEmployeeId)
                .header("x-client-token", TOKEN)
                .patch(URL_EMPLOYEE)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(is(notNullValue()))
                .body("id", equalTo(saveNewEmployeeId))
                .log().all();




        //получаем сотрудника
        given()
                .log().all()
                .pathParams("id", saveNewEmployeeId)
                .header("x-client-token", TOKEN)
                .get(URL_EMPLOYEE)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(is(notNullValue()))
                .body("id", equalTo(saveNewEmployeeId))
                .body("companyId", equalTo(newCompanyId))
                .log().all();
    }

    @AfterAll
    static void deleteCompany(){
        given()
                .log().all()
                .pathParams("id", newCompanyId)
                .header("x-client-token", TOKEN)
                .get(URL_COMPANY_DELETE)// пришел ответ
                .then()// работа с результатом -> проверка ответа
                .statusCode(200)
                .contentType(ContentType.JSON)
                .header("Vary", "Accept-Encoding")
                .header("Content-Type", "application/json; charset=utf-8")
                .body(is(notNullValue()))
                .log().all();
    }


    static String myJson(){
        return """ 
                {
                  "firstName": "Губка",
                  "lastName": "Боб",
                  "middleName": "Иванович",
                  "companyId": """ + newCompanyId +"""
                
                  ,
                  "url": "string",
                  "phone": "string",
                  "birthdate": "2024-03-16T14:18:28.674Z",
                  "isActive": true
                  }
                  """
                ;
    }

    //Создание и получение айдишника нового сотрудника
    static int newEmployeeId(String myJson){
        return given()
                .log().all()
                .body(myJson)
                .header("x-client-token", TOKEN)
                .contentType(ContentType.JSON)
                .when().post(URL)
                .then()
                .log().all()
                .statusCode(201)
                .body("id", greaterThan(0))
                .extract().path("id");
    }


}

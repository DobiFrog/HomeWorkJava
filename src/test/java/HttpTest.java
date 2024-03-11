import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTest {
    private static final String URL = "https://todo-app-sky.herokuapp.com/";
    private HttpClient client;

    //Создаём чистого клиента
    @BeforeEach
    public void createNewClient(){
        client = HttpClientBuilder.create().build();
    }

    @Test
    @DisplayName("Проверка статус-кода для get и заголовка Content-Type")
    public void GetTest() throws IOException {
        //Запрос
        HttpGet getRequest = new HttpGet(URL);
        //Ответ
        HttpResponse response = client.execute(getRequest);
        //Массив заголовков
        Header[] allHeaders = response.getAllHeaders();
        for(Header header : allHeaders){
            System.out.println(header);
        }

        Header contentType = response.getFirstHeader("Content-Type");
        assertTrue(response.containsHeader("Content-Type"));
        assertTrue(contentType.getValue().contains("application/json"));
        assertEquals(200, response.getStatusLine().getStatusCode());
    }


    @Test
    @DisplayName("Создать запись и проверить что она создалась")
    public void isBodyContains() throws IOException{
        String requestBody = "{\"title\": \"проверка\"}";

        HttpPost createNote = new HttpPost(URL);
        createNote.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        //Ответ для Post
        HttpResponse responsePost = client.execute(createNote);
        String responseBody = EntityUtils.toString(responsePost.getEntity());

        assertEquals(201,responsePost.getStatusLine().getStatusCode());
        assertTrue(responseBody.contains("проверка"));

        //Ответ для Get
        HttpGet getRequest = new HttpGet(URL);
        HttpResponse responseGet = client.execute(getRequest);
    }

    @Test
    @DisplayName("Редактирование записи")
    public void patchNote() throws IOException{
        String requestBody = "{\"title\": \"мой пост\"}";
        String newBody = "{\"title\": \"отредактированный пост\"}";

        HttpPost createNote = new HttpPost(URL);
        createNote.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        //Ответ для Post
        HttpResponse responsePost = client.execute(createNote);
        String responseBody = EntityUtils.toString(responsePost.getEntity());

        //Редактируем запись
        String id = responseBody.substring(6,12);
        HttpPatch newPatch = new HttpPatch(URL + id);
        newPatch.setEntity(new StringEntity(newBody, ContentType.APPLICATION_JSON));

        HttpResponse responsePatch = client.execute(newPatch);
        String responseBodyPatch = EntityUtils.toString(responsePatch.getEntity());

        assertEquals(200, responsePatch.getStatusLine().getStatusCode());
        assertTrue(responseBodyPatch.contains("отредактированный пост"));

    }
    @Test
    @DisplayName("Перечеркивание записи")
    public void doneNote() throws IOException{
        String requestBody = "{\"title\": \"мой пост\"}";
        String newBody = "{\"completed\": true}";

        HttpPost createNote = new HttpPost(URL);
        createNote.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        //Ответ для Post
        HttpResponse responsePost = client.execute(createNote);
        String responseBody = EntityUtils.toString(responsePost.getEntity());

        //Редактируем запись
        String id = responseBody.substring(6,12);
        HttpPatch newPatch = new HttpPatch(URL + id);
        newPatch.setEntity(new StringEntity(newBody, ContentType.APPLICATION_JSON));

        HttpResponse responsePatch = client.execute(newPatch);
        String responseBodyPatch = EntityUtils.toString(responsePatch.getEntity());

        assertEquals(200, responsePatch.getStatusLine().getStatusCode());
        assertTrue(responseBodyPatch.contains("мой пост"));

    }


    @Test
    @DisplayName("Создание и удаление записи")
    public void deleteNote() throws IOException{
        String requestBody = "{\"title\": \"мой пост\"}";

        HttpPost createNote = new HttpPost(URL);
        createNote.setEntity(new StringEntity(requestBody, ContentType.APPLICATION_JSON));

        //Ответ для Post
        HttpResponse responsePost = client.execute(createNote);
        String responseBody = EntityUtils.toString(responsePost.getEntity());

        //Редактируем запись
        String id = responseBody.substring(6,12);
        HttpDelete deleteNote = new HttpDelete(URL + id);

        HttpResponse responseDelete = client.execute(deleteNote);

        assertEquals(204, responseDelete.getStatusLine().getStatusCode());
    }

}

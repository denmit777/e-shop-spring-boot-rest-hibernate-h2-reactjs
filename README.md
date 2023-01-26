1.Name of project: e-shop-spring-boot-rest-hibernate-h2-reactjs

2.Launch of project: 
backend part: run EShopApplication.class
node modules: e-shop-spring-boot-rest-hibernate-h2-reactjs\src\frontend-react\java-learn-app-main>npm install
frontend part: e-shop-spring-boot-rest-hibernate-h2-reactjs\src\frontend-react\java-learn-app-main>npm start

3.Ports of the project:
backend: http://localhost:8081
frontend: http://localhost:3000

4.Start page: http://localhost:3000

5.Logins/passwords/emails/roles of users:

Den/1234/den_mogilev@yopmail.com/ROLE_ADMIN,
Peter/4321/peter_mogilev@yopmail.com/ROLE_BUYER,
Asya/5678/asya_mogilev@yopmail.com/ROLE_BUYER,
Jimmy/P@ssword1/jimmy_mogilev@yopmail.com/ROLE_ADMIN,
Maricel/221182/maricel_mogilev@yopmail.com/ROLE_BUYER

6.Configuration: resources/application.properties

7.Templates: resources/templates

8.Database scripts: resources/data.sql

9.Sender's email: "denmit777@yandex.by"

10.Rest controllers:

UserController:
registerUser(POST): http://localhost:8081 + body;
authenticationUser(POST): http://localhost:8081/auth + body

GoodController:
save(POST): http://localhost:8081/goods/forAdmin + body;
getAllForAdmin(GET): http://localhost:8081/goods/forAdmin;
getAllForBuyer(GET): http://localhost:8081/goods/forBuyer;
getById(GET): http://localhost:8081/goods/forAdmin/{id};
update(PUT): http://localhost:8081/goods/forAdmin/{id} + body;
delete(DELETE): http://localhost:8081/goods/forAdmin/{id};
getTotalAmount(GET): http://localhost:8081/goods/forAdmin/total;

OrderController:
save(POST): http://localhost:8081/orders + body;
getAll(GET): http://localhost:8081/orders;
getById(GET): http://localhost:8081/orders/{id};
getTotalAmount(GET): http://localhost:8081/orders/total;

AttachmentController:
uploadFile(POST): http://localhost:8081/orders/{orderId}/attachments + body;
getById(GET): http://localhost:8081/orders/{orderId}/attachments/{attachmentId};
getAllByOrderId(GET): http://localhost:8081/orders/{orderId}/attachments;
deleteFile(DELETE): http://localhost:8081/orders/{orderId}/attachments/{attachmentName};

CommentController:
save(POST): http://localhost:8081/orders/{orderId}/comments + body;
getAllByOrderId(GET): http://localhost:8081/orders/{orderId}/comments;

FeedbackController:
save(POST): http://localhost:8081/orders/{orderId}/feedbacks + body;
getAllByOrderId(GET): http://localhost:8081/orders/{orderId}/feedbacks;

HistoryController:
getAllByOrderId(GET): http://localhost:8081/orders/{orderId}/history;

11.Launch of all the tests:
EditConfiguration -> JUnit -> name:mvn test -> All In Directory: e-shop-spring-boot-rest-hibernate-h2-reactjs\src\test ->
Environment variables : clean test

Controller tests: GoodControllerTest, OrderControllerTest
Converter tests: GoodConverterImplTest
Dto tests: UserLoginDtoTest
ServiceTests: GoodServiceImplTest
UtilsTests: OrderUserComparatorTest

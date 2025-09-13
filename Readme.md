### Steps
1. Run Docker Compose
   - Command:
     ```
     docker compose up -d
     ```
2. Manually start the Spring Boot application
   - Examples:
     ```
     mvn spring-boot:run
     ```
3. Hit the API
   - URL: http://localhost:8080/api/index
   - Example body:
     ```json
     {
       "title": "Spring and OpenSearch",
       "content": "Keyword search demo",
       "tags": ["spring", "search"]
     }
     ```


<img width="1280" height="881" alt="image" src="https://github.com/user-attachments/assets/f81f0974-6202-42fa-a95e-2ca11be289a7" />
<img width="1348" height="919" alt="image" src="https://github.com/user-attachments/assets/3fa513e2-7c55-49da-aaee-922b1aa23215" />

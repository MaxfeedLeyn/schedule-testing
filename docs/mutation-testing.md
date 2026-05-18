# Mutation-testing

## FrontEnd

До зміни тестів
<img width="1349" height="627" alt="image" src="https://github.com/user-attachments/assets/b5b91c0a-4aa3-40ae-93ae-2c445eed8814" />
<img width="1283" height="208" alt="image" src="https://github.com/user-attachments/assets/53b91391-ec91-4e5d-b101-1b1f860f2e3c" />

Після зміни тестів
<img width="1319" height="385" alt="image" src="https://github.com/user-attachments/assets/bbcb1f7e-1243-46cb-a66a-c65f47fa4eb4" />
<img width="1246" height="489" alt="image" src="https://github.com/user-attachments/assets/b28ed5d8-d865-4204-9996-2b2a105adda4" />

Пояснення:
### Вбиті
schedule.js — StringLiteral → hoverLineClassName = "" addClassDayBoard і removeClassDayBoard мали статус NoCoverage — жодних тестів взагалі.
Зміна: додали describe('addClassDayBoard') і describe('removeClassDayBoard') з mock-ами DOM і явною перевіркою toHaveBeenCalledWith('hover-line').

### Залишились живими
schedule.js — if (!isEmpty(items)) → if (true) — еквівалентний мутант, вбити неможливо без зміни алгоритму. Коли items порожній, [].filter(...) все одно повертає [],
тому filteredLesson.length === 0 в обох випадках.

## Backend

До зміни тестів
<img width="914" height="598" alt="image" src="https://github.com/user-attachments/assets/91071af5-ebf7-44a0-b518-0c589ce0ea6a" />

Після зміни тестів
<img width="1066" height="610" alt="image" src="https://github.com/user-attachments/assets/473e17c1-e5be-41a3-aa8c-5ca18d564c46" />

Пояснення:
Всі зміни відбулись у файлі StudentService.test

### Вбиті

- save(StudentDTO)
- update(StudentDTO)
- saveFromFile(...) і saveStudentFromFile(...)

### Вижили

StudentImportDTO::setEmail - тест не доводив, що саме ці поля були змінені саме сервісом, а не вже були такими в мокнутому DTO. В тесті є mappedBackDto, який
вже містив потрібні значення.
